package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.util.ConnectedDrawers;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ControllerInventoryHandler implements Storage<ItemVariant> {

    public ControllerInventoryHandler() {
    }

    public abstract ConnectedDrawers getDrawers();
    
    public void invalidate() {
        
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (Storage<ItemVariant> storage : getDrawers().getItemHandlers()) {
            amount -= storage.insert(resource, amount, transaction);
            if (amount == 0) break;
        }
        return maxAmount - amount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (Storage<ItemVariant> storage : getDrawers().getItemHandlers()) {
            amount -= storage.extract(resource, amount, transaction);
            if (amount == 0) break;
        }
        return maxAmount - amount;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        List<Iterator<StorageView<ItemVariant>>> iterators = new ArrayList<>();
        for (Storage<ItemVariant> storage : getDrawers().getItemHandlers()) {
            iterators.add(storage.iterator());
        }
        
        return new Iterator<StorageView<ItemVariant>>() {
            int index = 0;
            Iterator<StorageView<ItemVariant>> current = iterators.isEmpty() ? null : iterators.get(0);

            @Override
            public boolean hasNext() {
                while (current != null) {
                    if (current.hasNext()) return true;
                    index++;
                    if (index < iterators.size()) {
                        current = iterators.get(index);
                    } else {
                        current = null;
                    }
                }
                return false;
            }

            @Override
            public StorageView<ItemVariant> next() {
                if (!hasNext()) throw new java.util.NoSuchElementException();
                return current.next();
            }
        };
    }
}
