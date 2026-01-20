package com.koudesuk.functionalstorage.block;

import com.koudesuk.functionalstorage.block.tile.FramedCompactingDrawerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
    public net.minecraft.world.ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide)
            return net.minecraft.world.ItemInteractionResult.SUCCESS;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof FramedCompactingDrawerTile tile) {
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
        if (blockEntity instanceof FramedCompactingDrawerTile tile) {
            return tile.onSlotActivated(player, InteractionHand.MAIN_HAND, hit.getDirection(), hit.getLocation().x,
                    hit.getLocation().y,
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
            if (!framedDrawerTile.isEverythingEmpty() && framedDrawerTile.getLevel() != null) {
                stack.set(FSAttachments.TILE,
                        com.koudesuk.functionalstorage.util.ItemStackHelper.saveBlockEntityData(framedDrawerTile));
            }
            if (framedDrawerTile.getFramedDrawerModelData() != null) {
                stack.set(FSAttachments.STYLE, framedDrawerTile.getFramedDrawerModelData()
                        .serializeNBT(framedDrawerTile.getLevel().registryAccess()));
            }
            if (framedDrawerTile.isLocked()) {
                stack.set(FSAttachments.LOCKED, framedDrawerTile.isLocked());
            }
        }
        stacks.add(stack);
        return stacks;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity instanceof FramedCompactingDrawerTile framedDrawerTile
                && framedDrawerTile.getFramedDrawerModelData() != null
                && !framedDrawerTile.getFramedDrawerModelData().getDesign().isEmpty()) {
            ItemStack stack = new ItemStack(this);
            stack.set(FSAttachments.STYLE,
                    framedDrawerTile.getFramedDrawerModelData().serializeNBT(level.registryAccess()));
            return stack;
        }
        return new ItemStack(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag flag) {
        String useText = Component.translatable("frameddrawer.use").getString();
        for (String line : useText.split("\n")) {
            if (!line.trim().isEmpty()) {
                tooltip.add(Component.literal(line.trim()).withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
