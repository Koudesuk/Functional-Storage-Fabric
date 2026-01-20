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
import org.jetbrains.annotations.NotNull;

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

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        if (inv.size() < 3)
            return false;
        return matches(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesSimpleCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesController(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesControllerExtension(inv.getItem(0), inv.getItem(1), inv.getItem(2));
    }

    @Override
    public ItemStack assemble(CraftingInput inv, HolderLookup.Provider registryAccess) {
        if (matches(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesSimpleCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesController(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesControllerExtension(inv.getItem(0), inv.getItem(1), inv.getItem(2))) {
            ItemStack drawer = inv.getItem(2);
            ItemStack first = inv.getItem(0);
            ItemStack second = inv.getItem(1);
            ItemStack divider = inv.size() > 3 ? inv.getItem(3) : ItemStack.EMPTY;

            return FramedDrawerBlock.fill(first, second, drawer, divider);
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
}
