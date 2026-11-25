package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.ItemControllableDrawerTile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Picks up the item entities in the block the upgrade faces, like the original's Collector Upgrade.
 */
public record CollectItemEntitiesBehavior() implements FunctionalUpgradeBehavior {
    public static final CollectItemEntitiesBehavior INSTANCE = new CollectItemEntitiesBehavior();
    public static final MapCodec<CollectItemEntitiesBehavior> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        if (drawer instanceof ItemControllableDrawerTile<?> itemDrawer)
            itemDrawer.collectItems(level, pos, upgradeStack);
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }
}
