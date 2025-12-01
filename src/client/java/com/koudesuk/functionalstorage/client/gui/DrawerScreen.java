package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
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
        // Top part (0-83)
        guiGraphics.blit(TEXTURE, i, j, 0, 0, 176, 83);
        // Middle part filler (using a slice of the texture or just grey)
        // We need to fill from 83 to 100 (17 pixels).
        // We can repeat a slice from the middle of dispenser.png
        guiGraphics.blit(TEXTURE, i, j + 83, 0, 83, 176, 17); // Just stretch or copy?
        // Actually dispenser.png is 166 high. 83 is middle.
        // Let's just draw the bottom part at y=100 (where player inv starts)
        // Player inv in dispenser starts at y=84.
        // So we draw the bottom part (84 to 166) at j + 100.
        guiGraphics.blit(TEXTURE, i, j + 100, 0, 83, 176, 83); // 166-83 = 83.

        // Fill the gap (83 to 100) with a grey rectangle
        guiGraphics.fill(i, j + 83, i + 176, j + 100, 0xFFC6C6C6);

        // Cover the dispenser slots (3x3 grid) with grey background
        // Dispenser slots start at 61, 16. 3*18 = 54 width/height.
        guiGraphics.fill(i + 61, j + 16, i + 61 + 54, j + 16 + 54, 0xFFC6C6C6);

        // Draw Drawer Face Background
        if (this.menu.getTile() instanceof DrawerTile tile) {
            ResourceLocation drawerFace = new ResourceLocation(FunctionalStorage.MOD_ID, "textures/block/"
                    + tile.getWoodType().getName() + "_front_" + tile.getDrawerType().getSlots() + ".png");
            guiGraphics.blit(drawerFace, i + 64, j + 16, 0, 0, 48, 48, 48, 48);
        }

        // Draw Slot Backgrounds for Upgrades (since we moved them)
        // Storage: 10, 70 - use dynamic count from menu
        int storageSlotCount = this.menu.getStorageSlotCount();
        for (int k = 0; k < storageSlotCount; k++) {
            drawSlotBackground(guiGraphics, i + 10 + k * 18, j + 70);
        }

        // Utility: 114, 70 (only for regular drawers, not Storage Controller)
        // Use dynamic count from menu
        if (!(this.menu.getTile() instanceof StorageControllerTile)) {
            int utilitySlotCount = this.menu.getUtilitySlotCount();
            for (int k = 0; k < utilitySlotCount; k++) {
                drawSlotBackground(guiGraphics, i + 114 + k * 18, j + 70);
            }
        }
    }

    private void drawSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        // Draw a standard slot box. We can use a part of dispenser.png
        // Slot is usually 18x18.
        // In dispenser.png, a slot is at 7, 83? No.
        // Let's just draw a dark box.
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B); // Border
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF373737); // Inner
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B); // Highlight?
        // Actually, let's try to blit a slot from the texture.
        // Dispenser slots are at (61, 16) etc.
        // We can grab a slot sprite.
        // Slot sprite is usually at 7, 83 in container textures?
        // Let's guess: 7, 83 is a slot in dispenser.png?
        // Dispenser has 3x3 grid.
        // Top left slot at 62, 17.
        guiGraphics.blit(TEXTURE, x - 1, y - 1, 61, 16, 18, 18);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Don't call super to avoid default labels

        // Check if this is a Storage Controller
        if (this.menu.getTile() instanceof StorageControllerTile) {
            // Storage Controller: Show "Range" label only
            guiGraphics.drawString(this.font, Component.translatable("gui.functionalstorage.storage_range"), 10, 59,
                    ChatFormatting.DARK_GRAY.getColor(), false);
        } else {
            // Regular Drawer: Show "Storage" and "Utility" labels
            guiGraphics.drawString(this.font, Component.translatable("gui.functionalstorage.storage"), 10, 59,
                    ChatFormatting.DARK_GRAY.getColor(), false);

            guiGraphics.drawString(this.font, Component.translatable("gui.functionalstorage.utility"), 114, 59,
                    ChatFormatting.DARK_GRAY.getColor(), false);
        }

        // Inventory Label (always show)
        guiGraphics.drawString(this.font, Component.translatable("key.categories.inventory"), 8, 92,
                ChatFormatting.DARK_GRAY.getColor(), false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render Drawer Contents
        if (this.menu.getTile() instanceof DrawerTile tile) {
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
                    float scale = 0.5f;
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(itemX + 8, itemY + 12, 200); // Center text?
                    guiGraphics.pose().scale(scale, scale, scale);

                    int textWidth = this.font.width(amount);
                    guiGraphics.drawString(this.font, amount, -textWidth / 2, 0, 0xFFFFFF, true);

                    guiGraphics.pose().popPose();
                }
            }
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
