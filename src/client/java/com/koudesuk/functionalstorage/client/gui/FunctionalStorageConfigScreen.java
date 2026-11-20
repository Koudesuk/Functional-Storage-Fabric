package com.koudesuk.functionalstorage.client.gui;

import com.koudesuk.functionalstorage.block.config.ConfigFile;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.client.FunctionalStorageClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Editor for both config files, opened from Mod Menu. Every value is a text box; input that is
 * not a number in range turns red and disables Done. While connected to another server the
 * common values are that server's and are shown read-only.
 */
public class FunctionalStorageConfigScreen extends Screen {

    private static final int ROW_WIDTH = 310;
    private static final int HEADER_HEIGHT = 32;
    private static final int FOOTER_HEIGHT = 32;
    private static final int LABEL_COLOR = 0xFFFFFF;
    private static final int VALID_COLOR = 0xE0E0E0;
    private static final int INVALID_COLOR = 0xFF5555;

    private final Screen parent;
    private final List<Row> fields = new ArrayList<>();
    private boolean serverControlled;
    private ConfigList list;
    private Button done;

    public FunctionalStorageConfigScreen(Screen parent) {
        super(Component.translatableWithFallback("config.functionalstorage.title", "Functional Storage Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        serverControlled = minecraft.getConnection() != null && !minecraft.hasSingleplayerServer();
        fields.clear();

        list = new ConfigList(minecraft, width, height, HEADER_HEIGHT, height - FOOTER_HEIGHT);
        list.add(new Row(font, Component.translatableWithFallback("config.functionalstorage.rejoin",
                "Some changes take effect after you rejoin the world").withStyle(ChatFormatting.GRAY)));
        addFile(FunctionalStorageClientConfig.FILE, true);
        if (serverControlled)
            list.add(new Row(font, Component.translatableWithFallback("config.functionalstorage.server",
                    "Set by the server you are connected to").withStyle(ChatFormatting.GRAY)));
        addFile(FunctionalStorageConfig.FILE, !serverControlled);
        addWidget(list);

        int buttonY = height - FOOTER_HEIGHT + 6;
        done = addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
            save();
            onClose();
        }).bounds(width / 2 - 154, buttonY, 150, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> onClose())
                .bounds(width / 2 + 4, buttonY, 150, 20).build());
        done.active = fields.stream().allMatch(Row::isValid);
    }

    private void addFile(ConfigFile file, boolean editable) {
        String section = null;
        for (ConfigFile.Value<?> value : file.values()) {
            if (!value.section().equals(section)) {
                section = value.section();
                list.add(new Row(font, Component.translatableWithFallback(
                        "config.functionalstorage.section." + section, pretty(section))
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)));
            }
            Row row = new Row(font, file, value, editable);
            row.box.setResponder(text -> {
                row.box.setTextColor(row.isValid() ? VALID_COLOR : INVALID_COLOR);
                if (done != null)
                    done.active = fields.stream().allMatch(Row::isValid);
            });
            fields.add(row);
            list.add(row);
        }
    }

    private void save() {
        save(FunctionalStorageClientConfig.FILE);
        if (!serverControlled)
            save(FunctionalStorageConfig.FILE);
    }

    private void save(ConfigFile file) {
        Map<String, String> raw = new HashMap<>();
        for (Row row : fields) {
            if (row.file == file)
                raw.put(row.value.key(), row.box.getValue());
        }
        file.apply(raw);
        file.save();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        list.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, LABEL_COLOR);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    /**
     * "drawer_1x1_stacks" -> "Drawer 1x1 Stacks"
     */
    private static String pretty(String id) {
        StringBuilder out = new StringBuilder();
        for (String word : id.split("_")) {
            if (word.isEmpty())
                continue;
            if (!out.isEmpty())
                out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    private static final class ConfigList extends ContainerObjectSelectionList<Row> {
        ConfigList(Minecraft minecraft, int width, int height, int top, int bottom) {
            super(minecraft, width, height, top, bottom, 24);
        }

        void add(Row row) {
            addEntry(row);
        }

        @Override
        public int getRowWidth() {
            return ROW_WIDTH;
        }

        @Override
        protected int getScrollbarPosition() {
            return width / 2 + ROW_WIDTH / 2 + 6;
        }
    }

    /**
     * A label, plus a text box when the row edits a config value.
     */
    private static final class Row extends ContainerObjectSelectionList.Entry<Row> {
        private final Font font;
        private final Component label;
        @Nullable
        private final ConfigFile file;
        @Nullable
        private final ConfigFile.Value<?> value;
        @Nullable
        private final EditBox box;
        private final boolean editable;

        Row(Font font, Component label) {
            this.font = font;
            this.label = label;
            this.file = null;
            this.value = null;
            this.box = null;
            this.editable = false;
        }

        Row(Font font, ConfigFile file, ConfigFile.Value<?> value, boolean editable) {
            String key = "config.functionalstorage." + value.key();
            this.font = font;
            this.label = Component.translatableWithFallback(key, pretty(value.key().substring(value.key().indexOf('.') + 1)));
            this.file = file;
            this.value = value;
            this.editable = editable;
            this.box = new EditBox(font, 0, 0, 100, 18, label);
            // An EditBox keeps 32 characters by default, too few for the id lists; this is what
            // ConfigSyncPayload can carry. Set before the value, which would be cut otherwise
            box.setMaxLength(Short.MAX_VALUE);
            box.setValue(value.toString());
            box.setEditable(editable);
            box.setTooltip(Tooltip.create(Component.translatableWithFallback(key + ".tooltip", value.comment())
                    .append("\n\n").append(Component.literal(value.rangeLine()).withStyle(ChatFormatting.GRAY))));
        }

        /**
         * Read-only rows are never saved, so they never block Done.
         */
        boolean isValid() {
            return !editable || value.accepts(box.getValue());
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX,
                int mouseY, boolean hovered, float partialTick) {
            graphics.drawString(font, label, left, top + (height - font.lineHeight) / 2, LABEL_COLOR, false);
            if (box != null) {
                box.setX(left + width - box.getWidth());
                box.setY(top + (height - box.getHeight()) / 2);
                box.render(graphics, mouseX, mouseY, partialTick);
            }
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return box == null ? List.of() : List.of(box);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return box == null ? List.of() : List.of(box);
        }
    }
}
