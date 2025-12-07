package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.IWoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class DrawerTile extends ItemControllableDrawerTile<DrawerTile>
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {

    private final DrawerType type;
    private final IWoodType woodType;
    private final BigInventoryHandler handler;

    public DrawerTile(BlockPos pos, BlockState blockState) {
        this(com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.DRAWER, pos, blockState);
    }

    protected DrawerTile(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
            BlockState blockState) {
        super(type, pos, blockState);
        if (blockState.getBlock() instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
            this.type = drawerBlock.getType();
            this.woodType = drawerBlock.getWoodType();
        } else {
            // Fallback or error
            this.type = DrawerType.X_1;
            this.woodType = com.koudesuk.functionalstorage.util.DrawerWoodType.OAK;
        }
        this.handler = new BigInventoryHandler(this.type) {
            @Override
            public void onChange() {
                DrawerTile.this.setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getMultiplier() {
                return DrawerTile.this.getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return DrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return DrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isLocked() {
                return DrawerTile.this.isLocked();
            }

            @Override
            public boolean isCreative() {
                return DrawerTile.this.isCreative();
            }
        };
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Handler")) {
            handler.deserializeNBT(tag.getCompound("Handler"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Handler", handler.serializeNBT());
    }

    private java.util.UUID lastClickUUID;
    private long lastClickTime;

    public net.minecraft.world.InteractionResult onSlotActivated(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand, net.minecraft.core.Direction facing, double hitX, double hitY,
            double hitZ, int slot) {
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof com.koudesuk.functionalstorage.item.ConfigurationToolItem
                || stack.getItem() instanceof com.koudesuk.functionalstorage.item.LinkingToolItem)
            return net.minecraft.world.InteractionResult.PASS;

        if (!stack.isEmpty() && stack.getItem() instanceof com.koudesuk.functionalstorage.item.UpgradeItem) {
            com.koudesuk.functionalstorage.item.UpgradeItem upgradeItem = (com.koudesuk.functionalstorage.item.UpgradeItem) stack
                    .getItem();
            if (upgradeItem instanceof com.koudesuk.functionalstorage.item.StorageUpgradeItem
                    || upgradeItem == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CREATIVE_UPGRADE) {
                net.minecraft.world.SimpleContainer container = getStorageUpgrades();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).isEmpty()) {
                        net.minecraft.world.item.ItemStack temp = stack.split(1);
                        container.setItem(i, temp);
                        this.setChanged();
                        if (level != null && !level.isClientSide) {
                            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                }
            } else {
                net.minecraft.world.SimpleContainer container = getUtilityUpgrades();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).isEmpty()) {
                        net.minecraft.world.item.ItemStack temp = stack.split(1);
                        container.setItem(i, temp);
                        this.setChanged();
                        if (level != null && !level.isClientSide) {
                            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                }
            }
        }

        if (slot != -1 && !player.isShiftKeyDown()) {
            long now = System.currentTimeMillis();
            if (!stack.isEmpty()) {
                // Insert item
                if (handler.isItemValid(slot, net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(stack))) {
                    int amount = stack.getCount();
                    if (amount > 0) {
                        // Handle empty locked drawer
                        if (handler.isLocked() && handler.getStoredStacks().get(slot).getAmount() == 0
                                && handler.getStoredStacks().get(slot).getStack().isEmpty()) {
                            handler.setLockedItem(slot, net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(stack));
                        }

                        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                .openOuter()) {
                            long inserted = handler.insertIntoSlot(slot,
                                    net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(stack), amount,
                                    transaction);
                            if (inserted > 0) {
                                stack.shrink((int) inserted);
                                transaction.commit();

                                // Double click detection
                                if (player.getUUID().equals(lastClickUUID) && (now - lastClickTime) < 500) {
                                    // FIX: When void upgrade is active, only insert items matching the stored item
                                    boolean hasVoid = this.isVoid();
                                    net.fabricmc.fabric.api.transfer.v1.item.ItemVariant slotVariant = handler
                                            .getResource(slot);

                                    // Insert matching items from inventory
                                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                        net.minecraft.world.item.ItemStack invStack = player.getInventory().getItem(i);
                                        if (!invStack.isEmpty()) {
                                            net.fabricmc.fabric.api.transfer.v1.item.ItemVariant invVariant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
                                                    .of(invStack);

                                            // Skip non-matching items when void is active
                                            if (hasVoid && !slotVariant.isBlank() && !invVariant.equals(slotVariant)) {
                                                continue;
                                            }

                                            if (handler.isItemValid(slot, invVariant)) {
                                                try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction invTransaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                                        .openOuter()) {
                                                    long invInserted = handler
                                                            .insert(invVariant, invStack.getCount(), invTransaction);
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

                                return net.minecraft.world.InteractionResult.SUCCESS;
                            }
                        }
                    }
                }
            } else {
                // Empty hand - Double click to deposit matching items
                if (player.getUUID().equals(lastClickUUID) && (now - lastClickTime) < 500) {
                    // FIX: When void upgrade is active, only insert items matching the stored item
                    boolean hasVoid = this.isVoid();
                    net.fabricmc.fabric.api.transfer.v1.item.ItemVariant slotVariant = handler.getResource(slot);

                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        net.minecraft.world.item.ItemStack invStack = player.getInventory().getItem(i);
                        if (!invStack.isEmpty()) {
                            net.fabricmc.fabric.api.transfer.v1.item.ItemVariant invVariant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
                                    .of(invStack);

                            // Skip non-matching items when void is active
                            if (hasVoid && !slotVariant.isBlank() && !invVariant.equals(slotVariant)) {
                                continue;
                            }

                            if (handler.isItemValid(slot, invVariant)) {
                                try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction invTransaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                        .openOuter()) {
                                    long invInserted = handler.insert(invVariant,
                                            invStack.getCount(), invTransaction);
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
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        } else if (slot == -1 || player.isShiftKeyDown()) {
            // Open GUI
            if (!level.isClientSide) {
                player.openMenu(this);
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    public void onClicked(net.minecraft.world.entity.player.Player player, int slot) {
        if (slot != -1) {
            // Extract item from the specific slot
            try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                    .openOuter()) {
                net.fabricmc.fabric.api.transfer.v1.item.ItemVariant resource = handler.getResource(slot);
                if (!resource.isBlank()) {
                    int maxExtract = player.isShiftKeyDown() ? resource.getItem().getMaxStackSize() : 1;
                    long extracted = handler.extractFromSlot(slot, resource, maxExtract, transaction);
                    if (extracted > 0) {
                        net.minecraft.world.item.ItemStack stack = resource.toStack((int) extracted);
                        player.getInventory().placeItemBackInInventory(stack);
                        transaction.commit();
                    }
                }
            }
        }
    }

    public BigInventoryHandler getHandler() {
        return handler;
    }

    public net.minecraft.core.Direction getFacingDirection() {
        return this.getBlockState()
                .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
    }

    public com.koudesuk.functionalstorage.util.DrawerType getDrawerType() {
        return type;
    }

    public com.koudesuk.functionalstorage.util.IWoodType getWoodType() {
        return woodType;
    }

    @Override
    public net.fabricmc.fabric.api.transfer.v1.storage.Storage<net.fabricmc.fabric.api.transfer.v1.item.ItemVariant> getStorage() {
        return handler;
    }

    @Override
    public int getBaseSize(int lost) {
        return type.getSlotAmount();
    }

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
    public void writeScreenOpeningData(net.minecraft.server.level.ServerPlayer player,
            net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }

    public boolean isEverythingEmpty() {
        for (int i = 0; i < getHandler().getStoredStacks().size(); i++) {
            if (!getHandler().getStoredStacks().get(i).getStack().isEmpty()) {
                return false;
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
    public void onUpgradeChanged() {
        // Call parent implementation which handles all the syncing
        super.onUpgradeChanged();
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        if (!locked) {
            handler.unlock();
        }
    }
}