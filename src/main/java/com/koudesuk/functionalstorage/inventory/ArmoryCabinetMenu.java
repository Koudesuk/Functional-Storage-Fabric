package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile;
import com.koudesuk.functionalstorage.registry.FunctionalStorageMenus;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The armory cabinet screen's menu, a port of the original's ArmoryCabinetMenu: an 8x4 window into the cabinet
 * that scrolls and can be narrowed by a search. Both sides keep the same filtered view, so the screen sends its
 * search and scroll row as menu button clicks.
 */
public class ArmoryCabinetMenu extends AbstractContainerMenu {

    public static final int COLUMNS = 8;
    public static final int VISIBLE_ROWS = 4;
    public static final int VISIBLE_SLOTS = COLUMNS * VISIBLE_ROWS;
    public static final int BUTTON_SCROLL_BASE = 10_000;
    public static final int BUTTON_QUERY_CLEAR = 20_000;
    public static final int BUTTON_QUERY_APPEND_BASE = 30_000;
    // Slot rows, laid out like a four row chest (generic_54.png)
    public static final int CABINET_Y = 18;
    public static final int INVENTORY_Y = 103;

    private final Inventory playerInventory;
    private final ArmoryCabinetInventoryHandler handler;
    private final BlockPos pos;
    private final List<ArmorySlot> armorySlots = new ArrayList<>();
    private final List<Integer> filteredSlots = new ArrayList<>();
    private String query = "";
    private int scrollRow;

    /**
     * Client side: the cabinet's contents come with the opening data, like the original.
     */
    public ArmoryCabinetMenu(int id, Inventory playerInventory, OpenData data) {
        this(id, playerInventory, clientHandler(playerInventory, data), data.pos());
    }

    public ArmoryCabinetMenu(int id, Inventory playerInventory, ArmoryCabinetInventoryHandler handler, BlockPos pos) {
        super(FunctionalStorageMenus.ARMORY_CABINET, id);
        this.playerInventory = playerInventory;
        this.handler = handler;
        this.pos = pos;

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            for (int column = 0; column < COLUMNS; column++) {
                ArmorySlot slot = new ArmorySlot(handler, column + row * COLUMNS, 8 + column * 18,
                        CABINET_Y + row * 18);
                armorySlots.add(slot);
                addSlot(slot);
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, INVENTORY_Y + 58));
        }

        rebuildFilter();
    }

    private static ArmoryCabinetInventoryHandler clientHandler(Inventory inventory, OpenData data) {
        ArmoryCabinetInventoryHandler handler = new ArmoryCabinetInventoryHandler() {
            @Override
            public void onChange() {
            }
        };
        handler.deserializeNBT(data.contents(), inventory.player.level().registryAccess());
        return handler;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query.toLowerCase(Locale.ROOT).trim();
        this.scrollRow = 0;
        rebuildFilter();
    }

    public void setScrollRow(int scrollRow) {
        this.scrollRow = Math.max(0, Math.min(scrollRow, getMaxScrollRow()));
        updateVisibleSlots();
    }

    public int getScrollRow() {
        return scrollRow;
    }

    public int getMaxScrollRow() {
        return Math.max(0, (filteredSlots.size() + COLUMNS - 1) / COLUMNS - VISIBLE_ROWS);
    }

    private void rebuildFilter() {
        filteredSlots.clear();
        for (int slot = 0; slot < handler.getContainerSize(); slot++) {
            if (query.isEmpty() || matches(handler.getItem(slot))) {
                filteredSlots.add(slot);
            }
        }
        setScrollRow(scrollRow);
    }

    private boolean matches(ItemStack stack) {
        if (stack.isEmpty())
            return false;
        if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query))
            return true;
        return stack.getTooltipLines(Item.TooltipContext.of(playerInventory.player.level()), playerInventory.player,
                TooltipFlag.NORMAL)
                .stream()
                .map(Component::getString)
                .anyMatch(text -> text.toLowerCase(Locale.ROOT).contains(query));
    }

    private void updateVisibleSlots() {
        int firstFiltered = scrollRow * COLUMNS;
        for (int i = 0; i < armorySlots.size(); i++) {
            int filteredIndex = firstFiltered + i;
            armorySlots.get(i).setArmoryIndex(filteredIndex < filteredSlots.size() ? filteredSlots.get(filteredIndex) : -1);
        }
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button >= BUTTON_SCROLL_BASE && button < BUTTON_QUERY_CLEAR) {
            setScrollRow(button - BUTTON_SCROLL_BASE);
            return true;
        }
        if (button == BUTTON_QUERY_CLEAR) {
            setQuery("");
            return true;
        }
        if (button >= BUTTON_QUERY_APPEND_BASE && button <= BUTTON_QUERY_APPEND_BASE + Character.MAX_VALUE) {
            setQuery(query + (char) (button - BUTTON_QUERY_APPEND_BASE));
            return true;
        }
        return super.clickMenuButton(player, button);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < VISIBLE_SLOTS) {
                if (!moveItemStackTo(stack, VISIBLE_SLOTS, this.slots.size(), true))
                    return ItemStack.EMPTY;
                // The cabinet slot only sees a copy, so it is emptied by what moved
                slot.set(stack.isEmpty() ? ItemStack.EMPTY : stack);
                return result;
            }
            // The cabinet takes one item per free slot
            int moved = 0;
            try (Transaction transaction = Transaction.openOuter()) {
                ItemVariant resource = ItemVariant.of(stack);
                while (moved < stack.getCount() && handler.insert(resource, 1, transaction) == 1)
                    moved++;
                transaction.commit();
            }
            if (moved == 0)
                return ItemStack.EMPTY;
            stack.shrink(moved);
            rebuildFilter();
            if (stack.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.level().getBlockEntity(pos) instanceof ArmoryCabinetTile tile && !tile.isRemoved()
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    /**
     * Shows the cabinet slot picked by the filter and scroll row, or nothing.
     */
    private static class ArmorySlot extends Slot {
        private final ArmoryCabinetInventoryHandler handler;
        private int armoryIndex;

        private ArmorySlot(ArmoryCabinetInventoryHandler handler, int visibleIndex, int x, int y) {
            super(new SimpleContainer(0), visibleIndex, x, y);
            this.handler = handler;
            this.armoryIndex = visibleIndex;
        }

        private void setArmoryIndex(int armoryIndex) {
            this.armoryIndex = armoryIndex;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return armoryIndex >= 0 && handler.isItemValid(stack);
        }

        @Override
        public ItemStack getItem() {
            return armoryIndex >= 0 ? handler.getItem(armoryIndex) : ItemStack.EMPTY;
        }

        @Override
        public boolean hasItem() {
            return !getItem().isEmpty();
        }

        @Override
        public void set(ItemStack stack) {
            if (armoryIndex >= 0) {
                handler.setItem(armoryIndex, stack);
                setChanged();
            }
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return 1;
        }

        @Override
        public boolean mayPickup(Player player) {
            return armoryIndex >= 0 && !handler.getItem(armoryIndex).isEmpty();
        }

        @Override
        public ItemStack remove(int amount) {
            return armoryIndex >= 0 ? handler.removeItem(armoryIndex, amount) : ItemStack.EMPTY;
        }

        @Override
        public boolean isActive() {
            return armoryIndex >= 0;
        }
    }

    /**
     * What the client needs to open the screen: where the cabinet is and what it holds.
     */
    public record OpenData(BlockPos pos, CompoundTag contents) {
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenData> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, OpenData::pos,
                ByteBufCodecs.TRUSTED_COMPOUND_TAG, OpenData::contents,
                OpenData::new);
    }
}
