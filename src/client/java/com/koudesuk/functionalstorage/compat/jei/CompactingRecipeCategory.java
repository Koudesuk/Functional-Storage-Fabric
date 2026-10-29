package com.koudesuk.functionalstorage.compat.jei;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The compacting drawers' own recipes, like the original's CompactingRecipeCategory: what many of the
 * lower item compact into.
 */
public class CompactingRecipeCategory implements IRecipeCategory<CustomCompactingRecipe> {

    public static final IRecipeType<CustomCompactingRecipe> TYPE = IRecipeType.create(FunctionalStorage.MOD_ID,
            "dissolution", CustomCompactingRecipe.class);

    @Override
    public IRecipeType<CustomCompactingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.literal("Custom Compacting");
    }

    @Override
    public int getWidth() {
        return 64;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    @Nullable
    public IDrawable getIcon() {
        return null;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CustomCompactingRecipe recipe, IFocusGroup focuses) {
        var input = builder.addInputSlot(8, 40);
        for (Holder<Item> item : recipe.getLowerInput().items().toList()) {
            input.add(new ItemStack(item, recipe.getNeeded()));
        }
        builder.addOutputSlot(24, 8).add(recipe.getHigherOutput());
    }
}
