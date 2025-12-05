package com.koudesuk.functionalstorage.block.tile;

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
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {

    public CompactingInventoryHandler handler;
    private boolean hasCheckedRecipes;

    public SimpleCompactingDrawerTile(BlockPos pos, BlockState state) {
        this(FunctionalStorageBlockEntities.SIMPLE_COMPACTING_DRAWER, pos, state);
    }

    public SimpleCompactingDrawerTile(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
            BlockState state) {
        super(type, pos, state);
        this.handler = new CompactingInventoryHandler(2) {
            @Override
            public void onChange() {
                SimpleCompactingDrawerTile.this.setChanged();
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

        // Open GUI when clicking non-slot areas
        if (slot == -1) {
            if (!level.isClientSide) {
                player.openMenu(this);
            }
            return InteractionResult.SUCCESS;
        }

        if (!handler.isSetup() && slot != -1) {
            stack = player.getItemInHand(hand).copy();
            stack.setCount(1);
            CompactingUtil compactingUtil = new CompactingUtil(this.level, 2);
            compactingUtil.setup(stack);
            handler.setup(compactingUtil);
            for (int i = 0; i < handler.getResultList().size(); i++) {
                if (ItemStack.isSameItem(handler.getResultList().get(i).getResult(), stack)) {
                    slot = i;
                    break;
                }
            }
        }
        return super.onSlotActivated(player, hand, facing, hitX, hitY, hitZ, slot);
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

    public boolean isEverythingEmpty() {
        for (net.fabricmc.fabric.api.transfer.v1.storage.StorageView<ItemVariant> view : getStorage()) {
            if (!view.isResourceBlank()) {
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
    public void writeScreenOpeningData(net.minecraft.server.level.ServerPlayer player,
            net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeBlockPos(getBlockPos());
    }
}
