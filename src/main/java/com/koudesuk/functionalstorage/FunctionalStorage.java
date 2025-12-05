package com.koudesuk.functionalstorage;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionalStorage implements ModInitializer {
        public static final String MOD_ID = "functionalstorage";
        public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

        @Override
        public void onInitialize() {
                LOGGER.info("Functional Storage initializing...");

                FunctionalStorageBlocks.register();
                com.koudesuk.functionalstorage.registry.FunctionalStorageGroup.register();
                com.koudesuk.functionalstorage.registry.FunctionalStorageRecipes.register();
                FunctionalStorageBlockEntities.register();
                FunctionalStorageItems.register();
                com.koudesuk.functionalstorage.registry.FunctionalStorageMenus.register();

                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (cabinet, direction) -> cabinet.handler,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.ARMORY_CABINET);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (extension, direction) -> extension.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.CONTROLLER_EXTENSION);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.SIMPLE_COMPACTING_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (controller, direction) -> controller.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.STORAGE_CONTROLLER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (controller, direction) -> controller.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_DRAWER_CONTROLLER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.COMPACTING_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_COMPACTING_DRAWER);
                net.fabricmc.fabric.api.transfer.v1.item.ItemStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getStorage(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FRAMED_SIMPLE_COMPACTING_DRAWER);

                // Register FluidStorage for Fluid Drawers
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FLUID_DRAWER_1);
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FLUID_DRAWER_2);
                net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage.SIDED.registerForBlockEntity(
                                (drawer, direction) -> drawer.getHandler(),
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlockEntities.FLUID_DRAWER_4);

                net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT
                                .register((player, world, hand, pos, direction) -> {
                                        net.minecraft.world.level.block.state.BlockState state = world
                                                        .getBlockState(pos);
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
                                                net.minecraft.world.phys.HitResult result = player.pick(20, 0, false);
                                                if (result instanceof net.minecraft.world.phys.BlockHitResult blockHitResult) {
                                                        if (blockHitResult.getBlockPos().equals(pos)) {
                                                                int hit = drawerBlock.getHit(state, pos,
                                                                                blockHitResult);
                                                                if (hit != -1) {
                                                                        if (!world.isClientSide()) {
                                                                                net.minecraft.world.level.block.entity.BlockEntity blockEntity = world
                                                                                                .getBlockEntity(pos);
                                                                                if (blockEntity instanceof com.koudesuk.functionalstorage.block.tile.DrawerTile drawerTile) {
                                                                                        drawerTile.onClicked(player,
                                                                                                        hit);
                                                                                }
                                                                        }
                                                                        return net.minecraft.world.InteractionResult.SUCCESS;
                                                                }
                                                        }
                                                }
                                        }
                                        return net.minecraft.world.InteractionResult.PASS;
                                });
        }
}
