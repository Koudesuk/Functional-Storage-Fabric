package com.koudesuk.functionalstorage.util;

import com.koudesuk.functionalstorage.FunctionalStorage;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class StorageTags {

    public static final TagKey<Item> DRAWER = TagKey.create(Registries.ITEM,
            new ResourceLocation(FunctionalStorage.MOD_ID, "drawer"));
    public static final TagKey<Item> IGNORE_CRAFTING_CHECK = TagKey.create(Registries.ITEM,
            new ResourceLocation(FunctionalStorage.MOD_ID, "ignore_crafting_check"));

}
