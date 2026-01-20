package com.koudesuk.functionalstorage.item;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.inventory.EnderInventoryHandler;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.world.EnderSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LinkingToolItem extends Item {

    public enum LinkingMode {
        SINGLE(TextColor.fromRgb(0x00FFFF)), // Cyan
        MULTIPLE(TextColor.fromRgb(0x00FF00)); // Green

        private final TextColor color;

        LinkingMode(TextColor color) {
            this.color = color;
        }

        public TextColor getColor() {
            return color;
        }
    }

    public enum ActionMode {
        ADD(TextColor.fromRgb(0x2883FA)), // Blue
        REMOVE(TextColor.fromRgb(0xFA9128)); // Orange

        private final TextColor color;

        ActionMode(TextColor color) {
            this.color = color;
        }

        public TextColor getColor() {
            return color;
        }
    }

    public LinkingToolItem(Properties properties) {
        super(properties);
    }

    public static LinkingMode getLinkingMode(ItemStack stack) {
        if (stack.has(FSAttachments.LINKING_MODE)) {
            return LinkingMode.valueOf(stack.get(FSAttachments.LINKING_MODE));
        }
        return LinkingMode.SINGLE;
    }

    public static ActionMode getActionMode(ItemStack stack) {
        if (stack.has(FSAttachments.ACTION_MODE)) {
            return ActionMode.valueOf(stack.get(FSAttachments.ACTION_MODE));
        }
        return ActionMode.ADD;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        initNbt(stack);
    }

    private ItemStack initNbt(ItemStack stack) {
        stack.set(FSAttachments.LINKING_MODE, LinkingMode.SINGLE.name());
        stack.set(FSAttachments.ACTION_MODE, ActionMode.ADD.name());
        return stack;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return stack.has(FSAttachments.ENDER_FREQUENCY);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        LinkingMode linkingMode = getLinkingMode(stack);
        ActionMode linkingAction = getActionMode(stack);

        if (blockEntity instanceof EnderDrawerTile) {
            if (stack.has(FSAttachments.ENDER_FREQUENCY)) {
                String frequency = stack.get(FSAttachments.ENDER_FREQUENCY);
                EnderInventoryHandler inventory = EnderSavedData.getInstance(context.getLevel())
                        .getFrequency(((EnderDrawerTile) blockEntity).getFrequency());
                if (inventory.getStoredStacks().get(0).getStack().isEmpty()
                        || (context.getPlayer().isShiftKeyDown() && stack.has(FSAttachments.ENDER_FREQUENCY))) {
                    ((EnderDrawerTile) blockEntity).setFrequency(frequency);
                    context.getPlayer().displayClientMessage(Component.translatable("linkingtool.ender.changed")
                            .setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                    stack.remove(FSAttachments.ENDER_SAFETY);
                } else {
                    context.getPlayer().displayClientMessage(
                            Component.translatable("linkingtool.ender.warning").withStyle(ChatFormatting.RED), true);
                    stack.set(FSAttachments.ENDER_SAFETY, true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        if (blockEntity instanceof StorageControllerTile) {
            stack.set(FSAttachments.CONTROLLER, pos);
            context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 0.5f, 1);
            context.getPlayer().displayClientMessage(
                    Component.translatable("linkingtool.controller.configured").withStyle(ChatFormatting.GREEN), true);
            stack.remove(FSAttachments.ENDER_FREQUENCY);
            return InteractionResult.SUCCESS;
        } else if (blockEntity instanceof ControllableDrawerTile && stack.has(FSAttachments.CONTROLLER)) {
            if (!level.isClientSide) {
                BlockPos controllerPos = stack.get(FSAttachments.CONTROLLER);
                BlockEntity controller = level.getBlockEntity(controllerPos);
                if (controller instanceof StorageControllerTile) {
                    if (linkingMode == LinkingMode.SINGLE) {
                        if (((StorageControllerTile) controller).addConnectedDrawers(linkingAction, pos)) {
                            if (linkingAction == ActionMode.ADD) {
                                context.getPlayer()
                                        .displayClientMessage(Component.translatable("linkingtool.single_drawer.linked")
                                                .setStyle(Style.EMPTY.withColor(linkingMode.color)), true);
                            } else {
                                context.getPlayer().displayClientMessage(
                                        Component.translatable("linkingtool.single_drawer.removed")
                                                .setStyle(Style.EMPTY.withColor(linkingMode.color)),
                                        true);
                            }
                        }
                    } else {
                        if (stack.has(FSAttachments.FIRST_POSITION)) {
                            BlockPos firstPos = stack.get(FSAttachments.FIRST_POSITION);
                            AABB aabb = new AABB(Math.min(firstPos.getX(), pos.getX()),
                                    Math.min(firstPos.getY(), pos.getY()), Math.min(firstPos.getZ(), pos.getZ()),
                                    Math.max(firstPos.getX(), pos.getX()) + 1,
                                    Math.max(firstPos.getY(), pos.getY()) + 1,
                                    Math.max(firstPos.getZ(), pos.getZ()) + 1);
                            if (((StorageControllerTile) controller).addConnectedDrawers(linkingAction,
                                    getBlockPosInAABB(aabb).toArray(BlockPos[]::new))) {
                                if (linkingAction == ActionMode.ADD) {
                                    context.getPlayer().displayClientMessage(
                                            Component.translatable("linkingtool.multiple_drawer.linked")
                                                    .setStyle(Style.EMPTY.withColor(linkingMode.color)),
                                            true);
                                } else {
                                    context.getPlayer().displayClientMessage(
                                            Component.translatable("linkingtool.multiple_drawer.removed")
                                                    .setStyle(Style.EMPTY.withColor(linkingMode.color)),
                                            true);
                                }
                            }
                            stack.remove(FSAttachments.FIRST_POSITION);
                        } else {
                            stack.set(FSAttachments.FIRST_POSITION, pos);
                        }
                    }
                }
            }
            context.getPlayer().playSound(SoundEvents.ITEM_FRAME_ROTATE_ITEM, 0.5f, 1);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            if (stack.has(FSAttachments.ENDER_FREQUENCY)) {
                if (player.isShiftKeyDown()) {
                    stack.remove(FSAttachments.ENDER_FREQUENCY);
                    player.displayClientMessage(Component.translatable("linkingtool.drawer.clear")
                            .setStyle(Style.EMPTY.withColor(ActionMode.ADD.getColor())), true);
                }
            } else {
                if (player.isShiftKeyDown()) {
                    LinkingMode linkingMode = getLinkingMode(stack);
                    if (linkingMode == LinkingMode.SINGLE) {
                        stack.set(FSAttachments.LINKING_MODE, LinkingMode.MULTIPLE.name());
                        player.displayClientMessage(Component
                                .translatable("linkingtool.linkingmode.swapped",
                                        Component.translatable("linkingtool.linkingmode."
                                                + LinkingMode.MULTIPLE.name().toLowerCase(Locale.ROOT)))
                                .setStyle(Style.EMPTY.withColor(LinkingMode.MULTIPLE.getColor())), true);
                    } else {
                        stack.set(FSAttachments.LINKING_MODE, LinkingMode.SINGLE.name());
                        player.displayClientMessage(Component
                                .translatable("linkingtool.linkingmode.swapped",
                                        Component.translatable("linkingtool.linkingmode."
                                                + LinkingMode.SINGLE.name().toLowerCase(Locale.ROOT)))
                                .setStyle(Style.EMPTY.withColor(LinkingMode.SINGLE.getColor())), true);
                    }
                    stack.remove(FSAttachments.FIRST_POSITION);
                } else {
                    ActionMode linkingMode = getActionMode(stack);
                    if (linkingMode == ActionMode.ADD) {
                        stack.set(FSAttachments.ACTION_MODE, ActionMode.REMOVE.name());
                        player.displayClientMessage(Component
                                .translatable("linkingtool.linkingaction.swapped",
                                        Component.translatable("linkingtool.linkingaction."
                                                + ActionMode.REMOVE.name().toLowerCase(Locale.ROOT)))
                                .setStyle(Style.EMPTY.withColor(ActionMode.REMOVE.getColor())), true);
                    } else {
                        stack.set(FSAttachments.ACTION_MODE, ActionMode.ADD.name());
                        player.displayClientMessage(Component
                                .translatable("linkingtool.linkingaction.swapped",
                                        Component.translatable("linkingtool.linkingaction."
                                                + ActionMode.ADD.name().toLowerCase(Locale.ROOT)))
                                .setStyle(Style.EMPTY.withColor(ActionMode.ADD.getColor())), true);
                    }
                }
            }
            player.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.5f, 1);
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag isAdvanced) {
        LinkingMode linkingMode = getLinkingMode(stack);
        ActionMode linkingAction = getActionMode(stack);

        if (stack.has(FSAttachments.ENDER_FREQUENCY)) {
            MutableComponent text = Component.translatable("linkingtool.ender.frequency");
            tooltip.add(text.withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("linkingtool.ender.clear").withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("linkingtool.linkingmode").withStyle(ChatFormatting.YELLOW)
                    .append(Component
                            .translatable("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT))
                            .withStyle(Style.EMPTY.withColor(linkingMode.getColor()))));
            tooltip.add(Component.translatable("linkingtool.linkingaction").withStyle(ChatFormatting.YELLOW)
                    .append(Component
                            .translatable("linkingtool.linkingaction." + linkingAction.name().toLowerCase(Locale.ROOT))
                            .withStyle(Style.EMPTY.withColor(linkingAction.getColor()))));
            if (stack.has(FSAttachments.CONTROLLER)) {
                BlockPos controllerPos = stack.get(FSAttachments.CONTROLLER);
                tooltip.add(Component.translatable("linkingtool.controller").withStyle(ChatFormatting.YELLOW)
                        .append(Component
                                .literal(controllerPos.getX() + ""
                                        + ChatFormatting.WHITE + ", " + ChatFormatting.DARK_AQUA
                                        + controllerPos.getY()
                                        + ChatFormatting.WHITE + ", " + ChatFormatting.DARK_AQUA
                                        + controllerPos.getZ())
                                .withStyle(ChatFormatting.DARK_AQUA)));
            } else {
                tooltip.add(Component.translatable("linkingtool.controller").withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal("???").withStyle(ChatFormatting.DARK_AQUA)));
            }
            tooltip.add(Component.literal(""));
            tooltip.add(Component
                    .translatable("linkingtool.linkingmode." + linkingMode.name().toLowerCase(Locale.ROOT) + ".desc")
                    .withStyle(ChatFormatting.GRAY));
            // Parse newlines in linkingtool.use and add each line as a separate Component
            // Empty lines (from \n\n) are preserved as blank lines in the tooltip
            String useText = Component.translatable("linkingtool.use").getString();
            for (String line : useText.split("\n")) {
                if (line.trim().isEmpty()) {
                    tooltip.add(Component.literal("")); // Add empty line
                } else {
                    tooltip.add(Component.literal(line.trim()).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    public static List<BlockPos> getBlockPosInAABB(AABB axisAlignedBB) {
        List<BlockPos> blocks = new ArrayList<>();
        for (double y = axisAlignedBB.minY; y < axisAlignedBB.maxY; ++y) {
            for (double x = axisAlignedBB.minX; x < axisAlignedBB.maxX; ++x) {
                for (double z = axisAlignedBB.minZ; z < axisAlignedBB.maxZ; ++z) {
                    blocks.add(new BlockPos((int) x, (int) y, (int) z));
                }
            }
        }
        return blocks;
    }
}
