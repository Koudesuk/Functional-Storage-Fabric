package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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

    @Override
    public Stream<Holder<Item>> items() {
        return StreamSupport.stream(BuiltInRegistries.ITEM.getTagOrEmpty(tag).spliterator(), false);
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
        private static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
                "tag_without_component");
        private static final MapCodec<TagWithoutComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagWithoutComponentIngredient::getTag))
                        .apply(instance, TagWithoutComponentIngredient::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, TagWithoutComponentIngredient> STREAM_CODEC = TagKey
                .streamCodec(Registries.ITEM)
                .map(TagWithoutComponentIngredient::new, TagWithoutComponentIngredient::getTag)
                .cast();

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<TagWithoutComponentIngredient> getCodec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TagWithoutComponentIngredient> getStreamCodec() {
            return STREAM_CODEC;
        }
    }
}
