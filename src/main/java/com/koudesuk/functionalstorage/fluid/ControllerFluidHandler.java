package com.koudesuk.functionalstorage.fluid;

import com.koudesuk.functionalstorage.util.ConnectedDrawers;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Controller fluid handler that aggregates all fluid drawers connected to a
 * storage controller.
 * Equivalent to Forge's ControllerFluidHandler.
 */
public abstract class ControllerFluidHandler implements Storage<FluidVariant> {

    public ControllerFluidHandler() {
    }

    public abstract ConnectedDrawers getDrawers();

    public void invalidate() {
        // Called when the connected drawers are rebuilt
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank())
            return 0;

        long remaining = maxAmount;

        // First pass: try to insert into slots that already contain this fluid
        for (Storage<FluidVariant> storage : getDrawers().getFluidHandlers()) {
            if (storage instanceof ControllerFluidHandler)
                continue;

            for (StorageView<FluidVariant> view : storage) {
                if (!view.isResourceBlank() && view.getResource().equals(resource)) {
                    long inserted = storage.insert(resource, remaining, transaction);
                    remaining -= inserted;
                    if (remaining <= 0)
                        return maxAmount;
                }
            }
        }

        // Second pass: try to insert into empty slots
        for (Storage<FluidVariant> storage : getDrawers().getFluidHandlers()) {
            if (storage instanceof ControllerFluidHandler)
                continue;

            for (StorageView<FluidVariant> view : storage) {
                if (view.isResourceBlank()) {
                    long inserted = storage.insert(resource, remaining, transaction);
                    remaining -= inserted;
                    if (remaining <= 0)
                        return maxAmount;
                }
            }
        }

        return maxAmount - remaining;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (resource.isBlank())
            return 0;

        long remaining = maxAmount;

        for (Storage<FluidVariant> storage : getDrawers().getFluidHandlers()) {
            if (storage instanceof ControllerFluidHandler)
                continue;

            long extracted = storage.extract(resource, remaining, transaction);
            remaining -= extracted;
            if (remaining <= 0)
                return maxAmount;
        }

        return maxAmount - remaining;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        List<Iterator<StorageView<FluidVariant>>> iterators = new ArrayList<>();
        for (Storage<FluidVariant> storage : getDrawers().getFluidHandlers()) {
            iterators.add(storage.iterator());
        }

        return new Iterator<StorageView<FluidVariant>>() {
            int index = 0;
            Iterator<StorageView<FluidVariant>> current = iterators.isEmpty() ? null : iterators.get(0);

            @Override
            public boolean hasNext() {
                while (current != null) {
                    if (current.hasNext())
                        return true;
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
            public StorageView<FluidVariant> next() {
                if (!hasNext())
                    throw new java.util.NoSuchElementException();
                return current.next();
            }
        };
    }
}
