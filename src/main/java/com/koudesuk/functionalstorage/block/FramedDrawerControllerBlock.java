package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedDrawerControllerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FramedDrawerControllerBlock extends DrawerControllerBlock {

    public FramedDrawerControllerBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FramedDrawerControllerTile(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(net.minecraft.world.level.Level level,
            BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER
                ? (world, pos, blockState, blockEntity) -> StorageControllerTile.tick(world, pos, blockState,
                        (StorageControllerTile) blockEntity)
                : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedDrawerControllerTile framedControllerTile) {
            framedControllerTile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof FramedDrawerControllerTile framedControllerTile) {
            if (framedControllerTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedControllerTile.getFramedDrawerModelData().serializeNBT());
            }
        }

        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FramedDrawerControllerTile framedControllerTile) {
            if (framedControllerTile.getFramedDrawerModelData() != null) {
                if (!framedControllerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
                    ItemStack stack = new ItemStack(this);
                    stack.getOrCreateTag().put("Style", framedControllerTile.getFramedDrawerModelData().serializeNBT());
                    return stack;
                }
            }
        }
        return super.getCloneItemStack(level, pos, state);
    }
}
