package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.CompactingUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CompactingDrawerTile extends ItemControllableDrawerTile<CompactingDrawerTile>
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {

    public CompactingInventoryHandler handler;
    private boolean hasCheckedRecipes;

    public CompactingDrawerTile(BlockPos pos, BlockState state) {
        this(FunctionalStorageBlockEntities.COMPACTING_DRAWER, pos, state);
    }

    public CompactingDrawerTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.handler = new CompactingInventoryHandler(3) {
            @Override
            public void onChange() {
                CompactingDrawerTile.this.setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return CompactingDrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return CompactingDrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isCreative() {
                return CompactingDrawerTile.this.isCreative();
            }

            @Override
            public boolean isLocked() {
                return CompactingDrawerTile.this.isLocked();
            }
        };
        this.hasCheckedRecipes = false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CompactingDrawerTile blockEntity) {
        if (level.isClientSide)
            return;
        if (!blockEntity.hasCheckedRecipes) {
            if (!blockEntity.handler.getParent().isEmpty()) {
                CompactingUtil compactingUtil = new CompactingUtil(level, 3);
                compactingUtil.setup(blockEntity.handler.getParent());
                blockEntity.handler.setup(compactingUtil);
            }
            blockEntity.hasCheckedRecipes = true;
        }
    }

    // Fields for double-click detection
    private java.util.UUID lastClickUUID;
    private long lastClickTime;

    @Override
    public InteractionResult onSlotActivated(Player player, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ, int slot) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem().equals(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CONFIGURATION_TOOL)
                || stack.getItem().equals(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.LINKING_TOOL))
            return InteractionResult.PASS;

        // Setup compacting if not already setup
        if (!handler.isSetup() && slot != -1 && !stack.isEmpty()) {
            ItemStack setupStack = stack.copy();
            setupStack.setCount(1);
            CompactingUtil compactingUtil = new CompactingUtil(this.level, 3);
            compactingUtil.setup(setupStack);
            handler.setup(compactingUtil);
            // Find the correct slot for the item
            for (int i = 0; i < handler.getResultList().size(); i++) {
                if (ItemStack.isSameItem(handler.getResultList().get(i).getResult(), setupStack)) {
                    slot = i;
                    break;
                }
            }
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }

        // Pass upgrade handling to parent
        InteractionResult parentResult = super.onSlotActivated(player, hand, facing, hitX, hitY, hitZ, slot);
        if (parentResult.consumesAction()) {
            return parentResult;
        }

        // Handle slot interaction
        if (slot != -1 && !player.isShiftKeyDown()) {
            long now = System.currentTimeMillis();
            if (!stack.isEmpty()) {
                // Insert item
                net.fabricmc.fabric.api.transfer.v1.item.ItemVariant resource = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
                        .of(stack);
                if (handler.isItemValid(slot, resource)) {
                    try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                            .openOuter()) {
                        long inserted = handler.insertIntoSlot(slot, resource, stack.getCount(), transaction);
                        if (inserted > 0) {
                            stack.shrink((int) inserted);
                            transaction.commit();

                            // Double click detection - insert all matching items from inventory
                            if (player.getUUID().equals(lastClickUUID) && (now - lastClickTime) < 500) {
                                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                                    ItemStack invStack = player.getInventory().getItem(i);
                                    if (!invStack.isEmpty()) {
                                        net.fabricmc.fabric.api.transfer.v1.item.ItemVariant invVariant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
                                                .of(invStack);
                                        if (handler.isItemValid(slot, invVariant)) {
                                            try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction invTransaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                                    .openOuter()) {
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
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            } else {
                // Empty hand - Double click to deposit matching items
                if (player.getUUID().equals(lastClickUUID) && (now - lastClickTime) < 500) {
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack invStack = player.getInventory().getItem(i);
                        if (!invStack.isEmpty()) {
                            net.fabricmc.fabric.api.transfer.v1.item.ItemVariant invVariant = net.fabricmc.fabric.api.transfer.v1.item.ItemVariant
                                    .of(invStack);
                            if (handler.isItemValid(slot, invVariant)) {
                                try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction invTransaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                        .openOuter()) {
                                    long invInserted = handler.insert(invVariant, invStack.getCount(), invTransaction);
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

    /**
     * Handle left-click extraction from a specific slot.
     */
    public void onClicked(Player player, int slot) {
        if (slot != -1) {
            try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                    .openOuter()) {
                net.fabricmc.fabric.api.transfer.v1.item.ItemVariant resource = handler.getResource(slot);
                if (!resource.isBlank()) {
                    int maxExtract = player.isShiftKeyDown() ? resource.getItem().getMaxStackSize() : 1;
                    long extracted = handler.extractFromSlot(slot, resource, maxExtract, transaction);
                    if (extracted > 0) {
                        ItemStack extractedStack = resource.toStack((int) extracted);
                        player.getInventory().placeItemBackInInventory(extractedStack);
                        transaction.commit();
                    }
                }
            }
        }
    }

    @Override
    public Storage<ItemVariant> getStorage() {
        return handler;
    }

    @Override
    public int getStorageSlotAmount() {
        return 3; // Match original Forge implementation for compacting drawers
    }

    @Override
    public int getBaseSize(int lost) {
        return handler.getSlotLimitBase(lost);
    }

    public boolean isEverythingEmpty() {
        for (net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view : getStorage()) {
            if (!view.isResourceBlank()) {
                return false;
            }
        }
        return true;
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
    public void writeScreenOpeningData(net.minecraft.server.level.ServerPlayer player,
            net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }
}
