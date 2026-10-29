package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CustomCompactingRecipe implements Recipe<CraftingInput> {

    private final Ingredient lowerInput;
    // Made on first use for the custom_compacting format: an ItemStack needs the item components, which datagen
    // has not bound yet when it builds the recipes (the original also keeps templates and makes stacks on demand)
    @Nullable
    private ItemStack higherOutput;
    private final int needed;
    // Set when the recipe uses the original's functionalstorage:custom_compacting format
    @Nullable
    private final ItemStackTemplate lowerTemplate;
    @Nullable
    private final ItemStackTemplate higherTemplate;

    public CustomCompactingRecipe(Ingredient lowerInput, ItemStack higherOutput, int needed) {
        this.lowerInput = lowerInput;
        this.higherOutput = higherOutput;
        this.needed = needed;
        this.lowerTemplate = null;
        this.higherTemplate = null;
    }

    /**
     * The original's format: {@code lower_input} items (with their count) compact into one {@code higher_input}.
     */
    public CustomCompactingRecipe(ItemStackTemplate lowerInput, ItemStackTemplate higherInput) {
        this.lowerInput = Ingredient.of(lowerInput.item().value());
        this.higherOutput = null;
        this.needed = lowerInput.count();
        this.lowerTemplate = lowerInput;
        this.higherTemplate = higherInput;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return false; // Not used in standard crafting
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        return getHigherOutput().copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<CustomCompactingRecipe> getSerializer() {
        return lowerTemplate != null ? Serializer.CUSTOM_INSTANCE : Serializer.INSTANCE;
    }

    @Override
    public RecipeType<CustomCompactingRecipe> getType() {
        return Type.INSTANCE;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(lowerInput);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public Ingredient getLowerInput() {
        return lowerInput;
    }

    public ItemStack getHigherOutput() {
        if (higherOutput == null)
            higherOutput = higherTemplate.create().copyWithCount(1);
        return higherOutput;
    }

    public int getNeeded() {
        return needed;
    }

    public ItemStackTemplate lowerTemplate() {
        return lowerTemplate;
    }

    public ItemStackTemplate higherTemplate() {
        return higherTemplate;
    }

    public void save(RecipeOutput output, Identifier id) {
        output.accept(ResourceKey.create(Registries.RECIPE, id), this, null);
    }

    public static class Type implements RecipeType<CustomCompactingRecipe> {
        private Type() {
        }

        public static final Type INSTANCE = new Type();
        public static final String ID = "compacting";
    }

    public static class Serializer {
        public static final Identifier ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
                "compacting");
        // Same id and fields as the original mod, so its data packs load here too
        public static final Identifier CUSTOM_ID = Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
                "custom_compacting");

        public static final MapCodec<CustomCompactingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        Ingredient.CODEC.fieldOf("lower_input")
                                .forGetter(CustomCompactingRecipe::getLowerInput),
                        ItemStack.CODEC.fieldOf("higher_output").forGetter(CustomCompactingRecipe::getHigherOutput),
                        Codec.INT.fieldOf("needed").forGetter(CustomCompactingRecipe::getNeeded))
                        .apply(instance, CustomCompactingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CustomCompactingRecipe> STREAM_CODEC = StreamCodec
                .composite(
                        Ingredient.CONTENTS_STREAM_CODEC, CustomCompactingRecipe::getLowerInput,
                        ItemStack.STREAM_CODEC, CustomCompactingRecipe::getHigherOutput,
                        net.minecraft.network.codec.ByteBufCodecs.INT, CustomCompactingRecipe::getNeeded,
                        CustomCompactingRecipe::new);

        public static final RecipeSerializer<CustomCompactingRecipe> INSTANCE = new RecipeSerializer<>(CODEC,
                STREAM_CODEC);

        public static final MapCodec<CustomCompactingRecipe> CUSTOM_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        ItemStackTemplate.CODEC.fieldOf("lower_input").forGetter(CustomCompactingRecipe::lowerTemplate),
                        ItemStackTemplate.CODEC.fieldOf("higher_input").forGetter(CustomCompactingRecipe::higherTemplate))
                        .apply(instance, CustomCompactingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CustomCompactingRecipe> CUSTOM_STREAM_CODEC = StreamCodec
                .composite(
                        ItemStackTemplate.STREAM_CODEC, CustomCompactingRecipe::lowerTemplate,
                        ItemStackTemplate.STREAM_CODEC, CustomCompactingRecipe::higherTemplate,
                        CustomCompactingRecipe::new);

        public static final RecipeSerializer<CustomCompactingRecipe> CUSTOM_INSTANCE = new RecipeSerializer<>(
                CUSTOM_CODEC, CUSTOM_STREAM_CODEC);
    }
}
