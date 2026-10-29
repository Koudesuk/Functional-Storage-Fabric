package com.koudesuk.functionalstorage.item.component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * What an item in a utility upgrade slot does every tick, stored in the
 * {@code functionalstorage:functional_upgrade_behavior} component like the original, so data packs can turn
 * any item into such an upgrade. The original keeps the behavior types in a synced registry; the same
 * types are fixed here, both sides register them at startup.
 */
public interface FunctionalUpgradeBehavior {

    BiMap<Identifier, MapCodec<? extends FunctionalUpgradeBehavior>> TYPES = HashBiMap.create();

    Codec<FunctionalUpgradeBehavior> CODEC = Identifier.CODEC.dispatch(behavior -> TYPES.inverse().get(behavior.codec()),
            TYPES::get);

    static void registerTypes() {
        register("delegate_to_item", DelegateToItemBehavior.CODEC);
        register("execute_every", ExecuteEveryBehavior.CODEC);
        register("generate_item", GenerateItemBehavior.CODEC);
        register("generate_fluid", GenerateFluidBehavior.CODEC);
        register("move_items", MoveItemsBehavior.CODEC);
        register("move_fluids", MoveFluidsBehavior.CODEC);
        register("collect_item_entities", CollectItemEntitiesBehavior.CODEC);
        register("collect_fluids", CollectFluidsBehavior.CODEC);
        register("emit_redstone", EmitRedstoneBehavior.CODEC);
        register("and", AndBehavior.CODEC);
    }

    private static void register(String name, MapCodec<? extends FunctionalUpgradeBehavior> codec) {
        TYPES.put(Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, name), codec);
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

    MapCodec<? extends FunctionalUpgradeBehavior> codec();
}
