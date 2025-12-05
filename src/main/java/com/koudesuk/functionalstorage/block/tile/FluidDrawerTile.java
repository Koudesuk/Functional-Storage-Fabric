package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class FluidDrawerTile extends ControllableDrawerTile<FluidDrawerTile>
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {

    private final DrawerType type;
    private final FluidInventoryHandler handler;

    public FluidDrawerTile(BlockPos pos, BlockState state, DrawerType type) {
        super(getType(type), pos, state);
        this.type = type;
        this.handler = new FluidInventoryHandler(type) {
            @Override
            public void onChange() {
                FluidDrawerTile.this.setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getMultiplier() {
                return FluidDrawerTile.this.getStorageMultiplier();
            }

            @Override
            public boolean isVoid() {
                return FluidDrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return FluidDrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isLocked() {
                return FluidDrawerTile.this.isLocked();
            }

            @Override
            public boolean isCreative() {
                return FluidDrawerTile.this.isCreative();
            }
        };
    }

    private static BlockEntityType<?> getType(DrawerType type) {
        if (type == DrawerType.X_1)
            return FunctionalStorageBlockEntities.FLUID_DRAWER_1;
        if (type == DrawerType.X_2)
            return FunctionalStorageBlockEntities.FLUID_DRAWER_2;
        if (type == DrawerType.X_4)
            return FunctionalStorageBlockEntities.FLUID_DRAWER_4;
        return FunctionalStorageBlockEntities.FLUID_DRAWER_1;
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

    @Override
    public InteractionResult onSlotActivated(Player player, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ, int slot) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof com.koudesuk.functionalstorage.item.ConfigurationToolItem
                || stack.getItem() instanceof com.koudesuk.functionalstorage.item.LinkingToolItem)
            return InteractionResult.PASS;

        if (!stack.isEmpty() && stack.getItem() instanceof com.koudesuk.functionalstorage.item.UpgradeItem) {
            return super.onSlotActivated(player, hand, facing, hitX, hitY, hitZ, slot);
        }

        if (slot != -1 && !player.isShiftKeyDown()) {
            // Right-click: Insert fluid from hand to drawer ONLY
            Storage<FluidVariant> handStorage = ContainerItemContext.ofPlayerHand(player, hand).find(FluidStorage.ITEM);
            if (handStorage != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    // Try to insert from hand to drawer
                    for (StorageView<FluidVariant> view : handStorage) {
                        if (!view.isResourceBlank()) {
                            long inserted = handler.insert(view.getResource(), view.getAmount(), transaction);
                            if (inserted > 0) {
                                handStorage.extract(view.getResource(), inserted, transaction);
                                transaction.commit();
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
            // Note: Extraction is done via left-click (onClicked method)
            return InteractionResult.SUCCESS;
        } else if (slot == -1 || player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                player.openMenu(this);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public void onClicked(Player player, int slot) {
        if (slot != -1) {
            // Left-click: Extract fluid from drawer to hand container (bucket)
            Storage<FluidVariant> handStorage = ContainerItemContext.ofPlayerHand(player, InteractionHand.MAIN_HAND)
                    .find(FluidStorage.ITEM);
            if (handStorage != null) {
                try (Transaction transaction = Transaction.openOuter()) {
                    // Try to fill the container from the drawer
                    FluidVariant resource = handler.getResource(slot);
                    if (!resource.isBlank()) {
                        long inserted = handStorage.insert(resource, Long.MAX_VALUE, transaction);
                        if (inserted > 0) {
                            handler.extract(resource, inserted, transaction);
                            transaction.commit();
                        }
                    }
                }
            }
        }
    }

    public FluidInventoryHandler getHandler() {
        return handler;
    }

    public DrawerType getDrawerType() {
        return type;
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
}
