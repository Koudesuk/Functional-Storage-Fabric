package com.koudesuk.functionalstorage.recipe;

import com.google.gson.JsonObject;
import com.koudesuk.functionalstorage.FunctionalStorage;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fabric port of the original's {@code functionalstorage:tag_without_component}: a tag ingredient that only
 * takes stacks without any data on them (1.20.1 keeps that in NBT), so a drawer carrying contents, upgrades or
 * a lock is never consumed by a crafting recipe.
 */
public class TagWithoutComponentIngredient implements CustomIngredient {

    public static final CustomIngredientSerializer<TagWithoutComponentIngredient> SERIALIZER = new Serializer();

    private final TagKey<Item> tag;

    public TagWithoutComponentIngredient(TagKey<Item> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(ItemStack stack) {
        return stack.is(tag) && (stack.getTag() == null || stack.getTag().isEmpty());
    }

    @Override
    public List<ItemStack> getMatchingStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag))
            stacks.add(new ItemStack(holder.value()));
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
        private static final ResourceLocation ID = new ResourceLocation(FunctionalStorage.MOD_ID,
                "tag_without_component");

        @Override
        public ResourceLocation getIdentifier() {
            return ID;
        }

        @Override
        public TagWithoutComponentIngredient read(JsonObject json) {
            return new TagWithoutComponentIngredient(TagKey.create(Registries.ITEM,
                    new ResourceLocation(GsonHelper.getAsString(json, "tag"))));
        }

        @Override
        public void write(JsonObject json, TagWithoutComponentIngredient ingredient) {
            json.addProperty("tag", ingredient.tag.location().toString());
        }

        @Override
        public TagWithoutComponentIngredient read(FriendlyByteBuf buf) {
            return new TagWithoutComponentIngredient(TagKey.create(Registries.ITEM, buf.readResourceLocation()));
        }

        @Override
        public void write(FriendlyByteBuf buf, TagWithoutComponentIngredient ingredient) {
            buf.writeResourceLocation(ingredient.tag.location());
        }
    }
}
