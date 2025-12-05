package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class FluidInventoryHandler implements Storage<FluidVariant> {

    public static String BIG_FLUIDS = "BigFluids";
    public static String FLUID = "Fluid";
    public static String AMOUNT = "Amount";

    private final DrawerType type;
    private final List<FluidStackStorage> slots;

    public FluidInventoryHandler(DrawerType type) {
        this.type = type;

        this.slots = new ArrayList<>();
        for (int i = 0; i < type.getSlots(); i++) {
            this.slots.add(new FluidStackStorage(i));
        }
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (FluidStackStorage slot : slots) {
            amount -= slot.insert(resource, amount, transaction);
            if (amount == 0)
                break;
        }
        return maxAmount - amount;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        long amount = maxAmount;
        for (FluidStackStorage slot : slots) {
            amount -= slot.extract(resource, amount, transaction);
            if (amount == 0)
                break;
        }
        return maxAmount - amount;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<StorageView<FluidVariant>> iterator() {
        return (Iterator<StorageView<FluidVariant>>) (Iterator<?>) slots.iterator();
    }

    public class FluidStackStorage
            extends net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage<FluidVariant> {
        private final int slotIndex;

        public FluidStackStorage(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return getSlotLimit(slotIndex);
        }

        @Override
        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            if (isVoid() && isResourceValid(resource) && getAmount() >= getCapacity(resource)) {
                return maxAmount; // Void excess
            }
            return super.insert(resource, maxAmount, transaction);
        }

        // Helper to check validity
        private boolean isResourceValid(FluidVariant resource) {
            if (isLocked() && !getResource().isBlank() && !getResource().equals(resource))
                return false;
            return true;
        }

        @Override
        protected void onFinalCommit() {
            onChange();
        }
    }

    public long getSlotLimit(int slot) {
        // Forge formula: (slotAmount / 64) * 1000 mB per bucket
        // Fabric uses droplets: 81000 droplets per bucket
        long baseBuckets = type.getSlotAmount() / 64; // X_1: 8192/64 = 128 buckets
        if (hasDowngrade())
            baseBuckets = 1; // 1 bucket when downgraded

        long totalBuckets = baseBuckets * getMultiplier();
        return totalBuckets * 81000; // droplets per bucket
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.slots.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(FLUID, this.slots.get(i).getResource().toNbt());
            bigStack.putLong(AMOUNT, this.slots.get(i).getAmount());
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_FLUIDS, items);
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        for (String allKey : nbt.getCompound(BIG_FLUIDS).getAllKeys()) {
            int i = Integer.parseInt(allKey);
            if (i < this.slots.size()) {
                CompoundTag stackTag = nbt.getCompound(BIG_FLUIDS).getCompound(allKey).getCompound(FLUID);
                long amount = nbt.getCompound(BIG_FLUIDS).getCompound(allKey).getLong(AMOUNT);
                this.slots.get(i).variant = FluidVariant.fromNbt(stackTag);
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

    public boolean isFluidValid(int slot, FluidVariant resource) {
        if (slot < 0 || slot >= slots.size())
            return false;
        return slots.get(slot).isResourceValid(resource);
    }

    public FluidVariant getResource(int slot) {
        if (slot < 0 || slot >= slots.size())
            return FluidVariant.blank();
        return slots.get(slot).getResource();
    }
}
