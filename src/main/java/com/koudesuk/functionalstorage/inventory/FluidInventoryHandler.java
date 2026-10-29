package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.StorageTags;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class FluidInventoryHandler implements Storage<FluidVariant> {

    public static String BIG_FLUIDS = "BigFluids";
    public static String FLUID = "Fluid";
    public static String AMOUNT = "Amount";
    public static String LOCKED = "Locked";

    private final DrawerType type;
    private final List<FluidStackStorage> slots;
    private final FluidVariant[] filterStack;

    public FluidInventoryHandler(DrawerType type) {
        this.type = type;

        this.slots = new ArrayList<>();
        this.filterStack = new FluidVariant[type.getSlots()];
        for (int i = 0; i < type.getSlots(); i++) {
            this.slots.add(new FluidStackStorage(i));
            this.filterStack[i] = FluidVariant.blank();
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

    /**
     * Get the storage for a specific slot. Used for targeted fluid operations.
     * 
     * @param slot The slot index
     * @return The storage for the slot, or null if invalid
     */
    public Storage<FluidVariant> getSlotStorage(int slot) {
        if (slot < 0 || slot >= slots.size())
            return null;
        return slots.get(slot);
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
            // Only fluid this slot would take is voided, never another fluid that should go elsewhere
            if (isVoid() && canInsert(resource) && getAmount() >= getCapacity(resource)) {
                return maxAmount; // Void excess
            }
            return super.insert(resource, maxAmount, transaction);
        }

        /**
         * Whether the slot takes this fluid at all, like the original's isFluidValid: never a denied
         * fluid, and while the drawer is locked only the slot's filter. A locked slot without a
         * filter takes nothing; right clicking it with a fluid container sets one (FluidDrawerTile).
         */
        private boolean isResourceValid(FluidVariant resource) {
            if (StorageTags.isFluidDrawerDenied(resource.getFluid()))
                return false;
            return !isLocked() || resource.equals(filterStack[slotIndex]);
        }

        /**
         * Called by SingleVariantStorage.insert() before inserting.
         */
        @Override
        protected boolean canInsert(FluidVariant variant) {
            return isResourceValid(variant) && (getResource().isBlank() || getResource().equals(variant));
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
        long droplets = totalBuckets * 81000; // droplets per bucket
        double factor = getSizeFactor();
        return factor == 1 ? droplets : (long) Math.floor(droplets * factor);
    }

    /**
     * Data-driven size upgrades (the original's fluid_storage_modifier), as a factor; 1 without any.
     */
    public double getSizeFactor() {
        return 1;
    }

    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag compoundTag = new CompoundTag();
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < this.slots.size(); i++) {
            CompoundTag bigStack = new CompoundTag();
            bigStack.put(FLUID, FluidVariant.CODEC.encodeStart(NbtOps.INSTANCE, this.slots.get(i).getResource())
                    .result().orElse(new CompoundTag()));
            bigStack.putLong(AMOUNT, this.slots.get(i).getAmount());
            bigStack.put(LOCKED, FluidVariant.CODEC.encodeStart(NbtOps.INSTANCE, this.filterStack[i]).result()
                    .orElse(new CompoundTag()));
            items.put(i + "", bigStack);
        }
        compoundTag.put(BIG_FLUIDS, items);
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag bigFluids = nbt.getCompoundOrEmpty(BIG_FLUIDS);
        for (String allKey : bigFluids.keySet()) {
            int i = Integer.parseInt(allKey);
            if (i < this.slots.size()) {
                CompoundTag slotTag = bigFluids.getCompoundOrEmpty(allKey);
                long amount = slotTag.getLongOr(AMOUNT, 0L);
                if (slotTag.contains(FLUID)) {
                    this.slots.get(i).variant = FluidVariant.CODEC.parse(NbtOps.INSTANCE, slotTag.get(FLUID)).result()
                            .orElse(FluidVariant.blank());
                } else {
                    this.slots.get(i).variant = FluidVariant.blank();
                }

                this.slots.get(i).amount = amount;
                if (slotTag.contains(LOCKED)) {
                    this.filterStack[i] = FluidVariant.CODEC.parse(NbtOps.INSTANCE, slotTag.get(LOCKED)).result()
                            .orElse(FluidVariant.blank());
                }
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

    public long getAmount(int slot) {
        if (slot < 0 || slot >= slots.size())
            return 0;
        return slots.get(slot).getAmount();
    }

    /**
     * Lock the handler by copying current fluids to filterStack.
     * Matching Forge BigFluidHandler.lockHandler() implementation.
     */
    public void lockHandler() {
        for (int i = 0; i < this.slots.size(); i++) {
            FluidVariant current = this.slots.get(i).getResource();
            this.filterStack[i] = current.isBlank() ? FluidVariant.blank() : current;
        }
    }

    /**
     * Get the filter stack for a slot (used for rendering locked empty slots).
     */
    public FluidVariant getFilterStack(int slot) {
        if (slot < 0 || slot >= filterStack.length)
            return FluidVariant.blank();
        return filterStack[slot];
    }

    /**
     * Sets the only fluid a slot takes while the drawer is locked, like the original's setFilterResource.
     */
    public void setFilterStack(int slot, FluidVariant fluid) {
        if (slot >= 0 && slot < filterStack.length)
            filterStack[slot] = fluid;
    }
}
