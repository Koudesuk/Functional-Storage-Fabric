package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.inventory.SlottedItemStorage;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public abstract class ItemControllableDrawerTile<T extends ItemControllableDrawerTile<T>>
        extends ControllableDrawerTile<T> {

    private UUID lastClickPlayer;
    private long lastClickTime;

    public ItemControllableDrawerTile(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    /**
     * Right click on a front slot. The held stack goes in; a second click within 500 ms also
     * pulls every stack of the slot's item out of the main inventory, whatever is in hand.
     */
    protected void useSlot(SlottedItemStorage storage, Player player, InteractionHand hand, int slot) {
        long now = System.currentTimeMillis();
        boolean doubleClick = player.getUUID().equals(lastClickPlayer) && now - lastClickTime < 500;
        lastClickPlayer = player.getUUID();
        lastClickTime = now;

        insertIntoSlot(storage, slot, player.getItemInHand(hand));
        ItemVariant resource = storage.getResource(slot);
        if (!doubleClick || resource.isBlank())
            return;
        for (ItemStack stack : player.getInventory().items) {
            if (resource.matches(stack))
                insertIntoSlot(storage, slot, stack);
        }
    }

    private static void insertIntoSlot(SlottedItemStorage storage, int slot, ItemStack stack) {
        if (stack.isEmpty())
            return;
        try (Transaction transaction = Transaction.openOuter()) {
            stack.shrink((int) storage.insertIntoSlot(slot, ItemVariant.of(stack), stack.getCount(), transaction));
            transaction.commit();
        }
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
        if (level.getGameTime() % FunctionalStorageConfig.UPGRADE_TICK.get() == 0) {
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
                                if (entity.isAlive() && !entity.getItem().isEmpty()) {
                                    try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                            .openOuter()) {
                                        long inserted = getStorage().insert(ItemVariant.of(entity.getItem()),
                                                Math.min(entity.getItem().getCount(),
                                                        FunctionalStorageConfig.UPGRADE_COLLECTOR_ITEMS.get()),
                                                transaction);
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
                StorageUtil.move(source, destination, variant -> true, drawerIsSource
                        ? FunctionalStorageConfig.UPGRADE_PUSH_ITEMS.get()
                        : FunctionalStorageConfig.UPGRADE_PULL_ITEMS.get(), transaction);
                transaction.commit();
            }
        }
    }
}