package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
        return !holder.unwrapKey().orElseThrow().identifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE);
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(Items.OAK_PLANKS) || stack.is(ItemTags.PLANKS) && isModded(stack.typeHolder());
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.concat(Stream.of(Items.OAK_PLANKS.builtInRegistryHolder()),
                StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(ItemTags.PLANKS).spliterator(), false)
                        .filter(DrawerlessWoodIngredient::isModded));
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
        private static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "woodless");

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<DrawerlessWoodIngredient> getCodec() {
            return MapCodec.unit(INSTANCE);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DrawerlessWoodIngredient> getStreamCodec() {
            return StreamCodec.unit(INSTANCE);
        }
    }
}
