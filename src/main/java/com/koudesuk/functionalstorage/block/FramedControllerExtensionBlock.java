package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedControllerExtensionTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Framed variant of the Controller Access Point that supports custom textures.
 * Extends ControllerExtensionBlock and adds support for FramedDrawerModelData.
 */
public class FramedControllerExtensionBlock extends ControllerExtensionBlock {

    public FramedControllerExtensionBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FramedControllerExtensionTile(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedControllerExtensionTile framedTile) {
            framedTile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);

        if (blockEntity instanceof FramedControllerExtensionTile framedTile) {
            if (framedTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedTile.getFramedDrawerModelData().serializeNBT());
            }
        }

        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FramedControllerExtensionTile framedTile) {
            if (framedTile.getFramedDrawerModelData() != null) {
                if (!framedTile.getFramedDrawerModelData().getDesign().isEmpty()) {
                    ItemStack stack = new ItemStack(this);
                    stack.getOrCreateTag().put("Style", framedTile.getFramedDrawerModelData().serializeNBT());
                    return stack;
                }
            }
        }
        return super.getCloneItemStack(level, pos, state);
    }
}
