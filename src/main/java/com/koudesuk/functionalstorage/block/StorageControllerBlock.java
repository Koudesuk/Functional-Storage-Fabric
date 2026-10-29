package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class StorageControllerBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public StorageControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public net.minecraft.world.InteractionResult useItemOn(net.minecraft.world.item.ItemStack stack,
            BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StorageControllerTile tile) {
            InteractionResult result = tile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y,
                    hit.getLocation().z, -1);
            return result == net.minecraft.world.InteractionResult.PASS ? net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND : result;
        }
        return net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StorageControllerTile tile) {
            return tile.onSlotActivated(player, InteractionHand.MAIN_HAND, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y,
                    hit.getLocation().z, -1);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
            BlockPos pos, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StorageControllerTile tile) {
            if (!tile.isEverythingEmpty()) {
                net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(this);
                stack.set(FSAttachments.TILE,
                        com.koudesuk.functionalstorage.util.ItemStackHelper.saveBlockEntityData(tile));
                if (tile.isLocked()) {
                    stack.set(FSAttachments.LOCKED, tile.isLocked());
                }
                Block.popResource(level, pos, stack);
            }
            // Unlinking the drawers happens in StorageControllerTile.preRemoveSideEffects
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageControllerTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof StorageControllerTile tile) {
                StorageControllerTile.tick(level1, pos, state1, tile);
            }
        };
    }
}
