package com.koudesuk.functionalstorage.util;

import com.koudesuk.functionalstorage.block.config.ConfigFile.IntValue;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public enum DrawerType {
    X_1(1, FunctionalStorageConfig.DRAWER_1X1_STACKS, "1x1", integer -> Pair.of(16, 16)),
    X_2(2, FunctionalStorageConfig.DRAWER_1X2_STACKS, "1x2", integer -> {
        if (integer == 0)
            return Pair.of(16, 28);
        return Pair.of(16, 4);
    }),
    X_4(4, FunctionalStorageConfig.DRAWER_2X2_STACKS, "2x2", integer -> {
        if (integer == 0)
            return Pair.of(28, 28);
        if (integer == 1)
            return Pair.of(4, 28);
        if (integer == 2)
            return Pair.of(28, 4);
        return Pair.of(4, 4);
    });

    private final int slots;
    private final IntValue stacks;
    private final String displayName;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;

    private DrawerType(int slots, IntValue stacks, String displayName,
            Function<Integer, Pair<Integer, Integer>> slotPosition) {
        this.slots = slots;
        this.stacks = stacks;
        this.displayName = displayName;
        this.slotPosition = slotPosition;
    }

    public int getSlots() {
        return slots;
    }

    /**
     * Base capacity of one slot in items (buckets * 64 for fluid drawers), before upgrades.
     */
    public int getSlotAmount() {
        return stacks.get() * 64;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Function<Integer, Pair<Integer, Integer>> getSlotPosition() {
        return slotPosition;
    }
}
