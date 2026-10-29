package com.koudesuk.functionalstorage.compat.jei;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/**
 * JEI support, a port of the original's JEIPlugin, loaded through the "jei_mod_plugin" entrypoint.
 */
public class JEIPlugin implements IModPlugin {

    public static final Identifier PLUGIN_ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "main");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CompactingRecipeCategory());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // The client only knows the compacting recipes because FunctionalStorageRecipes syncs them
        var level = Minecraft.getInstance().level;
        if (level == null)
            return;
        List<CustomCompactingRecipe> recipes = level.recipeAccess().getSynchronizedRecipes()
                .getAllOfType(CustomCompactingRecipe.Type.INSTANCE).stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(CompactingRecipeCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addCraftingStation(CompactingRecipeCategory.TYPE, FunctionalStorageBlocks.COMPACTING_DRAWER,
                FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER, FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER,
                FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Drawers that carry contents are told apart, like the original's InventorySubtypeInterpreter
        ISubtypeInterpreter<ItemStack> tile = (stack, context) -> stack.get(FSAttachments.TILE);
        FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(blocks -> blocks
                .forEach(block -> registration.registerSubtypeInterpreter(block.asItem(), tile)));
        FunctionalStorageBlocks.FRAMED_DRAWER.forEach(block -> registration.registerSubtypeInterpreter(block.asItem(), tile));
    }

    @Override
    public Identifier getPluginUid() {
        return PLUGIN_ID;
    }
}
