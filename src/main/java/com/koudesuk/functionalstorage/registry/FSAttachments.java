package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.item.component.FunctionalUpgradeBehavior;
import com.koudesuk.functionalstorage.item.component.SizeProvider;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

import java.util.function.UnaryOperator;

/**
 * Fabric equivalent of Forge's FSAttachments.
 * Uses Minecraft 1.21's Data Component API for storing custom data on
 * ItemStacks.
 */
public class FSAttachments {

        // Configuration Tool
        public static final DataComponentType<String> CONFIGURATION_ACTION = register("configuration_action",
                        builder -> builder.persistent(Codec.STRING));

        // Linking Tool
        public static final DataComponentType<String> ACTION_MODE = register("action_mode",
                        builder -> builder.persistent(Codec.STRING));

        public static final DataComponentType<String> LINKING_MODE = register("linking_mode",
                        builder -> builder.persistent(Codec.STRING));

        public static final DataComponentType<BlockPos> FIRST_POSITION = register("first_pos",
                        builder -> builder.persistent(BlockPos.CODEC));

        public static final DataComponentType<BlockPos> CONTROLLER = register("controller_position",
                        builder -> builder.persistent(BlockPos.CODEC));

        // Ender Drawer
        public static final DataComponentType<String> ENDER_FREQUENCY = register("ender_frequency",
                        builder -> builder.persistent(Codec.STRING));

        public static final DataComponentType<Boolean> ENDER_SAFETY = register("ender_safety",
                        builder -> builder.persistent(Codec.BOOL));

        // Upgrade
        public static final DataComponentType<Direction> DIRECTION = register("direction",
                        builder -> builder.persistent(Direction.CODEC));

        public static final DataComponentType<Integer> SLOT = register("slot",
                        builder -> builder.persistent(Codec.intRange(0, 8)));

        // Block data
        public static final DataComponentType<Boolean> LOCKED = register("locked",
                        builder -> builder.persistent(Codec.BOOL));

        public static final DataComponentType<CompoundTag> TILE = register("tile",
                        builder -> builder.persistent(CompoundTag.CODEC));

        public static final DataComponentType<CompoundTag> STYLE = register("style",
                        builder -> builder.persistent(CompoundTag.CODEC));

        // Storage modifiers (for upgrades)
        public static final DataComponentType<Integer> STORAGE_MULTIPLIER = register("storage_multiplier",
                        builder -> builder.persistent(Codec.INT));

        // Data-driven size upgrades like the original: any item with one goes into a storage upgrade slot
        public static final DataComponentType<SizeProvider> ITEM_STORAGE_MODIFIER = register("item_storage_modifier",
                        builder -> builder.persistent(SizeProvider.CODEC));

        public static final DataComponentType<SizeProvider> FLUID_STORAGE_MODIFIER = register(
                        "fluid_storage_modifier", builder -> builder.persistent(SizeProvider.CODEC));

        public static final DataComponentType<SizeProvider> CONTROLLER_RANGE_MODIFIER = register(
                        "controller_range_modifier", builder -> builder.persistent(SizeProvider.CODEC));

        // What an item does in a utility upgrade slot, and the upgrades it never shares the slots with
        public static final DataComponentType<FunctionalUpgradeBehavior> FUNCTIONAL_BEHAVIOR = register(
                        "functional_upgrade_behavior", builder -> builder.persistent(FunctionalUpgradeBehavior.CODEC));

        public static final DataComponentType<HolderSet<Item>> INCOMPATIBLE_UPGRADES = register("incompatible_upgrades",
                        builder -> builder.persistent(RegistryCodecs.homogeneousList(Registries.ITEM)));

        private static <T> DataComponentType<T> register(String name,
                        UnaryOperator<DataComponentType.Builder<T>> builder) {
                return Registry.register(
                                BuiltInRegistries.DATA_COMPONENT_TYPE,
                                Identifier.parse(FunctionalStorage.MOD_ID + ":" + name),
                                builder.apply(DataComponentType.builder()).build());
        }

        public static void register() {
                FunctionalStorage.LOGGER.info("Registering FSAttachments (Data Component Types)...");
                // All fields are initialized statically, so calling this method initializes the
                // class
        }
}
