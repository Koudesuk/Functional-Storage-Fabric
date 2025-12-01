package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.util.CompactingUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class CompactingInventoryHandler extends SnapshotParticipant<Integer> implements Storage<ItemVariant> {

    public static String PARENT = "Parent";
    public static String BIG_ITEMS = "BigItems";
    public static String STACK = "Stack";
    public static String AMOUNT = "Amount";

    public int totalAmount;

    private int amount;
    private ItemStack parent;
    private List<CompactingUtil.Result> resultList;
    private int slots;
    private final List<CompactingStackStorage> storageSlots;

    public CompactingInventoryHandler(int slots) {
        this.resultList = new ArrayList<>();
        this.slots = slots;
        this.totalAmount = 512;
        for (int i = 0; i < slots - 1; i++) {
            this.totalAmount *= 9;
        }
        for (int i = 0; i < slots; i++) {
            this.resultList.add(i, new CompactingUtil.Result(ItemStack.EMPTY, 1));
        }
        this.parent = ItemStack.EMPTY;
        this.storageSlots = new ArrayList<>();
        for (int i = 0; i < slots; i++) {
            this.storageSlots.add(new CompactingStackStorage(i));
        }
    }

    @Override
    protected Integer createSnapshot() {
        return amount;
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        this.amount = snapshot;
        onChange();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (CompactingStackStorage slot : storageSlots) {
            amount -= slot.insert(resource, amount, transaction);
            if (amount == 0)
                break;
        }
        return maxAmount - amount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (CompactingStackStorage slot : storageSlots) {
            amount -= slot.extract(resource, amount, transaction);
            if (amount == 0)
                break;
        }
        return maxAmount - amount;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<StorageView<ItemVariant>> iterator() {
        return (Iterator<StorageView<ItemVariant>>) (Iterator<?>) storageSlots.iterator();
    }

    public int getSlots() {
        if (isVoid())
            return this.slots + 1;
        return this.slots;
    }

    public ItemStack getStackInSlot(int slot) {
        if (slot >= this.slots)
            return ItemStack.EMPTY;
        CompactingUtil.Result bigStack = this.resultList.get(slot);
        if (bigStack.getResult().isEmpty())
            return ItemStack.EMPTY;
        ItemStack copied = bigStack.getResult().copy();
        copied.setCount(isCreative() ? Integer.MAX_VALUE : this.amount / bigStack.getNeeded());
        return copied;
    }

    public int getSlotLimit(int slot) {
        if (isCreative())
            return Integer.MAX_VALUE;
        if (slot == this.slots)
            return Integer.MAX_VALUE;
        int total = totalAmount;
        if (hasDowngrade())
            total = 64 * 9 * 9;
        if (this.resultList.get(slot).getNeeded() == 0)
            return 0;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(
                (double) (total * (long) getMultiplier()) / this.resultList.get(slot).getNeeded()));
    }

    public int getSlotLimitBase(int slot) {
        if (slot == this.slots)
            return Integer.MAX_VALUE;
        int total = totalAmount;
        if (hasDowngrade())
            total = 64 * 9 * 9;
        if (this.resultList.get(slot).getNeeded() == 0)
            return 0;
        return (int) Math.min(Integer.MAX_VALUE, Math.floor(total / this.resultList.get(slot).getNeeded()));
    }

    public boolean isItemValid(int slot, ItemStack stack) {
        return isSetup() && !stack.isEmpty();
    }

    public boolean isSetup() {
        return !this.resultList.get(this.resultList.size() - 1).getResult().isEmpty();
    }

    public void setup(CompactingUtil compactingUtil) {
        this.resultList = compactingUtil.getResults();
        this.parent = compactingUtil.getResults().get(0).getResult();
        if (this.parent.isEmpty()) {
            this.parent = compactingUtil.getResults().get(1).getResult();
        }
        if (this.parent.isEmpty() && compactingUtil.getResults().size() >= 3) {
            this.parent = compactingUtil.getResults().get(2).getResult();
        }
        onChange();
    }

    public void reset() {
        if (isLocked())
            return;
        this.resultList.forEach(result -> {
            result.setResult(ItemStack.EMPTY);
            result.setNeeded(1);
        });
    }

    public int getAmount() {
        return amount;
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.put(PARENT, this.getParent().save(new CompoundTag()));
        compoundTag.putInt(AMOUNT, this.amount);
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.resultList.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, this.resultList.get(i).getResult().save(new CompoundTag()));
            bigStack.putInt(AMOUNT, this.resultList.get(i).getNeeded());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.parent = ItemStack.of(nbt.getCompound(PARENT));
        this.amount = nbt.getInt(AMOUNT);
        for (String allKey : nbt.getCompound(BIG_ITEMS).getAllKeys()) {
            int index = Integer.parseInt(allKey);
            if (index < this.resultList.size()) {
                this.resultList.get(index)
                        .setResult(ItemStack.of(nbt.getCompound(BIG_ITEMS).getCompound(allKey).getCompound(STACK)));
                this.resultList.get(index)
                        .setNeeded(Math.max(1, nbt.getCompound(BIG_ITEMS).getCompound(allKey).getInt(AMOUNT)));
            }
        }
    }

    public abstract void onChange();

    public abstract int getMultiplier();

    public abstract boolean isVoid();

    public List<CompactingUtil.Result> getResultList() {
        return resultList;
    }

    public ItemStack getParent() {
        return parent;
    }

    public abstract boolean hasDowngrade();

    public abstract boolean isCreative();

    public abstract boolean isLocked();

    public class CompactingStackStorage implements StorageView<ItemVariant> {
        private final int slotIndex;

        public CompactingStackStorage(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (slotIndex >= resultList.size())
                return 0;
            CompactingUtil.Result result = resultList.get(slotIndex);
            if (result.getResult().isEmpty() || !resource.matches(result.getResult()))
                return 0;

            long stackAmount = maxAmount * result.getNeeded();
            if (!isCreative() && stackAmount > CompactingInventoryHandler.this.amount) {
                stackAmount = (long) (Math.floor((double) CompactingInventoryHandler.this.amount / result.getNeeded())
                        * result.getNeeded());
            }

            if (stackAmount > 0) {
                updateSnapshots(transaction);
                if (!isCreative()) {
                    CompactingInventoryHandler.this.amount -= stackAmount;
                    if (CompactingInventoryHandler.this.amount == 0)
                        reset();
                }
                onChange();
                return maxAmount;
            }
            return 0;
        }

        @Override
        public boolean isResourceBlank() {
            return getResource().isBlank();
        }

        @Override
        public ItemVariant getResource() {
            if (slotIndex >= resultList.size())
                return ItemVariant.blank();
            return ItemVariant.of(resultList.get(slotIndex).getResult());
        }

        @Override
        public long getAmount() {
            if (slotIndex >= resultList.size())
                return 0;
            CompactingUtil.Result result = resultList.get(slotIndex);
            if (result.getResult().isEmpty())
                return 0;
            if (isCreative())
                return Integer.MAX_VALUE;
            return CompactingInventoryHandler.this.amount / result.getNeeded();
        }

        @Override
        public long getCapacity() {
            return getSlotLimit(slotIndex);
        }

        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (slotIndex >= resultList.size())
                return 0;
            // Only insert into the last slot (the most compacted item) or if it matches
            // existing items
            // Actually for compacting drawers, we usually insert the items and they get
            // converted.
            // But here we are exposing views.
            // If we insert into a view, we should probably handle it.
            // But usually insertion happens via the handler's insert method which delegates
            // to slots.

            // Logic for insertion:
            // 1. Check if item is valid for this slot (matches result)
            // 2. Calculate how much we can fit
            // 3. Update amount

            CompactingUtil.Result result = resultList.get(slotIndex);
            if (isSetup()) {
                if (result.getResult().isEmpty())
                    return 0; // Should not happen if setup
                if (!resource.matches(result.getResult()))
                    return 0;
            } else {
                // Not setup, we can setup if we are inserting into the last slot (usually base
                // item)
                // Or maybe we allow inserting any item and it tries to find a compacting
                // recipe?
                // For now let's assume we only insert if it matches or if it's empty and we can
                // setup (handled in Tile)
                return 0;
            }

            long limit = getCapacity();
            long current = getAmount();
            long inserted = Math.min(maxAmount, limit - current);

            if (inserted > 0) {
                updateSnapshots(transaction);
                if (!isCreative()) {
                    CompactingInventoryHandler.this.amount += inserted * result.getNeeded();
                }
                onChange();
                return inserted;
            }
            return 0;
        }
    }
}
