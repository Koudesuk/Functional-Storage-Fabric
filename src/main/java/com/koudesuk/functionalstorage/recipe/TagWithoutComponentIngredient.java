package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fabric port of the original's {@code functionalstorage:tag_without_component}: a tag ingredient that only
 * takes stacks without any component changes, so a drawer carrying contents, upgrades or a lock is never
 * consumed by a crafting recipe.
 */
public class TagWithoutComponentIngredient implements CustomIngredient {

    public static final CustomIngredientSerializer<TagWithoutComponentIngredient> SERIALIZER = new Serializer();

    private final TagKey<Item> tag;

    public TagWithoutComponentIngredient(TagKey<Item> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(tag) && stack.getComponentsPatch().isEmpty();
    }

    // Like the original's getListOfItems: an empty tag shows a named barrier
    @Override
    public List<ItemStack> getMatchingStacks() {
        List<ItemStack> list = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            list.add(new ItemStack(holder));
        }
        if (list.isEmpty()) {
            ItemStack barrier = new ItemStack(Blocks.BARRIER);
            barrier.set(DataComponents.CUSTOM_NAME, Component.literal("Empty Tag: " + tag.location()));
            list.add(barrier);
        }
        return list;
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    public TagKey<Item> getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TagWithoutComponentIngredient other && tag.equals(other.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

    private static class Serializer implements CustomIngredientSerializer<TagWithoutComponentIngredient> {
        private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
                "tag_without_component");
        private static final MapCodec<TagWithoutComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagWithoutComponentIngredient::getTag))
                        .apply(instance, TagWithoutComponentIngredient::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, TagWithoutComponentIngredient> STREAM_CODEC = ResourceLocation.STREAM_CODEC
                .<TagWithoutComponentIngredient>map(id -> new TagWithoutComponentIngredient(TagKey.create(Registries.ITEM, id)),
                        ingredient -> ingredient.getTag().location())
                .cast();

        @Override
        public ResourceLocation getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<TagWithoutComponentIngredient> getCodec(boolean allowEmpty) {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TagWithoutComponentIngredient> getPacketCodec() {
            return STREAM_CODEC;
        }
    }
}
