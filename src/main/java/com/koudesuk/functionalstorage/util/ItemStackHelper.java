package com.koudesuk.functionalstorage.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Utility class for ItemStack NBT operations in Minecraft 1.21+
 * Replaces deprecated methods like hasTag(), getTag(), getOrCreateTag()
 * with the new Component API equivalents.
 */
public final class ItemStackHelper {

    private ItemStackHelper() {
    } // Utility class

    /**
     * Check if the ItemStack has custom data (replaces stack.hasTag())
     */
    public static boolean hasCustomData(ItemStack stack) {
        return stack.has(DataComponents.CUSTOM_DATA);
    }

    /**
     * Get custom data from ItemStack, or empty tag if none (replaces
     * stack.getTag())
     */
    public static CompoundTag getCustomData(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        return customData != null ? customData.copyTag() : new CompoundTag();
    }

    /**
     * Get or create custom data (replaces stack.getOrCreateTag())
     */
    public static CompoundTag getOrCreateCustomData(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            return stack.get(DataComponents.CUSTOM_DATA).copyTag();
        }
        return new CompoundTag();
    }

    /**
     * Set custom data on ItemStack (replaces stack.setTag() /
     * stack.getOrCreateTag().put())
     */
    public static void setCustomData(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Put a compound tag into custom data (replaces stack.getOrCreateTag().put(key,
     * value))
     */
    public static void putCompound(ItemStack stack, String key, CompoundTag value) {
        CompoundTag tag = getOrCreateCustomData(stack);
        tag.put(key, value);
        setCustomData(stack, tag);
    }

    /**
     * Put a boolean into custom data (replaces stack.getOrCreateTag().putBoolean())
     */
    public static void putBoolean(ItemStack stack, String key, boolean value) {
        CompoundTag tag = getOrCreateCustomData(stack);
        tag.putBoolean(key, value);
        setCustomData(stack, tag);
    }

    /**
     * Put an int into custom data (replaces stack.getOrCreateTag().putInt())
     */
    public static void putInt(ItemStack stack, String key, int value) {
        CompoundTag tag = getOrCreateCustomData(stack);
        tag.putInt(key, value);
        setCustomData(stack, tag);
    }

    /**
     * Get a compound tag from custom data (replaces stack.getTag().getCompound())
     */
    public static CompoundTag getCompound(ItemStack stack, String key) {
        CompoundTag tag = getCustomData(stack);
        return tag.getCompound(key);
    }

    /**
     * Get a boolean from custom data (replaces stack.getTag().getBoolean())
     */
    public static boolean getBoolean(ItemStack stack, String key) {
        CompoundTag tag = getCustomData(stack);
        return tag.getBoolean(key);
    }

    /**
     * Get an int from custom data (replaces stack.getTag().getInt() /
     * getOrCreateTag().getInt())
     */
    public static int getInt(ItemStack stack, String key) {
        CompoundTag tag = getCustomData(stack);
        return tag.getInt(key);
    }

    /**
     * Check if custom data contains a key (replaces stack.getTag().contains())
     */
    public static boolean containsKey(ItemStack stack, String key) {
        if (!hasCustomData(stack))
            return false;
        return getCustomData(stack).contains(key);
    }

    /**
     * Save BlockEntity data without metadata using registry access
     * (replaces tile.saveWithoutMetadata())
     */
    public static CompoundTag saveBlockEntityData(BlockEntity tile) {
        if (tile.getLevel() != null) {
            return tile.saveWithoutMetadata(tile.getLevel().registryAccess());
        }
        // Fallback if level is null - create a new CompoundTag and save manually
        return tile.saveWithFullMetadata(tile.getLevel() != null ? tile.getLevel().registryAccess() : null);
    }

    /**
     * Save BlockEntity data without metadata using provided registry access
     */
    public static CompoundTag saveBlockEntityData(BlockEntity tile, HolderLookup.Provider registryAccess) {
        return tile.saveWithoutMetadata(registryAccess);
    }
}
