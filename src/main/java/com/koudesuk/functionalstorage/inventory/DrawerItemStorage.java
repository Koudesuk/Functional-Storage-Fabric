package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.item.DrawerBlockItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * What a drawer item holds, reachable through ITEM_STORAGE and Fabric's FluidStorage.ITEM, like the
 * original's item capabilities (DrawerStackItemHandler and friends). Every call loads the item's drawer into an
 * unplaced tile, works on its handler and swaps the item for one carrying the new contents, so an aborted
 * transaction leaves the item as it was. Only the handler's data in the "Tile" tag is rewritten; upgrades,
 * options and the rest stay as they are.
 */
public final class DrawerItemStorage {

    /**
     * Fabric API 0.92 (1.20.1) has no ItemStorage.ITEM yet. This is the lookup newer Fabric versions define
     * there, with the same id and types, so any mod asking for it gets the same instance.
     */
    public static final ItemApiLookup<Storage<ItemVariant>, ContainerItemContext> ITEM_STORAGE = ItemApiLookup.get(
            new ResourceLocation("fabric", "item_storage"), Storage.asClass(), ContainerItemContext.class);

    private DrawerItemStorage() {
    }

    public static void register() {
        List<ItemLike> itemDrawers = new ArrayList<>();
        FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(itemDrawers::addAll);
        itemDrawers.addAll(FunctionalStorageBlocks.FRAMED_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.COMPACTING_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER);
        ITEM_STORAGE.registerForItems((stack, context) -> new ItemBacked<>(context,
                tile -> tile instanceof ItemControllableDrawerTile<?> drawer ? drawer.getStorage() : null),
                itemDrawers.toArray(new ItemLike[0]));

        List<ItemLike> fluidDrawers = new ArrayList<>(List.of(FunctionalStorageBlocks.FLUID_DRAWER_1,
                FunctionalStorageBlocks.FLUID_DRAWER_2, FunctionalStorageBlocks.FLUID_DRAWER_4));
        fluidDrawers.addAll(FunctionalStorageBlocks.FRAMED_FLUID_DRAWER);
        FluidStorage.ITEM.registerForItems((stack, context) -> new ItemBacked<>(context,
                tile -> tile instanceof FluidDrawerTile drawer ? drawer.getHandler() : null),
                fluidDrawers.toArray(new ItemLike[0]));
    }

    @Nullable
    private static CompoundTag handlerTag(ControllableDrawerTile<?> tile) {
        if (tile instanceof DrawerTile drawer)
            return drawer.getHandler().serializeNBT();
        if (tile instanceof CompactingDrawerTile drawer)
            return drawer.handler.serializeNBT();
        if (tile instanceof SimpleCompactingDrawerTile drawer)
            return drawer.handler.serializeNBT();
        if (tile instanceof FluidDrawerTile drawer)
            return drawer.getHandler().serializeNBT();
        return null;
    }

    // storageOf gives null for a tile that is not the right kind of drawer
    private record ItemBacked<T>(ContainerItemContext context,
            Function<ControllableDrawerTile<?>, Storage<T>> storageOf) implements Storage<T> {

        @Nullable
        private ControllableDrawerTile<?> load() {
            ItemStack stack = context.getItemVariant().toStack();
            return stack.getItem() instanceof DrawerBlockItem item ? item.createTile(stack) : null;
        }

        private long modify(ToLongFunction<Storage<T>> action, TransactionContext transaction) {
            ControllableDrawerTile<?> tile = load();
            Storage<T> storage = tile == null ? null : storageOf.apply(tile);
            if (storage == null)
                return 0;
            long moved = action.applyAsLong(storage);
            CompoundTag handler = moved == 0 ? null : handlerTag(tile);
            if (handler == null)
                return 0;
            ItemStack stack = context.getItemVariant().toStack();
            if (tile.isEverythingEmpty()) {
                stack.removeTagKey("Tile");
            } else {
                CompoundTag data = stack.getTag() != null && stack.getTag().contains("Tile")
                        ? stack.getTag().getCompound("Tile").copy()
                        : new CompoundTag();
                data.put("Handler", handler);
                stack.getOrCreateTag().put("Tile", data);
            }
            return context.exchange(ItemVariant.of(stack), 1, transaction) == 1 ? moved : 0;
        }

        @Override
        public long insert(T resource, long maxAmount, TransactionContext transaction) {
            return modify(storage -> storage.insert(resource, maxAmount, transaction), transaction);
        }

        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            return modify(storage -> storage.extract(resource, maxAmount, transaction), transaction);
        }

        /**
         * Snapshots of the slots. Taking from one goes through this storage, so the item is updated too.
         */
        @Override
        public Iterator<StorageView<T>> iterator() {
            ControllableDrawerTile<?> tile = load();
            Storage<T> storage = tile == null ? null : storageOf.apply(tile);
            List<StorageView<T>> views = new ArrayList<>();
            if (storage != null) {
                for (StorageView<T> view : storage) {
                    views.add(new SlotView<>(this, view.isResourceBlank(), view.getResource(), view.getAmount(),
                            view.getCapacity()));
                }
            }
            return views.iterator();
        }
    }

    private record SlotView<T>(ItemBacked<T> owner, boolean blank, T resource, long amount, long capacity)
            implements StorageView<T> {

        @Override
        public long extract(T resource, long maxAmount, TransactionContext transaction) {
            return resource.equals(this.resource) ? owner.extract(resource, Math.min(maxAmount, amount), transaction) : 0;
        }

        @Override
        public boolean isResourceBlank() {
            return blank;
        }

        @Override
        public T getResource() {
            return resource;
        }

        @Override
        public long getAmount() {
            return amount;
        }

        @Override
        public long getCapacity() {
            return capacity;
        }
    }
}
