package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

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
