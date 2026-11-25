package com.koudesuk.functionalstorage.compat.jei;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The compacting drawers' own recipes, like the original's CompactingRecipeCategory: what many of the
 * lower item compact into.
 */
public class CompactingRecipeCategory implements IRecipeCategory<CustomCompactingRecipe> {

    public static final RecipeType<CustomCompactingRecipe> TYPE = RecipeType.create(FunctionalStorage.MOD_ID,
            "dissolution", CustomCompactingRecipe.class);

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
            "textures/block/compacting_drawer_front.png");

    @Override
    public RecipeType<CustomCompactingRecipe> getRecipeType() {
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
        List<ItemStack> inputs = new ArrayList<>();
        for (ItemStack item : recipe.getLowerInput().getItems()) {
            inputs.add(item.copyWithCount(recipe.getNeeded()));
        }
        builder.addSlot(RecipeIngredientRole.INPUT, 8, 40).addItemStacks(inputs);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 24, 8).addItemStack(recipe.getHigherOutput());
    }

    @Override
    public void draw(CustomCompactingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
            double mouseX, double mouseY) {
        // The compacting drawer's front behind the slots, like the original
        guiGraphics.blit(BACKGROUND, 0, 0, 0, 0, 64, 64, 64, 64);
    }
}
