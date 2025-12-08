package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.util.CompactingUtil;
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
import net.minecraft.world.item.ItemStack;
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
        } else if (this.menu.getTile() instanceof CompactingDrawerTile) {
            // Draw Compacting Drawer Face Background (3-slot)
            boolean isFramed = this.menu
                    .getTile() instanceof com.koudesuk.functionalstorage.block.tile.FramedCompactingDrawerTile;
            ResourceLocation drawerFace = new ResourceLocation(FunctionalStorage.MOD_ID,
                    isFramed ? "textures/block/framed_front_compacting.png"
                            : "textures/block/compacting_drawer_front.png");
            guiGraphics.blit(drawerFace, i + 64, j + 16, 0, 0, 48, 48, 48, 48);
        } else if (this.menu.getTile() instanceof SimpleCompactingDrawerTile) {
            // Draw Simple Compacting Drawer Face Background (2-slot)
            boolean isFramed = this.menu
                    .getTile() instanceof com.koudesuk.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
            ResourceLocation drawerFace = new ResourceLocation(FunctionalStorage.MOD_ID,
                    isFramed ? "textures/block/framed_front_compacting.png"
                            : "textures/block/simple_compacting_drawer_front.png");
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
        // Render Compacting Drawer Contents (3-slot)
        else if (this.menu.getTile() instanceof CompactingDrawerTile compactingTile) {
            renderCompactingDrawerContents(guiGraphics, compactingTile);
        }
        // Render Simple Compacting Drawer Contents (2-slot)
        else if (this.menu.getTile() instanceof SimpleCompactingDrawerTile simpleCompactingTile) {
            renderSimpleCompactingDrawerContents(guiGraphics, simpleCompactingTile);
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

    /**
     * Render Compacting Drawer contents (3-slot layout).
     * Slot positions follow Forge's DrawerInfoGuiAddon:
     * - Slot 0 (lowest tier, e.g., nuggets): (28, 28) - bottom-right
     * - Slot 1 (middle tier, e.g., ingots): (4, 28) - bottom-left
     * - Slot 2 (highest tier, e.g., blocks): (16, 4) - top-center
     */
    private void renderCompactingDrawerContents(GuiGraphics guiGraphics, CompactingDrawerTile tile) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int x = i + 64;
        int y = j + 16;

        CompactingInventoryHandler handler = tile.handler;

        // Slot position mapping from Forge (based on 48x48 texture at 3x scale = 16x16
        // item icons)
        Pair<Integer, Integer>[] slotPositions = new Pair[] {
                Pair.of(28, 28), // Slot 0: bottom-right (nuggets)
                Pair.of(4, 28), // Slot 1: bottom-left (ingots)
                Pair.of(16, 4) // Slot 2: top-center (blocks)
        };

        for (int slot = 0; slot < 3; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            // Check if item type exists using resultList, not just stack count
            java.util.List<CompactingUtil.Result> resultList = handler.getResultList();
            if (slot < resultList.size() && !resultList.get(slot).getResult().isEmpty()) {
                Pair<Integer, Integer> pos = slotPositions[slot];
                int itemX = x + pos.getLeft();
                int itemY = y + pos.getRight();

                // Render item (use result item type, actual count from stack)
                ItemStack displayStack = resultList.get(slot).getResult().copy();
                displayStack.setCount(1); // Always show icon with count 1
                guiGraphics.renderItem(displayStack, itemX, itemY);

                // Render Amount text
                int amount = stack.getCount();
                int maxAmount = handler.getSlotLimit(slot);
                String amountStr = NumberUtils.getFormatedBigNumber(amount) + "/"
                        + NumberUtils.getFormatedBigNumber(maxAmount);
                renderAmountText(guiGraphics, itemX + 8, itemY + 12, amountStr);
            }
        }
    }

    /**
     * Render Simple Compacting Drawer contents (2-slot layout).
     * Slot positions follow X_2 pattern:
     * - Slot 0 (lower tier): (16, 28) - bottom
     * - Slot 1 (higher tier): (16, 4) - top
     */
    private void renderSimpleCompactingDrawerContents(GuiGraphics guiGraphics, SimpleCompactingDrawerTile tile) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int x = i + 64;
        int y = j + 16;

        CompactingInventoryHandler handler = tile.handler;

        // Slot position mapping (similar to X_2 drawer layout)
        Pair<Integer, Integer>[] slotPositions = new Pair[] {
                Pair.of(16, 28), // Slot 0: bottom (lower tier)
                Pair.of(16, 4) // Slot 1: top (higher tier)
        };

        for (int slot = 0; slot < 2; slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            // Check if item type exists using resultList, not just stack count
            java.util.List<CompactingUtil.Result> resultList = handler.getResultList();
            if (slot < resultList.size() && !resultList.get(slot).getResult().isEmpty()) {
                Pair<Integer, Integer> pos = slotPositions[slot];
                int itemX = x + pos.getLeft();
                int itemY = y + pos.getRight();

                // Render item (use result item type, actual count from stack)
                ItemStack displayStack = resultList.get(slot).getResult().copy();
                displayStack.setCount(1); // Always show icon with count 1
                guiGraphics.renderItem(displayStack, itemX, itemY);

                // Render Amount text
                int amount = stack.getCount();
                int maxAmount = handler.getSlotLimit(slot);
                String amountStr = NumberUtils.getFormatedBigNumber(amount) + "/"
                        + NumberUtils.getFormatedBigNumber(maxAmount);
                renderAmountText(guiGraphics, itemX + 8, itemY + 12, amountStr);
            }
        }
    }
}
