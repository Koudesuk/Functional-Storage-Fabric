package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.block.FramedCompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedControllerExtensionBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerControllerBlock;
import com.koudesuk.functionalstorage.block.FramedSimpleCompactingDrawerBlock;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class FramedDrawerRecipe extends CustomRecipe {

    public static final RecipeSerializer<FramedDrawerRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>(
            FramedDrawerRecipe::new);

    public FramedDrawerRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public CraftingBookCategory category() {
        return CraftingBookCategory.MISC; // Default category
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY; // Dynamic recipe
    }

    public static boolean matches(ItemStack first, ItemStack second, ItemStack drawer) {
        return !first.isEmpty() && first.getItem() instanceof BlockItem &&
                !second.isEmpty() && second.getItem() instanceof BlockItem &&
                !drawer.isEmpty() && drawer.getItem() instanceof BlockItem &&
                ((BlockItem) drawer.getItem()).getBlock() instanceof FramedDrawerBlock;
    }

    public static boolean matchesCompacting(ItemStack first, ItemStack second, ItemStack drawer) {
        return !first.isEmpty() && first.getItem() instanceof BlockItem &&
                !second.isEmpty() && second.getItem() instanceof BlockItem &&
                !drawer.isEmpty() && drawer.getItem() instanceof BlockItem &&
                ((BlockItem) drawer.getItem()).getBlock() instanceof FramedCompactingDrawerBlock;
    }

    public static boolean matchesController(ItemStack first, ItemStack second, ItemStack drawer) {
        return !first.isEmpty() && first.getItem() instanceof BlockItem &&
                !second.isEmpty() && second.getItem() instanceof BlockItem &&
                !drawer.isEmpty() && drawer.getItem() instanceof BlockItem &&
                ((BlockItem) drawer.getItem()).getBlock() instanceof FramedDrawerControllerBlock;
    }

    public static boolean matchesSimpleCompacting(ItemStack first, ItemStack second, ItemStack drawer) {
        return !first.isEmpty() && first.getItem() instanceof BlockItem &&
                !second.isEmpty() && second.getItem() instanceof BlockItem &&
                !drawer.isEmpty() && drawer.getItem() instanceof BlockItem &&
                ((BlockItem) drawer.getItem()).getBlock() instanceof FramedSimpleCompactingDrawerBlock;
    }

    public static boolean matchesControllerExtension(ItemStack first, ItemStack second, ItemStack drawer) {
        return !first.isEmpty() && first.getItem() instanceof BlockItem &&
                !second.isEmpty() && second.getItem() instanceof BlockItem &&
                !drawer.isEmpty() && drawer.getItem() instanceof BlockItem &&
                ((BlockItem) drawer.getItem()).getBlock() instanceof FramedControllerExtensionBlock;
    }

    private static Ingredients getIngredients(CraftingInput inv) {
        List<ItemStack> nonEmpty = new ArrayList<>();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                nonEmpty.add(stack);
            }
        }

        if (nonEmpty.size() < 3 || nonEmpty.size() > 4) {
            return null;
        }

        ItemStack first = nonEmpty.get(0);
        ItemStack second = nonEmpty.get(1);
        ItemStack drawer = nonEmpty.get(2);
        ItemStack divider = nonEmpty.size() == 4 ? nonEmpty.get(3) : ItemStack.EMPTY;

        if (!first.isEmpty() && !(first.getItem() instanceof BlockItem)) {
            return null;
        }
        if (!second.isEmpty() && !(second.getItem() instanceof BlockItem)) {
            return null;
        }
        if (!divider.isEmpty() && !(divider.getItem() instanceof BlockItem)) {
            return null;
        }

        return new Ingredients(first, second, drawer, divider);
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        Ingredients ingredients = getIngredients(inv);
        if (ingredients == null) {
            return false;
        }

        return matches(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesCompacting(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesSimpleCompacting(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesController(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesControllerExtension(ingredients.first(), ingredients.second(), ingredients.drawer());
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
        Ingredients ingredients = getIngredients(inv);
        if (ingredients == null) {
            return ItemStack.EMPTY;
        }

        if (matches(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesCompacting(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesSimpleCompacting(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesController(ingredients.first(), ingredients.second(), ingredients.drawer()) ||
                matchesControllerExtension(ingredients.first(), ingredients.second(), ingredients.drawer())) {
            return FramedDrawerBlock.fill(ingredients.first(), ingredients.second(), ingredients.drawer(),
                    ingredients.divider());
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private record Ingredients(ItemStack first, ItemStack second, ItemStack drawer, ItemStack divider) {
    }
}
