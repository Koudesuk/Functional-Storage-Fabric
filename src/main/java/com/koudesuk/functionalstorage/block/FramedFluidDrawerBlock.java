package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedFluidDrawerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof FramedFluidDrawerTile tile
                && tile.getFramedDrawerModelData() != null && tile.getLevel() != null) {
            for (ItemStack stack : drops) {
                stack.set(FSAttachments.STYLE,
                        tile.getFramedDrawerModelData().serializeNBT(tile.getLevel().registryAccess()));
            }
        }
        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        ItemStack stack = new ItemStack(this);
        if (level.getBlockEntity(pos) instanceof FramedFluidDrawerTile tile && tile.getFramedDrawerModelData() != null
                && !tile.getFramedDrawerModelData().getDesign().isEmpty()) {
            stack.set(FSAttachments.STYLE, tile.getFramedDrawerModelData().serializeNBT(level.registryAccess()));
        }
        return stack;
    }
}
