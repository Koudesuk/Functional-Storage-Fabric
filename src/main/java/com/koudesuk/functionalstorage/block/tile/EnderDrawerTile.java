package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.inventory.EnderInventoryHandler;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.world.EnderSavedData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EnderDrawerTile extends ItemControllableDrawerTile<EnderDrawerTile>
        implements ExtendedScreenHandlerFactory {

    private String frequency;
    private java.util.UUID lastClickUUID;
    private long lastClickTime;

    // Client-side cached handler for rendering (synced via NBT)
    private EnderInventoryHandler cachedHandler;

    public EnderDrawerTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.ENDER_DRAWER, pos, state);
        this.frequency = UUID.randomUUID().toString();
        // Create a cached handler for client-side rendering
        this.cachedHandler = new EnderInventoryHandler(this.frequency, null);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        // Initialize storage when level is set (ensures EnderSavedData is accessible)
    }

    @Override
    public int getStorageSlotAmount() {
        return 0; // Ender Drawers have no storage upgrade slots, only utility slots
    }

    public void setFrequency(String frequency) {
        if (frequency == null)
            return;
        this.frequency = frequency;
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public String getFrequency() {
        return frequency;
    }

    /**
     * Get the shared EnderInventoryHandler for this drawer's frequency.
     * On server: returns the actual shared handler from EnderSavedData.
     * On client: returns the cached handler synced via NBT.
     */
    public EnderInventoryHandler getHandler() {
        if (level == null) {
            return cachedHandler; // Fallback to cached
        }
        if (level.isClientSide) {
            return cachedHandler; // Client uses cached handler
        }
        return EnderSavedData.getInstance(level).getFrequency(frequency);
    }

    @Override
    public Storage<ItemVariant> getStorage() {
        EnderInventoryHandler handler = getHandler();
        return handler != null ? handler : Storage.empty();
    }

    /**
     * Server tick - syncs ender drawer data to clients every 20 ticks.
     * This ensures all ender drawers with the same frequency update together.
     */
    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);

        // Sync every 20 ticks (1 second) - matches Forge behavior
        if (level.getGameTime() % 20 == 0) {
            syncToClients();
        }
    }

    @Override
    public int getBaseSize(int lost) {
        return DrawerType.X_1.getSlotAmount();
    }

    @Override
    public InteractionResult onSlotActivated(Player player, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ, int slot) {
        ItemStack stack = player.getItemInHand(hand);

        // Pass through for configuration/linking tools
        if (stack.getItem() instanceof com.koudesuk.functionalstorage.item.ConfigurationToolItem
                || stack.getItem() instanceof com.koudesuk.functionalstorage.item.LinkingToolItem)
            return InteractionResult.PASS;

        // Handle upgrade insertion
        if (!stack.isEmpty() && stack.getItem() instanceof com.koudesuk.functionalstorage.item.UpgradeItem) {
            com.koudesuk.functionalstorage.item.UpgradeItem upgradeItem = (com.koudesuk.functionalstorage.item.UpgradeItem) stack
                    .getItem();
            if (upgradeItem instanceof com.koudesuk.functionalstorage.item.StorageUpgradeItem
                    || upgradeItem == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CREATIVE_UPGRADE) {
                SimpleContainer container = getStorageUpgrades();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).isEmpty()) {
                        ItemStack temp = stack.split(1);
                        container.setItem(i, temp);
                        this.setChanged();
                        if (level != null && !level.isClientSide) {
                            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            } else {
                SimpleContainer container = getUtilityUpgrades();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).isEmpty()) {
                        ItemStack temp = stack.split(1);
                        container.setItem(i, temp);
                        this.setChanged();
                        if (level != null && !level.isClientSide) {
                            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        EnderInventoryHandler handler = getHandler();
        if (handler == null) {
            return InteractionResult.PASS;
        }

        if (slot != -1 && !player.isShiftKeyDown()) {
            long now = System.currentTimeMillis();
            if (!stack.isEmpty()) {
                // Insert item
                if (handler.isItemValid(slot, ItemVariant.of(stack))) {
                    int amount = stack.getCount();
                    if (amount > 0) {
                        // Handle empty locked drawer
                        if (handler.isLocked() && handler.getStoredStacks().get(slot).getAmount() == 0
                                && handler.getStoredStacks().get(slot).getStack().isEmpty()) {
                            handler.setLockedItem(slot, ItemVariant.of(stack));
                        }

                        try (Transaction transaction = Transaction.openOuter()) {
                            long inserted = handler.insertIntoSlot(slot, ItemVariant.of(stack), amount, transaction);
                            if (inserted > 0) {
                                stack.shrink((int) inserted);
                                transaction.commit();

                                // Double click detection - insert all matching items from inventory
                                if (player.getUUID().equals(lastClickUUID) && (now - lastClickTime) < 500) {
                                    boolean hasVoid = this.isVoid();
                                    ItemVariant slotVariant = handler.getResource(slot);

                                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                        ItemStack invStack = player.getInventory().getItem(i);
                                        if (!invStack.isEmpty()) {
                                            ItemVariant invVariant = ItemVariant.of(invStack);

                                            if (hasVoid && !slotVariant.isBlank() && !invVariant.equals(slotVariant)) {
                                                continue;
                                            }

                                            if (handler.isItemValid(slot, invVariant)) {
                                                try (Transaction invTransaction = Transaction.openOuter()) {
                                                    long invInserted = handler.insert(invVariant, invStack.getCount(),
                                                            invTransaction);
                                                    if (invInserted > 0) {
                                                        invStack.shrink((int) invInserted);
                                                        invTransaction.commit();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                lastClickUUID = player.getUUID();
                                lastClickTime = now;
                                // Sync to clients after insertion
                                syncToClients();
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                }
            } else {
                // Empty hand - Double click to deposit matching items
                if (player.getUUID().equals(lastClickUUID) && (now - lastClickTime) < 500) {
                    boolean hasVoid = this.isVoid();
                    ItemVariant slotVariant = handler.getResource(slot);

                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (!invStack.isEmpty()) {
                            ItemVariant invVariant = ItemVariant.of(invStack);

                            if (hasVoid && !slotVariant.isBlank() && !invVariant.equals(slotVariant)) {
                                continue;
                            }

                            if (handler.isItemValid(slot, invVariant)) {
                                try (Transaction invTransaction = Transaction.openOuter()) {
                                    long invInserted = handler.insert(invVariant, invStack.getCount(), invTransaction);
                                    if (invInserted > 0) {
                                        invStack.shrink((int) invInserted);
                                        invTransaction.commit();
                                    }
                                }
                            }
                        }
                    }
                    // Sync after batch deposit
                    syncToClients();
                }
                lastClickUUID = player.getUUID();
                lastClickTime = now;
                return InteractionResult.SUCCESS;
            }
        } else if (slot == -1 || player.isShiftKeyDown()) {
            // Open GUI
            if (!level.isClientSide) {
                player.openMenu(this);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public void onClicked(Player player, int slot) {
        EnderInventoryHandler handler = getHandler();
        if (handler == null || slot == -1) {
            return;
        }

        // Extract item from the specific slot
        try (Transaction transaction = Transaction.openOuter()) {
            ItemVariant resource = handler.getResource(slot);
            if (!resource.isBlank()) {
                int maxExtract = player.isShiftKeyDown() ? resource.getItem().getMaxStackSize() : 1;
                long extracted = handler.extractFromSlot(slot, resource, maxExtract, transaction);
                if (extracted > 0) {
                    ItemStack extractedStack = resource.toStack((int) extracted);
                    player.getInventory().placeItemBackInInventory(extractedStack);
                    transaction.commit();
                    // Sync to clients after extraction
                    syncToClients();
                }
            }
        }
    }

    /**
     * Trigger block update to sync storage data to clients.
     */
    private void syncToClients() {
        if (level != null && !level.isClientSide) {
            this.setChanged();
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        EnderInventoryHandler handler = getHandler();
        if (handler != null) {
            handler.setLocked(locked);
            if (!locked) {
                handler.unlock();
            }
        }
    }

    @Override
    public boolean isVoid() {
        EnderInventoryHandler handler = getHandler();
        if (handler != null && handler.isVoid()) {
            return true;
        }
        return super.isVoid();
    }

    public boolean isEverythingEmpty() {
        EnderInventoryHandler handler = getHandler();
        if (handler != null) {
            for (int i = 0; i < handler.getStoredStacks().size(); i++) {
                if (!handler.getStoredStacks().get(i).getStack().isEmpty()) {
                    return false;
                }
            }
        }
        for (int i = 0; i < getStorageUpgrades().getContainerSize(); i++) {
            if (!getStorageUpgrades().getItem(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < getUtilityUpgrades().getContainerSize(); i++) {
            if (!getUtilityUpgrades().getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Frequency")) {
            this.frequency = tag.getString("Frequency");
        }
        // Load cached handler data for client-side rendering
        if (tag.contains("Handler")) {
            if (cachedHandler == null) {
                cachedHandler = new EnderInventoryHandler(this.frequency, null);
            }
            cachedHandler.deserializeNBT(tag.getCompound("Handler"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.frequency != null) {
            tag.putString("Frequency", this.frequency);
        }
        // Save handler data for client sync
        if (level != null && !level.isClientSide) {
            EnderInventoryHandler handler = EnderSavedData.getInstance(level).getFrequency(frequency);
            tag.put("Handler", handler.serializeNBT());
        } else if (cachedHandler != null) {
            tag.put("Handler", cachedHandler.serializeNBT());
        }
    }

    // ExtendedScreenHandlerFactory implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DrawerMenu(containerId, playerInventory, getStorageUpgrades(), getUtilityUpgrades(), this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }
}
