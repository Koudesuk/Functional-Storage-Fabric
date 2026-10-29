package com.koudesuk.functionalstorage.util;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class StorageTags {

        public static final TagKey<Item> DRAWER = TagKey.create(Registries.ITEM,
                        Identifier.parse(FunctionalStorage.MOD_ID + ":drawer"));
        public static final TagKey<Item> IGNORE_CRAFTING_CHECK = TagKey.create(Registries.ITEM,
                        Identifier.parse(FunctionalStorage.MOD_ID + ":ignore_crafting_check"));
        public static final TagKey<Item> ARMORY_CABINET_INSERTABLE = TagKey.create(Registries.ITEM,
                        Identifier.parse(FunctionalStorage.MOD_ID + ":armory_cabinet_insertable"));
        public static final TagKey<Item> DRAWER_STORAGE_DENYLIST = TagKey.create(Registries.ITEM,
                        Identifier.parse(FunctionalStorage.MOD_ID + ":drawer_storage_denylist"));
        public static final TagKey<Item> CREATIVE_VENDING_UPGRADE_INCOMPATIBLE = TagKey.create(Registries.ITEM,
                        Identifier.parse(FunctionalStorage.MOD_ID + ":creative_vending_upgrade_incompatible"));
        public static final TagKey<Fluid> FLUID_DRAWER_STORAGE_DENYLIST = TagKey.create(Registries.FLUID,
                        Identifier.parse(FunctionalStorage.MOD_ID + ":fluid_drawer_storage_denylist"));
        // Same tags as the original: the fluid drawers, and the drawers of each size
        public static final TagKey<Item> FLUID_DRAWER = TagKey.create(Registries.ITEM,
                        Identifier.parse(FunctionalStorage.MOD_ID + ":fluid_drawer"));

        public static TagKey<Item> drawerOfSize(DrawerType type) {
                return TagKey.create(Registries.ITEM,
                                Identifier.parse(FunctionalStorage.MOD_ID + ":drawer_" + type.getDisplayName()));
        }

        // Each tag also takes ids from the common config, for setups without a data pack

        public static boolean isArmoryInsertable(Item item) {
                return item.builtInRegistryHolder().is(ARMORY_CABINET_INSERTABLE)
                                || FunctionalStorageConfig.ARMORY_INSERTABLE_ITEMS.contains(BuiltInRegistries.ITEM.getKey(item));
        }

        public static boolean isDrawerDenied(Item item) {
                return item.builtInRegistryHolder().is(DRAWER_STORAGE_DENYLIST)
                                || FunctionalStorageConfig.DRAWER_DENIED_ITEMS.contains(BuiltInRegistries.ITEM.getKey(item));
        }

        public static boolean isCreativeIncompatible(Item item) {
                return item.builtInRegistryHolder().is(CREATIVE_VENDING_UPGRADE_INCOMPATIBLE)
                                || FunctionalStorageConfig.CREATIVE_INCOMPATIBLE_UPGRADES
                                                .contains(BuiltInRegistries.ITEM.getKey(item));
        }

        public static boolean isFluidDrawerDenied(Fluid fluid) {
                return fluid.builtInRegistryHolder().is(FLUID_DRAWER_STORAGE_DENYLIST)
                                || FunctionalStorageConfig.FLUID_DRAWER_DENIED_FLUIDS
                                                .contains(BuiltInRegistries.FLUID.getKey(fluid));
        }

}
