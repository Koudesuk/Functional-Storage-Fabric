package com.koudesuk.functionalstorage.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Per slot access shared by the item drawer handlers, so slot clicks are handled in one place.
 */
public interface SlottedItemStorage {

    ItemVariant getResource(int slot);

    long insertIntoSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction);
}
