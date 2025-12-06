package com.koudesuk.functionalstorage.util;

import java.text.DecimalFormat;

public class NumberUtils {

    private static DecimalFormat formatterWithUnits = new DecimalFormat("####0.#");

    public static String getFormatedBigNumber(int number) {
        if (number >= 1000000000) { // BILLION
            float numb = number / 1000000000F;
            return formatterWithUnits.format(numb) + "B";
        } else if (number >= 1000000) { // MILLION
            float numb = number / 1000000F;
            if (number > 100000000)
                numb = Math.round(numb);
            return formatterWithUnits.format(numb) + "M";
        } else if (number >= 1000) { // THOUSANDS
            float numb = number / 1000F;
            if (number > 100000)
                numb = Math.round(numb);
            return formatterWithUnits.format(numb) + "K";
        }
        return String.valueOf(number);
    }

    public static String getFormatedFluidBigNumber(int number) {
        if (number < 1000)
            return String.valueOf(number) + " mB";
        if (number >= 1000000000) { // BILLION
            float numb = number / 1000000000F;
            return formatterWithUnits.format(numb) + "M B";
        } else if (number >= 1000000) { // MILLION
            float numb = number / 1000000F;
            if (number > 100000000)
                numb = Math.round(numb);
            return formatterWithUnits.format(numb) + "K B";
        } else if (number >= 1000) { // THOUSANDS
            float numb = number / 1000F;
            if (number > 100000)
                numb = Math.round(numb);
            return formatterWithUnits.format(numb) + " B";
        }
        return String.valueOf(number) + " B";
    }

    /**
     * Formats fluid amount from droplets to millibuckets for display.
     * Fabric uses 81000 droplets per bucket (1000 mB per bucket).
     */
    public static String getFormatedFluidBigNumber(long droplets) {
        // Convert droplets to millibuckets (81 droplets = 1 mB)
        long mB = droplets / 81;
        return getFormatedFluidBigNumber((int) Math.min(mB, Integer.MAX_VALUE));
    }
}
