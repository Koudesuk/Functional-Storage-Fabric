package com.koudesuk.functionalstorage.recipe;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class CustomCompactingRecipe implements Recipe<Container> {

    private final ResourceLocation id;
    private final Ingredient lowerInput;
    private final ItemStack higherOutput;
    private final int needed;

    public CustomCompactingRecipe(ResourceLocation id, Ingredient lowerInput, ItemStack higherOutput, int needed) {
        this.id = id;
        this.lowerInput = lowerInput;
        this.higherOutput = higherOutput;
        this.needed = needed;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false; // Not used in standard crafting
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return higherOutput.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return higherOutput;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
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

    public static class Type implements RecipeType<CustomCompactingRecipe> {
        private Type() {
        }

        public static final Type INSTANCE = new Type();
        public static final String ID = "compacting";
    }

    public static class Serializer implements RecipeSerializer<CustomCompactingRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(FunctionalStorage.MOD_ID, "compacting");

        @Override
        public CustomCompactingRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient lowerInput = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "lower_input"));
            ItemStack higherOutput = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "higher_output"));
            int needed = GsonHelper.getAsInt(json, "needed");
            return new CustomCompactingRecipe(id, lowerInput, higherOutput, needed);
        }

        @Override
        public CustomCompactingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            Ingredient lowerInput = Ingredient.fromNetwork(buf);
            ItemStack higherOutput = buf.readItem();
            int needed = buf.readInt();
            return new CustomCompactingRecipe(id, lowerInput, higherOutput, needed);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CustomCompactingRecipe recipe) {
            recipe.lowerInput.toNetwork(buf);
            buf.writeItem(recipe.higherOutput);
            buf.writeInt(recipe.needed);
        }
    }
}
