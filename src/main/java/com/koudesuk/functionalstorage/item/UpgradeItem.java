package com.koudesuk.functionalstorage.item;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class UpgradeItem extends Item {

    public static int MAX_SLOT = 4;

    public static enum Type {
        STORAGE,
        UTILITY
    }

    private final Type type;

    public UpgradeItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public static Direction getDirection(ItemStack stack) {
        if (stack.has(FSAttachments.DIRECTION)) {
            return stack.get(FSAttachments.DIRECTION);
        }
        return Direction.NORTH;
    }

    public static boolean isDirectionUpgrade(Item item) {
        return item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PULLING_UPGRADE
                || item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PUSHING_UPGRADE
                || item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.COLLECTOR_UPGRADE;
    }

    /**
     * Where a side lies for someone looking at the drawer's front, like Titanium's FacingUtil.getFacingRelative
     * the original uses.
     */
    public static MutableComponent getRelativeDirection(Direction facing, Direction side) {
        String name = side == Direction.UP ? "up"
                : side == Direction.DOWN ? "down"
                : side == facing ? "front"
                : side == facing.getOpposite() ? "back"
                : side == facing.getClockWise() ? "left" : "right";
        return Component.translatable("drawer_upgrade.functionalstorage.sides." + name).withStyle(ChatFormatting.WHITE);
    }

    /**
     * What the upgrade does in this drawer, for Jade, like the original's getDescription.
     */
    @Nullable
    public Component getDescription(ItemStack stack, ControllableDrawerTile<?> tile) {
        String type = tile instanceof FluidDrawerTile ? "fluid" : "item";
        if (isDirectionUpgrade(this)) {
            String action = this == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PUSHING_UPGRADE ? "push"
                    : this == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PULLING_UPGRADE ? "pull"
                    : "collect";
            return Component.translatable("drawer_upgrade.functionalstorage." + action + "." + type,
                    getRelativeDirection(tile.getFacingDirection(), getDirection(stack)).withStyle(ChatFormatting.GOLD));
        }
        if (this == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.VOID_UPGRADE) {
            return Component.translatable("drawer_upgrade.functionalstorage.void." + type);
        }
        if (this == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
            return Component.translatable("drawer_upgrade.functionalstorage.redstone",
                    Component.literal(String.valueOf(stack.getOrDefault(FSAttachments.SLOT, 0)))
                            .withStyle(ChatFormatting.RED));
        }
        return null;
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        super.onCraftedBy(stack, level, player);
        initNbt(stack);
    }

    private ItemStack initNbt(ItemStack stack) {
        Item item = stack.getItem();
        if (item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PULLING_UPGRADE ||
                item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PUSHING_UPGRADE ||
                item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.COLLECTOR_UPGRADE) {
            stack.set(FSAttachments.DIRECTION, Direction.values()[0]);
        }
        if (item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
            stack.set(FSAttachments.SLOT, 0);
        }
        return stack;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack first, ItemStack second, Slot slot, ClickAction clickAction,
            Player player, SlotAccess slotAccess) {
        if (clickAction == ClickAction.SECONDARY && first.getCount() == 1) {
            Item item = first.getItem();
            // Handle direction cycling for puller/pusher/collector
            if (item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PULLING_UPGRADE ||
                    item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PUSHING_UPGRADE ||
                    item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.COLLECTOR_UPGRADE) {
                Direction direction = getDirection(first);
                Direction next = Direction.values()[(Arrays.asList(Direction.values()).indexOf(direction) + 1)
                        % Direction.values().length];
                first.set(FSAttachments.DIRECTION, next);
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1);
                return true;
            }
            // Handle slot cycling for redstone upgrade
            if (item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                int currentSlot = first.has(FSAttachments.SLOT) ? first.get(FSAttachments.SLOT) : 0;
                first.set(FSAttachments.SLOT, (currentSlot + 1) % MAX_SLOT);
                player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5f, 1);
                return true;
            }
        }
        return super.overrideOtherStackedOnMe(first, second, slot, clickAction, player, slotAccess);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
            TooltipFlag isAdvanced) {
        tooltip.add(Component.translatable("upgrade.type").withStyle(ChatFormatting.YELLOW)
                .append(Component.translatable("upgrade.type." + getType().name().toLowerCase(Locale.ROOT))
                        .withStyle(ChatFormatting.WHITE)));

        Item item = stack.getItem();
        // Direction tooltip for puller/pusher/collector
        if (item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PULLING_UPGRADE ||
                item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.PUSHING_UPGRADE ||
                item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.COLLECTOR_UPGRADE) {
            // Capitalize direction name directly (no Titanium dependency)
            String directionName = getDirection(stack).getName();
            String capitalizedName = directionName.substring(0, 1).toUpperCase() + directionName.substring(1);
            tooltip.add(Component.translatable("item.utility.direction").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(capitalizedName).withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.utility.direction.desc").withStyle(ChatFormatting.GRAY));
        }
        // Slot tooltip for redstone upgrade
        if (item == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
            tooltip.add(Component.translatable("item.utility.slot").withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal((stack.has(FSAttachments.SLOT) ? stack.get(FSAttachments.SLOT) : 0) + "")
                            .withStyle(ChatFormatting.WHITE)));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("item.utility.slot.desc").withStyle(ChatFormatting.GRAY));
        }
    }
}
