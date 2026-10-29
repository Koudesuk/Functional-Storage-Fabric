package com.koudesuk.functionalstorage.block.tile;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.network.BlockPosPayload;
import com.koudesuk.functionalstorage.inventory.DrawerMenu;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

public class FluidDrawerTile extends ControllableDrawerTile<FluidDrawerTile>
        implements net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider<BlockPosPayload> {

    private final DrawerType type;
    private final FluidInventoryHandler handler;

    public FluidDrawerTile(BlockPos pos, BlockState state, DrawerType type) {
        this(getType(type), pos, state, type);
    }

    protected FluidDrawerTile(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, DrawerType type) {
        super(blockEntityType, pos, state);
        this.type = type;
        this.handler = new FluidInventoryHandler(type) {
            @Override
            public void onChange() {
                FluidDrawerTile.this.setChanged();
                FluidDrawerTile.this.updateComparatorOutput();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                }
            }

            @Override
            public int getMultiplier() {
                return FluidDrawerTile.this.getStorageMultiplier();
            }

            @Override
            public double getSizeFactor() {
                // The original's base is the drawer's buckets, one bucket with the Iron Downgrade
                return FluidDrawerTile.this.getSizeFactor(FSAttachments.FLUID_STORAGE_MODIFIER,
                        hasDowngrade() ? 1 : FluidDrawerTile.this.type.getSlotAmount() / 64);
            }

            @Override
            public boolean isVoid() {
                return FluidDrawerTile.this.isVoid();
            }

            @Override
            public boolean hasDowngrade() {
                return FluidDrawerTile.this.hasDowngrade();
            }

            @Override
            public boolean isLocked() {
                return FluidDrawerTile.this.isLocked();
            }

            @Override
            public boolean isCreative() {
                return FluidDrawerTile.this.isCreative();
            }
        };
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidDrawerTile tile) {
        if (level.isClientSide())
            return;

        tile.tickFunctionalUpgrades(level);

        // Redstone update - check every 20 ticks like original Forge version
        if (level.getGameTime() % 20 == 0) {
            if (tile.getUtilitySlotAmount() > 0) {
                for (int i = 0; i < tile.getUtilityUpgrades().getContainerSize(); i++) {
                    net.minecraft.world.item.ItemStack stack = tile.getUtilityUpgrades().getItem(i);
                    if (!stack.isEmpty() && stack
                            .getItem() == com.koudesuk.functionalstorage.registry.FunctionalStorageItems.REDSTONE_UPGRADE) {
                        // Notify neighbors that redstone signal might have changed
                        level.updateNeighborsAt(pos, state.getBlock());
                        break;
                    }
                }
            }
        }

        // Pulling and Pushing Upgrades move fluids at the configured rate, the Collector Upgrade
        // picks up fluid sources at a third of it, like the original
        if (level.getGameTime() % FunctionalStorageConfig.UPGRADE_TICK.get() == 0) {
            boolean collect = level.getGameTime() % (FunctionalStorageConfig.UPGRADE_TICK.get() * 3L) == 0;
            for (int i = 0; i < tile.getUtilityUpgrades().getContainerSize(); i++) {
                ItemStack stack = tile.getUtilityUpgrades().getItem(i);
                if (stack.getItem() == FunctionalStorageItems.PULLING_UPGRADE) {
                    tile.moveFluids(level, pos, stack, false, FunctionalStorageConfig.UPGRADE_PULL_FLUID.get());
                } else if (stack.getItem() == FunctionalStorageItems.PUSHING_UPGRADE) {
                    tile.moveFluids(level, pos, stack, true, FunctionalStorageConfig.UPGRADE_PUSH_FLUID.get());
                } else if (collect && stack.getItem() == FunctionalStorageItems.COLLECTOR_UPGRADE) {
                    tile.collectFluid(level, pos, stack);
                }
            }
        }
    }

    private static BlockEntityType<?> getType(DrawerType type) {
        if (type == DrawerType.X_1)
            return FunctionalStorageBlockEntities.FLUID_DRAWER_1;
        if (type == DrawerType.X_2)
            return FunctionalStorageBlockEntities.FLUID_DRAWER_2;
        if (type == DrawerType.X_4)
            return FunctionalStorageBlockEntities.FLUID_DRAWER_4;
        return FunctionalStorageBlockEntities.FLUID_DRAWER_1;
    }

    @Override
    public int getStorageSlotAmount() {
        return 4;
    }

    @Override
    public double getStorageDiv() {
        return FunctionalStorageConfig.FLUID_DIVISOR.get();
    }

    @Override
    protected java.util.List<long[]> getSlotFill() {
        java.util.List<long[]> fill = new java.util.ArrayList<>();
        for (StorageView<FluidVariant> view : handler)
            fill.add(new long[] { view.getAmount(), view.getCapacity() });
        return fill;
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        input.read("Handler", CompoundTag.CODEC).ifPresent(tag -> handler.deserializeNBT(tag, input.lookup()));
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (this.level != null) {
            output.store("Handler", CompoundTag.CODEC, handler.serializeNBT(this.level.registryAccess()));
        }
    }

    @Override
    public InteractionResult onSlotActivated(Player player, InteractionHand hand, Direction facing, double hitX,
            double hitY, double hitZ, int slot) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof com.koudesuk.functionalstorage.item.ConfigurationToolItem
                || stack.getItem() instanceof com.koudesuk.functionalstorage.item.LinkingToolItem)
            return InteractionResult.PASS;

        if (isUpgrade(stack)) {
            return super.onSlotActivated(player, hand, facing, hitX, hitY, hitZ, slot);
        }

        if (slot != -1 && !player.isShiftKeyDown()) {
            // A locked slot only takes its filter fluid, so right clicking an empty one with a fluid
            // container makes that fluid its filter first, like the original. A slot that still
            // holds fluid keeps its filter
            FluidVariant held = getHeldFluid(player, hand);
            if (isLocked() && !held.isBlank() && handler.getResource(slot).isBlank()
                    && !held.equals(handler.getFilterStack(slot))) {
                handler.setFilterStack(slot, held);
                handler.onChange();
            }
            // Right click only empties the held container into the slot, like the original
            moveFluid(player, hand, slot, false);
            return InteractionResult.SUCCESS;
        } else if (slot == -1 || player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                player.openMenu(this);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void onClicked(Player player, int slot) {
        // Left click only fills the held container from the slot, like the original
        if (slot != -1) {
            moveFluid(player, InteractionHand.MAIN_HAND, slot, true);
        }
    }

    /**
     * One container's worth of fluid between the held item and a slot: fill takes it out of the
     * drawer, otherwise it goes in. The hand storage exchanges a single item per insert or extract,
     * so a stack of buckets moves one bucket, like the original's oneByOne item access.
     * FluidStorageUtil.interactWithFluidStorage is not used because it tries both directions.
     */
    private void moveFluid(Player player, InteractionHand hand, int slot, boolean fill) {
        Storage<FluidVariant> slotStorage = handler.getSlotStorage(slot);
        Storage<FluidVariant> handStorage = ContainerItemContext.forPlayerInteraction(player, hand)
                .find(FluidStorage.ITEM);
        if (slotStorage == null || handStorage == null) {
            return;
        }
        FluidVariant beforeResource = handler.getResource(slot);
        long beforeAmount = handler.getAmount(slot);
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = fill
                    ? StorageUtil.move(slotStorage, handStorage, fluid -> true, Long.MAX_VALUE, transaction)
                    : StorageUtil.move(handStorage, slotStorage, fluid -> true, Long.MAX_VALUE, transaction);
            if (moved == 0) {
                return;
            }
            transaction.commit();
        }
        playTransferSound(slot, player, hand, beforeResource, beforeAmount);
    }

    /**
     * One Pulling or Pushing Upgrade run: up to the configured millibuckets between the drawer and
     * the block the upgrade faces, like the original's MoveFluidsBehavior.
     * Some storages only move fluid in steps larger than one run: a lava cauldron takes or gives a
     * whole bucket, against 500 mB per run by default. When the configured amount moves nothing,
     * one bucket is tried instead, so those still work.
     */
    public void moveFluids(Level level, BlockPos pos, ItemStack upgrade, boolean drawerIsSource, int millibuckets) {
        Direction direction = UpgradeItem.getDirection(upgrade);
        Storage<FluidVariant> other = FluidStorage.SIDED.find(level, pos.relative(direction), direction.getOpposite());
        if (other == null) {
            return;
        }
        Storage<FluidVariant> source = drawerIsSource ? handler : other;
        Storage<FluidVariant> destination = drawerIsSource ? other : handler;
        long amount = millibuckets * (FluidConstants.BUCKET / 1000);
        try (Transaction transaction = Transaction.openOuter()) {
            long moved = StorageUtil.move(source, destination, fluid -> true, amount, transaction);
            if (moved == 0 && amount > 0 && amount < FluidConstants.BUCKET) {
                StorageUtil.move(source, destination, fluid -> true, FluidConstants.BUCKET, transaction);
            }
            transaction.commit();
        }
    }

    /**
     * One Collector Upgrade run: picks up the fluid source the upgrade faces, one bucket at a time,
     * like the original's CollectFluidsBehavior (NeoForge FluidUtil.tryPickupFluid). The block is
     * only taken once the drawer is known to have room, and outside any transaction because the
     * block update may open transactions of its own.
     */
    public void collectFluid(Level level, BlockPos pos, ItemStack upgrade) {
        BlockPos target = pos.relative(UpgradeItem.getDirection(upgrade));
        FluidState fluidState = level.getFluidState(target);
        if (fluidState.isEmpty() || !fluidState.isSource()
                || !(level.getBlockState(target).getBlock() instanceof BucketPickup pickup)) {
            return;
        }
        FluidVariant fluid = FluidVariant.of(fluidState.getType());
        if (StorageUtil.simulateInsert(handler, fluid, FluidConstants.BUCKET, null) != FluidConstants.BUCKET) {
            return;
        }
        if (!pickup.pickupBlock(null, level, target, level.getBlockState(target)).is(fluidState.getType().getBucket())) {
            return;
        }
        try (Transaction transaction = Transaction.openOuter()) {
            handler.insert(fluid, FluidConstants.BUCKET, transaction);
            transaction.commit();
        }
        level.playSound(null, target, FluidVariantAttributes.getFillSound(fluid), SoundSource.BLOCKS, 1.0f, 1.0f);
        level.gameEvent(null, GameEvent.FLUID_PICKUP, target);
    }

    public FluidInventoryHandler getHandler() {
        return handler;
    }

    @Override
    public int getComparatorSignal() {
        double fullness = 0;
        boolean hasContents = false;
        for (int tank = 0; tank < type.getSlots(); tank++) {
            if (!handler.getResource(tank).isBlank()) {
                hasContents = true;
                long capacity = handler.getSlotLimit(tank);
                fullness += capacity <= 0 ? 0 : Math.min(1D, handler.getAmount(tank) / (double) capacity);
            }
        }
        return comparatorSignal(fullness / type.getSlots(), hasContents);
    }

    public boolean isEverythingEmpty() {
        // A broken drawer keeps its priority, like the original
        if (getPriority() != 0)
            return false;
        for (int i = 0; i < type.getSlots(); i++) {
            if (handler.getAmount(i) > 0) {
                return false;
            }
        }
        // Also check if upgrades are empty
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

    public DrawerType getDrawerType() {
        return type;
    }

    private void playTransferSound(int slot, Player player, InteractionHand hand, FluidVariant beforeResource,
            long beforeAmount) {
        if (level == null || level.isClientSide() || slot < 0) {
            return;
        }

        FluidVariant afterResource = handler.getResource(slot);
        long afterAmount = handler.getAmount(slot);

        if (afterAmount > beforeAmount) {
            playFluidSound(afterResource.isBlank() ? beforeResource : afterResource, true);
            return;
        }
        if (afterAmount < beforeAmount) {
            playFluidSound(beforeResource.isBlank() ? afterResource : beforeResource, false);
            return;
        }

        FluidVariant heldFluid = getHeldFluid(player, hand);
        if (!beforeResource.isBlank()) {
            playFluidSound(beforeResource, false);
        } else if (!afterResource.isBlank()) {
            playFluidSound(afterResource, true);
        } else if (!heldFluid.isBlank()) {
            playFluidSound(heldFluid, true);
        }
    }

    private void playFluidSound(FluidVariant fluid, boolean storingIntoDrawer) {
        if (level == null || fluid.isBlank()) {
            return;
        }

        SoundEvent sound = storingIntoDrawer ? FluidVariantAttributes.getEmptySound(fluid)
                : FluidVariantAttributes.getFillSound(fluid);
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
    }

    private FluidVariant getHeldFluid(Player player, InteractionHand hand) {
        ContainerItemContext context = ContainerItemContext.ofPlayerHand(player, hand);
        Storage<FluidVariant> storage = context.find(FluidStorage.ITEM);
        if (storage == null) {
            return FluidVariant.blank();
        }

        for (StorageView<FluidVariant> view : storage) {
            if (!view.isResourceBlank() && view.getAmount() > 0) {
                return view.getResource();
            }
        }

        return FluidVariant.blank();
    }

    @Override
    public void setLocked(boolean locked) {
        super.setLocked(locked);
        // When locking, capture current fluids in filterStack (matching Forge
        // BigFluidHandler.lockHandler)
        if (locked) {
            handler.lockHandler();
        }
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new DrawerMenu(containerId, playerInventory, getStorageUpgrades(), getUtilityUpgrades(), this);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(net.minecraft.server.level.ServerPlayer player) {
        return new BlockPosPayload(this.getBlockPos());
    }
}
