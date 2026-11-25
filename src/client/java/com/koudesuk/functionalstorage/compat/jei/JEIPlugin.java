package com.koudesuk.functionalstorage.compat.jei;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

/**
 * JEI support, a port of the original's JEIPlugin, loaded through the "jei_mod_plugin" entrypoint.
 */
@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static final ResourceLocation PLUGIN_ID = ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
            "main");

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
                level.getRecipeManager().getAllRecipesFor(CustomCompactingRecipe.Type.INSTANCE).stream()
                        .map(RecipeHolder::value).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER), CompactingRecipeCategory.TYPE);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Drawers that carry contents are told apart, like the original's InventorySubtypeInterpreter
        FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(blocks -> blocks
                .forEach(block -> registration.registerSubtypeInterpreter(block.asItem(), InventorySubtypeInterpreter.INSTANCE)));
        FunctionalStorageBlocks.FRAMED_DRAWER.forEach(
                block -> registration.registerSubtypeInterpreter(block.asItem(), InventorySubtypeInterpreter.INSTANCE));
    }

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    /**
     * The original's InventorySubtypeInterpreter: a drawer item is told apart by the drawer it carries.
     */
    private enum InventorySubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
        INSTANCE;

        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            return ingredient.get(FSAttachments.TILE);
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            return ingredient.getOrDefault(FSAttachments.TILE, new CompoundTag()).toString();
        }
    }
}
