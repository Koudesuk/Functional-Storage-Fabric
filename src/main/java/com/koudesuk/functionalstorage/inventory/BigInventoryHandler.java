package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// Actually, Fabric doesn't have INBTSerializable in the same package.
// I should just implement the methods or use a custom interface.
// But BigInventoryHandler stub had INBTSerializable<CompoundTag> in imports?
// No, I removed it in the stub.
// I'll add `implements net.fabricmc.fabric.api.util.NbtSerializable` if available, or just keep methods.
// But I'm overriding serializeNBT/deserializeNBT which implies an interface.
// I'll check if I imported INBTSerializable.
// The stub had `public abstract class BigInventoryHandler implements Storage<ItemVariant> {`
// So I'm not implementing INBTSerializable.
// I'll just add the methods.
// But I used `@Override` annotation.
// I should remove `@Override` if not implementing interface.
// Or implement a custom interface.

public abstract class BigInventoryHandler implements Storage<ItemVariant> {

    public static String BIG_ITEMS = "BigItems";
    public static String STACK = "Stack";
    public static String AMOUNT = "Amount";

    private final DrawerType type;
    private final List<BigStackStorage> slots;

    public BigInventoryHandler(DrawerType type) {
        this.type = type;

        this.slots = new ArrayList<>();
        for (int i = 0; i < type.getSlots(); i++) {
            this.slots.add(new BigStackStorage(i));
        }
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (BigStackStorage slot : slots) {
            amount -= slot.insert(resource, amount, transaction);
            if (amount == 0)
                break;
        }
        return maxAmount - amount;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (BigStackStorage slot : slots) {
            amount -= slot.extract(resource, amount, transaction);
            if (amount == 0)
                break;
        }
        return maxAmount - amount;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return (Iterator<StorageView<ItemVariant>>) (Iterator<?>) slots.iterator();
    }

    public class BigStackStorage
            extends net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage<ItemVariant> {
        private final int slotIndex;

        public BigStackStorage(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        @Override
        protected ItemVariant getBlankVariant() {
            return ItemVariant.blank();
        }

        @Override
        protected long getCapacity(ItemVariant variant) {
            return getSlotLimit(slotIndex);
        }

        @Override
        public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (isLocked() && variant.isBlank()) {
                return 0;
            }
            if (isVoid() && isResourceValid(resource) && getAmount() >= getCapacity(resource)) {
                return maxAmount; // Void excess
            }
            return super.insert(resource, maxAmount, transaction);
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            if (resource.equals(variant)) {
                long extracted = Math.min(amount, maxAmount);
                if (extracted > 0) {
                    updateSnapshots(transaction);
                    amount -= extracted;
                    if (amount == 0) {
                        if (!isLocked()) {
                            variant = getBlankVariant();
                        }
                    }
                    return extracted;
                }
            }
            return 0;
        }

        // Helper to check validity
        private boolean isResourceValid(ItemVariant resource) {
            if (!getResource().isBlank() && !getResource().equals(resource))
                return false;
            if (isLocked() && !getResource().isBlank() && !getResource().equals(resource))
                return false;
            // Add more validation logic
            return true;
        }

        @Override
        protected void onFinalCommit() {
            onChange();
        }
    }

    public void setLockedItem(int slot, ItemVariant variant) {
        if (slot >= 0 && slot < slots.size()) {
            this.slots.get(slot).variant = variant;
            this.onChange();
        }
    }

    public void unlock() {
        for (BigStackStorage slot : slots) {
            if (slot.getAmount() == 0) {
                slot.variant = ItemVariant.blank();
            }
        }
        this.onChange();
    }

    public int getSlotLimit(int slot) {
        long capacity = type.getSlotAmount();
        if (hasDowngrade())
            capacity = 64;
        return (int) Math.min(Integer.MAX_VALUE, capacity * getMultiplier());
    }

    public List<BigStack> getStoredStacks() {
        List<BigStack> stacks = new ArrayList<>();
        for (BigStackStorage slot : slots) {
            stacks.add(new BigStack(slot.getResource().toStack(), (int) slot.getAmount()));
        }
        return stacks;
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.slots.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(STACK, this.slots.get(i).getResource().toStack().save(new CompoundTag()));
            bigStack.putInt(AMOUNT, (int) this.slots.get(i).getAmount());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_ITEMS, items);
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        for (String allKey : nbt.getCompound(BIG_ITEMS).getAllKeys()) {
            int i = Integer.parseInt(allKey);
            if (i < this.slots.size()) {
                CompoundTag stackTag = nbt.getCompound(BIG_ITEMS).getCompound(allKey).getCompound(STACK);
                int amount = nbt.getCompound(BIG_ITEMS).getCompound(allKey).getInt(AMOUNT);
                this.slots.get(i).variant = ItemVariant.of(ItemStack.of(stackTag));
                this.slots.get(i).amount = amount;
            }
        }
    }

    public abstract void onChange();

    public abstract int getMultiplier();

    public abstract boolean isVoid();

    public abstract boolean hasDowngrade();

    public abstract boolean isLocked();

    public abstract boolean isCreative();

    public boolean isItemValid(int slot, ItemVariant resource) {
        if (slot < 0 || slot >= slots.size())
            return false;
        return slots.get(slot).isResourceValid(resource);
    }

    public ItemVariant getResource(int slot) {
        if (slot < 0 || slot >= slots.size())
            return ItemVariant.blank();
        return slots.get(slot).getResource();
    }

    /**
     * Insert into a specific slot. This is required for multi-slot drawers where
     * the user clicks on a specific slot to insert items.
     * 
     * @param slot        The slot index to insert into
     * @param resource    The item variant to insert
     * @param maxAmount   The maximum amount to insert
     * @param transaction The transaction context
     * @return The amount actually inserted
     */
    public long insertIntoSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (slot < 0 || slot >= slots.size())
            return 0;
        return slots.get(slot).insert(resource, maxAmount, transaction);
    }

    /**
     * Extract from a specific slot. This is required for multi-slot drawers where
     * the user clicks on a specific slot to extract items.
     * 
     * @param slot        The slot index to extract from
     * @param resource    The item variant to extract
     * @param maxAmount   The maximum amount to extract
     * @param transaction The transaction context
     * @return The amount actually extracted
     */
    public long extractFromSlot(int slot, ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (slot < 0 || slot >= slots.size())
            return 0;
        return slots.get(slot).extract(resource, maxAmount, transaction);
    }

    public boolean isEverythingEmpty() {
        for (BigStackStorage slot : slots) {
            if (slot.getAmount() > 0) {
                return false;
            }
        }
        return true;
    }

    public static class BigStack {
        private ItemStack stack;
        private ItemStack slotStack;
        private int amount;

        public BigStack(ItemStack stack, int amount) {
            this.stack = stack.copy();
            this.amount = amount;
            this.slotStack = stack.copy();
            this.slotStack.setCount(Math.min(amount, stack.getMaxStackSize()));
        }

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack.copy();
            this.slotStack = stack.copy();
            this.slotStack.setCount(Math.min(amount, stack.getMaxStackSize()));
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
            this.slotStack.setCount(Math.min(amount, stack.getMaxStackSize()));
        }
    }
}
