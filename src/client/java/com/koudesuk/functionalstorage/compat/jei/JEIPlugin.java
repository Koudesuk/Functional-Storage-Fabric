package com.koudesuk.functionalstorage.compat.jei;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI support, a port of the original's JEIPlugin, loaded through the "jei_mod_plugin" entrypoint.
 */
@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(FunctionalStorage.MOD_ID, "main");

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new CompactingRecipeCategory());
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        if (level == null)
            return;
        registration.addRecipes(CompactingRecipeCategory.TYPE,
                level.getRecipeManager().getAllRecipesFor(CustomCompactingRecipe.Type.INSTANCE));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
    }

    @Override
    @SuppressWarnings("removal")
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Drawers that carry contents are told apart, like the original's InventorySubtypeInterpreter
        IIngredientSubtypeInterpreter<ItemStack> tile = (stack, context) -> stack.hasTag()
                && stack.getTag().contains("Tile") ? stack.getTag().getCompound("Tile").toString()
                        : IIngredientSubtypeInterpreter.NONE;
        FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(blocks -> blocks
                .forEach(block -> registration.registerSubtypeInterpreter(block.asItem(), tile)));
        FunctionalStorageBlocks.FRAMED_DRAWER.forEach(block -> registration.registerSubtypeInterpreter(block.asItem(), tile));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }
}
