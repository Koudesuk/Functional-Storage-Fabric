package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.item.component.SizeProvider;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.util.StorageTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public abstract class ControllableDrawerTile<T extends ControllableDrawerTile<T>> extends BlockEntity {

    // Cache mechanism from original Forge version
    private boolean needsUpgradeCache = true;
    private int mult = 1;

    private final SimpleContainer storageUpgrades;
    private final SimpleContainer utilityUpgrades;
    private final DrawerOptions drawerOptions;
    private BlockPos controllerPos;
    // Controllers fill and empty higher priority drawers first, like the original
    private int priority;

    public ControllableDrawerTile(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.drawerOptions = new DrawerOptions();
        this.storageUpgrades = new SimpleContainer(getStorageSlotAmount()) {
            @Override
            public boolean canPlaceItem(int slot, ItemStack stack) {
                return isStorageUpgrade(stack) && canChangeStorageUpgrade(slot, stack);
            }

            @Override
            public ItemStack removeItem(int slot, int amount) {
                // Like the original's extractItem, an upgrade stays while the contents need its room
                if (!canChangeStorageUpgrade(slot, ItemStack.EMPTY))
                    return ItemStack.EMPTY;
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
                return canPlaceUtilityUpgrade(this, stack);
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

        if (installUpgrade(stack))
            return InteractionResult.SUCCESS;

        return InteractionResult.PASS;
    }

    /**
     * Whether right clicking a drawer with the stack installs it: an upgrade item, or any item with a
     * functional behavior like the original's generator upgrades.
     */
    public static boolean isUpgrade(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof UpgradeItem
                || stack.has(FSAttachments.FUNCTIONAL_BEHAVIOR) || SizeProvider.hasAny(stack));
    }

    /**
     * Whether a storage upgrade slot takes the stack: a storage upgrade, or like the original any item with
     * a data-driven size component.
     */
    public static boolean isStorageUpgrade(ItemStack stack) {
        return stack.getItem() instanceof UpgradeItem upgradeItem && upgradeItem.getType() == UpgradeItem.Type.STORAGE
                || SizeProvider.hasAny(stack);
    }

    /**
     * Upgrades that change how much fits. The Creative Vending Upgrade is not one, so like the original
     * it always comes out.
     */
    private static boolean affectsCapacity(ItemStack stack) {
        return stack.getItem() instanceof StorageUpgradeItem || SizeProvider.hasAny(stack);
    }

    /**
     * Whether storage upgrade slot {@code slot} may hold {@code replacement} instead of what it holds now
     * (EMPTY to take it out), like the original's canChangeMultiplier: no slot may end up holding more
     * than it fits. A slot already over its capacity only blocks changes that shrink it further, so
     * contents from before a capacity change never lock the upgrades in.
     */
    public boolean canChangeStorageUpgrade(int slot, ItemStack replacement) {
        if (slot < 0 || slot >= storageUpgrades.getContainerSize())
            return false;
        ItemStack current = storageUpgrades.getItem(slot);
        if (!affectsCapacity(current) && !affectsCapacity(replacement))
            return true;
        List<long[]> before = getSlotFill();
        // Swap the stack in the backing list directly, so trying it neither syncs nor saves anything
        var items = storageUpgrades.getItems();
        items.set(slot, replacement);
        needsUpgradeCache = true;
        List<long[]> after;
        try {
            after = getSlotFill();
        } finally {
            items.set(slot, current);
            needsUpgradeCache = true;
        }
        for (int i = 0; i < after.size() && i < before.size(); i++) {
            long amount = after.get(i)[0];
            long capacity = after.get(i)[1];
            if (amount > capacity && capacity < before.get(i)[1])
                return false;
        }
        return true;
    }

    /**
     * Amount and capacity of every slot, {@code {amount, capacity}}, for the storage upgrade checks.
     */
    protected List<long[]> getSlotFill() {
        return List.of();
    }

    /**
     * The original's data-driven size upgrades (SizeProvider) over the storage upgrade slots, as a factor
     * of {@code base}: exactly 1 unless an installed item carries the component.
     */
    public double getSizeFactor(DataComponentType<SizeProvider> component, double base) {
        return base <= 0 ? 1 : SizeProvider.calculate(storageUpgrades, component, base) / base;
    }

    /**
     * Whether a utility upgrade slot of these upgrades takes the stack: a utility upgrade or an item with a
     * functional behavior, never next to an upgrade the two mark as incompatible (like the original, this
     * keeps the Void Upgrade to one per drawer).
     */
    public static boolean canPlaceUtilityUpgrade(Container upgrades, ItemStack stack) {
        if (!(stack.getItem() instanceof UpgradeItem upgradeItem && upgradeItem.getType() == UpgradeItem.Type.UTILITY)
                && !stack.has(FSAttachments.FUNCTIONAL_BEHAVIOR))
            return false;
        var incompatibleIn = stack.get(FSAttachments.INCOMPATIBLE_UPGRADES);
        for (int i = 0; i < upgrades.getContainerSize(); i++) {
            ItemStack installed = upgrades.getItem(i);
            if (installed.isEmpty())
                continue;
            var incompatibleOn = installed.get(FSAttachments.INCOMPATIBLE_UPGRADES);
            if (incompatibleIn != null && installed.is(incompatibleIn)
                    || incompatibleOn != null && stack.is(incompatibleOn))
                return false;
        }
        return true;
    }

    /**
     * Moves one of the held upgrade into the first free slot that takes it.
     */
    protected boolean installUpgrade(ItemStack stack) {
        if (!isUpgrade(stack))
            return false;
        boolean storage = stack.getItem() instanceof StorageUpgradeItem
                || stack.is(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CREATIVE_UPGRADE)
                || SizeProvider.hasAny(stack);
        SimpleContainer container = storage ? getStorageUpgrades() : getUtilityUpgrades();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (container.getItem(i).isEmpty() && (storage
                    ? canUseStorageUpgradeWithCreative(stack, i) && canChangeStorageUpgrade(i, stack.copyWithCount(1))
                    : canPlaceUtilityUpgrade(container, stack))) {
                container.setItem(i, stack.split(1));
                return true;
            }
        }
        return false;
    }

    /**
     * Runs the upgrades that carry a functional behavior, like the original's serverTick.
     */
    protected void tickFunctionalUpgrades(Level level) {
        for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
            ItemStack stack = utilityUpgrades.getItem(i);
            var behavior = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
            if (behavior != null)
                behavior.work(level, getBlockPos(), this, stack, i);
        }
    }

    /**
     * The signal of the first data-driven behavior in the utility slots that gives one, like the
     * original's Drawer.getSignal; -1 when none does. The built-in Redstone Upgrade keeps its own code.
     */
    public int getBehaviorSignal(BlockState state, Direction direction) {
        if (level == null)
            return -1;
        for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
            ItemStack stack = utilityUpgrades.getItem(i);
            var behavior = stack.get(FSAttachments.FUNCTIONAL_BEHAVIOR);
            if (behavior != null) {
                int signal = behavior.getRedstoneSignal(level, getBlockPos(), state, this, direction, stack, i);
                if (signal >= 0)
                    return signal;
            }
        }
        return -1;
    }

    /**
     * Left click on a front slot, see {@link com.koudesuk.functionalstorage.block.DrawerFront}.
     */
    public void onClicked(Player player, int slot) {
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

    // Cached upgrade calculation from original Forge version. Upgrades multiply together; every
    // factor is at least 1 and the product stops at Integer.MAX_VALUE instead of overflowing.
    private void maybeCacheUpgrades() {
        if (needsUpgradeCache) {
            double div = Math.max(1, getStorageDiv());
            long product = 1;
            for (int i = 0; i < storageUpgrades.getContainerSize(); i++) {
                if (storageUpgrades.getItem(i).getItem() instanceof StorageUpgradeItem upgrade)
                    product = Math.min(Integer.MAX_VALUE,
                            product * Math.max(1, (long) (upgrade.getStorageMultiplier() / div)));
            }
            mult = (int) product;
            needsUpgradeCache = false;
        }
    }

    public void setNeedsUpgradeCache(boolean needsUpgradeCache) {
        this.needsUpgradeCache = needsUpgradeCache;
    }

    public void onUpgradeChanged() {
        // Force block entity to mark as changed
        this.setChanged();
        // Storage upgrades change the capacity, so the fullness the comparator reads
        updateComparatorOutput();

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

    /**
     * Comparator output like the original Drawer.getAnalogOutputSignal: 0 when empty, otherwise 1
     * to 15 from the average fullness of the slots.
     */
    public int getComparatorSignal() {
        return 0;
    }

    protected static int comparatorSignal(double fullness, boolean hasContents) {
        return hasContents ? Math.min(15, (int) Math.floor(Math.min(1D, fullness) * 14D) + 1) : 0;
    }

    public void updateComparatorOutput() {
        if (level != null && !level.isClientSide) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
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
        net.minecraft.world.level.block.state.properties.BooleanProperty lockedProperty = this.getLockedProperty();
        return lockedProperty != null && this.getBlockState().getValue(lockedProperty);
    }

    public void setLocked(boolean locked) {
        net.minecraft.world.level.block.state.properties.BooleanProperty lockedProperty = this.getLockedProperty();
        if (lockedProperty != null && this.level != null) {
            this.level.setBlock(this.getBlockPos(),
                    this.getBlockState().setValue(lockedProperty, locked), 3);
        }
    }

    /**
     * The block's "locked" property, whichever block class declared it.
     */
    private net.minecraft.world.level.block.state.properties.BooleanProperty getLockedProperty() {
        for (net.minecraft.world.level.block.state.properties.Property<?> property : this.getBlockState().getProperties()) {
            if (property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty booleanProperty
                    && "locked".equals(booleanProperty.getName())) {
                return booleanProperty;
            }
        }
        return null;
    }

    public SimpleContainer getStorageUpgrades() {
        return storageUpgrades;
    }

    /**
     * The Creative Vending Upgrade never shares the storage upgrade slots with an upgrade marked
     * incompatible with it, like the original. The target slot itself is skipped, so replacing
     * what is in it is still allowed.
     */
    public boolean canUseStorageUpgradeWithCreative(ItemStack stack, int slot) {
        boolean insertingCreative = stack.is(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CREATIVE_UPGRADE);
        boolean insertingIncompatible = StorageTags.isCreativeIncompatible(stack.getItem());
        if (!insertingCreative && !insertingIncompatible)
            return true;
        for (int i = 0; i < storageUpgrades.getContainerSize(); i++) {
            ItemStack stored = storageUpgrades.getItem(i);
            if (i == slot || stored.isEmpty())
                continue;
            if (insertingCreative && StorageTags.isCreativeIncompatible(stored.getItem())
                    || insertingIncompatible
                            && stored.is(com.koudesuk.functionalstorage.registry.FunctionalStorageItems.CREATIVE_UPGRADE))
                return false;
        }
        return true;
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.storageUpgrades.clearContent();
        this.utilityUpgrades.clearContent();
        if (tag.contains("StorageUpgrades")) {
            net.minecraft.nbt.ListTag list = tag.getList("StorageUpgrades", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < storageUpgrades.getContainerSize()) {
                    storageUpgrades.setItem(slot,
                            ItemStack.CODEC.parse(net.minecraft.resources.RegistryOps
                                    .create(net.minecraft.nbt.NbtOps.INSTANCE, registries), itemTag).result()
                                    .orElse(ItemStack.EMPTY));
                }
            }
        }
        if (tag.contains("UtilityUpgrades")) {
            net.minecraft.nbt.ListTag list = tag.getList("UtilityUpgrades", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getInt("Slot");
                if (slot >= 0 && slot < utilityUpgrades.getContainerSize()) {
                    utilityUpgrades.setItem(slot,
                            ItemStack.CODEC.parse(net.minecraft.resources.RegistryOps
                                    .create(net.minecraft.nbt.NbtOps.INSTANCE, registries), itemTag).result()
                                    .orElse(ItemStack.EMPTY));
                }
            }
        }
        if (tag.contains("DrawerOptions")) {
            drawerOptions.deserializeNBT(tag.getCompound("DrawerOptions"));
        }
        if (tag.contains("ControllerPos")) {
            this.controllerPos = BlockPos.of(tag.getLong("ControllerPos"));
        }
        this.priority = tag.getInt("Priority");
        this.needsUpgradeCache = true;
    }

    /**
     * Public method for loading tile data from an ItemStack's component data.
     * This is used in Block.setPlacedBy() to restore tile state.
     */
    public void loadFromTag(CompoundTag tag, HolderLookup.Provider registries) {
        this.loadAdditional(tag, registries);
    }

    /**
     * The block's setPlacedBy reads TILE and LOCKED. Getting them here only marks them as used, so
     * vanilla does not also keep them in the components it saves; those went into the next drop's
     * TILE, which nested one level deeper on every break and place.
     */
    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput components) {
        super.applyImplicitComponents(components);
        components.get(FSAttachments.TILE);
        components.get(FSAttachments.LOCKED);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        net.minecraft.nbt.ListTag storageList = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < storageUpgrades.getContainerSize(); i++) {
            ItemStack stack = storageUpgrades.getItem(i);
            if (!stack.isEmpty()) {
                Tag encoded = ItemStack.CODEC.encodeStart(
                        net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, registries),
                        stack).result().orElse(null);
                if (encoded instanceof CompoundTag itemTag) {
                    itemTag.putInt("Slot", i);
                    storageList.add(itemTag);
                }
            }
        }
        tag.put("StorageUpgrades", storageList);

        net.minecraft.nbt.ListTag utilityList = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < utilityUpgrades.getContainerSize(); i++) {
            ItemStack stack = utilityUpgrades.getItem(i);
            if (!stack.isEmpty()) {
                Tag encoded = ItemStack.CODEC.encodeStart(
                        net.minecraft.resources.RegistryOps.create(net.minecraft.nbt.NbtOps.INSTANCE, registries),
                        stack).result().orElse(null);
                if (encoded instanceof CompoundTag itemTag) {
                    itemTag.putInt("Slot", i);
                    utilityList.add(itemTag);
                }
            }
        }
        tag.put("UtilityUpgrades", utilityList);
        tag.put("DrawerOptions", drawerOptions.serializeNBT());
        if (controllerPos != null) {
            tag.putLong("ControllerPos", controllerPos.asLong());
        }
        tag.putInt("Priority", priority);
    }

    @Override
    public net.minecraft.network.protocol.Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ControllableDrawerTile<?> blockEntity) {
        if (level.isClientSide)
            return;
        // Tick logic if needed
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    /**
     * Links the drawer to a controller. Like the original, a drawer linked to another controller
     * first leaves the old one's list, so no controller keeps a drawer that does not point back to
     * it (breaking such a drawer could not tell that controller).
     */
    public void setControllerPos(BlockPos controllerPos) {
        if (this.controllerPos != null && !this.controllerPos.equals(controllerPos) && level != null
                && level.getBlockEntity(this.controllerPos) instanceof StorageControllerTile old) {
            old.addConnectedDrawers(com.koudesuk.functionalstorage.item.LinkingToolItem.ActionMode.REMOVE,
                    getBlockPos());
        }
        this.controllerPos = controllerPos;
        this.setChanged();
    }

    public void clearControllerPos() {
        this.controllerPos = null;
        this.setChanged();
    }

    /**
     * Removes the drawer from its controller's list, for when the block is broken or replaced.
     */
    public void unlinkFromController() {
        if (controllerPos != null && level != null
                && level.getBlockEntity(controllerPos) instanceof StorageControllerTile controller) {
            controller.addConnectedDrawers(com.koudesuk.functionalstorage.item.LinkingToolItem.ActionMode.REMOVE,
                    getBlockPos());
        }
    }

    public int getPriority() {
        return priority;
    }

    /**
     * Whether a broken drawer drops a plain item, without its tile data. Drawers holding nothing say so.
     */
    public boolean isEverythingEmpty() {
        return false;
    }

    /**
     * Set from the drawer screen (DrawerPriorityPayload). The linked controller sorts its drawers again.
     */
    public void setPriority(int priority) {
        if (this.priority == priority)
            return;
        this.priority = priority;
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            if (controllerPos != null && level.getBlockEntity(controllerPos) instanceof StorageControllerTile controller) {
                controller.getConnectedDrawers().rebuild();
                controller.setChanged();
                level.sendBlockUpdated(controllerPos, controller.getBlockState(), controller.getBlockState(), 3);
            }
        }
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
