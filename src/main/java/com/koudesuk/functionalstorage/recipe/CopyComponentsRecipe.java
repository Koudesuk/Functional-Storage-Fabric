package com.koudesuk.functionalstorage.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Port of the original's {@code functionalstorage:copy_components}: a shaped recipe that copies the listed
 * components from the ingredient at {@code copyIndex} onto the result, so converting a drawer keeps its contents.
 */
public class CopyComponentsRecipe implements CraftingRecipe {
    public static final MapCodec<CopyComponentsRecipe> CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(ShapedRecipe.MAP_CODEC.keys(ops),
                    Stream.of(ops.createString("components"), ops.createString("copyIndex")));
        }

        @Override
        public <T> DataResult<CopyComponentsRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
            return ShapedRecipe.MAP_CODEC.decode(ops, input)
                    .flatMap(recipe -> ops.getNumberValue(input.get("copyIndex"))
                            .flatMap(ci -> ops.getList(input.get("components"))
                                    .map(comp -> {
                                        var components = new ArrayList<DataComponentType<?>>();
                                        comp.accept(t -> components.add(BuiltInRegistries.DATA_COMPONENT_TYPE
                                                .getValue(Identifier.parse(ops.getStringValue(t).getOrThrow()))));
                                        return new CopyComponentsRecipe(recipe, ci.intValue(), components);
                                    })));
        }

        @Override
        public <T> RecordBuilder<T> encode(CopyComponentsRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            var builder = ShapedRecipe.MAP_CODEC.encode(input.wrapped, ops, prefix);
            builder.add("copyIndex", ops.createInt(input.copyIndex));
            builder.add("components", ops.createList(input.components.stream()
                    .map(c -> ops.createString(BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(c).toString()))));
            return builder;
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, CopyComponentsRecipe> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CopyComponentsRecipe decode(RegistryFriendlyByteBuf buffer) {
            var recipe = ShapedRecipe.STREAM_CODEC.decode(buffer);
            return new CopyComponentsRecipe(recipe, buffer.readVarInt(),
                    buffer.readList(ByteBufCodecs.idMapper(BuiltInRegistries.DATA_COMPONENT_TYPE)));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, CopyComponentsRecipe value) {
            ShapedRecipe.STREAM_CODEC.encode(buffer, value.wrapped);
            buffer.writeVarInt(value.copyIndex);
            buffer.writeCollection(value.components, ByteBufCodecs.idMapper(BuiltInRegistries.DATA_COMPONENT_TYPE));
        }
    };
    public static final RecipeSerializer<CopyComponentsRecipe> SERIALIZER = new RecipeSerializer<>(CODEC,
            STREAM_CODEC);

    private final ShapedRecipe wrapped;
    private final int copyIndex;
    private final List<DataComponentType<?>> components;

    public CopyComponentsRecipe(ShapedRecipe other, int copyIndex, List<DataComponentType<?>> components) {
        this.wrapped = other;
        this.copyIndex = copyIndex;
        this.components = components;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ItemStack assemble(CraftingInput input) {
        var result = wrapped.assemble(input);
        var base = input.getItem(copyIndex);
        for (DataComponentType type : components) {
            var component = base.get(type);
            if (component != null)
                result.set(type, component);
        }
        return result;
    }

    @Override
    public RecipeSerializer<CopyComponentsRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return wrapped.matches(input, level);
    }

    @Override
    public boolean showNotification() {
        return wrapped.showNotification();
    }

    @Override
    public String group() {
        return wrapped.group();
    }

    @Override
    public CraftingBookCategory category() {
        return wrapped.category();
    }

    @Override
    public PlacementInfo placementInfo() {
        return wrapped.placementInfo();
    }

    public static RecipeOutput output(RecipeOutput output, int copyIndex, DataComponentType<?>... components) {
        return new RecipeOutput() {
            @Override
            public Advancement.Builder advancement() {
                return output.advancement();
            }

            @Override
            public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
                output.accept(id, new CopyComponentsRecipe((ShapedRecipe) recipe, copyIndex, Arrays.asList(components)),
                        advancement);
            }

            @Override
            public void includeRootAdvancement() {
                output.includeRootAdvancement();
            }

            @Override
            public Identifier getRecipeIdentifier(Identifier identifier) {
                return output.getRecipeIdentifier(identifier);
            }
        };
    }
}
