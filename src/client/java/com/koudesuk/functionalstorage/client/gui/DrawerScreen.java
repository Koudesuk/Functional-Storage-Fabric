package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.network.DrawerPriorityPayload;
import com.koudesuk.functionalstorage.util.CompactingUtil;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.NumberUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DrawerScreen extends AbstractContainerScreen<DrawerMenu> {

    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/dispenser.png");

    // 26.1 draws nothing for a color without alpha, so every color here is ARGB
    private static final int TITLE_COLOR = 0xFF404040;
    private static final int LABEL_COLOR = ARGB.opaque(ChatFormatting.DARK_GRAY.getColor());
    private static final int AMOUNT_COLOR = 0xFFFFFFFF;
    private static final int GUI_GREY = 0xFFC6C6C6;
    private static final int HIGHLIGHT = 0x80FFFFFF;

    // The original's DrawerPriorityGuiAddon; controllers are never sorted, so they get none
    private EditBox priorityBox;

    public DrawerScreen(DrawerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 184);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        if (this.menu.getTile() instanceof StorageControllerTile)
            return;
        priorityBox = new EditBox(this.font, leftPos + 115, topPos + 30, 50, 16,
                Component.translatable("gui.functionalstorage.priority"));
        priorityBox.setBordered(true);
        priorityBox.setMaxLength(9);
        priorityBox.setValue(String.valueOf(this.menu.getTile().getPriority()));
        priorityBox.setResponder(this::onPriorityChanged);
        addRenderableWidget(priorityBox);
    }

    // 26.1's EditBox has no input filter any more, so anything but digits is taken out here
    private void onPriorityChanged(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        if (!digits.equals(value)) {
            priorityBox.setValue(digits);
            return;
        }
        ClientPlayNetworking.send(new DrawerPriorityPayload(this.menu.getTile().getBlockPos(),
                digits.isEmpty() ? 0 : Integer.parseInt(digits)));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Typing in the priority box must not close the screen with the inventory key, like the anvil
        if (priorityBox != null && !event.isEscape()
                && (priorityBox.keyPressed(event) || priorityBox.canConsumeInput()))
            return true;
        return super.keyPressed(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        // Draw background using dispenser.png but extended
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j, 0, 0, 176, 83, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + 83, 0, 83, 176, 17, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, i, j + 100, 0, 83, 176, 83, 256, 256);

        // Fill the gap (83 to 100) with a grey rectangle
        guiGraphics.fill(i, j + 83, i + 176, j + 100, GUI_GREY);

        // Render title at top of GUI (centered) - matching Titanium library's approach
        int titleX = i + (this.imageWidth - this.font.width(this.title)) / 2;
        int titleY = j + 6;
        guiGraphics.text(this.font, this.title, titleX, titleY, TITLE_COLOR, false);

        // Cover the dispenser slots (3x3 grid) with grey background
        guiGraphics.fill(i + 61, j + 16, i + 61 + 54, j + 16 + 54, GUI_GREY);

        // Draw Drawer Face Background
        if (this.menu.getTile() instanceof DrawerTile tile) {
            drawFace(guiGraphics, i, j, "textures/block/"
                    + tile.getWoodType().getName() + "_front_" + tile.getDrawerType().getSlots() + ".png");
        } else if (this.menu.getTile() instanceof FluidDrawerTile fluidTile) {
            // Draw Fluid Drawer Face Background
            String slotSuffix = fluidTile.getDrawerType().getSlots() == 1 ? ""
                    : "_" + fluidTile.getDrawerType().getSlots();
            drawFace(guiGraphics, i, j, "textures/block/fluid_front" + slotSuffix + ".png");
        } else if (this.menu.getTile() instanceof CompactingDrawerTile) {
            // Draw Compacting Drawer Face Background (3-slot)
            boolean isFramed = this.menu
                    .getTile() instanceof com.koudesuk.functionalstorage.block.tile.FramedCompactingDrawerTile;
            drawFace(guiGraphics, i, j, isFramed ? "textures/block/framed_front_compacting.png"
                    : "textures/block/compacting_drawer_front.png");
        } else if (this.menu.getTile() instanceof SimpleCompactingDrawerTile) {
            // Draw Simple Compacting Drawer Face Background (2-slot). The framed one gets the framed 2-slot
            // face, the front its block model uses in the original (framed_front_compacting has 3 slots)
            boolean isFramed = this.menu
                    .getTile() instanceof com.koudesuk.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile;
            drawFace(guiGraphics, i, j, isFramed ? "textures/block/framed_front_2.png"
                    : "textures/block/simple_compacting_drawer_front.png");
        } else if (this.menu.getTile() instanceof EnderDrawerTile) {
            // Draw Ender Drawer Face Background
            drawFace(guiGraphics, i, j, "textures/block/ender_front.png");
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

    private void drawFace(GuiGraphicsExtractor guiGraphics, int i, int j, String path) {
        Identifier drawerFace = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, path);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, drawerFace, i + 64, j + 16, 0, 0, 48, 48, 48, 48);
    }

    private void drawSlotBackground(GuiGraphicsExtractor guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, 0xFF8B8B8B);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF373737);
        guiGraphics.fill(x + 1, y + 1, x + 17, y + 17, 0xFF8B8B8B);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x - 1, y - 1, 61, 16, 18, 18, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (this.menu.getTile() instanceof StorageControllerTile) {
            guiGraphics.text(this.font, Component.translatable("gui.functionalstorage.storage_range"), 10, 59,
                    LABEL_COLOR, false);
        } else if (this.menu.getTile() instanceof EnderDrawerTile) {
            // Ender Drawer has no storage upgrade slots, only utility
            guiGraphics.text(this.font, Component.translatable("gui.functionalstorage.utility"), 114, 59,
                    LABEL_COLOR, false);
        } else {
            guiGraphics.text(this.font, Component.translatable("gui.functionalstorage.storage"), 10, 59,
                    LABEL_COLOR, false);

            guiGraphics.text(this.font, Component.translatable("gui.functionalstorage.utility"), 114, 59,
                    LABEL_COLOR, false);
        }

        if (priorityBox != null)
            guiGraphics.text(this.font, Component.translatable("gui.functionalstorage.priority"), 114, 20,
                    LABEL_COLOR, false);

        // 26.1 strips key.categories.inventory on load (lang/deprecated.json), so use the vanilla inventory title
        guiGraphics.text(this.font, this.playerInventoryTitle, 8, 92, LABEL_COLOR, false);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);

        // Render Item Drawer Contents
        if (this.menu.getTile() instanceof DrawerTile tile) {
            renderItemDrawerContents(guiGraphics, tile);
            renderItemDrawerTooltip(guiGraphics, tile, mouseX, mouseY);
        }
        // Render Fluid Drawer Contents
        else if (this.menu.getTile() instanceof FluidDrawerTile fluidTile) {
            renderFluidDrawerContents(guiGraphics, fluidTile);
            renderFluidDrawerTooltip(guiGraphics, fluidTile, mouseX, mouseY);
        }
        // Render Compacting Drawer Contents (3-slot)
        else if (this.menu.getTile() instanceof CompactingDrawerTile compactingTile) {
            renderCompactingDrawerContents(guiGraphics, compactingTile);
            renderCompactingDrawerTooltip(guiGraphics, compactingTile, mouseX, mouseY);
        }
        // Render Simple Compacting Drawer Contents (2-slot)
        else if (this.menu.getTile() instanceof SimpleCompactingDrawerTile simpleCompactingTile) {
            renderSimpleCompactingDrawerContents(guiGraphics, simpleCompactingTile);
            renderSimpleCompactingDrawerTooltip(guiGraphics, simpleCompactingTile, mouseX, mouseY);
        }
        // Render Ender Drawer Contents (1-slot, uses EnderInventoryHandler)
        else if (this.menu.getTile() instanceof EnderDrawerTile enderTile) {
            renderEnderDrawerContents(guiGraphics, enderTile);
            renderEnderDrawerTooltip(guiGraphics, enderTile, mouseX, mouseY);
        }
    }

    private void renderItemDrawerContents(GuiGraphicsExtractor guiGraphics, DrawerTile tile) {
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

                guiGraphics.item(stack.getStack(), itemX, itemY);
                guiGraphics.itemDecorations(this.font, stack.getStack(), itemX, itemY, null);

                // Render Amount
                String amount = NumberUtils.getFormatedBigNumber(stack.getAmount()) + "/"
                        + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(k));
                renderAmountText(guiGraphics, itemX + 8, itemY + 12, amount);
            }
        }
    }

    private void renderFluidDrawerContents(GuiGraphicsExtractor guiGraphics, FluidDrawerTile tile) {
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

            // Use filterStack when locked and resource is blank
            FluidVariant displayResource = resource;
            if (resource.isBlank() && tile.isLocked()) {
                displayResource = handler.getFilterStack(k);
            }

            if (!displayResource.isBlank()) {
                Pair<Integer, Integer> pos = type.getSlotPosition().apply(k);
                int itemX = x + pos.getLeft();
                int itemY = y + pos.getRight();

                // Render fluid sprite, the same still texture the fluid uses in the world
                TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                        .get(displayResource.getFluid().defaultFluidState()).stillMaterial().sprite();
                if (sprite != null) {
                    int rgb = FluidVariantRendering.getColor(displayResource) & 0xFFFFFF;
                    float alpha = resource.isBlank() ? 0.4f : 1.0f; // Dimmed when locked but empty
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, itemX, itemY, 16, 16,
                            ARGB.color(alpha, rgb));
                }

                // Render Amount
                String amountStr = NumberUtils.getFormatedFluidBigNumber(amount) + "/"
                        + NumberUtils.getFormatedFluidBigNumber(capacity);
                renderAmountText(guiGraphics, itemX + 8, itemY + 12, amountStr);
            }
        }
    }

    private void renderAmountText(GuiGraphicsExtractor guiGraphics, int x, int y, String amount) {
        float scale = 0.5f;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(scale, scale);

        int textWidth = this.font.width(amount);
        guiGraphics.text(this.font, amount, -textWidth / 2, 0, AMOUNT_COLOR, true);

        guiGraphics.pose().popMatrix();
    }

    /**
     * Render Compacting Drawer contents (3-slot layout).
     * Slot positions follow Forge's DrawerInfoGuiAddon:
     * - Slot 0 (lowest tier, e.g., nuggets): (28, 28) - bottom-right
     * - Slot 1 (middle tier, e.g., ingots): (4, 28) - bottom-left
     * - Slot 2 (highest tier, e.g., blocks): (16, 4) - top-center
     */
    private void renderCompactingDrawerContents(GuiGraphicsExtractor guiGraphics, CompactingDrawerTile tile) {
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
                guiGraphics.item(displayStack, itemX, itemY);

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
    private void renderSimpleCompactingDrawerContents(GuiGraphicsExtractor guiGraphics, SimpleCompactingDrawerTile tile) {
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
                guiGraphics.item(displayStack, itemX, itemY);

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
     * Render tooltip when hovering over item drawer slots.
     */
    private void renderItemDrawerTooltip(GuiGraphicsExtractor guiGraphics, DrawerTile tile, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int baseX = i + 64;
        int baseY = j + 16;

        DrawerType type = tile.getDrawerType();
        BigInventoryHandler handler = tile.getHandler();

        for (int slot = 0; slot < type.getSlots(); slot++) {
            Pair<Integer, Integer> pos = type.getSlotPosition().apply(slot);
            int slotX = baseX + pos.getLeft();
            int slotY = baseY + pos.getRight();

            // Check if mouse is hovering over this slot (18x18 area)
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                // Render highlight
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, HIGHLIGHT);

                // Build tooltip
                List<Component> componentList = new ArrayList<>();
                BigInventoryHandler.BigStack bigStack = handler.getStoredStacks().get(slot);

                if (bigStack.getStack().isEmpty()) {
                    componentList
                            .add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD)
                                    .append(Component.translatable("gui.functionalstorage.empty")
                                            .withStyle(ChatFormatting.WHITE)));
                } else {
                    componentList
                            .add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD)
                                    .append(bigStack.getStack().getHoverName().copy().withStyle(ChatFormatting.WHITE)));
                    String amount = NumberUtils.getFormatedBigNumber(bigStack.getAmount()) + "/"
                            + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(slot));
                    componentList
                            .add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(amount).withStyle(ChatFormatting.WHITE)));
                }
                componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(slot)).withStyle(ChatFormatting.WHITE)));

                guiGraphics.setTooltipForNextFrame(this.font, componentList, Optional.empty(), mouseX, mouseY);
                return; // Only show one tooltip
            }
        }
    }

    /**
     * Render tooltip when hovering over fluid drawer slots.
     */
    private void renderFluidDrawerTooltip(GuiGraphicsExtractor guiGraphics, FluidDrawerTile tile, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int baseX = i + 64;
        int baseY = j + 16;

        DrawerType type = tile.getDrawerType();
        FluidInventoryHandler handler = tile.getHandler();

        for (int slot = 0; slot < type.getSlots(); slot++) {
            Pair<Integer, Integer> pos = type.getSlotPosition().apply(slot);
            int slotX = baseX + pos.getLeft();
            int slotY = baseY + pos.getRight();

            // Check if mouse is hovering over this slot (18x18 area)
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                // Render highlight
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, HIGHLIGHT);

                // Build tooltip
                List<Component> componentList = new ArrayList<>();
                FluidVariant resource = handler.getResource(slot);
                long amount = handler.getAmount(slot);
                long capacity = handler.getSlotLimit(slot);

                // Use filterStack if locked and resource is blank
                FluidVariant displayResource = resource;
                if (resource.isBlank() && tile.isLocked()) {
                    displayResource = handler.getFilterStack(slot);
                }

                if (displayResource.isBlank()) {
                    componentList
                            .add(Component.translatable("gui.functionalstorage.fluid").withStyle(ChatFormatting.GOLD)
                                    .append(Component.translatable("gui.functionalstorage.empty")
                                            .withStyle(ChatFormatting.WHITE)));
                } else {
                    componentList.add(Component.translatable("gui.functionalstorage.fluid")
                            .withStyle(ChatFormatting.GOLD)
                            .append(FluidVariantAttributes.getName(displayResource).copy()
                                    .withStyle(ChatFormatting.WHITE)));
                    String amountStr = NumberUtils.getFormatedFluidBigNumber(amount) + "/"
                            + NumberUtils.getFormatedFluidBigNumber(capacity);
                    componentList
                            .add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD)
                                    .append(Component.literal(amountStr).withStyle(ChatFormatting.WHITE)));
                }
                componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(slot)).withStyle(ChatFormatting.WHITE)));

                guiGraphics.setTooltipForNextFrame(this.font, componentList, Optional.empty(), mouseX, mouseY);
                return; // Only show one tooltip
            }
        }
    }

    /**
     * Render tooltip when hovering over compacting drawer slots (3-slot layout).
     */
    private void renderCompactingDrawerTooltip(GuiGraphicsExtractor guiGraphics, CompactingDrawerTile tile, int mouseX,
            int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int baseX = i + 64;
        int baseY = j + 16;

        CompactingInventoryHandler handler = tile.handler;

        // Same slot positions as renderCompactingDrawerContents
        Pair<Integer, Integer>[] slotPositions = new Pair[] {
                Pair.of(28, 28), // Slot 0: bottom-right (nuggets)
                Pair.of(4, 28), // Slot 1: bottom-left (ingots)
                Pair.of(16, 4) // Slot 2: top-center (blocks)
        };

        List<CompactingUtil.Result> resultList = handler.getResultList();

        for (int slot = 0; slot < 3; slot++) {
            if (slot >= resultList.size() || resultList.get(slot).getResult().isEmpty()) {
                continue;
            }

            Pair<Integer, Integer> pos = slotPositions[slot];
            int slotX = baseX + pos.getLeft();
            int slotY = baseY + pos.getRight();

            // Check if mouse is hovering over this slot (18x18 area)
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                // Render highlight
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, HIGHLIGHT);

                // Build tooltip
                List<Component> componentList = new ArrayList<>();
                ItemStack displayStack = resultList.get(slot).getResult();
                int amount = handler.getStackInSlot(slot).getCount();
                int maxAmount = handler.getSlotLimit(slot);

                componentList.add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD)
                        .append(displayStack.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
                String amountStr = NumberUtils.getFormatedBigNumber(amount) + "/"
                        + NumberUtils.getFormatedBigNumber(maxAmount);
                componentList.add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(amountStr).withStyle(ChatFormatting.WHITE)));
                componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(slot)).withStyle(ChatFormatting.WHITE)));

                guiGraphics.setTooltipForNextFrame(this.font, componentList, Optional.empty(), mouseX, mouseY);
                return; // Only show one tooltip
            }
        }
    }

    /**
     * Render tooltip when hovering over simple compacting drawer slots (2-slot
     * layout).
     */
    private void renderSimpleCompactingDrawerTooltip(GuiGraphicsExtractor guiGraphics, SimpleCompactingDrawerTile tile,
            int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int baseX = i + 64;
        int baseY = j + 16;

        CompactingInventoryHandler handler = tile.handler;

        // Same slot positions as renderSimpleCompactingDrawerContents
        Pair<Integer, Integer>[] slotPositions = new Pair[] {
                Pair.of(16, 28), // Slot 0: bottom (lower tier)
                Pair.of(16, 4) // Slot 1: top (higher tier)
        };

        List<CompactingUtil.Result> resultList = handler.getResultList();

        for (int slot = 0; slot < 2; slot++) {
            if (slot >= resultList.size() || resultList.get(slot).getResult().isEmpty()) {
                continue;
            }

            Pair<Integer, Integer> pos = slotPositions[slot];
            int slotX = baseX + pos.getLeft();
            int slotY = baseY + pos.getRight();

            // Check if mouse is hovering over this slot (18x18 area)
            if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
                // Render highlight
                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, HIGHLIGHT);

                // Build tooltip
                List<Component> componentList = new ArrayList<>();
                ItemStack displayStack = resultList.get(slot).getResult();
                int amount = handler.getStackInSlot(slot).getCount();
                int maxAmount = handler.getSlotLimit(slot);

                componentList.add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD)
                        .append(displayStack.getHoverName().copy().withStyle(ChatFormatting.WHITE)));
                String amountStr = NumberUtils.getFormatedBigNumber(amount) + "/"
                        + NumberUtils.getFormatedBigNumber(maxAmount);
                componentList.add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(amountStr).withStyle(ChatFormatting.WHITE)));
                componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(String.valueOf(slot)).withStyle(ChatFormatting.WHITE)));

                guiGraphics.setTooltipForNextFrame(this.font, componentList, Optional.empty(), mouseX, mouseY);
                return; // Only show one tooltip
            }
        }
    }

    /**
     * Render Ender Drawer contents (1-slot layout, uses EnderInventoryHandler).
     */
    private void renderEnderDrawerContents(GuiGraphicsExtractor guiGraphics, EnderDrawerTile tile) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int x = i + 64;
        int y = j + 16;

        com.koudesuk.functionalstorage.inventory.EnderInventoryHandler handler = tile.getHandler();
        if (handler == null) {
            return;
        }

        // Single slot at center (similar to X_1 drawer)
        BigInventoryHandler.BigStack stack = handler.getStoredStacks().get(0);
        if (!stack.getStack().isEmpty()) {
            int itemX = x + 16; // Centered in 48x48 area
            int itemY = y + 16;

            guiGraphics.item(stack.getStack(), itemX, itemY);
            guiGraphics.itemDecorations(this.font, stack.getStack(), itemX, itemY, null);

            // Render Amount
            String amount = NumberUtils.getFormatedBigNumber(stack.getAmount()) + "/"
                    + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(0));
            renderAmountText(guiGraphics, itemX + 8, itemY + 12, amount);
        }
    }

    /**
     * Render tooltip when hovering over ender drawer slot.
     */
    private void renderEnderDrawerTooltip(GuiGraphicsExtractor guiGraphics, EnderDrawerTile tile, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int baseX = i + 64;
        int baseY = j + 16;

        com.koudesuk.functionalstorage.inventory.EnderInventoryHandler handler = tile.getHandler();
        if (handler == null) {
            return;
        }

        // Single slot centered
        int slotX = baseX + 16;
        int slotY = baseY + 16;

        // Check if mouse is hovering over this slot (18x18 area)
        if (mouseX >= slotX && mouseX < slotX + 18 && mouseY >= slotY && mouseY < slotY + 18) {
            // Render highlight
            guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, HIGHLIGHT);

            // Build tooltip
            List<Component> componentList = new ArrayList<>();
            BigInventoryHandler.BigStack bigStack = handler.getStoredStacks().get(0);

            if (bigStack.getStack().isEmpty()) {
                componentList
                        .add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD)
                                .append(Component.translatable("gui.functionalstorage.empty")
                                        .withStyle(ChatFormatting.WHITE)));
            } else {
                componentList
                        .add(Component.translatable("gui.functionalstorage.item").withStyle(ChatFormatting.GOLD)
                                .append(bigStack.getStack().getHoverName().copy().withStyle(ChatFormatting.WHITE)));
                String amount = NumberUtils.getFormatedBigNumber(bigStack.getAmount()) + "/"
                        + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(0));
                componentList
                        .add(Component.translatable("gui.functionalstorage.amount").withStyle(ChatFormatting.GOLD)
                                .append(Component.literal(amount).withStyle(ChatFormatting.WHITE)));
            }
            componentList.add(Component.translatable("gui.functionalstorage.slot").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("0").withStyle(ChatFormatting.WHITE)));

            guiGraphics.setTooltipForNextFrame(this.font, componentList, Optional.empty(), mouseX, mouseY);
        }
    }
}
