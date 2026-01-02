package com.koudesuk.functionalstorage.item;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageUpgradeItem extends UpgradeItem {

    public static enum StorageTier {
        COPPER(1, getColorFromRgb(204, 109, 81)),
        GOLD(2, getColorFromRgb(233, 177, 21)),
        DIAMOND(3, getColorFromRgb(32, 197, 181)),
        NETHERITE(4, getColorFromRgb(49, 41, 42)),
        IRON(0, getColorFromRgb(130, 130, 130)),
        MAX_STORAGE(-1, getColorFromRgb(167, 54, 247));

        private final int level;
        private final int color;

        StorageTier(int level, int color) {
            this.level = level;
            this.color = color;
        }

        public int getLevel() {
            return level;
        }

        public int getColor() {
            return color;
        }

        private static int getColorFromRgb(int r, int g, int b) {
            return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
        }
    }

    private final StorageTier storageTier;

    public StorageUpgradeItem(StorageTier tier) {
        super(new Properties(), Type.STORAGE);
        this.storageTier = tier;
    }

    public int getStorageMultiplier() {
        return FunctionalStorageConfig.getLevelMult(storageTier.getLevel());
    }

    public StorageTier getStorageTier() {
        return storageTier;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return storageTier == StorageTier.MAX_STORAGE;
    }

    @Override
    public Component getName(ItemStack stack) {
        Component component = super.getName(stack);
        if (component instanceof MutableComponent mutableComponent) {
            if (storageTier == StorageTier.NETHERITE) {
                // Rainbow effect using system time - cycles through HSV colors
                // Using System.currentTimeMillis() to avoid client-only Minecraft imports
                int color = Mth.hsvToRgb((System.currentTimeMillis() / 50 % 360) / 360f, 1, 1);
                mutableComponent.setStyle(Style.EMPTY.withColor(color));
            } else {
                mutableComponent.setStyle(Style.EMPTY.withColor(storageTier.getColor()));
            }
        }
        return component;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
            TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltip, isAdvanced);
        if (storageTier == StorageTier.IRON) {
            tooltip.add(Component.translatable("item.utility.downgrade").withStyle(ChatFormatting.GRAY));
        } else {
            java.text.DecimalFormat format = new java.text.DecimalFormat();
            tooltip.add(Component.translatable("storageupgrade.desc.item").withStyle(ChatFormatting.GRAY)
                    .append(format.format(getStorageMultiplier())));
            tooltip.add(Component.translatable("storageupgrade.desc.fluid").withStyle(ChatFormatting.GRAY)
                    .append(format.format(getStorageMultiplier() / FunctionalStorageConfig.FLUID_DIVISOR)));
            tooltip.add(Component.translatable("storageupgrade.desc.range",
                    format.format(getStorageMultiplier() / FunctionalStorageConfig.RANGE_DIVISOR))
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
