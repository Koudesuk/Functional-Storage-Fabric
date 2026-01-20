package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.DrawerWoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class FluidDrawerBlock extends DrawerBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public FluidDrawerBlock(DrawerType type, Properties properties) {
        super(DrawerWoodType.OAK, type, properties);
        this.registerDefaultState(this.defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDrawerTile(pos, state, this.getType());
    }

    @Override
    public net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide)
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidDrawerTile fluidDrawerTile) {
            InteractionResult result = fluidDrawerTile.onSlotActivated(player, hand, hit.getDirection(),
                    hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    getHit(state, pos, hit));
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
        if (blockEntity instanceof FluidDrawerTile fluidDrawerTile) {
            return fluidDrawerTile.onSlotActivated(player, InteractionHand.MAIN_HAND, hit.getDirection(),
                    hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    getHit(state, pos, hit));
        }
        return InteractionResult.PASS;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide)
            return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidDrawerTile fluidDrawerTile) {
            HitResult result = player.pick(20, 0, false);
            if (result instanceof BlockHitResult blockHitResult) {
                fluidDrawerTile.onClicked(player, getHit(state, pos, blockHitResult));
            }
        }
    }

    @Override
    public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level, BlockState state, net.minecraft.world.level.block.entity.BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof FluidDrawerTile tile) {
                FluidDrawerTile.tick(level1, pos, state1, tile);
            }
        };
    }

    @Override
    public java.util.List<ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        net.minecraft.core.NonNullList<ItemStack> stacks = net.minecraft.core.NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder
                .getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof FluidDrawerTile tile) {
            if (!tile.isEverythingEmpty() && tile.getLevel() != null) {
                stack.set(FSAttachments.TILE,
                        com.koudesuk.functionalstorage.util.ItemStackHelper.saveBlockEntityData(tile));
            }
            if (tile.isLocked()) {
                stack.set(FSAttachments.LOCKED, tile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FluidDrawerTile tile) {
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
    public int getSignal(BlockState state, net.minecraft.world.level.BlockGetter blockGetter, BlockPos pos,
            Direction direction) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(pos);
        if (blockEntity instanceof FluidDrawerTile tile) {
            net.minecraft.world.SimpleContainer utilityUpgrades = tile.getUtilityUpgrades();
            for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
                ItemStack stack = utilityUpgrades.getItem(i);
                if (stack
                        .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                    int redstoneSlot = stack.getOrDefault(FSAttachments.SLOT, 0);
                    var handler = tile.getHandler();
                    if (redstoneSlot < this.getType().getSlots()) {
                        long slotLimit = handler.getSlotLimit(redstoneSlot);
                        long amount = handler.getAmount(redstoneSlot);
                        if (slotLimit > 0) {
                            int signal = (int) (amount * 15 / slotLimit);
                            return Math.min(15, signal);
                        }
                    }
                }
            }
        }
        return 0;
    }
}
