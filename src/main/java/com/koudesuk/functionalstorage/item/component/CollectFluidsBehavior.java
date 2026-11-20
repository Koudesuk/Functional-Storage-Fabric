package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Picks up the fluid source the upgrade faces, like the original's Collector Upgrade in a fluid drawer.
 */
public record CollectFluidsBehavior() implements FunctionalUpgradeBehavior {
    public static final CollectFluidsBehavior INSTANCE = new CollectFluidsBehavior();
    public static final Codec<CollectFluidsBehavior> CODEC = Codec.unit(INSTANCE);

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        if (drawer instanceof FluidDrawerTile fluidDrawer && !level.isClientSide())
            fluidDrawer.collectFluid(level, pos, upgradeStack);
    }

    @Override
    public Codec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
