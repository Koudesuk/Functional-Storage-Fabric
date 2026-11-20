package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Gives a redstone signal from how full the slot set on the upgrade is (its "Slot" NBT, like the Redstone
 * Upgrade's), like the original's Redstone Upgrade.
 */
public record EmitRedstoneBehavior() implements FunctionalUpgradeBehavior {
    public static final EmitRedstoneBehavior INSTANCE = new EmitRedstoneBehavior();
    public static final Codec<EmitRedstoneBehavior> CODEC = Codec.unit(INSTANCE);

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        if (level.getGameTime() % 20 == 0) {
            level.updateNeighborsAt(pos, drawer.getBlockState().getBlock());
        }
    }

    @Override
    public boolean canConnectRedstone(Level level, BlockPos blockPos, BlockState state,
            ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        return drawer instanceof ItemControllableDrawerTile<?> || drawer instanceof FluidDrawerTile;
    }

    @Override
    public int getRedstoneSignal(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer,
            Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        int redstoneSlot = UpgradeItem.getSlot(upgradeStack);
        if (drawer instanceof ItemControllableDrawerTile<?> itemDrawer) {
            StorageView<?> view = slot(itemDrawer.getStorage(), redstoneSlot);
            if (view == null)
                return -1;
            int amount = view.getCapacity() <= 0 ? 0
                    : (int) Math.floor(view.getAmount() * 14f / view.getCapacity());
            return amount + (amount > 0 ? 1 : 0);
        }
        if (drawer instanceof FluidDrawerTile fluidDrawer) {
            StorageView<?> view = slot(fluidDrawer.getHandler(), redstoneSlot);
            if (view == null)
                return -1;
            return view.getCapacity() <= 0 ? 0 : (int) Math.floor(view.getAmount() * 15f / view.getCapacity());
        }
        return -1;
    }

    private static StorageView<?> slot(Iterable<? extends StorageView<?>> storage, int slot) {
        int index = 0;
        for (StorageView<?> view : storage) {
            if (index++ == slot)
                return view;
        }
        return null;
    }

    @Override
    public Codec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
