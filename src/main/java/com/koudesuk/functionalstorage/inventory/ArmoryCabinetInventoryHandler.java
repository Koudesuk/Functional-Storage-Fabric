package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HorseArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ArmoryCabinetInventoryHandler implements Storage<ItemVariant>, Container {

    private List<ItemStack> stackList;

    public ArmoryCabinetInventoryHandler() {
        this.stackList = new ArrayList<>(FunctionalStorageConfig.ARMORY_CABINET_SIZE);
        for (int i = 0; i < FunctionalStorageConfig.ARMORY_CABINET_SIZE; i++) {
            this.stackList.add(ItemStack.EMPTY);
        }
    }

    @Override
    public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
        if (!isCertifiedStack(resource.toStack()))
            return 0;

        // Find empty slot
        for (int i = 0; i < stackList.size(); i++) {
            if (stackList.get(i).isEmpty()) {
                updateSnapshots(transaction);
                stackList.set(i, resource.toStack((int) 1));
                onChange();
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
                onChange();
                return 1;
            }
        }
        return 0;
    }

    private void updateSnapshots(TransactionContext transaction) {
        transaction.addCloseCallback((t, result) -> {
            if (result.wasAborted()) {
                // TODO: Implement rollback if needed
            }
        });
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
                    stack.shrink((int) extracted);
                }
                onChange();
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
        return stack.hasTag() || stack.isDamageableItem() || stack.isEnchantable()
                || stack.getItem() instanceof RecordItem || stack.getItem() instanceof HorseArmorItem;
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < this.stackList.size(); i++) {
            ItemStack stack = this.stackList.get(i);
            if (!stack.isEmpty()) {
                compoundTag.put(i + "", stack.save(new CompoundTag()));
            }
        }
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        for (String allKey : nbt.getAllKeys()) {
            int pos = Integer.parseInt(allKey);
            if (pos < this.stackList.size()) {
                this.stackList.set(pos, ItemStack.of(nbt.getCompound(allKey)));
            }
        }
    }
}
