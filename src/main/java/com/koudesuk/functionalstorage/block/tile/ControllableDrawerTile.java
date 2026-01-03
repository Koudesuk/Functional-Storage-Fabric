package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ControllableDrawerTile<T extends ControllableDrawerTile<T>> extends BlockEntity {

    // Cache mechanism from original Forge version
    private boolean needsUpgradeCache = true;
    private int mult = 1;

    private final SimpleContainer storageUpgrades;
    private final SimpleContainer utilityUpgrades;
    private final DrawerOptions drawerOptions;
    private BlockPos controllerPos;

    public ControllableDrawerTile(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.drawerOptions = new DrawerOptions();
        this.storageUpgrades = new SimpleContainer(getStorageSlotAmount()) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return stack.getItem() instanceof UpgradeItem
                        && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.STORAGE;
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack removedStack = getItem(slot);
                // Only check capacity for Drawers (which have limited storage), not Controllers
                // (which use upgrades for radius)
                if (!removedStack.isEmpty()
                        && removedStack.getItem() instanceof StorageUpgradeItem
                        && !(ControllableDrawerTile.this instanceof com.koudesuk.functionalstorage.block.tile.StorageControllerTile)) {
                    // Calculate what the new capacity would be after removing this upgrade
                    if (ControllableDrawerTile.this instanceof ItemControllableDrawerTile<?> drawerTile) {
                        Storage<ItemVariant> storage = drawerTile.getStorage();

                        // Temporarily remove the upgrade to calculate new limits
                        ItemStack temp = super.removeItem(slot, amount);

                        // KEY FIX: Invalidate cache immediately so getStorageMultiplier calculates the
                        // NEW limit
                        ControllableDrawerTile.this.setNeedsUpgradeCache(true);

                        int baseSize = drawerTile.getBaseSize(0);
                        long newLimit = (long) baseSize * ControllableDrawerTile.this.getStorageMultiplier();

                        // Check if any slot exceeds new limit
                        boolean canRemove = true;
                        for (var view : storage) {
                            if (view.getAmount() > newLimit) {
                                canRemove = false;
                                break;
                            }
                        }

                        // Put the upgrade back if we can't remove it
                        if (!canRemove) {
                            super.setItem(slot, temp);
                            // Reset cache again because we put it back
                            ControllableDrawerTile.this.setNeedsUpgradeCache(true);
                            return ItemStack.EMPTY;
                        }

                        // Trigger GUI update
                        this.setChanged();
                        return temp;
                    }
                }
                ItemStack result = super.removeItem(slot, amount);
                if (!result.isEmpty()) {
                    // KEY FIX: Mark upgrade cache as needing refresh
                    ControllableDrawerTile.this.setNeedsUpgradeCache(true);
                    this.setChanged();
                }
                return result;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                super.setItem(slot, stack);
                // KEY FIX: Mark upgrade cache as needing refresh
                ControllableDrawerTile.this.setNeedsUpgradeCache(true);
                this.setChanged();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ControllableDrawerTile.this.onUpgradeChanged();
                ControllableDrawerTile.this.setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }
        };
        this.utilityUpgrades = new SimpleContainer(getUtilitySlotAmount()) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return stack.getItem() instanceof UpgradeItem
                        && ((UpgradeItem) stack.getItem()).getType() == UpgradeItem.Type.UTILITY;
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                ItemStack result = super.removeItem(slot, amount);
                if (!result.isEmpty()) {
                    this.setChanged();
                }
                return result;
            }

            @Override
            public void setItem(int slot, ItemStack stack) {
                super.setItem(slot, stack);
                this.setChanged();
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ControllableDrawerTile.this.onUpgradeChanged();
                ControllableDrawerTile.this.setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                    // Notify neighbors that redstone signal might have changed (for Redstone
                    // Upgrade add/remove)
                    level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
                }
            }
        };
    }

    public InteractionResult onSlotActivated(Player player, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ, int slot) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ConfigurationToolItem
                || stack.getItem() instanceof com.koudesuk.functionalstorage.item.LinkingToolItem)
            return InteractionResult.PASS;

        if (!stack.isEmpty() && stack.getItem() instanceof UpgradeItem) {
            UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();
            if (upgradeItem instanceof StorageUpgradeItem
                    || upgradeItem == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CREATIVE_UPGRADE) {
                SimpleContainer container = getStorageUpgrades();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).isEmpty()) {
                        ItemStack temp = stack.split(1);
                        container.setItem(i, temp);
                        return InteractionResult.SUCCESS;
                    }
                }
            } else {
                SimpleContainer container = getUtilityUpgrades();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.getItem(i).isEmpty()) {
                        ItemStack temp = stack.split(1);
                        container.setItem(i, temp);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }

        return InteractionResult.PASS;
    }

    public abstract int getStorageSlotAmount();

    public int getUtilitySlotAmount() {
        return 3;
    }

    public double getStorageDiv() {
        return 1;
    }

    public int getStorageMultiplier() {
        maybeCacheUpgrades();
        return mult;
    }

    // Cached upgrade calculation from original Forge version
    private void maybeCacheUpgrades() {
        if (needsUpgradeCache) {
            mult = 1;
            for (int i = 0; i < storageUpgrades.getContainerSize(); i++) {
                ItemStack stack = storageUpgrades.getItem(i);
                if (stack.getItem() instanceof StorageUpgradeItem) {
                    StorageUpgradeItem upgrade = (StorageUpgradeItem) stack.getItem();
                    double div = getStorageDiv();
                    if (div == 0)
                        div = 1;
                    if (mult == 1)
                        mult = (int) (upgrade.getStorageMultiplier() / div);
                    else
                        mult *= (int) (upgrade.getStorageMultiplier() / div);
                }
            }
            needsUpgradeCache = false;
        }
    }

    public void setNeedsUpgradeCache(boolean needsUpgradeCache) {
        this.needsUpgradeCache = needsUpgradeCache;
    }

    public void onUpgradeChanged() {
        // Force block entity to mark as changed
        this.setChanged();

        // Force sync to client - this is critical for front face icon update
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);

            // Force update all open containers (for GUI capacity display)
            level.getServer().getPlayerList().getPlayers().forEach(player -> {
                if (player.containerMenu instanceof com.koudesuk.functionalstorage.inventory.DrawerMenu) {
                    // Force the menu to refresh
                    player.containerMenu.broadcastChanges();
                }
            });
        }
    }

    public boolean isVoid() {
        for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
            if (utilityUpgrades.getItem(i)
                    .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.VOID_UPGRADE)
                return true;
        }
        return false;
    }

    public boolean isCreative() {
        for (int i = 0; i < storageUpgrades.getContainerSize(); i++) {
            if (storageUpgrades.getItem(i)
                    .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CREATIVE_UPGRADE)
                return true;
        }
        return false;
    }

    public boolean hasDowngrade() {
        for (int i = 0; i < storageUpgrades.getContainerSize(); i++) {
            if (storageUpgrades.getItem(i).getItem() instanceof StorageUpgradeItem) {
                StorageUpgradeItem upgrade = (StorageUpgradeItem) storageUpgrades.getItem(i).getItem();
                if (upgrade.getStorageTier() == StorageUpgradeItem.StorageTier.IRON)
                    return true;
            }
        }
        return false;
    }

    public boolean isLocked() {
        return this.getBlockState().hasProperty(com.koudesuk.functionalstorage.block.DrawerBlock.LOCKED)
                && this.getBlockState().getValue(com.koudesuk.functionalstorage.block.DrawerBlock.LOCKED);
    }

    public void setLocked(boolean locked) {
        if (this.getBlockState().hasProperty(com.koudesuk.functionalstorage.block.DrawerBlock.LOCKED)) {
            this.level.setBlock(this.getBlockPos(),
                    this.getBlockState().setValue(com.koudesuk.functionalstorage.block.DrawerBlock.LOCKED, locked), 3);
        }
    }

    public SimpleContainer getStorageUpgrades() {
        return storageUpgrades;
    }

    public SimpleContainer getUtilityUpgrades() {
        return utilityUpgrades;
    }

    public static class DrawerOptions {
        public java.util.HashMap<ConfigurationToolItem.ConfigurationAction, Boolean> options;
        public java.util.HashMap<ConfigurationToolItem.ConfigurationAction, Integer> advancedOptions;

        public DrawerOptions() {
            this.options = new java.util.HashMap<>();
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER, true);
            this.options.put(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES, true);
            this.advancedOptions = new java.util.HashMap<>();
            this.advancedOptions.put(ConfigurationToolItem.ConfigurationAction.INDICATOR, 0);
        }

        public boolean isActive(ConfigurationToolItem.ConfigurationAction configurationAction) {
            return options.getOrDefault(configurationAction, true);
        }

        public void setActive(ConfigurationToolItem.ConfigurationAction configurationAction, boolean active) {
            this.options.put(configurationAction, active);
        }

        public int getAdvancedValue(ConfigurationToolItem.ConfigurationAction configurationAction) {
            return advancedOptions.getOrDefault(configurationAction, 0);
        }

        public void setAdvancedValue(ConfigurationToolItem.ConfigurationAction configurationAction, int value) {
            this.advancedOptions.put(configurationAction, value);
        }

        public CompoundTag serializeNBT() {
            CompoundTag compoundTag = new CompoundTag();
            for (ConfigurationToolItem.ConfigurationAction action : this.options.keySet()) {
                compoundTag.putBoolean(action.name(), this.options.get(action));
            }
            for (ConfigurationToolItem.ConfigurationAction action : this.advancedOptions.keySet()) {
                compoundTag.putInt("Advanced: " + action.name(), this.advancedOptions.get(action));
            }
            return compoundTag;
        }

        public void deserializeNBT(CompoundTag nbt) {
            for (String allKey : nbt.getAllKeys()) {
                if (allKey.startsWith("Advanced: ")) {
                    this.advancedOptions.put(
                            ConfigurationToolItem.ConfigurationAction.valueOf(allKey.replace("Advanced: ", "")),
                            nbt.getInt(allKey));
                } else {
                    try {
                        this.options.put(ConfigurationToolItem.ConfigurationAction.valueOf(allKey),
                                nbt.getBoolean(allKey));
                    } catch (IllegalArgumentException e) {
                        // Ignore
                    }
                }
            }
        }
    }

    public DrawerOptions getDrawerOptions() {
        return drawerOptions;
    }

    public void toggleLocking() {
        setLocked(!this.isLocked());
    }

    public void toggleOption(ConfigurationToolItem.ConfigurationAction action) {
        if (action.getMax() == 1) {
            this.drawerOptions.setActive(action, !this.drawerOptions.isActive(action));
        } else {
            this.drawerOptions.setAdvancedValue(action,
                    (this.drawerOptions.getAdvancedValue(action) + 1) % (action.getMax() + 1));
        }
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.storageUpgrades.clearContent();
        this.utilityUpgrades.clearContent();
        if (tag.contains("StorageUpgrades")) {
            net.minecraft.nbt.ListTag list = tag.getList("StorageUpgrades", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < storageUpgrades.getContainerSize()) {
                    storageUpgrades.setItem(slot, ItemStack.of(itemTag));
                }
            }
        }
        if (tag.contains("UtilityUpgrades")) {
            net.minecraft.nbt.ListTag list = tag.getList("UtilityUpgrades", net.minecraft.nbt.Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < utilityUpgrades.getContainerSize()) {
                    utilityUpgrades.setItem(slot, ItemStack.of(itemTag));
                }
            }
        }
        if (tag.contains("DrawerOptions")) {
            drawerOptions.deserializeNBT(tag.getCompound("DrawerOptions"));
        }
        if (tag.contains("ControllerPos")) {
            this.controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        }
        this.needsUpgradeCache = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        net.minecraft.nbt.ListTag storageList = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < storageUpgrades.getContainerSize(); i++) {
            ItemStack stack = storageUpgrades.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                storageList.add(itemTag);
            }
        }
        tag.put("StorageUpgrades", storageList);

        net.minecraft.nbt.ListTag utilityList = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
            ItemStack stack = utilityUpgrades.getItem(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                stack.save(itemTag);
                utilityList.add(itemTag);
            }
        }
        tag.put("UtilityUpgrades", utilityList);
        tag.put("DrawerOptions", drawerOptions.serializeNBT());
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ControllableDrawerTile<?> blockEntity) {
        if (level.isClientSide)
            return;
        // Tick logic if needed
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public void setControllerPos(BlockPos controllerPos) {
        this.controllerPos = controllerPos;
        this.setChanged();
    }

    public void clearControllerPos() {
        this.controllerPos = null;
        this.setChanged();
    }

    public Direction getFacingDirection() {
        BlockState state = getBlockState();
        if (state
                .hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            return state
                    .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }
}