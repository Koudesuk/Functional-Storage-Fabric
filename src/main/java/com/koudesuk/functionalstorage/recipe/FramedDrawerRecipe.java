package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.CompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedCompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerControllerBlock;
import com.koudesuk.functionalstorage.block.FramedSimpleCompactingDrawerBlock;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraft.world.level.Level;

public class FramedDrawerRecipe extends CustomRecipe {

    public static RecipeSerializer<FramedDrawerRecipe> SERIALIZER = new SimpleCraftingRecipeSerializer<>((p_250892_, p_249920_) -> new FramedDrawerRecipe(p_250892_, p_249920_));

    public FramedDrawerRecipe(ResourceLocation idIn, CraftingBookCategory category) {
        super(idIn, category);
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

    @Override
    public boolean matches(CraftingContainer inv, Level worldIn) {
        if (inv.getContainerSize() < 3) return false;
        return matches(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesSimpleCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesController(inv.getItem(0), inv.getItem(1), inv.getItem(2));
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess registryAccess) {
        if (matches(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesSimpleCompacting(inv.getItem(0), inv.getItem(1), inv.getItem(2)) ||
                matchesController(inv.getItem(0), inv.getItem(1), inv.getItem(2)))
        {
            ItemStack drawer = inv.getItem(2);
            ItemStack first = inv.getItem(0);
            ItemStack second = inv.getItem(1);
            ItemStack divider = inv.getContainerSize() > 3 ? inv.getItem(3) : ItemStack.EMPTY;
            
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
