package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
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

    public static void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
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

        List<ClientTooltipComponent> lines = List.of(
                Component.translatable("gui.functionalstorage.fluid_drawer_hint").withStyle(ChatFormatting.GOLD),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.empty",
                        minecraft.options.keyUse.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY),
                Component.translatable("gui.functionalstorage.fluid_drawer_hint.fill",
                        minecraft.options.keyAttack.getTranslatedKeyMessage()).withStyle(ChatFormatting.GRAY))
                .stream()
                .map(line -> ClientTooltipComponent.create(line.getVisualOrderText()))
                .toList();
        int x = graphics.guiWidth() / 2 + 10;
        int y = graphics.guiHeight() / 2 - 4;
        graphics.tooltip(minecraft.font, lines, x, y,
                (screenWidth, screenHeight, mouseX, mouseY, width, height) -> new Vector2i(x, y), null);
    }

    private static boolean isFluidContainer(ItemStack stack) {
        return !stack.isEmpty() && ContainerItemContext.withConstant(stack).find(FluidStorage.ITEM) != null;
    }
}
