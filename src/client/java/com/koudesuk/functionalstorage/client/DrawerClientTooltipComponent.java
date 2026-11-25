package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.EnderDrawerBlock;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.item.DrawerBlockItem;
import com.koudesuk.functionalstorage.item.DrawerContentsTooltip;
import com.koudesuk.functionalstorage.util.NumberUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * The original's drawer tooltip image: a row of icons for what a drawer item carries. Item drawers
 * also draw the "Contents:" line above the row, because Fabric (vanilla) puts the image right below
 * the name while the original inserted it after that line.
 */
public final class DrawerClientTooltipComponent implements ClientTooltipComponent {
    private static final int ITEM_SIZE = 16;
    private static final int PADDING = 4;
    private static final int LINE_HEIGHT = 10;

    @Nullable
    private final FormattedCharSequence header;
    private final List<Entry> entries;
    private final int spacing;

    private DrawerClientTooltipComponent(@Nullable Component header, List<Entry> entries, int spacing) {
        this.header = header != null ? header.getVisualOrderText() : null;
        this.entries = entries;
        this.spacing = spacing;
    }

    public static DrawerClientTooltipComponent create(DrawerContentsTooltip data) {
        ItemStack stack = data.drawer();
        Level level = Minecraft.getInstance().level;
        ControllableDrawerTile<?> tile = level != null && stack.getItem() instanceof DrawerBlockItem item
                ? item.loadTile(stack, level.registryAccess())
                : null;

        if (tile instanceof EnderDrawerTile ender && ender.getFrequency() != null) {
            return new DrawerClientTooltipComponent(null, EnderDrawerBlock.getFrequencyDisplay(ender.getFrequency())
                    .stream().map(icon -> new Entry(icon, null)).toList(), 18);
        }
        List<Entry> entries = new ArrayList<>();
        if (tile instanceof DrawerTile drawer) {
            for (BigInventoryHandler.BigStack stored : drawer.getHandler().getStoredStacks()) {
                if (!stored.getStack().isEmpty()) {
                    entries.add(new Entry(stored.getStack(), NumberUtils.getFormatedBigNumber(
                            drawer.isCreative() ? Integer.MAX_VALUE : stored.getAmount())));
                }
            }
            return new DrawerClientTooltipComponent(
                    Component.translatable("drawer.block.contents").withStyle(ChatFormatting.GRAY), entries, 32);
        }
        if (tile instanceof ItemControllableDrawerTile<?> itemTile
                && itemTile.getStorage() instanceof CompactingInventoryHandler handler) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                ItemStack stored = handler.getStackInSlot(slot);
                if (!stored.isEmpty()) {
                    entries.add(new Entry(stored, NumberUtils.getFormatedBigNumber(stored.getCount()) + "/"
                            + NumberUtils.getFormatedBigNumber(handler.getSlotLimit(slot))));
                }
            }
        }
        return new DrawerClientTooltipComponent(null, entries, 32);
    }

    @Override
    public int getHeight() {
        return headerHeight() + (entries.isEmpty() ? 0 : ITEM_SIZE + 2);
    }

    @Override
    public int getWidth(Font font) {
        int width = entries.isEmpty() ? 0 : PADDING * 2 + ITEM_SIZE + (entries.size() - 1) * spacing;
        return header != null ? Math.max(width, font.width(header)) : width;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        if (header != null) {
            font.drawInBatch(header, x, y, -1, true, matrix, bufferSource, Font.DisplayMode.NORMAL, 0,
                    LightTexture.FULL_BRIGHT);
        }
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int itemY = y + headerHeight();
        for (int index = 0; index < entries.size(); index++) {
            Entry entry = entries.get(index);
            int itemX = x + PADDING + index * spacing;
            graphics.renderItem(entry.stack(), itemX, itemY);
            if (entry.amount() != null) {
                renderAmount(graphics, font, entry.amount(), itemX, itemY);
            }
        }
    }

    private int headerHeight() {
        return header != null ? LINE_HEIGHT : 0;
    }

    // TooltipUtil.renderItemStackOverlay from the original: right aligned at the bottom of the icon,
    // smaller for longer numbers
    private static void renderAmount(GuiGraphics graphics, Font font, String amount, int x, int y) {
        int scaled = amount.length() - 2;
        float scale = scaled >= 2 ? 0.5F : scaled == 1 ? 0.75F : 1.0F;
        graphics.pose().pushPose();
        // Above the item icon
        graphics.pose().translate(0, 0, 200);
        graphics.pose().scale(scale, scale, 1);
        graphics.drawString(font, amount, Math.round((x + 17) / scale) - font.width(amount),
                Math.round((y + 9) / scale), 0xFFFFFF, true);
        graphics.pose().popPose();
    }

    private record Entry(ItemStack stack, @Nullable String amount) {
    }
}
