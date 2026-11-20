package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedCompactingDrawerTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FramedCompactingDrawerBlock extends CompactingDrawerBlock implements FramedBlock {

    public FramedCompactingDrawerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FramedCompactingDrawerTile(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
            BlockHitResult hit) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FramedCompactingDrawerTile tile) {
            return tile.onSlotActivated(player, hand, hit.getDirection(), hit.getLocation().x, hit.getLocation().y,
                    hit.getLocation().z, getHit(state, pos, hit));
        }
        return InteractionResult.PASS;
    }


    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedCompactingDrawerTile framedDrawerTile) {
            framedDrawerTile.setFramedDrawerModelData(FramedDrawerBlock.getDrawerModelData(stack));
        }
    }

    @Override
    public ItemStack getDropStack(BlockState state, @Nullable BlockEntity drawerTile) {
        ItemStack stack = new ItemStack(this);
        if (drawerTile instanceof FramedCompactingDrawerTile framedDrawerTile) {
            if (!framedDrawerTile.isEverythingEmpty()) {
                stack.getOrCreateTag().put("Tile", drawerTile.saveWithoutMetadata());
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            }
            if (framedDrawerTile.isLocked()) {
                stack.getOrCreateTag().putBoolean("Locked", framedDrawerTile.isLocked());
            }
        }
        return stack;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedCompactingDrawerTile framedDrawerTile
                && framedDrawerTile.getFramedDrawerModelData() != null
                && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateTag().put("Style", framedDrawerTile.getFramedDrawerModelData().serializeNBT());
            return stack;
        }
        return super.getCloneItemStack(level, pos, state);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip,
            TooltipFlag flag) {
        // Parse newlines in frameddrawer.use and add each line as a separate Component
        FramedBlock.appendTooltip(tooltip);
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
