package com.koudesuk.functionalstorage.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * A block that takes the textures of other blocks when crafted with them (FramedDrawerRecipe),
 * like the original's FramedBlock.
 */
public interface FramedBlock {

    /**
     * How to change the texture, one tooltip line per line of the translation.
     */
    static void appendTooltip(List<Component> tooltip) {
        String useText = Component.translatable("frameddrawer.use").getString();
        for (String line : useText.split("\n")) {
            if (!line.trim().isEmpty()) {
                tooltip.add(Component.literal(line.trim()).withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
