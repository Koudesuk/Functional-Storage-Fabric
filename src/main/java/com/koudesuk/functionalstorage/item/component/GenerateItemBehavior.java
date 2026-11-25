package com.koudesuk.functionalstorage.item.component;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Puts items into the drawer it is installed in, like the original's Obsidian Generator Upgrade.
 * The item is written like the original's ({@code {"id": ..., "count": ...}}, ItemStack.STRICT_CODEC).
 */
public record GenerateItemBehavior(ItemStack item) implements FunctionalUpgradeBehavior {
    public static final MapCodec<GenerateItemBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            ItemStack.STRICT_CODEC.fieldOf("item").forGetter(GenerateItemBehavior::item))
            .apply(in, GenerateItemBehavior::new));

    @Override
    public void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, Direction.UP);
        if (storage != null) {
            try (Transaction transaction = Transaction.openOuter()) {
                storage.insert(ItemVariant.of(item), item.getCount(), transaction);
                transaction.commit();
            }
        }
    }

    @Override
    public MapCodec<? extends FunctionalUpgradeBehavior> codec() {
        return CODEC;
    }

    // ItemStack has no value equality, and component values must have it, or two upgrades carrying the
    // same behavior would never stack
    @Override
    public boolean equals(Object other) {
        return other instanceof GenerateItemBehavior behavior && ItemStack.matches(item, behavior.item);
    }

    @Override
    public int hashCode() {
        return 31 * ItemStack.hashItemAndComponents(item) + item.getCount();
    }

    @Override
    public List<Component> getTooltip() {
        var list = FunctionalUpgradeBehavior.super.getTooltip();
        list.add(Component.translatable("functionalupgrade.desc.generate_item", item.getCount(),
                item.getHoverName()));
        return list;
    }
}
