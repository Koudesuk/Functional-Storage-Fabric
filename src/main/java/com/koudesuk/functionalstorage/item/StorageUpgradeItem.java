package com.koudesuk.functionalstorage.item;

import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageUpgradeItem extends UpgradeItem {

    public static enum StorageTier {
        COPPER(1),
        GOLD(2),
        DIAMOND(3),
        NETHERITE(4),
        IRON(0),
        MAX_STORAGE(-1);

        private final int level;

        StorageTier(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
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
