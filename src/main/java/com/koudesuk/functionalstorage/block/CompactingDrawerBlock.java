package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CompactingDrawerBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LOCKED = BooleanProperty.create("locked");

    public CompactingDrawerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LOCKED, false));
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
        return new CompactingDrawerTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level level,
            BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> blockEntityType) {
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
        if (blockEntity instanceof CompactingDrawerTile tile) {
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
        if (blockEntity instanceof CompactingDrawerTile tile) {
            net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
            if (result instanceof BlockHitResult blockHitResult) {
                tile.onClicked(player, getHit(state, pos, blockHitResult));
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CompactingDrawerTile tile) {
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

    @Override
    public float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level,
            BlockPos pos) {
        net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
        if (result instanceof BlockHitResult blockHitResult) {
            Direction facing = state.getValue(FACING);
            if (blockHitResult.getDirection() == facing) {
                return 0.0f; // Prevent breaking from front face
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    public int getHit(BlockState state, BlockPos pos, BlockHitResult blockHitResult) {
        Direction facing = state.getValue(FACING);
        Direction hitFace = blockHitResult.getDirection();

        // Only process hits on the front face
        if (hitFace != facing) {
            return -1;
        }

        Vec3 hit = blockHitResult.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        double hitX = 0;
        double hitY = hit.y;

        // Calculate hitX based on facing direction
        if (facing == Direction.NORTH) {
            hitX = 1 - hit.x;
        } else if (facing == Direction.SOUTH) {
            hitX = hit.x;
        } else if (facing == Direction.EAST) {
            hitX = 1 - hit.z;
        } else if (facing == Direction.WEST) {
            hitX = hit.z;
        }

        // Margin for edges/dividers (1/16th of a block)
        double margin = 0.0625;

        // Check if clicking on edges
        if (hitX < margin || hitX > 1 - margin || hitY < margin || hitY > 1 - margin) {
            return -1; // Edge click - open GUI
        }

        // Compacting drawer has 3 slots: bottom-left(0), bottom-right(1), top(2)
        // Strictly follow DrawerBlock X_4 pattern

        // Check horizontal divider at y=0.5
        if (hitY > 0.5 - margin && hitY < 0.5 + margin) {
            return -1; // Clicking on horizontal divider - open GUI
        }

        if (hitY > 0.5) {
            // Top slot (slot 2)
            return 2;
        } else {
            // Bottom half - check for vertical divider at x=0.5
            if (hitX > 0.5 - margin && hitX < 0.5 + margin) {
                return -1; // Clicking on vertical divider - open GUI
            }

            // Match DrawerBlock X_4 exactly:
            // hitX > 0.5 -> return 0
            // hitX < 0.5 -> return 1
            if (hitX > 0.5) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    @Override
    public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> stacks = net.minecraft.core.NonNullList
                .create();
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(this);
        BlockEntity drawerTile = builder
                .getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof CompactingDrawerTile tile) {
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
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, net.minecraft.world.level.BlockGetter blockGetter, BlockPos pos,
            Direction direction) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(pos);
        if (blockEntity instanceof CompactingDrawerTile tile) {
            net.minecraft.world.SimpleContainer utilityUpgrades = tile.getUtilityUpgrades();
            for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = utilityUpgrades.getItem(i);
                if (stack
                        .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                    int redstoneSlot = stack.getOrCreateTag().getInt("Slot");
                    var handler = tile.handler;
                    if (redstoneSlot < handler.getSlots()) {
                        int slotLimit = handler.getSlotLimit(redstoneSlot);
                        int amount = handler.getStackInSlot(redstoneSlot).getCount();
                        if (slotLimit > 0) {
                            int signal = amount * 14 / slotLimit;
                            return signal + (signal > 0 ? 1 : 0);
                        }
                    }
                }
            }
        }
        return 0;
    }
}
