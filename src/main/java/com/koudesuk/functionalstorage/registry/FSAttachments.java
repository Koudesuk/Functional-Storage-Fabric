package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.item.component.FunctionalUpgradeBehavior;
import com.koudesuk.functionalstorage.item.component.ItemComponent;
import com.koudesuk.functionalstorage.item.component.SizeProvider;
import com.koudesuk.functionalstorage.item.component.UpgradeSet;
import com.mojang.serialization.Codec;

/**
 * The original's data-driven upgrade components, see {@link ItemComponent}. The other data the original
 * keeps in components (tool modes, upgrade directions, drawer contents) stays in the NBT keys this port
 * has always used, so existing items keep working.
 */
public final class FSAttachments {

    // Data-driven size upgrades like the original: any item with one goes into a storage upgrade slot
    public static final ItemComponent<SizeProvider> ITEM_STORAGE_MODIFIER = create("item_storage_modifier",
            SizeProvider.CODEC);

    public static final ItemComponent<SizeProvider> FLUID_STORAGE_MODIFIER = create("fluid_storage_modifier",
            SizeProvider.CODEC);

    public static final ItemComponent<SizeProvider> CONTROLLER_RANGE_MODIFIER = create("controller_range_modifier",
            SizeProvider.CODEC);

    // What an item does in a utility upgrade slot, and the upgrades it never shares the slots with
    public static final ItemComponent<FunctionalUpgradeBehavior> FUNCTIONAL_BEHAVIOR = create(
            "functional_upgrade_behavior", FunctionalUpgradeBehavior.CODEC);

    public static final ItemComponent<UpgradeSet> INCOMPATIBLE_UPGRADES = create("incompatible_upgrades",
            UpgradeSet.CODEC);

    private FSAttachments() {
    }

    private static <T> ItemComponent<T> create(String name, Codec<T> codec) {
        return new ItemComponent<>(FunctionalStorage.MOD_ID + ":" + name, codec);
    }

    public static void register() {
        // All fields are initialized statically, so calling this method initializes the class
    }
}
