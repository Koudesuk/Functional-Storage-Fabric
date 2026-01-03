package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class ItemControllableDrawerTile<T extends ItemControllableDrawerTile<T>>
        extends ControllableDrawerTile<T> {

    public ItemControllableDrawerTile(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public abstract Storage<ItemVariant> getStorage();

    public abstract int getBaseSize(int lost);

    public static void tick(Level level, BlockPos pos, BlockState state, ItemControllableDrawerTile<?> entity) {
        if (level.isClientSide)
            return;
        entity.serverTick(level, pos, state);
    }

    public void serverTick(Level level, BlockPos pos, BlockState state) {
        // Redstone update - check every 20 ticks like original Forge version
        if (level.getGameTime() % 20 == 0) {
            if (getUtilitySlotAmount() > 0) {
                for (int i = 0; i < this.getUtilityUpgrades().getContainerSize(); i++) {
                    ItemStack stack = this.getUtilityUpgrades().getItem(i);
                    if (!stack.isEmpty() && stack.getItem().equals(FunctionalStorageItems.REDSTONE_UPGRADE)) {
                        // Notify neighbors that redstone signal might have changed
                        level.updateNeighborsAt(pos, state.getBlock());
                        break;
                    }
                }
            }
        }

        // Process other upgrades at the configured rate
        if (level.getGameTime() % FunctionalStorageConfig.UPGRADE_TICK == 0) {
            if (getUtilitySlotAmount() > 0) {
                for (int i = 0; i < this.getUtilityUpgrades().getContainerSize(); i++) {
                    ItemStack stack = this.getUtilityUpgrades().getItem(i);
                    if (!stack.isEmpty()) {
                        if (stack.getItem().equals(FunctionalStorageItems.PULLING_UPGRADE)) {
                            processUpgrade(level, pos, stack, false);
                        } else if (stack.getItem().equals(FunctionalStorageItems.PUSHING_UPGRADE)) {
                            processUpgrade(level, pos, stack, true);
                        } else if (stack.getItem().equals(FunctionalStorageItems.COLLECTOR_UPGRADE)) {
                            Direction direction = UpgradeItem.getDirection(stack);
                            AABB box = new AABB(pos.relative(direction));
                            for (ItemEntity entity : level.getEntitiesOfClass(ItemEntity.class, box)) {
                                if (entity.isAlive()) {
                                    try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                            .openOuter()) {
                                        long inserted = getStorage().insert(ItemVariant.of(entity.getItem()),
                                                entity.getItem().getCount(), transaction);
                                        if (inserted > 0) {
                                            entity.getItem().shrink((int) inserted);
                                            transaction.commit();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void processUpgrade(Level level, BlockPos pos, ItemStack stack, boolean drawerIsSource) {
        Direction direction = UpgradeItem.getDirection(stack);
        Storage<ItemVariant> neighborStorage = ItemStorage.SIDED.find(level, pos.relative(direction),
                direction.getOpposite());
        if (neighborStorage != null) {
            Storage<ItemVariant> drawerStorage = getStorage();
            Storage<ItemVariant> source = drawerIsSource ? drawerStorage : neighborStorage;
            Storage<ItemVariant> destination = drawerIsSource ? neighborStorage : drawerStorage;

            try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                    .openOuter()) {
                StorageUtil.move(source, destination, variant -> true, FunctionalStorageConfig.UPGRADE_PULL_ITEMS,
                        transaction);
                transaction.commit();
            }
        }
    }
}