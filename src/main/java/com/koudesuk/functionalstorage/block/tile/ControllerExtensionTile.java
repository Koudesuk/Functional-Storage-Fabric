package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ControllerExtensionTile extends ItemControllableDrawerTile<ControllerExtensionTile> {

    public ControllerExtensionTile(BlockPos pos, BlockState state) {
        super(FunctionalStorageBlockEntities.CONTROLLER_EXTENSION, pos, state);
    }

    /**
     * Protected constructor for subclasses (e.g., FramedControllerExtensionTile)
     * that need to specify
     * their own BlockEntityType.
     */
    protected ControllerExtensionTile(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
            BlockState state) {
        super(type, pos, state);
    }

    @Override
    public Storage<ItemVariant> getStorage() {
        if (getControllerPos() != null && level != null) {
            BlockEntity entity = level.getBlockEntity(getControllerPos());
            if (entity instanceof StorageControllerTile controller) {
                return controller.getStorage();
            }
        }
        return null;
    }

    @Override
    public int getStorageSlotAmount() {
        return 1;
    }

    @Override
    public int getBaseSize(int lost) {
        return 1;
    }

    public InteractionResult onSlotActivated(Player player, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem().equals(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CONFIGURATION_TOOL)
                || stack.getItem().equals(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.LINKING_TOOL))
            return InteractionResult.PASS;

        if (getControllerPos() != null && level != null) {
            BlockEntity entity = level.getBlockEntity(getControllerPos());
            if (entity instanceof StorageControllerTile controller) {
                return controller.onSlotActivated(player, hand, facing, hitX, hitY, hitZ, -1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
