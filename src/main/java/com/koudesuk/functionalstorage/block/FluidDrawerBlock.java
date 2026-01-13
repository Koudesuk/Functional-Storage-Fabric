package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.DrawerWoodType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FluidDrawerTile fluidDrawerTile) {
            return fluidDrawerTile.onSlotActivated(player, hand, hit.getDirection(),
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
    public java.util.List<net.minecraft.world.item.ItemStack> getDrops(BlockState state,
            net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> stacks = net.minecraft.core.NonNullList
                .create();
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(this);
        BlockEntity drawerTile = builder
                .getOptionalParameter(net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_ENTITY);
        if (drawerTile instanceof FluidDrawerTile tile) {
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
    public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
            @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity placer,
            net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (stack.hasTag()) {
            if (stack.getTag().contains("Tile")) {
                if (entity instanceof FluidDrawerTile tile) {
                    entity.load(stack.getTag().getCompound("Tile"));
                    tile.setChanged();
                    if (!level.isClientSide) {
                        level.sendBlockUpdated(pos, state, state, 3);
                    }
                }
            }
            if (stack.getTag().contains("Locked")) {
                if (entity instanceof FluidDrawerTile tile) {
                    tile.setLocked(stack.getTag().getBoolean("Locked"));
                }
            }
        }
    }

    @Override
    public int getSignal(BlockState state, net.minecraft.world.level.BlockGetter blockGetter, BlockPos pos,
            net.minecraft.core.Direction direction) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(pos);
        if (blockEntity instanceof FluidDrawerTile tile) {
            net.minecraft.world.SimpleContainer utilityUpgrades = tile.getUtilityUpgrades();
            for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = utilityUpgrades.getItem(i);
                if (stack
                        .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                    int redstoneSlot = stack.getOrCreateTag().getInt("Slot");
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
