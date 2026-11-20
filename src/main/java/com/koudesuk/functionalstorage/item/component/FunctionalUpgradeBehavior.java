package com.koudesuk.functionalstorage.item.component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * What an item in a utility upgrade slot does every tick, stored in the
 * {@code functionalstorage:functional_upgrade_behavior} component like the original (see {@link ItemComponent}),
 * so any item can be turned into such an upgrade. The original keeps the behavior types in a synced registry;
 * the same types are fixed here, both sides register them at startup.
 */
public interface FunctionalUpgradeBehavior {

    BiMap<ResourceLocation, Codec<? extends FunctionalUpgradeBehavior>> TYPES = HashBiMap.create();

    Codec<FunctionalUpgradeBehavior> CODEC = ResourceLocation.CODEC.partialDispatch("type",
            behavior -> DataResult.success(TYPES.inverse().get(behavior.codec())),
            Types::byId);

    static void registerTypes() {
        Types.register("delegate_to_item", DelegateToItemBehavior.CODEC);
        Types.register("execute_every", ExecuteEveryBehavior.CODEC);
        Types.register("generate_item", GenerateItemBehavior.CODEC);
        Types.register("generate_fluid", GenerateFluidBehavior.CODEC);
        Types.register("move_items", MoveItemsBehavior.CODEC);
        Types.register("move_fluids", MoveFluidsBehavior.CODEC);
        Types.register("collect_item_entities", CollectItemEntitiesBehavior.CODEC);
        Types.register("collect_fluids", CollectFluidsBehavior.CODEC);
        Types.register("emit_redstone", EmitRedstoneBehavior.CODEC);
        Types.register("and", AndBehavior.CODEC);
    }

    default void work(Level level, BlockPos pos, ControllableDrawerTile<?> drawer, ItemStack upgradeStack,
            int upgradeSlot) {
    }

    /**
     * The redstone signal the drawer gives, or -1 to leave it to the next upgrade.
     */
    default int getRedstoneSignal(Level level, BlockPos blockPos, BlockState state, ControllableDrawerTile<?> drawer,
            Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        return -1;
    }

    /**
     * Kept from the original's API. Vanilla redstone dust already connects to every signal source, which
     * drawers are, so nothing asks this on Fabric.
     */
    default boolean canConnectRedstone(Level level, BlockPos blockPos, BlockState state,
            ControllableDrawerTile<?> drawer, Direction direction, ItemStack upgradeStack, int upgradeSlot) {
        return false;
    }

    default List<Component> getTooltip() {
        return new ArrayList<>();
    }

    Codec<? extends FunctionalUpgradeBehavior> codec();

    final class Types {
        private Types() {
        }

        private static void register(String name, Codec<? extends FunctionalUpgradeBehavior> codec) {
            TYPES.put(new ResourceLocation(FunctionalStorage.MOD_ID, name), codec);
        }

        private static DataResult<Codec<? extends FunctionalUpgradeBehavior>> byId(ResourceLocation id) {
            Codec<? extends FunctionalUpgradeBehavior> codec = TYPES.get(id);
            if (codec == null)
                return DataResult.error(() -> "Unknown functional upgrade behavior: " + id);
            return DataResult.success(codec);
        }
    }
}
