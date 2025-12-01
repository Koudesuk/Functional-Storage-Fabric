package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.ControllerExtensionTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class ControllerExtensionBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public ControllerExtensionBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOCKED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerExtensionTile(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ControllerExtensionTile tile) {
            return tile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x, hit.getLocation().y,
                    hit.getLocation().z);
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ControllerExtensionTile tile) {
                if (tile.getControllerPos() != null) {
                    BlockEntity controller = level.getBlockEntity(tile.getControllerPos());
                    if (controller instanceof StorageControllerTile controllerTile) {
                        controllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, pos);
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
