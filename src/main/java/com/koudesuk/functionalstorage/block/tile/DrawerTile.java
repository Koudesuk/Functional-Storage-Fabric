package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.network.BlockPosPayload;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.registry.FSAttachments;
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
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<BlockPosPayload> {

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
                DrawerTile.this.updateComparatorOutput();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getMultiplier() {
                return DrawerTile.this.getStorageMultiplier();
            }

            @Override
            public double getSizeFactor() {
                // The original's base is the drawer's stacks, one stack with the Iron Downgrade
                return DrawerTile.this.getSizeFactor(FSAttachments.ITEM_STORAGE_MODIFIER,
                        hasDowngrade() ? 1 : DrawerTile.this.type.getSlotAmount() / 64D);
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
    protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
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

    public net.minecraft.world.InteractionResult onSlotActivated(net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand, net.minecraft.core.Direction facing, double hitX, double hitY,
            double hitZ, int slot) {
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof com.koudesuk.functionalstorage.item.ConfigurationToolItem
                || stack.getItem() instanceof com.koudesuk.functionalstorage.item.LinkingToolItem)
            return net.minecraft.world.InteractionResult.PASS;

        if (installUpgrade(stack))
            return net.minecraft.world.InteractionResult.SUCCESS;

        if (slot != -1 && !player.isShiftKeyDown()) {
            // An empty slot of a locked drawer takes on whatever it is clicked with
            if (!stack.isEmpty() && handler.isLocked() && handler.getResource(slot).isBlank())
                handler.setLockedItem(slot, net.fabricmc.fabric.api.transfer.v1.item.ItemVariant.of(stack));
            useSlot(handler, player, hand, slot);
        } else if (!level.isClientSide) {
            player.openMenu(this);
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public void onClicked(net.minecraft.world.entity.player.Player player, int slot) {
        if (slot != -1) {
            // Extract item from the specific slot
            try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                    .openOuter()) {
                net.fabricmc.fabric.api.transfer.v1.item.ItemVariant resource = handler.getResource(slot);
                if (!resource.isBlank()) {
                    int maxExtract = player.isShiftKeyDown() ? resource.toStack().getMaxStackSize() : 1;
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
    public BlockPosPayload getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
        return new BlockPosPayload(this.getBlockPos());
    }

    @Override
    public boolean isEverythingEmpty() {
        // A broken drawer keeps its priority, like the original
        if (getPriority() != 0)
            return false;
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