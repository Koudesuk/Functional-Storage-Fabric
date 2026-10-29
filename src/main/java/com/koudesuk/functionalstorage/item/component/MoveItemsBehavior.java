package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Moves items between an item drawer and the inventory the upgrade faces, like the original's Pulling
 * ({@code drawer_is_source: false}) and Pushing Upgrades.
 */
public record MoveItemsBehavior(boolean drawerIsSource, int itemsPerOperation) implements FunctionalUpgradeBehavior {
    public static final MapCodec<MoveItemsBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.BOOL.fieldOf("drawer_is_source").forGetter(MoveItemsBehavior::drawerIsSource),
            Codec.INT.fieldOf("items_per_operation").forGetter(MoveItemsBehavior::itemsPerOperation))
            .apply(in, MoveItemsBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        if (drawer instanceof ItemControllableDrawerTile<?> itemDrawer)
            itemDrawer.moveItems(level, pos, upgradeStack, drawerIsSource, itemsPerOperation);
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
