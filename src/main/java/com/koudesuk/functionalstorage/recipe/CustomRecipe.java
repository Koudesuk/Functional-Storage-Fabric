package com.koudesuk.functionalstorage.recipe;

import net.minecraft.world.item.crafting.CraftingBookCategory;
// import net.minecraft.world.item.crafting.CustomRecipe; // Removed to avoid conflict
// import net.minecraft.world.java.util.function.Consumer; // Fixed below

// In 1.21 Fabric/Mojang CustomRecipe is abstract class in crafting package
// We need to match what FramedDrawerRecipe extends.
public abstract class CustomRecipe implements net.minecraft.world.item.crafting.CraftingRecipe {
    public CustomRecipe(CraftingBookCategory category) {
        // No super call needed for interface
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeType<?> getType() {
        return net.minecraft.world.item.crafting.RecipeType.CRAFTING;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
