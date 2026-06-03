package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class DrawerScreen extends AbstractContainerScreen<DrawerMenu> {
    public DrawerScreen(DrawerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 176, 184);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
    }
}
