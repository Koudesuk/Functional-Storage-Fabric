package com.koudesuk.functionalstorage.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * A block that takes the textures of other blocks when crafted with them (FramedDrawerRecipe),
 * like the original's FramedBlock.
 */
public interface FramedBlock {

    /**
     * How to change the texture, one tooltip line per line of the translation. Item tooltips come
     * from Item.appendHoverText since 26.1 dropped Block.appendHoverText.
     */
    static void appendTooltip(Consumer<Component> tooltip) {
        String useText = Component.translatable("frameddrawer.use").getString();
        for (String line : useText.split("\n")) {
            if (!line.trim().isEmpty()) {
                tooltip.accept(Component.literal(line.trim()).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
