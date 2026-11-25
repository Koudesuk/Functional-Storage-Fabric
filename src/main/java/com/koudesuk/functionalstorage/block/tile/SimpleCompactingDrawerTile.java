package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.network.BlockPosPayload;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.util.StorageTags;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.CompactingUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class SimpleCompactingDrawerTile extends ItemControllableDrawerTile<SimpleCompactingDrawerTile>
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<BlockPosPayload> {

    public CompactingInventoryHandler handler;
    private boolean hasCheckedRecipes;

    public SimpleCompactingDrawerTile(BlockPos pos, BlockState state) {
        this(FunctionalStorageBlockEntities.SIMPLE_COMPACTING_DRAWER, pos, state);
    }

    public SimpleCompactingDrawerTile(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
            BlockState state) {
        super(type, pos, state);
        this.handler = new CompactingInventoryHandler(2, FunctionalStorageConfig.SIMPLE_COMPACTING_DRAWER_STACKS.get()) {
            @Override
            public void onChange() {
                SimpleCompactingDrawerTile.this.setChanged();
                SimpleCompactingDrawerTile.this.updateComparatorOutput();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getMultiplier() {
                return getStorageMultiplier();
            }

            @Override
            public double getSizeFactor() {
                return SimpleCompactingDrawerTile.this.getSizeFactor(FSAttachments.ITEM_STORAGE_MODIFIER,
                        FunctionalStorageConfig.SIMPLE_COMPACTING_DRAWER_STACKS.get());
            }

            @Override
            public boolean isVoid() {
                return SimpleCompactingDrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return SimpleCompactingDrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isCreative() {
                return SimpleCompactingDrawerTile.this.isCreative();
            }

            @Override
            public boolean isLocked() {
                return SimpleCompactingDrawerTile.this.isLocked();
            }
        };
        this.hasCheckedRecipes = false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SimpleCompactingDrawerTile blockEntity) {
        if (level.isClientSide)
            return;
        if (!blockEntity.hasCheckedRecipes) {
            if (!blockEntity.handler.getParent().isEmpty()) {
                CompactingUtil compactingUtil = new CompactingUtil(level, 2);
                compactingUtil.setup(blockEntity.handler.getParent());
                blockEntity.handler.setup(compactingUtil);
            }
            blockEntity.hasCheckedRecipes = true;
        }
    }

    @Override
    public InteractionResult onSlotActivated(Player player, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ, int slot) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem().equals(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CONFIGURATION_TOOL)
                || stack.getItem().equals(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.LINKING_TOOL))
            return InteractionResult.PASS;

        // Setup compacting if not already setup; a denied item never becomes the drawer's item
        if (!handler.isSetup() && slot != -1 && !stack.isEmpty() && !StorageTags.isDrawerDenied(stack.getItem())) {
            ItemStack setupStack = stack.copy();
            setupStack.setCount(1);
            CompactingUtil compactingUtil = new CompactingUtil(this.level, 2);
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

        if (slot != -1 && !player.isShiftKeyDown()) {
            useSlot(handler, player, hand, slot);
        } else if (!level.isClientSide) {
            player.openMenu(this);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Handle left-click extraction from a specific slot.
     */
    @Override
    public void onClicked(Player player, int slot) {
        if (slot != -1) {
            try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                    .openOuter()) {
                net.fabricmc.fabric.api.transfer.v1.item.ItemVariant resource = handler.getResource(slot);
                if (!resource.isBlank()) {
                    int maxExtract = player.isShiftKeyDown() ? resource.toStack().getMaxStackSize() : 1;
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
        return 3; // Match original Forge implementation for simple compacting drawers
    }

    @Override
    public int getBaseSize(int lost) {
        return handler.getSlotLimitBase(lost);
    }

    @Override
    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Handler")) {
            handler.deserializeNBT(tag.getCompound("Handler"), registries);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Handler", handler.serializeNBT(registries));
    }

    @Override
    public boolean isEverythingEmpty() {
        // A broken drawer keeps its priority, like the original
        if (getPriority() != 0)
            return false;
        for (net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view : getStorage()) {
            if (!view.isResourceBlank()) {
                return false;
            }
        }
        // Check upgrades (matching DrawerTile pattern)
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

    // ExtendedScreenHandlerFactory implementation
    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int containerId,
            net.minecraft.world.entity.player.Inventory playerInventory, Player player) {
        return new com.koudesuk.functionalstorage.inventory.DrawerMenu(containerId, playerInventory,
                getStorageUpgrades(), getUtilityUpgrades(), this);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
        return new BlockPosPayload(this.getBlockPos());
    }
}
