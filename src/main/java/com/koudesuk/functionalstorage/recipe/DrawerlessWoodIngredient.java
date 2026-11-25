package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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

    private static boolean isModded(Holder<Item> holder) {
        return holder.unwrapKey().map(key -> !key.location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE))
                .orElse(false);
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(Items.OAK_PLANKS) || stack.is(ItemTags.PLANKS) && isModded(stack.getItemHolder());
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(Items.OAK_PLANKS));
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.PLANKS)) {
            if (isModded(holder))
                stacks.add(new ItemStack(holder));
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
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
                "woodless");

        @Override
        public ResourceLocation getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<DrawerlessWoodIngredient> getCodec(boolean allowEmpty) {
            return MapCodec.unit(INSTANCE);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DrawerlessWoodIngredient> getPacketCodec() {
            return StreamCodec.unit(INSTANCE);
        }
    }
}
