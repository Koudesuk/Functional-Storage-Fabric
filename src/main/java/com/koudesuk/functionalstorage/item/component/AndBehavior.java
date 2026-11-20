package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Runs several behaviors.
 */
public record AndBehavior(List<FunctionalUpgradeBehavior> behaviors) implements FunctionalUpgradeBehavior {
    public static final Codec<AndBehavior> CODEC = ExtraCodecs.nonEmptyList(FunctionalUpgradeBehavior.CODEC.listOf())
            .fieldOf("behaviors")
            .xmap(AndBehavior::new, AndBehavior::behaviors).codec();

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        for (FunctionalUpgradeBehavior behavior : behaviors) {
            behavior.work(level, pos, drawer, upgradeStack, upgradeSlot);
        }
    }

    @Override
    public Codec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }

    @Override
    public List<Component> getTooltip() {
        var list = FunctionalUpgradeBehavior.super.getTooltip();
        for (FunctionalUpgradeBehavior behavior : behaviors) {
            list.addAll(behavior.getTooltip());
        }
        return list;
    }
}
