package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class FunctionalStorageGroup {

        public static CreativeModeTab TAB;

        public static void register() {
                TAB = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "main"), FabricItemGroup.builder()
                                                .icon(() -> new ItemStack(FunctionalStorageBlocks.DRAWER_CONTROLLER))
                                                .title(Component.translatable("itemGroup.functionalstorage"))
                                                .displayItems((displayContext, entries) -> {
                                                        // Add all drawer types (includes regular drawers AND fluid
                                                        // drawers)
                                                        FunctionalStorageBlocks.DRAWER_TYPES.values()
                                                                        .forEach(list -> list.forEach(entries::accept));

                                                        // Add framed drawers
                                                        FunctionalStorageBlocks.FRAMED_DRAWER.forEach(entries::accept);

                                                        // Add special blocks
                                                        entries.accept(FunctionalStorageBlocks.DRAWER_CONTROLLER);
                                                        entries.accept(FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER);
                                                        entries.accept(FunctionalStorageBlocks.ARMORY_CABINET);
                                                        entries.accept(FunctionalStorageBlocks.COMPACTING_DRAWER);
                                                        entries.accept(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER);
                                                        entries.accept(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER);
                                                        entries.accept(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER);
                                                        entries.accept(FunctionalStorageBlocks.ENDER_DRAWER);
                                                        entries.accept(FunctionalStorageBlocks.CONTROLLER_EXTENSION);

                                                        // Add Fluid Drawers explicitly (not in DRAWER_TYPES)
                                                        entries.accept(FunctionalStorageBlocks.FLUID_DRAWER_1);
                                                        entries.accept(FunctionalStorageBlocks.FLUID_DRAWER_2);
                                                        entries.accept(FunctionalStorageBlocks.FLUID_DRAWER_4);

                                                        // Add tools
                                                        entries.accept(FunctionalStorageItems.LINKING_TOOL);
                                                        entries.accept(FunctionalStorageItems.CONFIGURATION_TOOL);

                                                        // Add storage upgrades
                                                        entries.accept(FunctionalStorageItems.STORAGE_UPGRADES
                                                                        .get(com.koudesuk.functionalstorage.item.StorageUpgradeItem.StorageTier.COPPER));
                                                        entries.accept(FunctionalStorageItems.STORAGE_UPGRADES
                                                                        .get(com.koudesuk.functionalstorage.item.StorageUpgradeItem.StorageTier.GOLD));
                                                        entries.accept(FunctionalStorageItems.STORAGE_UPGRADES
                                                                        .get(com.koudesuk.functionalstorage.item.StorageUpgradeItem.StorageTier.DIAMOND));
                                                        entries.accept(FunctionalStorageItems.STORAGE_UPGRADES
                                                                        .get(com.koudesuk.functionalstorage.item.StorageUpgradeItem.StorageTier.NETHERITE));
                                                        entries.accept(FunctionalStorageItems.STORAGE_UPGRADES
                                                                        .get(com.koudesuk.functionalstorage.item.StorageUpgradeItem.StorageTier.IRON));
                                                        entries.accept(FunctionalStorageItems.STORAGE_UPGRADES
                                                                        .get(com.koudesuk.functionalstorage.item.StorageUpgradeItem.StorageTier.MAX_STORAGE));

                                                        // Add utility upgrades
                                                        entries.accept(FunctionalStorageItems.PULLING_UPGRADE);
                                                        entries.accept(FunctionalStorageItems.PUSHING_UPGRADE);
                                                        entries.accept(FunctionalStorageItems.COLLECTOR_UPGRADE);
                                                        entries.accept(FunctionalStorageItems.REDSTONE_UPGRADE);
                                                        entries.accept(FunctionalStorageItems.VOID_UPGRADE);
                                                        entries.accept(FunctionalStorageItems.CREATIVE_UPGRADE);
                                                })
                                                .build());
        }
}
