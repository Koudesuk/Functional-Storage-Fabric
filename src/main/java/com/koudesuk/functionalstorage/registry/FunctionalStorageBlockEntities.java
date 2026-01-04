package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;

public class FunctionalStorageBlockEntities {

        public static BlockEntityType<DrawerTile> DRAWER;
        public static BlockEntityType<StorageControllerTile> STORAGE_CONTROLLER;
        public static BlockEntityType<EnderDrawerTile> ENDER_DRAWER;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile> ARMORY_CABINET;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.ControllerExtensionTile> CONTROLLER_EXTENSION;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile> SIMPLE_COMPACTING_DRAWER;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile> COMPACTING_DRAWER;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FramedCompactingDrawerTile> FRAMED_COMPACTING_DRAWER;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FramedDrawerTile> FRAMED_DRAWER;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FluidDrawerTile> FLUID_DRAWER_1;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FluidDrawerTile> FLUID_DRAWER_2;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FluidDrawerTile> FLUID_DRAWER_4;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FramedDrawerControllerTile> FRAMED_DRAWER_CONTROLLER;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile> FRAMED_SIMPLE_COMPACTING_DRAWER;
        public static BlockEntityType<com.koudesuk.functionalstorage.block.tile.FramedControllerExtensionTile> FRAMED_CONTROLLER_EXTENSION;

        public static void register() {
                List<Block> drawerBlocks = new ArrayList<>();
                FunctionalStorageBlocks.DRAWER_TYPES.values().forEach(drawerBlocks::addAll);

                DRAWER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "drawer"),
                                FabricBlockEntityTypeBuilder.create(DrawerTile::new, drawerBlocks.toArray(new Block[0]))
                                                .build(null));

                STORAGE_CONTROLLER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "storage_controller"),
                                FabricBlockEntityTypeBuilder.create(StorageControllerTile::new,
                                                FunctionalStorageBlocks.DRAWER_CONTROLLER).build(null));

                ENDER_DRAWER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "ender_drawer"),
                                FabricBlockEntityTypeBuilder
                                                .create(EnderDrawerTile::new, FunctionalStorageBlocks.ENDER_DRAWER)
                                                .build(null));

                ARMORY_CABINET = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "armory_cabinet"),
                                FabricBlockEntityTypeBuilder
                                                .create(com.koudesuk.functionalstorage.block.tile.ArmoryCabinetTile::new,
                                                                FunctionalStorageBlocks.ARMORY_CABINET)
                                                .build(null));

                CONTROLLER_EXTENSION = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "controller_extension"),
                                BlockEntityType.Builder.of(
                                                com.koudesuk.functionalstorage.block.tile.ControllerExtensionTile::new,
                                                FunctionalStorageBlocks.CONTROLLER_EXTENSION).build(null));
                SIMPLE_COMPACTING_DRAWER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "simple_compacting_drawer"),
                                FabricBlockEntityTypeBuilder.create(
                                                com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile::new,
                                                FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER).build(null));
                COMPACTING_DRAWER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "compacting_drawer"),
                                FabricBlockEntityTypeBuilder.create(
                                                com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile::new,
                                                FunctionalStorageBlocks.COMPACTING_DRAWER).build(null));
                FRAMED_COMPACTING_DRAWER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_compacting_drawer"),
                                FabricBlockEntityTypeBuilder.create(
                                                com.koudesuk.functionalstorage.block.tile.FramedCompactingDrawerTile::new,
                                                FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER).build(null));

                FRAMED_SIMPLE_COMPACTING_DRAWER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_simple_compacting_drawer"),
                                FabricBlockEntityTypeBuilder.create(
                                                com.koudesuk.functionalstorage.block.tile.FramedSimpleCompactingDrawerTile::new,
                                                FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER).build(null));

                List<Block> framedDrawerBlocks = new ArrayList<>(FunctionalStorageBlocks.FRAMED_DRAWER);
                FRAMED_DRAWER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_drawer"),
                                FabricBlockEntityTypeBuilder.create((pos, state) -> {
                                        if (state.getBlock() instanceof com.koudesuk.functionalstorage.block.FramedDrawerBlock framedDrawerBlock) {
                                                return new com.koudesuk.functionalstorage.block.tile.FramedDrawerTile(
                                                                pos, state, framedDrawerBlock.getType());
                                        }
                                        return new com.koudesuk.functionalstorage.block.tile.FramedDrawerTile(pos,
                                                        state,
                                                        com.koudesuk.functionalstorage.util.DrawerType.X_1);
                                }, framedDrawerBlocks.toArray(new Block[0])).build(null));

                // Fluid Drawers - use direct block references since they're not in DRAWER_TYPES
                FLUID_DRAWER_1 = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "fluid_drawer_1"),
                                FabricBlockEntityTypeBuilder.create((pos,
                                                state) -> new com.koudesuk.functionalstorage.block.tile.FluidDrawerTile(
                                                                pos, state,
                                                                com.koudesuk.functionalstorage.util.DrawerType.X_1),
                                                FunctionalStorageBlocks.FLUID_DRAWER_1).build(null));
                FLUID_DRAWER_2 = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "fluid_drawer_2"),
                                FabricBlockEntityTypeBuilder.create((pos,
                                                state) -> new com.koudesuk.functionalstorage.block.tile.FluidDrawerTile(
                                                                pos, state,
                                                                com.koudesuk.functionalstorage.util.DrawerType.X_2),
                                                FunctionalStorageBlocks.FLUID_DRAWER_2).build(null));
                FLUID_DRAWER_4 = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "fluid_drawer_4"),
                                FabricBlockEntityTypeBuilder.create((pos,
                                                state) -> new com.koudesuk.functionalstorage.block.tile.FluidDrawerTile(
                                                                pos, state,
                                                                com.koudesuk.functionalstorage.util.DrawerType.X_4),
                                                FunctionalStorageBlocks.FLUID_DRAWER_4).build(null));

                FRAMED_DRAWER_CONTROLLER = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_storage_controller"),
                                FabricBlockEntityTypeBuilder.create(
                                                com.koudesuk.functionalstorage.block.tile.FramedDrawerControllerTile::new,
                                                FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER).build(null));

                FRAMED_CONTROLLER_EXTENSION = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_controller_extension"),
                                FabricBlockEntityTypeBuilder.create(
                                                com.koudesuk.functionalstorage.block.tile.FramedControllerExtensionTile::new,
                                                FunctionalStorageBlocks.FRAMED_CONTROLLER_EXTENSION).build(null));
        }
}
