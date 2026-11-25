package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector2i;

import java.util.List;

/**
 * Next to the crosshair, says which click empties or fills a held fluid container on a fluid drawer, like the
 * original's renderFluidDrawerHint.
 */
public final class FluidDrawerHintHud {

    private FluidDrawerHintHud() {
    }

    public static void render(GuiGraphics graphics, DeltaTracker tickCounter) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.screen != null || minecraft.player == null || minecraft.level == null)
            return;
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK)
            return;
        if (!(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof FluidDrawerTile))
            return;
        if (!isFluidContainer(minecraft.player.getItemInHand(InteractionHand.MAIN_HAND))
                && !isFluidContainer(minecraft.player.getItemInHand(InteractionHand.OFF_HAND)))
            return;

        List<FormattedCharSequence> lines = List.of(
                Component.translatable("gui.functionalstorage.fluid_drawer_hint").withStyle(ChatFormatting.GOLD),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.empty",
                        minecraft.options.keyUse.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.fill",
                        minecraft.options.keyAttack.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY))
                .stream()
                .map(Component::getVisualOrderText)
                .toList();
        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() / 2 - 4;
        graphics.renderTooltip(minecraft.font, lines,
                (screenWidth, screenHeight, mouseX, mouseY, width, height) -> new Vector2i(x, y), x, y);
    }

    private static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM) != null;
    }
}
