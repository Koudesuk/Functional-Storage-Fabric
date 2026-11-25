package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.inventory.ArmoryCabinetMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/**
 * The armory cabinet's screen, a port of the original's ArmoryCabinetScreen: 8x4 slots, a search box and a
 * scroll bar. The original draws Titanium's background, which Fabric does not have, so this one is the vanilla
 * four row chest with the ninth slot column made room for the scroll bar.
 */
public class ArmoryCabinetScreen extends AbstractContainerScreen<ArmoryCabinetMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = ResourceLocation.withDefaultNamespace(
            "textures/gui/container/generic_54.png");
    // The creative inventory's scroller sprites
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace(
            "container/creative_inventory/scroller_disabled");
    private static final int ROWS_HEIGHT = ArmoryCabinetMenu.VISIBLE_ROWS * 18;
    private static final int SEARCH_X = 96;
    private static final int SEARCH_Y = 4;
    private static final int SEARCH_WIDTH = 72;
    private static final int SEARCH_HEIGHT = 12;
    private static final int SCROLLBAR_X = 155;
    private static final int SCROLLBAR_Y = ArmoryCabinetMenu.CABINET_Y;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int THUMB_HEIGHT = 15;
    private static final int LABEL_COLOR = 0x404040;
    private static final int GUI_GREY = 0xFFC6C6C6;
    private static final int MAX_QUERY_LENGTH = 50;

    private EditBox searchBox;
    private boolean scrolling;

    public ArmoryCabinetScreen(ArmoryCabinetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 114 + ArmoryCabinetMenu.VISIBLE_ROWS * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        searchBox = new EditBox(font, leftPos + SEARCH_X, topPos + SEARCH_Y, SEARCH_WIDTH, SEARCH_HEIGHT,
                Component.translatable("gui.functionalstorage.armory_search"));
        searchBox.setMaxLength(MAX_QUERY_LENGTH);
        searchBox.setResponder(this::onSearchChanged);
        addRenderableWidget(searchBox);
    }

    /**
     * The server keeps the same filtered view, so slot clicks reach the slot shown here. Like the original,
     * the search goes to the server as menu button clicks: one to clear it, then one per character.
     */
    private void onSearchChanged(String text) {
        menu.setQuery(text);
        if (minecraft == null || minecraft.gameMode == null)
            return;
        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ArmoryCabinetMenu.BUTTON_QUERY_CLEAR);
        text.chars().limit(MAX_QUERY_LENGTH).forEach(value -> minecraft.gameMode.handleInventoryButtonClick(
                menu.containerId, ArmoryCabinetMenu.BUTTON_QUERY_APPEND_BASE + value));
        syncScroll();
    }

    private void syncScroll() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    ArmoryCabinetMenu.BUTTON_SCROLL_BASE + menu.getScrollRow());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(CONTAINER_BACKGROUND, leftPos, topPos, 0, 0, imageWidth, ROWS_HEIGHT + 17);
        graphics.blit(CONTAINER_BACKGROUND, leftPos, topPos + ROWS_HEIGHT + 17, 0, 126, imageWidth, 96);
        // The cabinet has 8 columns: the ninth makes room for the scroll bar
        int columnX = leftPos + 7 + ArmoryCabinetMenu.COLUMNS * 18;
        graphics.fill(columnX, topPos + 17, columnX + 18, topPos + 17 + ROWS_HEIGHT, GUI_GREY);
        drawScrollbar(graphics);
    }

    private void drawScrollbar(GuiGraphics graphics) {
        int barX = leftPos + SCROLLBAR_X;
        int barY = topPos + SCROLLBAR_Y;
        graphics.fill(barX, barY, barX + SCROLLBAR_WIDTH, barY + ROWS_HEIGHT, 0xFF8B8B8B);
        graphics.fill(barX + 1, barY + 1, barX + SCROLLBAR_WIDTH - 1, barY + ROWS_HEIGHT - 1, 0xFFCCCCCC);
        int max = menu.getMaxScrollRow();
        int thumbY = max <= 0 ? barY
                : barY + (int) ((ROWS_HEIGHT - THUMB_HEIGHT) * (menu.getScrollRow() / (float) max));
        graphics.blitSprite(max <= 0 ? SCROLLER_DISABLED_SPRITE : SCROLLER_SPRITE, barX, thumbY, SCROLLBAR_WIDTH,
                THUMB_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, LABEL_COLOR, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Typing a search must not close the screen with the inventory key
        if (searchBox != null && searchBox.isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            searchBox.keyPressed(keyCode, scanCode, modifiers);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isInScrollbar(mouseX, mouseY)) {
            scrolling = true;
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (scrolling) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        scrolling = false;
        return super.mouseReleased(mouseX, mouseY, button);
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
