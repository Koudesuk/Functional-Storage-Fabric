package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FunctionalStorageRecipes {

    public static void register() {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CustomCompactingRecipe.Serializer.ID,
                CustomCompactingRecipe.Serializer.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_TYPE,
                new ResourceLocation(FunctionalStorage.MOD_ID, CustomCompactingRecipe.Type.ID),
                CustomCompactingRecipe.Type.INSTANCE);
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(FunctionalStorage.MOD_ID, "framed_recipe"),
                com.koudesuk.functionalstorage.recipe.FramedDrawerRecipe.SERIALIZER);
    }
}
