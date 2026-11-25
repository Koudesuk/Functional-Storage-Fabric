package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedFluidDrawerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A fluid drawer retextured like the framed drawers, like the original's FramedFluidDrawerBlock.
 */
public class FramedFluidDrawerBlock extends FluidDrawerBlock implements FramedBlock {

    public FramedFluidDrawerBlock(DrawerType type, Properties properties) {
        super(type, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FramedFluidDrawerTile(pos, state, this.getType());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof FramedFluidDrawerTile tile) {
            tile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        }
    }

    @Override
    public ItemStack getDropStack(BlockState state, @Nullable BlockEntity blockEntity) {
        ItemStack stack = super.getDropStack(state, blockEntity);
        if (blockEntity instanceof FramedFluidDrawerTile tile && tile.getFramedDrawerModelData() != null
                && tile.getLevel() != null) {
            stack.set(FSAttachments.STYLE,
                    tile.getFramedDrawerModelData().serializeNBT(tile.getLevel().registryAccess()));
        }
        return stack;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof FramedFluidDrawerTile tile && tile.getFramedDrawerModelData() != null
                && !tile.getFramedDrawerModelData().getDesign().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.set(FSAttachments.STYLE, tile.getFramedDrawerModelData().serializeNBT(level.registryAccess()));
            return stack;
        }
        return new ItemStack(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        FramedBlock.appendTooltip(tooltip);
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
