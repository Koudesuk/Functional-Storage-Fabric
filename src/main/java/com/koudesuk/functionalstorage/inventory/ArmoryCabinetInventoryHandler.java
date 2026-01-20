package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ArmoryCabinetInventoryHandler extends SnapshotParticipant<List<ItemStack>>
        implements Storage<ItemVariant>, Container {

    private List<ItemStack> stackList;

    public ArmoryCabinetInventoryHandler() {
        this.stackList = new ArrayList<>(FunctionalStorageConfig.ARMORY_CABINET_SIZE);
        for (int i = 0; i < FunctionalStorageConfig.ARMORY_CABINET_SIZE; i++) {
            this.stackList.add(ItemStack.EMPTY);
        }
    }

    // SnapshotParticipant implementation for transactional safety
    @Override
    protected List<ItemStack> createSnapshot() {
        List<ItemStack> snapshot = new ArrayList<>(stackList.size());
        for (ItemStack stack : stackList) {
            snapshot.add(stack.copy());
        }
        return snapshot;
    }

    @Override
    protected void readSnapshot(List<ItemStack> snapshot) {
        this.stackList = new ArrayList<>(snapshot.size());
        for (ItemStack stack : snapshot) {
            this.stackList.add(stack.copy());
        }
    }

    @Override
    protected void onFinalCommit() {
        onChange();
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!isCertifiedStack(resource.toStack()))
            return 0;

        // Find empty slot
        for (int i = 0; i < stackList.size(); i++) {
            if (stackList.get(i).isEmpty()) {
                updateSnapshots(transaction);
                stackList.set(i, resource.toStack(1));
                return 1; // Only insert 1
            }
        }
        return 0;
    }

    @Override
    public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        for (int i = 0; i < stackList.size(); i++) {
            ItemStack stack = stackList.get(i);
            if (!stack.isEmpty() && resource.matches(stack)) {
                updateSnapshots(transaction);
                stackList.set(i, ItemStack.EMPTY);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public Iterator<StorageView<ItemVariant>> iterator() {
        return new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < stackList.size();
            }

            @Override
            public StorageView<ItemVariant> next() {
                return new ArmorySlotView(index++);
            }
        };
    }

    private class ArmorySlotView implements StorageView<ItemVariant> {
        private final int slot;

        public ArmorySlotView(int slot) {
            this.slot = slot;
        }

        @Override
        public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
            ItemStack stack = stackList.get(slot);
            if (stack.isEmpty() || !resource.matches(stack))
                return 0;
            long extracted = Math.min(stack.getCount(), maxAmount);
            if (extracted > 0) {
                updateSnapshots(transaction);
                if (extracted == stack.getCount()) {
                    stackList.set(slot, ItemStack.EMPTY);
                } else {
                    ItemStack remaining = stack.copy();
                    remaining.shrink((int) extracted);
                    stackList.set(slot, remaining);
                }
            }
            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return stackList.get(slot).isEmpty();
        }

        @Override
        public ItemVariant getResource() {
            return ItemVariant.of(stackList.get(slot));
        }

        @Override
        public long getAmount() {
            return stackList.get(slot).getCount();
        }

        @Override
        public long getCapacity() {
            return 1;
        }
    }

    @Override
    public int getContainerSize() {
        return FunctionalStorageConfig.ARMORY_CABINET_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : stackList) {
            if (!stack.isEmpty())
                return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return stackList.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = stackList.get(slot);
        if (!stack.isEmpty()) {
            stackList.set(slot, ItemStack.EMPTY);
            onChange();
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = stackList.get(slot);
        stackList.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        stackList.set(slot, stack);
        onChange();
    }

    @Override
    public void setChanged() {
        onChange();
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        stackList.clear();
        for (int i = 0; i < FunctionalStorageConfig.ARMORY_CABINET_SIZE; i++) {
            stackList.add(ItemStack.EMPTY);
        }
    }

    public abstract void onChange();

    private boolean isCertifiedStack(ItemStack stack) {
        if (stack.getMaxStackSize() > 1)
            return false;
        // In 1.21, use Component API to check for custom data instead of hasTag()
        // RecordItem was removed in 1.21, using isEnchantable and isDamageableItem as
        // checks
        return !stack.getComponents().isEmpty() || stack.isDamageableItem() || stack.isEnchantable()
                || stack.getItem() instanceof AnimalArmorItem;
    }

    public CompoundTag serializeNBT(net.minecraft.core.HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        net.minecraft.nbt.ListTag nbtList = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemTag.put("Item",
                        ItemStack.CODEC
                                .encodeStart(net.minecraft.resources.RegistryOps
                                        .create(net.minecraft.nbt.NbtOps.INSTANCE, registries), stack)
                                .result().orElse(new CompoundTag()));
                nbtList.add(itemTag);
            }
        }
        nbt.put("Items", nbtList);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        net.minecraft.nbt.ListTag tagList = nbt.getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
        for (int i = 0; i < tagList.size(); i++) {
            CompoundTag itemTag = tagList.getCompound(i);
            int slot = itemTag.getInt("Slot");
            if (slot >= 0 && slot < this.getContainerSize()) {
                this.setItem(slot,
                        ItemStack.CODEC.parse(net.minecraft.resources.RegistryOps
                                .create(net.minecraft.nbt.NbtOps.INSTANCE, registries), itemTag.getCompound("Item"))
                                .result().orElse(ItemStack.EMPTY));
            }
        }
    }
}
