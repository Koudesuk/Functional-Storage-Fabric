package com.koudesuk.functionalstorage.block.config;

import com.koudesuk.functionalstorage.block.config.ConfigFile.IntValue;

/**
 * Gameplay settings, stored in config/functionalstorage-common.properties.
 * The defaults are the values the mod has always used.
 */
public final class FunctionalStorageConfig {

    public static final ConfigFile FILE = new ConfigFile("functionalstorage-common", """
            Functional Storage - common settings
            Restart the game or server after editing this file.
            In multiplayer the server sends its values to every client that joins, so only the server copy matters.
            Numbers outside their range are clamped, unreadable ones fall back to the default.""");

    // Storage Controller

    public static final IntValue DRAWER_CONTROLLER_LINKING_RANGE = FILE.defineInt("controller.linking_range",
            "Radius, in blocks, around a Storage Controller in which drawers can be linked, before upgrades.",
            8, 0, 1024);

    public static final IntValue RANGE_DIVISOR = FILE.defineInt("controller.range_divisor", """
            Storage upgrades inside a controller extend its reach instead of a capacity.
            Reach = linking_range + the product of (upgrade multiplier / range_divisor, rounded down) over all upgrades.
            With the defaults one Netherite Upgrade gives 8 + 32 = 40 blocks.""",
            1, 1, Integer.MAX_VALUE);

    // Storage

    public static final IntValue DRAWER_1X1_STACKS = FILE.defineInt("storage.drawer_1x1_stacks", """
            Base capacity of a 1x1 drawer slot, counted in stacks of 64 items (32 = 2048 items).
            Fluid drawers of the same size hold this many buckets per slot. Ender Drawers are based on this value too.""",
            32, 1, 1_000_000);

    public static final IntValue DRAWER_1X2_STACKS = FILE.defineInt("storage.drawer_1x2_stacks",
            "Base capacity of each slot of a 1x2 drawer, counted in stacks of 64 items (buckets for fluid drawers).",
            16, 1, 1_000_000);

    public static final IntValue DRAWER_2X2_STACKS = FILE.defineInt("storage.drawer_2x2_stacks",
            "Base capacity of each slot of a 2x2 drawer, counted in stacks of 64 items (buckets for fluid drawers).",
            8, 1, 1_000_000);

    public static final IntValue COMPACTING_DRAWER_STACKS = FILE.defineInt("storage.compacting_drawer_stacks",
            "Base capacity of a Compacting Drawer, in stacks of its most compacted item (8 stacks = 512 iron blocks).",
            8, 1, 100_000);

    public static final IntValue SIMPLE_COMPACTING_DRAWER_STACKS = FILE.defineInt(
            "storage.simple_compacting_drawer_stacks",
            "Base capacity of a Simple Compacting Drawer, in stacks of its most compacted item.",
            8, 1, 100_000);

    public static final IntValue ENDER_DRAWER_MULTIPLIER = FILE.defineInt("storage.ender_drawer_multiplier",
            "An Ender Drawer holds this many times the base capacity of a 1x1 drawer. Storage upgrades do not apply.",
            4, 1, 1_000_000);

    // Storage upgrades

    public static final IntValue COPPER_MULTIPLIER = FILE.defineInt("upgrades.copper_multiplier",
            "Capacity multiplier of the Copper Upgrade. Several upgrades in one drawer multiply together.",
            8, 1, Integer.MAX_VALUE);

    public static final IntValue GOLD_MULTIPLIER = FILE.defineInt("upgrades.gold_multiplier",
            "Capacity multiplier of the Gold Upgrade.",
            16, 1, Integer.MAX_VALUE);

    public static final IntValue DIAMOND_MULTIPLIER = FILE.defineInt("upgrades.diamond_multiplier",
            "Capacity multiplier of the Diamond Upgrade.",
            24, 1, Integer.MAX_VALUE);

    public static final IntValue NETHERITE_MULTIPLIER = FILE.defineInt("upgrades.netherite_multiplier",
            "Capacity multiplier of the Netherite Upgrade.",
            32, 1, Integer.MAX_VALUE);

    public static final IntValue FLUID_DIVISOR = FILE.defineInt("upgrades.fluid_divisor",
            "Fluid drawers divide every storage upgrade multiplier by this (rounded down, never below 1).",
            1, 1, Integer.MAX_VALUE);

    // Utility upgrades

    public static final IntValue UPGRADE_TICK = FILE.defineInt("upgrades.tick_interval",
            "How often, in ticks, the Pulling, Pushing and Collector upgrades run (20 ticks = 1 second).",
            4, 1, Integer.MAX_VALUE);

    public static final IntValue UPGRADE_PULL_ITEMS = FILE.defineInt("upgrades.pulling_items",
            "Items a Pulling Upgrade moves each time it runs.",
            4, 0, Integer.MAX_VALUE);

    public static final IntValue UPGRADE_PUSH_ITEMS = FILE.defineInt("upgrades.pushing_items",
            "Items a Pushing Upgrade moves each time it runs.",
            4, 0, Integer.MAX_VALUE);

    public static final IntValue UPGRADE_COLLECTOR_ITEMS = FILE.defineInt("upgrades.collector_items",
            "Items a Collector Upgrade takes from each dropped item entity each time it runs.",
            64, 0, Integer.MAX_VALUE);

    // Armory Cabinet

    public static final IntValue ARMORY_CABINET_SIZE = FILE.defineInt("armory.cabinet_size",
            "Number of slots in an Armory Cabinet. Lowering it deletes whatever is stored in the removed slots.",
            4096, 1, 65536);

    private FunctionalStorageConfig() {
    }

    public static int getLevelMult(int level) {
        return switch (level) {
            case -1 -> Integer.MAX_VALUE;
            case 1 -> COPPER_MULTIPLIER.get();
            case 2 -> GOLD_MULTIPLIER.get();
            case 3 -> DIAMOND_MULTIPLIER.get();
            case 4 -> NETHERITE_MULTIPLIER.get();
            default -> 1;
        };
    }
}
