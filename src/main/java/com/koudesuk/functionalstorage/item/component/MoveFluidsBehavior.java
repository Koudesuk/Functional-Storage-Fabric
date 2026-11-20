package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Moves fluid between a fluid drawer and the tank the upgrade faces, like the original's Pulling and
 * Pushing Upgrades. Amounts are in millibuckets, as in the original's data.
 */
public record MoveFluidsBehavior(boolean drawerIsSource, int fluidPerOperation) implements FunctionalUpgradeBehavior {
    public static final Codec<MoveFluidsBehavior> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.BOOL.fieldOf("drawer_is_source").forGetter(MoveFluidsBehavior::drawerIsSource),
            Codec.INT.fieldOf("fluid_per_operation").forGetter(MoveFluidsBehavior::fluidPerOperation))
            .apply(in, MoveFluidsBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        if (drawer instanceof FluidDrawerTile fluidDrawer)
            fluidDrawer.moveFluids(level, pos, upgradeStack, drawerIsSource, fluidPerOperation);
    }

    @Override
    public Codec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
