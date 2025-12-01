package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class StorageControllerBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

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
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StorageControllerTile tile) {
            return tile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x, hit.getLocation().y,
                    hit.getLocation().z, -1);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof StorageControllerTile tile) {
                if (!tile.isEverythingEmpty()) {
                    net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(this);
                    net.minecraft.nbt.CompoundTag tag = tile.saveWithoutMetadata();
                    stack.getOrCreateTag().put("Tile", tag);
                    if (tile.isLocked()) {
                        stack.getOrCreateTag().putBoolean("Locked", tile.isLocked());
                    }
                    Block.popResource(level, pos, stack);
                }
                for (Long connectedDrawer : new ArrayList<>(tile.getConnectedDrawers().getConnectedDrawers())) {
                    BlockEntity connectedEntity = level.getBlockEntity(BlockPos.of(connectedDrawer));
                    if (connectedEntity instanceof StorageControllerTile)
                        continue;
                    if (connectedEntity instanceof ControllableDrawerTile<?> controllableDrawerTile) {
                        controllableDrawerTile.clearControllerPos();
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
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
        return level.isClientSide ? null : (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof StorageControllerTile tile) {
                StorageControllerTile.tick(level1, pos, state1, tile);
            }
        };
    }
}
