package com.koudesuk.functionalstorage.block.config;

public class FunctionalStorageConfig {
    public static int UPGRADE_TICK = 4;
    public static int UPGRADE_PULL_ITEMS = 4;
    public static int UPGRADE_COLLECTOR_ITEMS = 4;
    public static int FLUID_DIVISOR = 1; // Placeholder
    public static int RANGE_DIVISOR = 1; // Placeholder
    public static int DRAWER_CONTROLLER_LINKING_RANGE = 8;
    public static int ARMORY_CABINET_SIZE = 4096;

    public static int getLevelMult(int level) {
        if (level == -1)
            return Integer.MAX_VALUE; // Creative/Max
        if (level == 0)
            return 1; // Iron/Downgrade? No, Iron is 0 in enum but logic might be different.
        // Forge code: IRON(0), COPPER(1), GOLD(2), DIAMOND(3), NETHERITE(4)
        // Multipliers: Copper=8, Gold=16, Diamond=24, Netherite=32
        // Iron (downgrade) -> 1 (handled in tile logic as 64 items limit)
        // Wait, StorageUpgradeItem.getStorageMultiplier calls this.
        // Let's assume standard multipliers.
        // Example: 1->8, 2->64? No, usually linear or exponential.
        // Functional Storage usually: Copper x8, Gold x16, Diamond x24, Netherite x32
        // So it's level * 8?
        // Let's use level * 8 for now.
        // Actually, let's check the Forge code if possible, or just implement sensible
        // defaults.
        // Forge code uses config.
        // I'll implement a simple multiplier for now.
        return Math.max(1, level * 8);
    }
}
