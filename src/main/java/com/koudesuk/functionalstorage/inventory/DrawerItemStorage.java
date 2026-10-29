package com.koudesuk.functionalstorage.inventory;

import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.item.DrawerBlockItem;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToLongFunction;

/**
 * What a drawer item holds, reachable through Fabric's ItemStorage.ITEM and FluidStorage.ITEM, like the
 * original's item capabilities (DrawerStackItemHandler and friends). Every call loads the item's drawer into an
 * unplaced tile, works on its handler and swaps the item for one carrying the new contents, so an aborted
 * transaction leaves the item as it was. Only the handler's data in the tile component is rewritten; upgrades,
 * options and the rest stay as they are.
 */
public final class DrawerItemStorage {

    // Stored items need the world's registries (enchantments and the like), so this only works while a server runs
    @Nullable
    private static HolderLookup.Provider registries;

    private DrawerItemStorage() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> registries = server.registryAccess());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> registries = null);

        List<ItemLike> itemDrawers = new ArrayList<>();
        FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(itemDrawers::addAll);
        itemDrawers.addAll(FunctionalStorageBlocks.FRAMED_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.COMPACTING_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER);
        itemDrawers.add(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER);
        ItemStorage.ITEM.registerForItems((stack, context) -> new ItemBacked<>(context,
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
    private static CompoundTag handlerTag(ControllableDrawerTile<?> tile, HolderLookup.Provider registries) {
        if (tile instanceof DrawerTile drawer)
            return drawer.getHandler().serializeNBT(registries);
        if (tile instanceof CompactingDrawerTile drawer)
            return drawer.handler.serializeNBT(registries);
        if (tile instanceof SimpleCompactingDrawerTile drawer)
            return drawer.handler.serializeNBT(registries);
        if (tile instanceof FluidDrawerTile drawer)
            return drawer.getHandler().serializeNBT(registries);
        return null;
    }

    // storageOf gives null for a tile that is not the right kind of drawer
    private record ItemBacked<T>(ContainerItemContext context,
            Function<ControllableDrawerTile<?>, Storage<T>> storageOf) implements Storage<T> {

        @Nullable
        private ControllableDrawerTile<?> load(@Nullable HolderLookup.Provider lookup) {
            ItemStack stack = context.getItemVariant().toStack();
            return lookup != null && stack.getItem() instanceof DrawerBlockItem item
                    ? item.createTile(stack, lookup)
                    : null;
        }

        private long modify(ToLongFunction<Storage<T>> action, TransactionContext transaction) {
            HolderLookup.Provider lookup = registries;
            ControllableDrawerTile<?> tile = load(lookup);
            Storage<T> storage = tile == null ? null : storageOf.apply(tile);
            if (storage == null)
                return 0;
            long moved = action.applyAsLong(storage);
            CompoundTag handler = moved == 0 ? null : handlerTag(tile, lookup);
            if (handler == null)
                return 0;
            ItemStack stack = context.getItemVariant().toStack();
            if (tile.isEverythingEmpty()) {
                stack.remove(FSAttachments.TILE);
            } else {
                CompoundTag data = stack.getOrDefault(FSAttachments.TILE, new CompoundTag()).copy();
                data.put("Handler", handler);
                stack.set(FSAttachments.TILE, data);
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
            ControllableDrawerTile<?> tile = load(registries);
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
