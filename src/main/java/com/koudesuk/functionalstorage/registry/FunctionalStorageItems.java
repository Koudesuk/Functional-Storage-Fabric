package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import com.koudesuk.functionalstorage.item.component.ExecuteEveryBehavior;
import com.koudesuk.functionalstorage.item.component.GenerateFluidBehavior;
import com.koudesuk.functionalstorage.item.component.GenerateItemBehavior;
import com.koudesuk.functionalstorage.item.component.UpgradeSet;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.HashMap;
import java.util.Map;

public class FunctionalStorageItems {

        public static LinkingToolItem LINKING_TOOL;
        public static ConfigurationToolItem CONFIGURATION_TOOL;
        public static Map<StorageUpgradeItem.StorageTier, StorageUpgradeItem> STORAGE_UPGRADES = new HashMap<>();
        public static UpgradeItem PULLING_UPGRADE;
        public static UpgradeItem PUSHING_UPGRADE;
        public static UpgradeItem COLLECTOR_UPGRADE;
        public static UpgradeItem REDSTONE_UPGRADE;
        public static UpgradeItem VOID_UPGRADE;
        public static UpgradeItem CREATIVE_UPGRADE;
        public static Item DRIPPING_UPGRADE;
        public static Item WATER_GENERATOR_UPGRADE;
        public static Item OBSIDIAN_UPGRADE;

        public static void register() {
                LINKING_TOOL = registerItem("linking_tool", new LinkingToolItem(new Item.Properties()));
                CONFIGURATION_TOOL = registerItem("configuration_tool",
                                new ConfigurationToolItem(new Item.Properties()));

                for (StorageUpgradeItem.StorageTier tier : StorageUpgradeItem.StorageTier.values()) {
                        String itemName = tier == StorageUpgradeItem.StorageTier.IRON
                                        ? "iron_downgrade"
                                        : tier.name().toLowerCase() + "_upgrade";
                        STORAGE_UPGRADES.put(tier,
                                        registerItem(itemName,
                                                        new StorageUpgradeItem(tier)));
                }

                PULLING_UPGRADE = registerItem("puller_upgrade",
                                new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
                PUSHING_UPGRADE = registerItem("pusher_upgrade",
                                new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
                COLLECTOR_UPGRADE = registerItem("collector_upgrade",
                                new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
                REDSTONE_UPGRADE = registerItem("redstone_upgrade",
                                new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
                VOID_UPGRADE = registerItem("void_upgrade",
                                new UpgradeItem(new Item.Properties(), UpgradeItem.Type.UTILITY));
                CREATIVE_UPGRADE = registerItem("creative_vending_upgrade",
                                new UpgradeItem(new Item.Properties(), UpgradeItem.Type.STORAGE)); // Creative is
                                                                                                   // storage type? Or
                                                                                                   // utility? Forge
                                                                                                   // says
                                                                                                   // STORAGE/UTILITY
                                                                                                   // depending on
                                                                                                   // usage. Let's check
                                                                                                   // UpgradeItem.Type.
                // In Forge, CreativeUpgrade extends UpgradeItem with Type.STORAGE usually, but
                // it acts special.
                // Let's assume Type.STORAGE for now as it provides infinite storage.

                // Plain items whose functional behavior makes them utility upgrades, like the original's
                // 1.6 generator upgrades: they put what they make into the drawer they are installed in
                DRIPPING_UPGRADE = registerItem("dripping_upgrade", new Item(new Item.Properties()));
                FSAttachments.FUNCTIONAL_BEHAVIOR.setDefault(DRIPPING_UPGRADE, new ExecuteEveryBehavior(20,
                                new GenerateFluidBehavior(new GenerateFluidBehavior.FluidStackTemplate(Fluids.LAVA, 20))));
                WATER_GENERATOR_UPGRADE = registerItem("water_generator_upgrade", new Item(new Item.Properties()));
                FSAttachments.FUNCTIONAL_BEHAVIOR.setDefault(WATER_GENERATOR_UPGRADE, new ExecuteEveryBehavior(1,
                                new GenerateFluidBehavior(new GenerateFluidBehavior.FluidStackTemplate(Fluids.WATER, 2000))));
                OBSIDIAN_UPGRADE = registerItem("obsidian_upgrade", new Item(new Item.Properties()));
                FSAttachments.FUNCTIONAL_BEHAVIOR.setDefault(OBSIDIAN_UPGRADE, new ExecuteEveryBehavior(300,
                                new GenerateItemBehavior(new GenerateItemBehavior.ItemStackTemplate(Items.OBSIDIAN))));

                // The Void Upgrade can only be installed once, like the original's incompatible_upgrades
                FSAttachments.INCOMPATIBLE_UPGRADES.setDefault(VOID_UPGRADE, UpgradeSet.of(VOID_UPGRADE));

                // Add to creative tab
                // Items are added to the custom tab in FunctionalStorageGroup.java
                /*
                 * ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).
                 * register(content -> {
                 * content.accept(LINKING_TOOL);
                 * content.accept(CONFIGURATION_TOOL);
                 * STORAGE_UPGRADES.values().forEach(content::accept);
                 * content.accept(PULLING_UPGRADE);
                 * content.accept(PUSHING_UPGRADE);
                 * content.accept(COLLECTOR_UPGRADE);
                 * content.accept(REDSTONE_UPGRADE);
                 * content.accept(VOID_UPGRADE);
                 * content.accept(CREATIVE_UPGRADE);
                 * 
                 * // Also add blocks
                 * FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(list ->
                 * list.forEach(content::accept));
                 * content.accept(FunctionalStorageBlocks.DRAWER_CONTROLLER);
                 * content.accept(FunctionalStorageBlocks.ENDER_DRAWER);
                 * });
                 */
        }

        private static <T extends Item> T registerItem(String name, T item) {
                return Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(FunctionalStorage.MOD_ID, name),
                                item);
        }
}
