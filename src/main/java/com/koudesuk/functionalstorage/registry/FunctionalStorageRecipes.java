package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.recipe.CopyComponentsRecipe;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import com.koudesuk.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.koudesuk.functionalstorage.recipe.TagWithoutComponentIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class FunctionalStorageRecipes {

        public static void register() {
                Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CustomCompactingRecipe.Serializer.ID,
                                CustomCompactingRecipe.Serializer.INSTANCE);
                // The original's custom_compacting recipes, same id and format
                Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CustomCompactingRecipe.Serializer.CUSTOM_ID,
                                CustomCompactingRecipe.Serializer.CUSTOM_INSTANCE);
                Registry.register(BuiltInRegistries.RECIPE_TYPE,
                                ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
                                                CustomCompactingRecipe.Type.ID),
                                CustomCompactingRecipe.Type.INSTANCE);
                Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                                ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "framed_recipe"),
                                com.koudesuk.functionalstorage.recipe.FramedDrawerRecipe.SERIALIZER);
                Registry.register(BuiltInRegistries.RECIPE_SERIALIZER,
                                ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "copy_components"),
                                CopyComponentsRecipe.SERIALIZER);

                CustomIngredientSerializer.register(TagWithoutComponentIngredient.SERIALIZER);
                CustomIngredientSerializer.register(DrawerlessWoodIngredient.SERIALIZER);
        }
}
