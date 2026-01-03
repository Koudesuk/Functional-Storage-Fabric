package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderDrawerBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public EnderDrawerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LOCKED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LOCKED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderDrawerTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile tile) {
                com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile.tick(level1, pos, state1, tile);
            }
        };
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnderDrawerTile tile) {
            return tile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x, hit.getLocation().y,
                    hit.getLocation().z, getHit(state, pos, hit));
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide)
            return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnderDrawerTile tile) {
            HitResult result = player.pick(20, 0, false);
            if (result instanceof BlockHitResult blockHitResult) {
                tile.onClicked(player, getHit(state, pos, blockHitResult));
            }
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player,
            net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        HitResult result = player.pick(20, 0, false);
        if (result instanceof BlockHitResult blockHitResult) {
            Direction facing = state.getValue(FACING);
            if (blockHitResult.getDirection() == facing) {
                return 0.0f;
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    /**
     * Determine which slot was clicked on the drawer face.
     * Ender Drawers are X_1 type (single slot), so returns 0 for front face, -1
     * otherwise.
     */
    public int getHit(BlockState state, BlockPos pos, BlockHitResult blockHitResult) {
        Direction facing = state.getValue(FACING);
        Direction hitFace = blockHitResult.getDirection();
        if (hitFace == facing) {
            Vec3 hit = blockHitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            double x = hit.x;
            double y = hit.y;
            double z = hit.z;

            double hitX = 0;
            double hitY = y;

            if (facing == Direction.NORTH)
                hitX = 1 - x;
            else if (facing == Direction.SOUTH)
                hitX = x;
            else if (facing == Direction.EAST)
                hitX = 1 - z;
            else if (facing == Direction.WEST)
                hitX = z;

            // Check margins (1/16th)
            double margin = 0.0625;
            if (hitX < margin || hitX > 1 - margin || hitY < margin || hitY > 1 - margin) {
                return -1;
            }

            // Ender Drawer is X_1 type - single slot
            return 0;
        }
        return -1;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof EnderDrawerTile tile) {
            if (!tile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.saveWithoutMetadata());
            }
            if (tile.isLocked()) {
                stack.getOrCreateTag().putBoolean("Locked", tile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (stack.hasTag()) {
            if (stack.getTag().contains("Tile")) {
                if (entity instanceof ControllableDrawerTile tile) {
                    entity.load(stack.getTag().getCompound("Tile"));
                    tile.setChanged();
                    if (!level.isClientSide) {
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
            if (stack.getTag().contains("Locked")) {
                if (entity instanceof ControllableDrawerTile tile) {
                    tile.setLocked(stack.getTag().getBoolean("Locked"));
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof EnderDrawerTile tile) {
                if (tile.getControllerPos() != null) {
                    BlockEntity controller = level.getBlockEntity(tile.getControllerPos());
                    if (controller instanceof StorageControllerTile storageControllerTile) {
                        storageControllerTile.addConnectedDrawers(LinkingToolItem.ActionMode.REMOVE, pos);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, net.minecraft.world.level.BlockGetter blockGetter, BlockPos pos,
            Direction direction) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(pos);
        if (blockEntity instanceof EnderDrawerTile tile) {
            net.minecraft.world.SimpleContainer utilityUpgrades = tile.getUtilityUpgrades();
            for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = utilityUpgrades.getItem(i);
                if (stack
                        .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                    int redstoneSlot = stack.getOrCreateTag().getInt("Slot");
                    var handler = tile.getHandler();
                    if (handler != null) {
                        var storedStacks = handler.getStoredStacks();
                        if (redstoneSlot < storedStacks.size()) {
                            int slotLimit = handler.getSlotLimit(redstoneSlot);
                            int amount = storedStacks.get(redstoneSlot).getAmount();
                            if (slotLimit > 0) {
                                int signal = amount * 14 / slotLimit;
                                return signal + (signal > 0 ? 1 : 0);
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
}
