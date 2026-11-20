package com.koudesuk.functionalstorage.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * The items of the {@code functionalstorage:incompatible_upgrades} component: item ids and {@code #tags},
 * like the HolderSet the original stores there.
 */
public record UpgradeSet(List<String> entries) {

    public static final Codec<UpgradeSet> CODEC = Codec.STRING.listOf().comapFlatMap(UpgradeSet::validate,
            UpgradeSet::entries);

    public static UpgradeSet of(Item... items) {
        List<String> entries = new ArrayList<>();
        for (Item item : items)
            entries.add(BuiltInRegistries.ITEM.getKey(item).toString());
        return new UpgradeSet(List.copyOf(entries));
    }

    private static DataResult<UpgradeSet> validate(List<String> entries) {
        for (String entry : entries) {
            if (ResourceLocation.tryParse(entry.startsWith("#") ? entry.substring(1) : entry) == null)
                return DataResult.error(() -> "Not an item or tag id: " + entry);
        }
        return DataResult.success(new UpgradeSet(List.copyOf(entries)));
    }

    public boolean contains(ItemStack stack) {
        for (String entry : entries) {
            if (entry.startsWith("#")) {
                if (stack.is(TagKey.create(Registries.ITEM, new ResourceLocation(entry.substring(1)))))
                    return true;
            } else if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(new ResourceLocation(entry))) {
                return true;
            }
        }
        return false;
    }
}
