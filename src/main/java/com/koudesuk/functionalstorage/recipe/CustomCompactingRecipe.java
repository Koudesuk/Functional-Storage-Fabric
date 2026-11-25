package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CustomCompactingRecipe implements Recipe<CraftingInput> {

    private final Ingredient lowerInput;
    private final ItemStack higherOutput;
    private final int needed;
    // Set when the recipe uses the original's functionalstorage:custom_compacting format
    @Nullable
    private final ItemStack lowerStack;
    @Nullable
    private final ItemStack higherStack;

    public CustomCompactingRecipe(Ingredient lowerInput, ItemStack higherOutput, int needed) {
        this.lowerInput = lowerInput;
        this.higherOutput = higherOutput;
        this.needed = needed;
        this.lowerStack = null;
        this.higherStack = null;
    }

    /**
     * The original's format: {@code lower_input} items (with their count) compact into one {@code higher_input}.
     */
    public CustomCompactingRecipe(ItemStack lowerInput, ItemStack higherInput) {
        this.lowerInput = Ingredient.of(lowerInput.getItem());
        this.higherOutput = higherInput.copyWithCount(1);
        this.needed = lowerInput.getCount();
        this.lowerStack = lowerInput;
        this.higherStack = higherInput;
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return false; // Not used in standard crafting
    }

    @Override
    public ItemStack assemble(CraftingInput container, HolderLookup.Provider registryAccess) {
        return higherOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
        return higherOutput;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return lowerStack != null ? Serializer.CUSTOM_INSTANCE : Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
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

    public ItemStack lowerStack() {
        return lowerStack;
    }

    public ItemStack higherStack() {
        return higherStack;
    }

    public void save(RecipeOutput output, ResourceLocation id) {
        output.accept(id, this, null);
    }

    public static class Type implements RecipeType<CustomCompactingRecipe> {
        private Type() {
        }

        public static final Type INSTANCE = new Type();
        public static final String ID = "compacting";
    }

    public static class Serializer implements RecipeSerializer<CustomCompactingRecipe> {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
                "compacting");
        // Same id and fields as the original mod, so its data packs load here too
        public static final ResourceLocation CUSTOM_ID = ResourceLocation.fromNamespaceAndPath(FunctionalStorage.MOD_ID,
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

        public static final MapCodec<CustomCompactingRecipe> CUSTOM_CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                        ItemStack.CODEC.fieldOf("lower_input").forGetter(CustomCompactingRecipe::lowerStack),
                        ItemStack.CODEC.fieldOf("higher_input").forGetter(CustomCompactingRecipe::higherStack))
                        .apply(instance, CustomCompactingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CustomCompactingRecipe> CUSTOM_STREAM_CODEC = StreamCodec
                .composite(
                        ItemStack.STREAM_CODEC, CustomCompactingRecipe::lowerStack,
                        ItemStack.STREAM_CODEC, CustomCompactingRecipe::higherStack,
                        CustomCompactingRecipe::new);

        public static final Serializer INSTANCE = new Serializer(CODEC, STREAM_CODEC);
        public static final Serializer CUSTOM_INSTANCE = new Serializer(CUSTOM_CODEC, CUSTOM_STREAM_CODEC);

        private final MapCodec<CustomCompactingRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, CustomCompactingRecipe> streamCodec;

        private Serializer(MapCodec<CustomCompactingRecipe> codec,
                StreamCodec<RegistryFriendlyByteBuf, CustomCompactingRecipe> streamCodec) {
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        @Override
        public MapCodec<CustomCompactingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CustomCompactingRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
