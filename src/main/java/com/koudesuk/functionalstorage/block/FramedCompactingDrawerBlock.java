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

public class FramedCompactingDrawerBlock extends CompactingDrawerBlock {

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
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide)
            return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FramedCompactingDrawerTile tile) {
            net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
            if (result instanceof BlockHitResult blockHitResult) {
                tile.onClicked(player, getHit(state, pos, blockHitResult));
            }
        }
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
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        ItemStack stack = new ItemStack(this);
        BlockEntity drawerTile = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
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
        stacks.add(stack);
        return stacks;
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
        tooltip.add(Component.translatable("frameddrawer.use.line1").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("frameddrawer.use.line2").withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(Component.translatable("frameddrawer.use.line3").withStyle(net.minecraft.ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
