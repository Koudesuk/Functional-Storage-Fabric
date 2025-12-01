package com.koudesuk.functionalstorage.util;

import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Function;

public enum DrawerType {
    X_1(1, 32 * 64, "1x1", integer -> Pair.of(16, 16)),
    X_2(2, 16 * 64, "1x2", integer -> {
        if (integer == 0)
            return Pair.of(16, 28);
        return Pair.of(16, 4);
    }),
    X_4(4, 8 * 64, "2x2", integer -> {
        if (integer == 0)
            return Pair.of(28, 28);
        if (integer == 1)
            return Pair.of(4, 28);
        if (integer == 2)
            return Pair.of(28, 4);
        return Pair.of(4, 4);
    });

    private final int slots;
    private final int slotAmount;
    private final String displayName;
    private final Function<Integer, Pair<Integer, Integer>> slotPosition;

    private DrawerType(int slots, int slotAmount, String displayName,
            Function<Integer, Pair<Integer, Integer>> slotPosition) {
        this.slots = slots;
        this.slotAmount = slotAmount;
        this.displayName = displayName;
        this.slotPosition = slotPosition;
    }

    public int getSlots() {
        return slots;
    }

    public int getSlotAmount() {
        return slotAmount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Function<Integer, Pair<Integer, Integer>> getSlotPosition() {
        return slotPosition;
    }
}
