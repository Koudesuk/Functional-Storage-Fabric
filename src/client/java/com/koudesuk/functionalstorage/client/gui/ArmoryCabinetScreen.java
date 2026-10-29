package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * The armory cabinet's screen, a port of the original's ArmoryCabinetScreen: 8x4 slots, a search box and a
 * scroll bar. The original draws Titanium's background, which Fabric does not have, so this one is the vanilla
 * four row chest with the ninth slot column made room for the scroll bar.
 */
public class ArmoryCabinetScreen extends AbstractContainerScreen<ArmoryCabinetMenu> {

    private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private static final int ROWS_HEIGHT = ArmoryCabinetMenu.VISIBLE_ROWS * 18;
    private static final int SEARCH_X = 96;
    private static final int SEARCH_Y = 4;
    private static final int SEARCH_WIDTH = 72;
    private static final int SEARCH_HEIGHT = 12;
    private static final int SCROLLBAR_X = 155;
    private static final int SCROLLBAR_Y = ArmoryCabinetMenu.CABINET_Y;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int THUMB_HEIGHT = 15;
    // 26.1 draws nothing for a color without alpha, so every color here is ARGB
    private static final int LABEL_COLOR = 0xFF404040;
    private static final int GUI_GREY = 0xFFC6C6C6;

    private EditBox searchBox;
    private boolean scrolling;

    public ArmoryCabinetScreen(ArmoryCabinetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 114 + ArmoryCabinetMenu.VISIBLE_ROWS * 18);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT,
                Component.translatable("gui.functionalstorage.armory_search"));
        searchBox.setMaxLength(50);
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);
    }

    private void onSearchChanged(String text) {
        menu.setQuery(text);
        if (minecraft == null || minecraft.gameMode == null)
            return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ArmoryCabinetMenu.BUTTON_QUERY_CLEAR);
        text.chars().limit(50).forEach(value -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                ArmoryCabinetMenu.BUTTON_QUERY_APPEND_BASE + value));
        syncScroll();
    }

    private void syncScroll() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    ArmoryCabinetMenu.BUTTON_SCROLL_BASE + menu.getScrollRow());
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, leftPos, topPos, 0, 0, imageWidth,
                ROWS_HEIGHT + 17, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, leftPos, topPos + ROWS_HEIGHT + 17, 0, 126,
                imageWidth, 96, 256, 256);
        // The cabinet has 8 columns: the ninth makes room for the scroll bar
        int columnX = leftPos + 7 + ArmoryCabinetMenu.COLUMNS * 18;
        graphics.fill(columnX, topPos + 17, columnX + 18, topPos + 17 + ROWS_HEIGHT, GUI_GREY);
        drawScrollbar(graphics);
    }

    private void drawScrollbar(GuiGraphicsExtractor graphics) {
        int barX = leftPos + SCROLLBAR_X;
        int barY = topPos + SCROLLBAR_Y;
        graphics.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + ROWS_HEIGHT, 0xFF8B8B8B);
        graphics.fill(barX + 1, barY + 1, barX + SCROLLBAR_WIDTH - 1, barY + ROWS_HEIGHT - 1, 0xFFCCCCCC);
        int max = menu.getMaxScrollRow();
        int thumbY = max <= 0 ? barY
                : barY + (int) ((ROWS_HEIGHT - THUMB_HEIGHT) * (menu.getScrollRow() / (float) max));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, max <= 0 ? SCROLLER_DISABLED_SPRITE : SCROLLER_SPRITE,
                barX, thumbY, SCROLLBAR_WIDTH, THUMB_HEIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isInCabinetArea(mouseX, mouseY) || isInScrollbar(mouseX, mouseY)) {
            menu.setScrollRow(menu.getScrollRow() - (int) Math.signum(scrollY));
            syncScroll();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Typing a search must not close the screen with the inventory key
        if (searchBox != null && searchBox.isFocused() && !event.isEscape()) {
            searchBox.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && isInScrollbar(event.x(), event.y())) {
            scrolling = true;
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (scrolling) {
            updateScrollFromMouse(event.y());
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        scrolling = false;
        return super.mouseReleased(event);
    }

    private boolean isInCabinetArea(double mouseX, double mouseY) {
        return mouseX >= leftPos + 8 && mouseX < leftPos + 8 + ArmoryCabinetMenu.COLUMNS * 18
                && mouseY >= topPos + ArmoryCabinetMenu.CABINET_Y
                && mouseY < topPos + ArmoryCabinetMenu.CABINET_Y + ROWS_HEIGHT;
    }

    private boolean isInScrollbar(double mouseX, double mouseY) {
        return mouseX >= leftPos + SCROLLBAR_X && mouseX < leftPos + SCROLLBAR_X + SCROLLBAR_WIDTH
                && mouseY >= topPos + SCROLLBAR_Y && mouseY < topPos + SCROLLBAR_Y + ROWS_HEIGHT;
    }

    private void updateScrollFromMouse(double mouseY) {
        int max = menu.getMaxScrollRow();
        int relative = (int) (mouseY - (topPos + SCROLLBAR_Y) - THUMB_HEIGHT / 2.0D);
        menu.setScrollRow(max <= 0 ? 0 : Math.round((relative / (float) (ROWS_HEIGHT - THUMB_HEIGHT)) * max));
        syncScroll();
    }
}
