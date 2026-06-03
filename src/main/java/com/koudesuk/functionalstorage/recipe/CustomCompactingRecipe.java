package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CustomCompactingRecipe implements Recipe<CraftingInput> {

    private final Ingredient lowerInput;
    private final ItemStack higherOutput;
    private final int needed;

    public CustomCompactingRecipe(Ingredient lowerInput, ItemStack higherOutput, int needed) {
        this.lowerInput = lowerInput;
        this.higherOutput = higherOutput;
        this.needed = needed;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return false; // Not used in standard crafting
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        return higherOutput.copy();
    }

    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public RecipeSerializer<CustomCompactingRecipe> getSerializer() {
        return Serializer.INSTANCE;
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
        return higherOutput;
    }

    public int getNeeded() {
        return needed;
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
    }
}
