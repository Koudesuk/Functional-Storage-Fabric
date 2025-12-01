package com.koudesuk.functionalstorage.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DrawerBlockItem extends BlockItem {

    public DrawerBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (stack.hasTag() && stack.getTag().contains("Tile")) {
            CompoundTag tileTag = stack.getTag().getCompound("Tile");
            if (tileTag.contains("Handler")) {
                CompoundTag handlerTag = tileTag.getCompound("Handler");
                if (handlerTag.contains("BigItems")) {
                    CompoundTag bigItems = handlerTag.getCompound("BigItems");
                    for (String key : bigItems.getAllKeys()) {
                        CompoundTag itemTag = bigItems.getCompound(key);
                        int amount = itemTag.getInt("Amount");
                        ItemStack itemStack = ItemStack.of(itemTag.getCompound("Stack"));
                        if (!itemStack.isEmpty()) {
                            MutableComponent text = Component.literal("Slot " + key + ": ")
                                    .withStyle(ChatFormatting.GRAY);
                            text.append(itemStack.getDisplayName().copy().withStyle(ChatFormatting.WHITE));
                            text.append(Component.literal(" x" + amount).withStyle(ChatFormatting.GOLD));
                            tooltip.add(text);
                        }
                    }
                }
            }
        }
    }
}
