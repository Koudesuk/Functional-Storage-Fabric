package com.koudesuk.functionalstorage.recipe;

import com.google.gson.JsonObject;
import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric port of the original's {@code functionalstorage:woodless}: oak planks plus every plank from another
 * mod, so wood types without a drawer of their own still make oak drawers. The original also skips vanilla
 * planks other than oak, since those have their own drawers.
 */
public class DrawerlessWoodIngredient implements CustomIngredient {

    public static final DrawerlessWoodIngredient INSTANCE = new DrawerlessWoodIngredient();
    public static final CustomIngredientSerializer<DrawerlessWoodIngredient> SERIALIZER = new Serializer();

    private DrawerlessWoodIngredient() {
    }

    private static boolean isModded(Item item) {
        return !BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE);
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(Items.OAK_PLANKS) || stack.is(ItemTags.PLANKS) && isModded(stack.getItem());
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.OAK_PLANKS));
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.PLANKS)) {
            if (isModded(holder.value()))
                stacks.add(new ItemStack(holder.value()));
        }
        return stacks;
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    private static class Serializer implements CustomIngredientSerializer<DrawerlessWoodIngredient> {
        private static final ResourceLocation ID = new ResourceLocation(FunctionalStorage.MOD_ID, "woodless");

        @Override
        public ResourceLocation getIdentifier() {
            return ID;
        }

        @Override
        public DrawerlessWoodIngredient read(JsonObject json) {
            return INSTANCE;
        }

        @Override
        public void write(JsonObject json, DrawerlessWoodIngredient ingredient) {
        }

        @Override
        public DrawerlessWoodIngredient read(FriendlyByteBuf buf) {
            return INSTANCE;
        }

        @Override
        public void write(FriendlyByteBuf buf, DrawerlessWoodIngredient ingredient) {
        }
    }
}
