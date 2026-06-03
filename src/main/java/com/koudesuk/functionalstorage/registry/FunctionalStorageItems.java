package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.item.UpgradeItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
                LINKING_TOOL = registerItem("linking_tool", LinkingToolItem::new);
                CONFIGURATION_TOOL = registerItem("configuration_tool",
                                ConfigurationToolItem::new);

                for (StorageUpgradeItem.StorageTier tier : StorageUpgradeItem.StorageTier.values()) {
                        String itemName = tier == StorageUpgradeItem.StorageTier.IRON
                                        ? "iron_downgrade"
                                        : tier.name().toLowerCase() + "_upgrade";
                        STORAGE_UPGRADES.put(tier,
                                        registerItem(itemName,
                                                        properties -> new StorageUpgradeItem(tier, properties)));
                }

                PULLING_UPGRADE = registerItem("puller_upgrade",
                                properties -> new UpgradeItem(properties, UpgradeItem.Type.UTILITY));
                PUSHING_UPGRADE = registerItem("pusher_upgrade",
                                properties -> new UpgradeItem(properties, UpgradeItem.Type.UTILITY));
                COLLECTOR_UPGRADE = registerItem("collector_upgrade",
                                properties -> new UpgradeItem(properties, UpgradeItem.Type.UTILITY));
                REDSTONE_UPGRADE = registerItem("redstone_upgrade",
                                properties -> new UpgradeItem(properties, UpgradeItem.Type.UTILITY));
                VOID_UPGRADE = registerItem("void_upgrade",
                                properties -> new UpgradeItem(properties, UpgradeItem.Type.UTILITY));
                CREATIVE_UPGRADE = registerItem("creative_vending_upgrade",
                                properties -> new UpgradeItem(properties, UpgradeItem.Type.STORAGE)); // Creative is
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
                 * CreativeModeTabEvents.modifyOutput(CreativeModeTabs.FUNCTIONAL_BLOCKS).
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

        private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
                T item = factory.apply(itemProperties(name));
                return Registry.register(BuiltInRegistries.ITEM,
                                id(name),
                                item);
        }

        private static Item.Properties itemProperties(String name) {
                return new Item.Properties().setId(itemKey(name));
        }

        private static ResourceKey<Item> itemKey(String name) {
                return ResourceKey.create(Registries.ITEM, id(name));
        }

        private static Identifier id(String name) {
                return Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, name);
        }
}
