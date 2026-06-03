package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class DrawerControllerBlock extends Block implements EntityBlock {

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DrawerControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StorageControllerTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.STORAGE_CONTROLLER
                ? (world, pos, blockState, blockEntity) -> StorageControllerTile.tick(world, pos, blockState,
                        (StorageControllerTile) blockEntity)
                : null;
    }

    @Override
    public net.minecraft.world.InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StorageControllerTile tile) {
            InteractionResult result = tile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y,
                    hit.getLocation().z, 0);
            return result == net.minecraft.world.InteractionResult.PASS ? net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND : result;
        }
        return net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof StorageControllerTile tile) {
            return tile.onSlotActivated(player, InteractionHand.MAIN_HAND, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y,
                    hit.getLocation().z, 0);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, net.minecraft.server.level.ServerLevel level,
            BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        net.minecraft.core.NonNullList<ItemStack> stacks = net.minecraft.core.NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder
                .getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (blockEntity instanceof StorageControllerTile tile) {
            if (!tile.isEverythingEmpty() && tile.getLevel() != null) {
                stack.set(FSAttachments.TILE, tile.saveWithoutMetadata(tile.getLevel().registryAccess()));
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof StorageControllerTile tile) {
            if (stack.has(FSAttachments.TILE)) {
                CompoundTag tileData = stack.get(FSAttachments.TILE);
                if (tileData != null) {
                    tile.loadFromTag(tileData, level.registryAccess());
                    tile.setChanged();
                    if (!level.isClientSide()) {
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
        }
    }
}
