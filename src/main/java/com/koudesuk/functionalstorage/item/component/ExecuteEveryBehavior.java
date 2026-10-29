package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Runs another behavior every {@code speed} ticks.
 */
public record ExecuteEveryBehavior(int ticks, FunctionalUpgradeBehavior behavior) implements FunctionalUpgradeBehavior {
    public static final MapCodec<ExecuteEveryBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("speed").forGetter(ExecuteEveryBehavior::ticks),
            FunctionalUpgradeBehavior.CODEC.fieldOf("behavior").forGetter(ExecuteEveryBehavior::behavior))
            .apply(in, ExecuteEveryBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        if (level.getGameTime() % ticks == 0) {
            behavior.work(level, pos, drawer, upgradeStack, upgradeSlot);
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }

    @Override
    public List<Component> getTooltip() {
        var list = FunctionalUpgradeBehavior.super.getTooltip();
        list.add(Component.translatable("functionalupgrade.desc.execute_every_tick", ticks)
                .withStyle(ChatFormatting.YELLOW));
        for (Component component : behavior.getTooltip()) {
            list.add(Component.literal(" ").append(component));
        }
        return list;
    }
}
