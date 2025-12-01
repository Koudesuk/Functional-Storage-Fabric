package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.registry.FunctionalStorageMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DrawerMenu extends AbstractContainerMenu {

    private final Container storageUpgrades;
    private final Container utilityUpgrades;
    private final com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile<?> tile;
    private final int storageSlotCount;
    private final int utilitySlotCount;

    public DrawerMenu(int containerId, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                (com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile<?>) playerInventory.player.level()
                        .getBlockEntity(buf.readBlockPos()));
    }

    public DrawerMenu(int containerId, Inventory playerInventory,
            com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile<?> tile) {
        this(containerId, playerInventory, tile.getStorageUpgrades(), tile.getUtilityUpgrades(), tile);
    }

    public DrawerMenu(int containerId, Inventory playerInventory, Container storageUpgrades, Container utilityUpgrades,
            com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile<?> tile) {
        super(FunctionalStorageMenus.DRAWER, containerId);
        this.storageUpgrades = storageUpgrades;
        this.utilityUpgrades = utilityUpgrades;
        this.tile = tile;
        
        // Get dynamic slot counts from tile
        this.storageSlotCount = tile.getStorageSlotAmount();
        this.utilitySlotCount = tile.getUtilitySlotAmount();

        // Use dynamic slot counts for validation
        checkContainerSize(storageUpgrades, storageSlotCount);
        checkContainerSize(utilityUpgrades, utilitySlotCount);

        storageUpgrades.startOpen(playerInventory.player);
        utilityUpgrades.startOpen(playerInventory.player);

        // Storage Upgrades - dynamic count
        for (int i = 0; i < storageSlotCount; ++i) {
            this.addSlot(new Slot(storageUpgrades, i, 10 + i * 18, 70) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof com.koudesuk.functionalstorage.item.UpgradeItem
                            && ((com.koudesuk.functionalstorage.item.UpgradeItem) stack.getItem())
                                    .getType() == com.koudesuk.functionalstorage.item.UpgradeItem.Type.STORAGE;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }
            });
        }

        // Utility Upgrades - dynamic count
        for (int i = 0; i < utilitySlotCount; ++i) {
            this.addSlot(new Slot(utilityUpgrades, i, 114 + i * 18, 70) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stack.getItem() instanceof com.koudesuk.functionalstorage.item.UpgradeItem
                            && ((com.koudesuk.functionalstorage.item.UpgradeItem) stack.getItem())
                                    .getType() == com.koudesuk.functionalstorage.item.UpgradeItem.Type.UTILITY;
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }

                @Override
                public int getMaxStackSize(ItemStack stack) {
                    return 1;
                }
            });
        }

        // Player Inventory
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 100 + i * 18));
            }
        }

        // Hotbar
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 158));
        }
    }

    public com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile<?> getTile() {
        return tile;
    }

    public int getStorageSlotCount() {
        return storageSlotCount;
    }

    public int getUtilitySlotCount() {
        return utilitySlotCount;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.storageUpgrades.stillValid(player) && this.utilityUpgrades.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        
        // Calculate dynamic slot boundaries
        int totalUpgradeSlots = storageSlotCount + utilitySlotCount;
        int playerInvStart = totalUpgradeSlots;
        int playerInvEnd = totalUpgradeSlots + 36; // 36 player inventory slots
        
        if (slot != null && slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < totalUpgradeSlots) {
                // Moving from upgrade slots to player inventory
                if (!this.moveItemStackTo(itemStack2, playerInvStart, playerInvEnd, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to upgrade slots
                // Custom logic for upgrades to distribute them 1 by 1
                boolean isUpgrade = false;
                if (itemStack2.getItem() instanceof com.koudesuk.functionalstorage.item.UpgradeItem upgradeItem) {
                    int start = -1;
                    int end = -1;
                    if (upgradeItem.getType() == com.koudesuk.functionalstorage.item.UpgradeItem.Type.STORAGE) {
                        start = 0;
                        end = storageSlotCount;
                    } else if (upgradeItem.getType() == com.koudesuk.functionalstorage.item.UpgradeItem.Type.UTILITY) {
                        start = storageSlotCount;
                        end = totalUpgradeSlots;
                    }

                    if (start != -1) {
                        isUpgrade = true;
                        for (int i = start; i < end; i++) {
                            Slot targetSlot = this.slots.get(i);
                            if (!targetSlot.hasItem() && targetSlot.mayPlace(itemStack2)) {
                                ItemStack one = itemStack2.split(1);
                                targetSlot.set(one);
                                targetSlot.setChanged();
                            }
                            if (itemStack2.isEmpty())
                                break;
                        }
                    }
                }
                
                if (!isUpgrade && !this.moveItemStackTo(itemStack2, 0, totalUpgradeSlots, false)) {
                     return ItemStack.EMPTY;
                }
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemStack2.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemStack2);
        }

        return itemStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.storageUpgrades.stopOpen(player);
        this.utilityUpgrades.stopOpen(player);
    }
}
