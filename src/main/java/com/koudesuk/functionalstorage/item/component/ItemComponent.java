package com.koudesuk.functionalstorage.item.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.koudesuk.functionalstorage.FunctionalStorage;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 1.20.1 has no data components, so this stands in for the ones the original keeps its data-driven upgrade
 * settings in. An item gets a value from code, like a default component ({@link #setDefault}); a single
 * stack can carry its own value in its NBT under the component's id ({@code "functionalstorage:<name>"}),
 * written in the same format as the original's JSON, which wins over the item's default.
 */
public final class ItemComponent<T> {

    private final String key;
    private final Codec<T> codec;
    private final Map<Item, T> defaults = new IdentityHashMap<>();
    // Values decoded from stack NBT, by tag instance, so a ticking upgrade does not decode every tick
    private final Cache<Tag, Optional<T>> decoded = CacheBuilder.newBuilder().weakKeys().maximumSize(4096).build();

    public ItemComponent(String key, Codec<T> codec) {
        this.key = key;
        this.codec = codec;
    }

    public String key() {
        return key;
    }

    public Codec<T> codec() {
        return codec;
    }

    /**
     * Gives every stack of the item this value unless the stack carries its own.
     */
    public void setDefault(Item item, T value) {
        defaults.put(item, value);
    }

    @Nullable
    public T get(ItemStack stack) {
        if (stack.isEmpty())
            return null;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(key)) {
            Tag raw = tag.get(key);
            Optional<T> value = decoded.getIfPresent(raw);
            if (value == null) {
                value = codec.parse(NbtOps.INSTANCE, raw).resultOrPartial(
                        error -> FunctionalStorage.LOGGER.warn("Invalid {} on {}: {}", key, stack, error));
                decoded.put(raw, value);
            }
            if (value.isPresent())
                return value.get();
        }
        return defaults.get(stack.getItem());
    }

    public T getOrDefault(ItemStack stack, T fallback) {
        T value = get(stack);
        return value == null ? fallback : value;
    }

    public boolean has(ItemStack stack) {
        return get(stack) != null;
    }

    /**
     * Stores the value in the stack's own NBT.
     */
    public void set(ItemStack stack, T value) {
        codec.encodeStart(NbtOps.INSTANCE, value).resultOrPartial(
                error -> FunctionalStorage.LOGGER.warn("Could not write {}: {}", key, error))
                .ifPresent(tag -> stack.getOrCreateTag().put(key, tag));
    }
}
