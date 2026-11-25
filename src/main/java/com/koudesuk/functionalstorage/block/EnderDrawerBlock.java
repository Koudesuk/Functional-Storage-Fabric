package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EnderDrawerBlock extends Block implements EntityBlock, DrawerFront {

    private static final java.util.Map<String, List<ItemStack>> FREQUENCY_LOOK = new java.util.HashMap<>();

    /**
     * The icons that stand for a frequency, one vanilla non-block item per part of the UUID, picked
     * the same way as the original so a frequency looks the same in both.
     */
    public static List<ItemStack> getFrequencyDisplay(String frequency) {
        return FREQUENCY_LOOK.computeIfAbsent(frequency, key -> {
            List<net.minecraft.world.item.Item> items = net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
                    .filter(item -> item != net.minecraft.world.item.Items.AIR
                            && !(item instanceof net.minecraft.world.item.BlockItem)
                            && net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace()
                                    .equals("minecraft"))
                    .toList();
            return java.util.Arrays.stream(key.split("-"))
                    .map(part -> new ItemStack(items.get(Math.abs(part.hashCode() % items.size()))))
                    .toList();
        });
    }

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
    public net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide)
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnderDrawerTile tile) {
            InteractionResult result = tile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y,
                    hit.getLocation().z, getHit(state, pos, hit));
            return switch (result) {
                case SUCCESS -> net.minecraft.world.ItemInteractionResult.SUCCESS;
                case CONSUME -> net.minecraft.world.ItemInteractionResult.CONSUME;
                case FAIL -> net.minecraft.world.ItemInteractionResult.FAIL;
                default -> net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            };
        }
        return net.minecraft.world.ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof EnderDrawerTile tile) {
            return tile.onSlotActivated(player, InteractionHand.MAIN_HAND, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y,
                    hit.getLocation().z, getHit(state, pos, hit));
        }
        return InteractionResult.PASS;
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, net.minecraft.world.level.BlockGetter level,
            BlockPos pos) {
        return DrawerFront.isMiningFront(state, player, pos) ? 0 : super.getDestroyProgress(state, player, level, pos);
    }

    @Override
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

            double margin = 0.0625;
            if (hitX < margin || hitX > 1 - margin || hitY < margin || hitY > 1 - margin) {
                return -1;
            }

            return 0;
        }
        return -1;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        stacks.add(getDropStack(state, builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY)));
        return stacks;
    }

    @Override
    public ItemStack getDropStack(BlockState state, @Nullable BlockEntity drawerTile) {
        ItemStack stack = new ItemStack(this);
        if (drawerTile instanceof EnderDrawerTile tile) {
            if (!tile.isEverythingEmpty() && tile.getLevel() != null) {
                stack.set(FSAttachments.TILE,
                        com.koudesuk.functionalstorage.util.ItemStackHelper.saveBlockEntityData(tile));
            }
            if (tile.isLocked()) {
                stack.set(FSAttachments.LOCKED, tile.isLocked());
            }
        }
        return stack;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof ControllableDrawerTile tile) {
            if (stack.has(FSAttachments.LOCKED)) {
                tile.setLocked(stack.getOrDefault(FSAttachments.LOCKED, false));
            }
            if (stack.has(FSAttachments.TILE)) {
                CompoundTag tileData = stack.get(FSAttachments.TILE);
                if (tileData != null) {
                    tile.loadFromTag(tileData, level.registryAccess());
                    tile.setChanged();
                    if (!level.isClientSide) {
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
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

    // Comparators read how full the drawer is, like the original
    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof ControllableDrawerTile<?> tile ? tile.getComparatorSignal() : 0;
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
                ItemStack stack = utilityUpgrades.getItem(i);
                if (stack
                        .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                    int redstoneSlot = stack.getOrDefault(FSAttachments.SLOT, 0);
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
        // Items with a data-driven behavior (the original's emit_redstone) give their own signal
        return blockEntity instanceof ControllableDrawerTile<?> drawer
                ? Math.max(0, drawer.getBehaviorSignal(state, direction))
                : 0;
    }
}
