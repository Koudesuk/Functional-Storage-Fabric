package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.network.BlockPosPayload;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.fluid.ControllerFluidHandler;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.inventory.ControllerInventoryHandler;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.util.ConnectedDrawers;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class StorageControllerTile extends ItemControllableDrawerTile<StorageControllerTile>
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<BlockPosPayload> {

    protected static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    private ConnectedDrawers connectedDrawers;
    public ControllerInventoryHandler inventoryHandler;
    public ControllerFluidHandler fluidHandler;

    public StorageControllerTile(BlockPos pos, BlockState blockState) {
        this(FunctionalStorageBlockEntities.STORAGE_CONTROLLER, pos, blockState);
    }

    public StorageControllerTile(net.minecraft.world.level.block.entity.BlockEntityType<?> type, BlockPos pos,
            BlockState blockState) {
        super(type, pos, blockState);
        this.connectedDrawers = new ConnectedDrawers(null, this);
        this.inventoryHandler = new ControllerInventoryHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
        this.fluidHandler = new ControllerFluidHandler() {
            @Override
            public ConnectedDrawers getDrawers() {
                return connectedDrawers;
            }
        };
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.connectedDrawers.setLevel(level);
        if (level != null) {
            if (level.isClientSide) {
                this.connectedDrawers.rebuildShapes();
            }
        }
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public double getStorageDiv() {
        return FunctionalStorageConfig.RANGE_DIVISOR;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StorageControllerTile blockEntity) {
        if (level.isClientSide)
            return;

        if (blockEntity.connectedDrawers.getConnectedDrawers()
                .size() != (blockEntity.connectedDrawers.getItemHandlers().size()
                        + blockEntity.connectedDrawers.getFluidHandlers().size()
                        + blockEntity.connectedDrawers.getExtensions())) {
            blockEntity.connectedDrawers.setLevel(level);
            blockEntity.connectedDrawers.rebuild();
            blockEntity.setChanged();
        }
    }

    @Override
    public InteractionResult onSlotActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ, int slot) {
        ItemStack stack = playerIn.getItemInHand(hand);

        // Configuration Tool and Linking Tool should be handled elsewhere
        if (stack.getItem() instanceof com.koudesuk.functionalstorage.item.ConfigurationToolItem
                || stack.getItem() instanceof LinkingToolItem)
            return InteractionResult.PASS;

        if (!level.isClientSide) {
            // Open GUI when sneaking
            if (playerIn.isCrouching()) {
                openGui(playerIn);
                return InteractionResult.SUCCESS;
            } else {
                // Display "Sneak to open GUI" message
                playerIn.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable("gui.functionalstorage.open_gui")
                                .withStyle(net.minecraft.ChatFormatting.GRAY),
                        true);
            }

            long now = System.currentTimeMillis();
            Long lastClick = INTERACTION_LOGGER.get(playerIn.getUUID());
            boolean isDouble = lastClick != null && (now - lastClick < 300);
            INTERACTION_LOGGER.put(playerIn.getUUID(), now);

            // First priority: Try locked drawers
            for (Storage<ItemVariant> storage : this.connectedDrawers.getItemHandlers()) {
                if (storage instanceof BigInventoryHandler handler && handler.isLocked()) {
                    // A locked drawer only takes items its slots are already configured for.
                    // Unconfigured slots can only be set by clicking that slot on the drawer
                    // itself, never through the controller.
                    // Single click: Insert held item
                    if (!stack.isEmpty()) {
                        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                .openOuter()) {
                            long insertedAmount = handler.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                            if (insertedAmount > 0) {
                                stack.shrink((int) insertedAmount);
                                transaction.commit();
                                return InteractionResult.SUCCESS;
                            }
                        }
                    }

                    if (isDouble) {
                        for (int i = 0; i < playerIn.getInventory().getContainerSize(); i++) {
                            ItemStack invStack = playerIn.getInventory().getItem(i);
                            if (!invStack.isEmpty()) {
                                try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                        .openOuter()) {
                                    long insertedAmount = handler.insert(ItemVariant.of(invStack),
                                            invStack.getCount(), transaction);
                                    if (insertedAmount > 0) {
                                        invStack.shrink((int) insertedAmount);
                                        transaction.commit();
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Second priority: Try unlocked drawers (ONLY IF MATCHING ITEM EXISTS)
            for (Storage<ItemVariant> storage : this.connectedDrawers.getItemHandlers()) {
                if (storage instanceof BigInventoryHandler handler && !handler.isLocked()) {
                    // Single click: Insert held item
                    if (!stack.isEmpty()) {
                        boolean hasItem = false;
                        for (var stored : handler.getStoredStacks()) {
                            if (stored.getAmount() > 0
                                    && ItemVariant.of(stored.getStack()).equals(ItemVariant.of(stack))) {
                                hasItem = true;
                                break;
                            }
                        }

                        if (hasItem) {
                            try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                    .openOuter()) {
                                long insertedAmount = handler.insert(ItemVariant.of(stack), stack.getCount(),
                                        transaction);
                                if (insertedAmount > 0) {
                                    stack.shrink((int) insertedAmount);
                                    transaction.commit();
                                    return InteractionResult.SUCCESS;
                                }
                            }
                        }
                    }
                    // Double click: Insert all matching items from inventory
                    if (isDouble) {
                        for (int i = 0; i < playerIn.getInventory().getContainerSize(); i++) {
                            ItemStack invStack = playerIn.getInventory().getItem(i);
                            if (!invStack.isEmpty()) {
                                boolean hasItem = false;
                                for (var stored : handler.getStoredStacks()) {
                                    if (stored.getAmount() > 0
                                            && ItemVariant.of(stored.getStack()).equals(ItemVariant.of(invStack))) {
                                        hasItem = true;
                                        break;
                                    }
                                }

                                if (hasItem) {
                                    try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                            .openOuter()) {
                                        long insertedAmount = handler.insert(ItemVariant.of(invStack),
                                                invStack.getCount(),
                                                transaction);
                                        if (insertedAmount > 0) {
                                            invStack.shrink((int) insertedAmount);
                                            transaction.commit();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Third priority: Try locked Compacting Drawers
            for (Storage<ItemVariant> storage : this.connectedDrawers.getItemHandlers()) {
                if (storage instanceof CompactingInventoryHandler handler && handler.isLocked()) {
                    // Check if the item matches any of the compacting tiers
                    if (!stack.isEmpty() && handler.isSetup()) {
                        for (var result : handler.getResultList()) {
                            if (!result.getResult().isEmpty()
                                    && ItemVariant.of(result.getResult()).equals(ItemVariant.of(stack))) {
                                try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                        .openOuter()) {
                                    long insertedAmount = handler.insert(ItemVariant.of(stack), stack.getCount(),
                                            transaction);
                                    if (insertedAmount > 0) {
                                        stack.shrink((int) insertedAmount);
                                        transaction.commit();
                                        return InteractionResult.SUCCESS;
                                    }
                                }
                                break;
                            }
                        }
                    }

                    // Double click: Insert all matching items from inventory
                    if (isDouble) {
                        for (int i = 0; i < playerIn.getInventory().getContainerSize(); i++) {
                            ItemStack invStack = playerIn.getInventory().getItem(i);
                            if (!invStack.isEmpty() && handler.isSetup()) {
                                for (var result : handler.getResultList()) {
                                    if (!result.getResult().isEmpty()
                                            && ItemVariant.of(result.getResult()).equals(ItemVariant.of(invStack))) {
                                        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                                .openOuter()) {
                                            long insertedAmount = handler.insert(ItemVariant.of(invStack),
                                                    invStack.getCount(), transaction);
                                            if (insertedAmount > 0) {
                                                invStack.shrink((int) insertedAmount);
                                                transaction.commit();
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Fourth priority: Try unlocked Compacting Drawers (ONLY IF SETUP)
            for (Storage<ItemVariant> storage : this.connectedDrawers.getItemHandlers()) {
                if (storage instanceof CompactingInventoryHandler handler && !handler.isLocked()) {
                    // Check if the item matches any of the compacting tiers
                    if (!stack.isEmpty() && handler.isSetup()) {
                        for (var result : handler.getResultList()) {
                            if (!result.getResult().isEmpty()
                                    && ItemVariant.of(result.getResult()).equals(ItemVariant.of(stack))) {
                                try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                        .openOuter()) {
                                    long insertedAmount = handler.insert(ItemVariant.of(stack), stack.getCount(),
                                            transaction);
                                    if (insertedAmount > 0) {
                                        stack.shrink((int) insertedAmount);
                                        transaction.commit();
                                        return InteractionResult.SUCCESS;
                                    }
                                }
                                break;
                            }
                        }
                    }
                    // Double click: Insert all matching items from inventory
                    if (isDouble) {
                        for (int i = 0; i < playerIn.getInventory().getContainerSize(); i++) {
                            ItemStack invStack = playerIn.getInventory().getItem(i);
                            if (!invStack.isEmpty() && handler.isSetup()) {
                                for (var result : handler.getResultList()) {
                                    if (!result.getResult().isEmpty()
                                            && ItemVariant.of(result.getResult()).equals(ItemVariant.of(invStack))) {
                                        try (net.fabricmc.fabric.api.transfer.v1.transaction.Transaction transaction = net.fabricmc.fabric.api.transfer.v1.transaction.Transaction
                                                .openOuter()) {
                                            long insertedAmount = handler.insert(ItemVariant.of(invStack),
                                                    invStack.getCount(),
                                                    transaction);
                                            if (insertedAmount > 0) {
                                                invStack.shrink((int) insertedAmount);
                                                transaction.commit();
                                            }
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.SUCCESS;
    }

    public void openGui(Player player) {
        player.openMenu(this);
    }

    @Override
    public net.minecraft.network.chat.Component getDisplayName() {
        return net.minecraft.network.chat.Component.translatable("block.functionalstorage.storage_controller");
    }

    @Override
    public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int i,
            net.minecraft.world.entity.player.Inventory inventory, Player player) {
        return new com.koudesuk.functionalstorage.inventory.DrawerMenu(i, inventory, this.getStorageUpgrades(),
                this.getUtilityUpgrades(), this);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
        return new BlockPosPayload(this.getBlockPos());
    }

    @Override
    public void onUpgradeChanged() {
        this.connectedDrawers.rebuild();
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Storage<ItemVariant> getStorage() {
        return inventoryHandler;
    }

    @Override
    public int getBaseSize(int lost) {
        return 1;
    }

    public ConnectedDrawers getConnectedDrawers() {
        return connectedDrawers;
    }

    /**
     * Every drawer linked to this controller. Controllers and extensions are not
     * storage themselves, so they are skipped.
     */
    private List<ControllableDrawerTile<?>> getConnectedDrawerTiles() {
        List<ControllableDrawerTile<?>> drawers = new ArrayList<>();
        if (level == null || level.isClientSide)
            return drawers;
        for (Long connectedDrawer : new ArrayList<>(this.connectedDrawers.getConnectedDrawers())) {
            BlockEntity entity = level.getBlockEntity(BlockPos.of(connectedDrawer));
            if (entity instanceof StorageControllerTile || entity instanceof ControllerExtensionTile)
                continue;
            if (entity instanceof ControllableDrawerTile<?> drawer)
                drawers.add(drawer);
        }
        return drawers;
    }

    /** True when every connected drawer is locked. False when there are none. */
    public boolean areAllDrawersLocked() {
        List<ControllableDrawerTile<?>> drawers = getConnectedDrawerTiles();
        if (drawers.isEmpty())
            return false;
        for (ControllableDrawerTile<?> drawer : drawers) {
            if (!drawer.isLocked())
                return false;
        }
        return true;
    }

    /**
     * The controller has no storage of its own, so locking it means locking every
     * connected drawer. Any unlocked drawer means "lock them all", otherwise they
     * all get unlocked.
     */
    @Override
    public void toggleLocking() {
        boolean lock = !areAllDrawersLocked();
        for (ControllableDrawerTile<?> drawer : getConnectedDrawerTiles()) {
            drawer.setLocked(lock);
        }
    }

    /**
     * The controller keeps the master value (it is what the configuration tool
     * reports back to the player) and pushes it onto every connected drawer.
     */
    @Override
    public void toggleOption(com.koudesuk.functionalstorage.item.ConfigurationToolItem.ConfigurationAction action) {
        super.toggleOption(action);
        for (ControllableDrawerTile<?> drawer : getConnectedDrawerTiles()) {
            DrawerOptions options = drawer.getDrawerOptions();
            if (action.getMax() == 1) {
                options.setActive(action, getDrawerOptions().isActive(action));
            } else {
                options.setAdvancedValue(action, getDrawerOptions().getAdvancedValue(action));
            }
            drawer.setChanged();
            level.sendBlockUpdated(drawer.getBlockPos(), drawer.getBlockState(), drawer.getBlockState(), 3);
        }
    }

    @Override
    public int getUtilitySlotAmount() {
        return 3; // Must be 3 to match DrawerMenu's hardcoded checkContainerSize
    }

    public boolean addConnectedDrawers(LinkingToolItem.ActionMode action, BlockPos... positions) {
        var extraRange = getStorageMultiplier();
        if (extraRange == 1) {
            extraRange = 0;
        }
        var didWork = false;
        var area = new AABB(this.getBlockPos())
                .inflate(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE + extraRange);
        this.connectedDrawers.setLevel(this.getLevel());
        for (BlockPos position : positions) {
            if (level != null && level.getBlockState(position)
                    .getBlock() instanceof com.koudesuk.functionalstorage.block.StorageControllerBlock)
                continue;
            if (area.contains(Vec3.atCenterOf(position)) && this.getLevel()
                    .getBlockEntity(position) instanceof ControllableDrawerTile<?> controllableDrawerTile) {
                if (action == LinkingToolItem.ActionMode.ADD) {
                    controllableDrawerTile.setControllerPos(this.getBlockPos());
                    if (!connectedDrawers.getConnectedDrawers().contains(position.asLong())) {
                        this.connectedDrawers.getConnectedDrawers().add(position.asLong());
                        didWork = true;
                    }
                }
            } else {
                // Debug logging for connection failure
                if (!area.contains(Vec3.atCenterOf(position))) {
                    System.out.println("Failed to link: Position " + position + " is out of range. Area: " + area);
                } else if (!(this.getLevel().getBlockEntity(position) instanceof ControllableDrawerTile)) {
                    System.out.println("Failed to link: Block at " + position
                            + " is not a ControllableDrawerTile. Entity: " + this.getLevel().getBlockEntity(position));
                }
            }
            if (action == LinkingToolItem.ActionMode.REMOVE) {
                this.connectedDrawers.getConnectedDrawers().removeIf(aLong -> aLong == position.asLong());
                BlockEntity be = level.getBlockEntity(position);
                if (be instanceof ControllableDrawerTile<?> controllableDrawerTile) {
                    controllableDrawerTile.clearControllerPos();
                }
                didWork = true;
            }
        }
        this.connectedDrawers.rebuild();
        this.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
        return didWork;
    }

    @Override
    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ConnectedDrawers")) {
            connectedDrawers.deserializeNBT(tag.getCompound("ConnectedDrawers"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("ConnectedDrawers", connectedDrawers.serializeNBT());
    }

    public ControllerInventoryHandler getInventoryHandler() {
        return inventoryHandler;
    }

    public ControllerFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public boolean isEverythingEmpty() {
        for (int i = 0; i < getStorageUpgrades().getContainerSize(); i++) {
            if (!getStorageUpgrades().getItem(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < getUtilityUpgrades().getContainerSize(); i++) {
            if (!getUtilityUpgrades().getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
