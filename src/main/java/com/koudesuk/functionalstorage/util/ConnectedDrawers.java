package com.koudesuk.functionalstorage.util;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ConnectedDrawers {

    private StorageControllerTile controllerTile;
    private List<Long> connectedDrawers;
    private List<Storage<ItemVariant>> itemHandlers;
    private Level level;
    private int extensions;
    private VoxelShape cachedVoxelShape;

    public ConnectedDrawers(Level level, StorageControllerTile controllerTile) {
        this.controllerTile = controllerTile;
        this.connectedDrawers = new ArrayList<>();
        this.itemHandlers = new ArrayList<>();
        this.level = level;
        this.extensions = 0;
        this.cachedVoxelShape = null;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public void rebuild() {
        this.itemHandlers = new ArrayList<>();
        this.extensions = 0;
        if (level != null && !level.isClientSide()) {
            var extraRange = controllerTile.getStorageMultiplier();
            if (extraRange == 1) {
                extraRange = 0;
            }
            var area = new AABB(controllerTile.getBlockPos())
                    .inflate(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE + extraRange);
            this.connectedDrawers.removeIf(
                    aLong -> !(level.getBlockEntity(BlockPos.of(aLong)) instanceof ControllableDrawerTile<?>)
                            || !area.contains(Vec3.atCenterOf(BlockPos.of(aLong))));
            this.connectedDrawers.sort(
                    Comparator.comparingDouble(value -> BlockPos.of(value).distSqr(controllerTile.getBlockPos())));
            for (Long connectedDrawer : this.connectedDrawers) {
                BlockPos pos = BlockPos.of(connectedDrawer);
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof StorageControllerTile)
                    continue;
                // Extension check omitted for now

                if (entity instanceof ItemControllableDrawerTile<?> drawer) {
                    // Get the storage directly from the drawer (returns BigInventoryHandler)
                    Storage<ItemVariant> storage = drawer.getStorage();
                    if (storage != null) {
                        this.itemHandlers.add(storage);
                    }
                }
            }
        }

        if (this.controllerTile.getInventoryHandler() != null) {
            this.controllerTile.getInventoryHandler().invalidate();
        }
    }

    public void rebuildShapes() {
        this.cachedVoxelShape = Shapes.create(new AABB(controllerTile.getBlockPos()));
        for (Long connectedDrawer : this.connectedDrawers) {
            this.cachedVoxelShape = Shapes.or(this.cachedVoxelShape,
                    Shapes.create(new AABB(BlockPos.of(connectedDrawer))));
        }
    }

    public CompoundTag serializeNBT() {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < this.connectedDrawers.size(); i++) {
            compoundTag.putLong(i + "", this.connectedDrawers.get(i));
        }
        return compoundTag;
    }

    public void deserializeNBT(CompoundTag nbt) {
        this.connectedDrawers = new ArrayList<>();
        for (String allKey : nbt.getAllKeys()) {
            connectedDrawers.add(nbt.getLong(allKey));
        }
        rebuild();
        if (controllerTile.getLevel() != null && controllerTile.getLevel().isClientSide) {
            rebuildShapes();
        }
    }

    public List<Long> getConnectedDrawers() {
        return connectedDrawers;
    }

    public List<Storage<ItemVariant>> getItemHandlers() {
        return itemHandlers;
    }

    public int getExtensions() {
        return extensions;
    }

    public VoxelShape getCachedVoxelShape() {
        return cachedVoxelShape;
    }
}
