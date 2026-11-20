package com.koudesuk.functionalstorage.item.component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.Map;

/**
 * The original's data-driven size upgrades, stored in the {@code functionalstorage:item_storage_modifier},
 * {@code fluid_storage_modifier} and {@code controller_range_modifier} components: any item carrying one
 * changes the base or the factor of a capacity (or a controller's range) from a storage upgrade slot.
 * The built-in storage upgrades keep their config multipliers and carry none of these components.
 */
public interface SizeProvider {
    BiMap<String, Codec<? extends SizeProvider>> REGISTRY = HashBiMap.create(Map.of(
            "modify_factor", ModifyFactor.CODEC,
            "set_base", SetBase.CODEC,
            "modify_base", ModifyBase.CODEC));
    Codec<SizeProvider> CODEC = Codec.STRING.partialDispatch("type",
            provider -> DataResult.success(REGISTRY.inverse().get(provider.codec())),
            Codecs::byType);

    default float applyBaseModifier(float source) {
        return source;
    }

    default float applyFactorModifier(float source) {
        return source;
    }

    Component getTooltip(Component object);

    Codec<? extends SizeProvider> codec();

    /**
     * The original's calculateAsFactor: the base after every base modifier times every factor, floored to
     * two decimals. Without an upgrade carrying the component the base comes back unchanged, so drawers
     * with only built-in upgrades keep their exact capacity.
     */
    static double calculate(Container upgrades, ItemComponent<SizeProvider> component, double baseIn) {
        float factor = 1f;
        float base = (float) baseIn;
        boolean found = false;
        for (int i = 0; i < upgrades.getContainerSize(); i++) {
            SizeProvider comp = component.get(upgrades.getItem(i));
            if (comp != null) {
                found = true;
                factor = comp.applyFactorModifier(factor);
                base = comp.applyBaseModifier(base);
            }
        }
        if (!found)
            return baseIn;

        factor = Math.max(factor, 0f);
        base = Math.max(base, 0f);
        return Math.floor(base * factor * 100f) / 100d;
    }

    static boolean hasAny(ItemStack stack) {
        return FSAttachments.ITEM_STORAGE_MODIFIER.has(stack)
                || FSAttachments.FLUID_STORAGE_MODIFIER.has(stack)
                || FSAttachments.CONTROLLER_RANGE_MODIFIER.has(stack);
    }

    /**
     * Kept out of the interface itself: calling an interface method from a record's codec would start the
     * interface's initialization, whose REGISTRY needs that same codec.
     */
    final class Codecs {
        private Codecs() {
        }

        private static DataResult<Codec<? extends SizeProvider>> byType(String type) {
            Codec<? extends SizeProvider> codec = REGISTRY.get(type);
            if (codec == null)
                return DataResult.error(() -> "Unknown size provider type: " + type);
            return DataResult.success(codec);
        }

        /**
         * A float field that also reads a whole number, like the original's Codec.withAlternative.
         */
        static Codec<Float> floatOrInt(Codec<Float> floats, Codec<Integer> ints) {
            return Codec.either(floats, ints.xmap(Integer::floatValue, Float::intValue))
                    .xmap(either -> either.map(value -> value, value -> value), Either::left);
        }
    }

    record ModifyFactor(float factor) implements SizeProvider {
        private static final DecimalFormat TWO_DECIMALS = new DecimalFormat("#0.00");
        public static final Codec<ModifyFactor> CODEC = Codec.floatRange(0, Float.MAX_VALUE)
                .fieldOf("factor")
                .xmap(ModifyFactor::new, ModifyFactor::factor).codec();

        @Override
        public float applyFactorModifier(float source) {
            return source * factor;
        }

        @Override
        public Component getTooltip(Component object) {
            if (factor < 1) {
                return Component.translatable("storageupgrade.desc.modify_factor_div", object, format(1 / factor));
            }
            return Component.translatable("storageupgrade.desc.modify_factor_mult", object, format(factor));
        }

        private static String format(float value) {
            if (Integer.valueOf((int) value).floatValue() == value) {
                return String.valueOf((int) value);
            }
            return TWO_DECIMALS.format(value);
        }

        @Override
        public Codec<? extends SizeProvider> codec() {
            return CODEC;
        }
    }

    record SetBase(float amount) implements SizeProvider {
        public static final Codec<SetBase> CODEC = Codecs.floatOrInt(Codec.floatRange(0, Float.MAX_VALUE),
                Codec.intRange(1, Integer.MAX_VALUE))
                .fieldOf("amount")
                .xmap(SetBase::new, SetBase::amount).codec();

        @Override
        public float applyBaseModifier(float source) {
            return amount;
        }

        @Override
        public Component getTooltip(Component object) {
            return Component.translatable("storageupgrade.desc.set_base", object, amount);
        }

        @Override
        public Codec<? extends SizeProvider> codec() {
            return CODEC;
        }
    }

    record ModifyBase(float amount) implements SizeProvider {
        public static final Codec<ModifyBase> CODEC = Codecs.floatOrInt(Codec.FLOAT, Codec.INT)
                .fieldOf("amount")
                .xmap(ModifyBase::new, ModifyBase::amount).codec();

        @Override
        public float applyBaseModifier(float source) {
            return source + amount;
        }

        @Override
        public Component getTooltip(Component object) {
            return amount < 0 ? Component.translatable("storageupgrade.desc.modify_base_dec", object, -amount)
                    : Component.translatable("storageupgrade.desc.modify_base_inc", object, amount);
        }

        @Override
        public Codec<? extends SizeProvider> codec() {
            return CODEC;
        }
    }
}
