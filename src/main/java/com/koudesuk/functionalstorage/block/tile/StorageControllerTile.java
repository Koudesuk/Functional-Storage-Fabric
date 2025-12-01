package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
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

import java.util.HashMap;
import java.util.UUID;

public class StorageControllerTile extends ItemControllableDrawerTile<StorageControllerTile>
        implements net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory {

    protected static HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    private ConnectedDrawers connectedDrawers;
    public ControllerInventoryHandler inventoryHandler;

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
                    // Single click: Insert held item
                    if (!stack.isEmpty()) {
                        // Logic for setting locked item if empty
                        var storedStacks = handler.getStoredStacks();
                        for (int i = 0; i < storedStacks.size(); i++) {
                            if (handler.isLocked() && storedStacks.get(i).getAmount() == 0
                                    && storedStacks.get(i).getStack().isEmpty()) {
                                if (handler.isItemValid(i, ItemVariant.of(stack))) {
                                    handler.setLockedItem(i, ItemVariant.of(stack));
                                }
                            }
                        }

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
                                // Same locked empty check for inventory items
                                var storedStacks = handler.getStoredStacks();
                                for (int s = 0; s < storedStacks.size(); s++) {
                                    if (handler.isLocked() && storedStacks.get(s).getAmount() == 0
                                            && storedStacks.get(s).getStack().isEmpty()) {
                                        if (handler.isItemValid(s, ItemVariant.of(invStack))) {
                                            handler.setLockedItem(s, ItemVariant.of(invStack));
                                        }
                                    }
                                }

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
                            if (stored.getAmount() > 0 && ItemVariant.of(stored.getStack()).equals(ItemVariant.of(stack))) {
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
                                    if (stored.getAmount() > 0 && ItemVariant.of(stored.getStack()).equals(ItemVariant.of(invStack))) {
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
    public void writeScreenOpeningData(net.minecraft.server.level.ServerPlayer player,
            net.minecraft.network.FriendlyByteBuf buf) {
        buf.writeBlockPos(this.getBlockPos());
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
            if (level != null && level.getBlockState(position).getBlock() instanceof com.koudesuk.functionalstorage.block.StorageControllerBlock)
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
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("ConnectedDrawers")) {
            connectedDrawers.deserializeNBT(tag.getCompound("ConnectedDrawers"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("ConnectedDrawers", connectedDrawers.serializeNBT());
    }

    public ControllerInventoryHandler getInventoryHandler() {
        return inventoryHandler;
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
