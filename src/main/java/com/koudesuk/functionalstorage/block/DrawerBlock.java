package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.IWoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public class DrawerBlock extends Block implements EntityBlock {

    private final DrawerType type;
    private final IWoodType woodType;

    public static final net.minecraft.world.level.block.state.properties.BooleanProperty LOCKED = net.minecraft.world.level.block.state.properties.BooleanProperty
            .create("locked");

    public DrawerBlock(IWoodType woodType, DrawerType type, Properties properties) {
        super(properties);
        this.woodType = woodType;
        this.type = type;
        this.registerDefaultState(this.defaultBlockState().setValue(LOCKED, false).setValue(
                net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,
                net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(
            net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LOCKED, net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        return this.defaultBlockState().setValue(
                net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING,
                context.getHorizontalDirection().getOpposite());
    }

    @Override
    public net.minecraft.world.InteractionResult use(BlockState state, net.minecraft.world.level.Level level,
            BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand,
            net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide)
            return net.minecraft.world.InteractionResult.SUCCESS;
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.DrawerTile drawerTile) {
            return drawerTile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y, hit.getLocation().z, getHit(state, pos, hit));
        }
        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
            net.minecraft.world.entity.player.Player player) {
        if (level.isClientSide)
            return;
        net.minecraft.world.level.block.entity.BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.DrawerTile drawerTile) {
            net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
            if (result instanceof net.minecraft.world.phys.BlockHitResult blockHitResult) {
                drawerTile.onClicked(player, getHit(state, pos, blockHitResult));
            }
        }
    }

    @Override
    public float getDestroyProgress(BlockState state, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
        if (result instanceof net.minecraft.world.phys.BlockHitResult blockHitResult) {
            net.minecraft.core.Direction facing = state
                    .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            if (blockHitResult.getDirection() == facing) {
                return 0.0f;
            }
        }
        return super.getDestroyProgress(state, player, level, pos);
    }

    public int getHit(BlockState state, BlockPos pos, net.minecraft.world.phys.BlockHitResult blockHitResult) {
        net.minecraft.core.Direction facing = state
                .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        net.minecraft.core.Direction hitFace = blockHitResult.getDirection();
        if (hitFace == facing) {
            net.minecraft.world.phys.Vec3 hit = blockHitResult.getLocation().subtract(pos.getX(), pos.getY(),
                    pos.getZ());
            double x = hit.x;
            double y = hit.y;
            double z = hit.z;

            double hitX = 0;
            double hitY = y;

            if (facing == net.minecraft.core.Direction.NORTH)
                hitX = 1 - x;
            else if (facing == net.minecraft.core.Direction.SOUTH)
                hitX = x;
            else if (facing == net.minecraft.core.Direction.EAST)
                hitX = 1 - z;
            else if (facing == net.minecraft.core.Direction.WEST)
                hitX = z;

            // Check margins (1/16th)
            double margin = 0.0625;
            if (hitX < margin || hitX > 1 - margin || hitY < margin || hitY > 1 - margin) {
                return -1;
            }

            if (this.type == com.koudesuk.functionalstorage.util.DrawerType.X_1)
                return 0;

            if (this.type == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                // Divider at 0.5, check margin around it
                if (hitY > 0.5 - margin && hitY < 0.5 + margin)
                    return -1;
                // Match original Forge VoxelShape ordering: Bottom=0, Top=1
                if (hitY > 0.5)
                    return 1; // Top slot
                return 0; // Bottom slot
            }

            if (this.type == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                // Dividers at 0.5
                if (hitX > 0.5 - margin && hitX < 0.5 + margin)
                    return -1;
                if (hitY > 0.5 - margin && hitY < 0.5 + margin)
                    return -1;

                // Match original Forge VoxelShape ordering:
                // Forge builds shapes in block coordinates, not visual coordinates.
                // For NORTH-facing: low block X appears on player's RIGHT, high block X on
                // LEFT.
                // hitX is transformed so hitX < 0.5 = visual LEFT, hitX > 0.5 = visual RIGHT
                //
                // Forge VoxelShape index order:
                // Index 0 = Bottom, low block X = visual RIGHT
                // Index 1 = Bottom, high block X = visual LEFT
                // Index 2 = Top, low block X = visual RIGHT
                // Index 3 = Top, high block X = visual LEFT
                if (hitX > 0.5 && hitY < 0.5)
                    return 0; // visual RIGHT, BOTTOM
                if (hitX < 0.5 && hitY < 0.5)
                    return 1; // visual LEFT, BOTTOM
                if (hitX > 0.5 && hitY > 0.5)
                    return 2; // visual RIGHT, TOP
                return 3; // visual LEFT, TOP
            }
        }
        return -1;
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        net.minecraft.core.NonNullList<ItemStack> stacks = net.minecraft.core.NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder
                .getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof com.koudesuk.functionalstorage.block.tile.DrawerTile tile) {
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
    public void onRemove(BlockState state, net.minecraft.world.level.Level level, BlockPos pos,
            BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof DrawerTile drawerTile) {
                if (drawerTile.getControllerPos() != null) {
                    BlockEntity controller = level.getBlockEntity(drawerTile.getControllerPos());
                    if (controller instanceof com.koudesuk.functionalstorage.block.tile.StorageControllerTile storageControllerTile) {
                        storageControllerTile.addConnectedDrawers(
                                com.koudesuk.functionalstorage.item.LinkingToolItem.ActionMode.REMOVE, pos);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
            @Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (stack.hasTag()) {
            if (stack.getTag().contains("Tile")) {
                if (entity instanceof com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile tile) {
                    entity.load(stack.getTag().getCompound("Tile"));
                    tile.setChanged();
                    if (!level.isClientSide) {
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
            if (stack.getTag().contains("Locked")) {
                if (entity instanceof com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile tile) {
                    tile.setLocked(stack.getTag().getBoolean("Locked"));
                }
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DrawerTile(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            net.minecraft.world.level.Level level, BlockState state,
            net.minecraft.world.level.block.entity.BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile tile) {
                com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile.tick(level1, pos, state1, tile);
            }
        };
    }

    public DrawerType getType() {
        return type;
    }

    public IWoodType getWoodType() {
        return woodType;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, net.minecraft.world.level.BlockGetter blockGetter, BlockPos pos,
            net.minecraft.core.Direction direction) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(pos);
        if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.DrawerTile tile) {
            net.minecraft.world.SimpleContainer utilityUpgrades = tile.getUtilityUpgrades();
            for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = utilityUpgrades.getItem(i);
                if (stack
                        .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                    int redstoneSlot = stack.getOrCreateTag().getInt("Slot");
                    var storedStacks = tile.getHandler().getStoredStacks();
                    if (redstoneSlot < storedStacks.size()) {
                        int slotLimit = tile.getHandler().getSlotLimit(redstoneSlot);
                        int amount = storedStacks.get(redstoneSlot).getAmount();
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
