package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.apache.commons.lang3.tuple.Pair;

public class DrawerScreen extends AbstractContainerScreen<DrawerMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/dispenser.png");

    public DrawerScreen(DrawerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 184;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        // Draw background using dispenser.png but extended
        guiGraphics.blit(TEXTURE, i, j, 0, 0, 176, 83);
        guiGraphics.blit(TEXTURE, i, j + 83, 0, 83, 176, 17);
        guiGraphics.blit(TEXTURE, i, j + 100, 0, 83, 176, 83);

        // Fill the gap (83 to 100) with a grey rectangle
        guiGraphics.fill(i, j + 83, i + 176, j + 100, 0xFFC6C6C6);

        // Cover the dispenser slots (3x3 grid) with grey background
        guiGraphics.fill(i + 61, j + 16, i + 61 + 54, j + 16 + 54, 0xFFC6C6C6);

        // Draw Drawer Face Background
        if (this.menu.getTile() instanceof DrawerTile tile) {
            ResourceLocation drawerFace = new ResourceLocation(FunctionalStorage.MOD_ID, "textures/block/"
                    + tile.getWoodType().getName() + "_front_" + tile.getDrawerType().getSlots() + ".png");
            guiGraphics.blit(drawerFace, i + 64, j + 16, 0, 0, 48, 48, 48, 48);
        } else if (this.menu.getTile() instanceof FluidDrawerTile fluidTile) {
            // Draw Fluid Drawer Face Background
            String slotSuffix = fluidTile.getDrawerType().getSlots() == 1 ? ""
                    : "_" + fluidTile.getDrawerType().getSlots();
            ResourceLocation drawerFace = new ResourceLocation(FunctionalStorage.MOD_ID,
                    "textures/block/fluid_front" + slotSuffix + ".png");
            guiGraphics.blit(drawerFace, i + 64, j + 16, 0, 0, 48, 48, 48, 48);
        }

        // Draw Slot Backgrounds for Upgrades
        int storageSlotCount = this.menu.getStorageSlotCount();
        for (int k = 0; k < storageSlotCount; k++) {
            drawSlotBackground(guiGraphics, i + 10 + k * 18, j + 70);
        }

        // Utility slots (only for regular drawers, not Storage Controller)
        if (!(this.menu.getTile() instanceof StorageControllerTile)) {
            int utilitySlotCount = this.menu.getUtilitySlotCount();
            for (int k = 0; k < utilitySlotCount; k++) {
                drawSlotBackground(guiGraphics, i + 114 + k * 18, j + 70);
            }
        }
    }

    private void drawSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF373737);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        guiGraphics.blit(TEXTURE, x - 1, y - 1, 61, 16, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.menu.getTile() instanceof StorageControllerTile) {
            guiGraphics.drawString(this.font, Component.translatable("gui.functionalstorage.storage_range"), 10, 59,
                    ChatFormatting.DARK_GRAY.getColor(), false);
        } else {
            guiGraphics.drawString(this.font, Component.translatable("gui.functionalstorage.storage"), 10, 59,
                    ChatFormatting.DARK_GRAY.getColor(), false);

            guiGraphics.drawString(this.font, Component.translatable("gui.functionalstorage.utility"), 114, 59,
                    ChatFormatting.DARK_GRAY.getColor(), false);
        }

        guiGraphics.drawString(this.font, Component.translatable("key.categories.inventory"), 8, 92,
                ChatFormatting.DARK_GRAY.getColor(), false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render Item Drawer Contents
        if (this.menu.getTile() instanceof DrawerTile tile) {
            renderItemDrawerContents(guiGraphics, tile);
        }
        // Render Fluid Drawer Contents
        else if (this.menu.getTile() instanceof FluidDrawerTile fluidTile) {
            renderFluidDrawerContents(guiGraphics, fluidTile);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private void renderItemDrawerContents(GuiGraphics guiGraphics, DrawerTile tile) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int x = i + 64;
        int y = j + 16;

        DrawerType type = tile.getDrawerType();
        BigInventoryHandler handler = tile.getHandler();

        for (int k = 0; k < type.getSlots(); k++) {
            BigInventoryHandler.BigStack stack = handler.getStoredStacks().get(k);
            if (!stack.getStack().isEmpty()) {
                Pair<Integer, Integer> pos = type.getSlotPosition().apply(k);
                int itemX = x + pos.getLeft();
                int itemY = y + pos.getRight();

                guiGraphics.renderItem(stack.getStack(), itemX, itemY);
                guiGraphics.renderItemDecorations(this.font, stack.getStack(), itemX, itemY, null);

                // Render Amount
                String amount = NumberUtils.getFormatedBigNumber(stack.getAmount()) + "/"
                        + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(k));
                renderAmountText(guiGraphics, itemX + 8, itemY + 12, amount);
            }
        }
    }

    private void renderFluidDrawerContents(GuiGraphics guiGraphics, FluidDrawerTile tile) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int x = i + 64;
        int y = j + 16;

        DrawerType type = tile.getDrawerType();
        FluidInventoryHandler handler = tile.getHandler();

        for (int k = 0; k < type.getSlots(); k++) {
            FluidVariant resource = handler.getResource(k);
            long amount = handler.getAmount(k);
            long capacity = handler.getSlotLimit(k);

            if (!resource.isBlank()) {
                Pair<Integer, Integer> pos = type.getSlotPosition().apply(k);
                int itemX = x + pos.getLeft();
                int itemY = y + pos.getRight();

                // Render fluid sprite
                TextureAtlasSprite sprite = FluidVariantRendering.getSprite(resource);
                if (sprite != null) {
                    int color = FluidVariantRendering.getColor(resource);
                    float red = ((color >> 16) & 0xFF) / 255f;
                    float green = ((color >> 8) & 0xFF) / 255f;
                    float blue = (color & 0xFF) / 255f;

                    RenderSystem.setShaderColor(red, green, blue, 1.0f);
                    RenderSystem.enableBlend();
                    guiGraphics.blit(itemX, itemY, 0, 16, 16, sprite);
                    RenderSystem.disableBlend();
                    RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
                }

                // Render Amount
                String amountStr = NumberUtils.getFormatedFluidBigNumber(amount) + "/"
                        + NumberUtils.getFormatedFluidBigNumber(capacity);
                renderAmountText(guiGraphics, itemX + 8, itemY + 12, amountStr);
            }
        }
    }

    private void renderAmountText(GuiGraphics guiGraphics, int x, int y, String amount) {
        float scale = 0.5f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 200);
        guiGraphics.pose().scale(scale, scale, scale);

        int textWidth = this.font.width(amount);
        guiGraphics.drawString(this.font, amount, -textWidth / 2, 0, 0xFFFFFF, true);

        guiGraphics.pose().popPose();
    }
}
