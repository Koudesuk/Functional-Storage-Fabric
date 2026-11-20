package com.koudesuk.functionalstorage.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Port of the original's {@code functionalstorage:copy_components}: a shaped recipe that copies data from the
 * ingredient at {@code copyIndex} onto the result, so converting a drawer keeps its contents. 1.20.1 has no
 * components, so {@code nbt} names the top level NBT keys that are copied (the drawer data is "Tile").
 */
public class CopyComponentsRecipe extends ShapedRecipe {

    public static final Serializer SERIALIZER = new Serializer();

    private final int copyIndex;
    private final List<String> keys;

    public CopyComponentsRecipe(ShapedRecipe other, int copyIndex, List<String> keys) {
        super(other.getId(), other.getGroup(), other.category(), other.getWidth(), other.getHeight(),
                other.getIngredients(), other.getResultItem(RegistryAccess.EMPTY), other.showNotification());
        this.copyIndex = copyIndex;
        this.keys = List.copyOf(keys);
    }

    @Override
    public ItemStack assemble(CraftingContainer container, RegistryAccess registryAccess) {
        ItemStack result = super.assemble(container, registryAccess);
        ItemStack base = findBase(container);
        CompoundTag from = base.getTag();
        if (from != null) {
            for (String key : keys) {
                if (from.contains(key))
                    result.getOrCreateTag().put(key, from.get(key).copy());
            }
        }
        return result;
    }

    /**
     * The grid item standing for the pattern's {@code copyIndex} ingredient. A shaped recipe can sit anywhere in
     * the grid (and mirrored), so it is found by that ingredient rather than by position.
     */
    private ItemStack findBase(CraftingContainer container) {
        if (copyIndex < 0 || copyIndex >= getIngredients().size())
            return ItemStack.EMPTY;
        Ingredient ingredient = getIngredients().get(copyIndex);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty() && ingredient.test(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    /**
     * Wraps a recipe output so the shaped recipes saved to it become copy recipes, for the data generator.
     */
    public static Consumer<FinishedRecipe> output(Consumer<FinishedRecipe> output, int copyIndex, String... keys) {
        return recipe -> output.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                recipe.serializeRecipeData(json);
                json.addProperty("copyIndex", copyIndex);
                JsonArray array = new JsonArray();
                for (String key : keys)
                    array.add(key);
                json.add("nbt", array);
            }

            @Override
            public ResourceLocation getId() {
                return recipe.getId();
            }

            @Override
            public RecipeSerializer<?> getType() {
                return SERIALIZER;
            }

            @Nullable
            @Override
            public JsonObject serializeAdvancement() {
                return recipe.serializeAdvancement();
            }

            @Nullable
            @Override
            public ResourceLocation getAdvancementId() {
                return recipe.getAdvancementId();
            }
        });
    }

    public static class Serializer implements RecipeSerializer<CopyComponentsRecipe> {
        @Override
        public CopyComponentsRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);
            List<String> keys = new ArrayList<>();
            if (json.has("nbt")) {
                for (JsonElement element : GsonHelper.getAsJsonArray(json, "nbt"))
                    keys.add(element.getAsString());
            } else {
                keys.add("Tile");
            }
            return new CopyComponentsRecipe(recipe, GsonHelper.getAsInt(json, "copyIndex"), keys);
        }

        @Override
        public CopyComponentsRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ShapedRecipe recipe = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
            int copyIndex = buf.readVarInt();
            List<String> keys = buf.readList(FriendlyByteBuf::readUtf);
            return new CopyComponentsRecipe(recipe, copyIndex, keys);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CopyComponentsRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
            buf.writeVarInt(recipe.copyIndex);
            buf.writeCollection(recipe.keys, FriendlyByteBuf::writeUtf);
        }
    }
}
