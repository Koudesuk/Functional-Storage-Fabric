package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.*;
import com.koudesuk.functionalstorage.util.DrawerType;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FunctionalStorageBlocks {

        public static HashMap<DrawerType, List<Block>> DRAWER_TYPES = new HashMap<>();
        public static Block ARMORY_CABINET;
        public static Block CONTROLLER_EXTENSION;
        public static Block SIMPLE_COMPACTING_DRAWER;
        public static Block FRAMED_SIMPLE_COMPACTING_DRAWER;
        public static Block COMPACTING_DRAWER;
        public static Block FRAMED_COMPACTING_DRAWER;
        public static Block ENDER_DRAWER;
        public static Block DRAWER_CONTROLLER;
        public static Block FRAMED_DRAWER_CONTROLLER;

        public static Block FLUID_DRAWER_1;
        public static Block FLUID_DRAWER_2;
        public static Block FLUID_DRAWER_4;

        public static List<Block> FRAMED_DRAWER = new ArrayList<>();

        public static void register() {
                // Register Drawer Types
                for (DrawerType type : DrawerType.values()) {
                        List<Block> blocks = new ArrayList<>();
                        for (com.koudesuk.functionalstorage.util.DrawerWoodType woodType : com.koudesuk.functionalstorage.util.DrawerWoodType
                                        .values()) {
                                if (woodType == com.koudesuk.functionalstorage.util.DrawerWoodType.FRAMED)
                                        continue;
                                Block block = new DrawerBlock(woodType, type,
                                                FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));
                                blocks.add(block);
                                Registry.register(BuiltInRegistries.BLOCK,
                                                new ResourceLocation(FunctionalStorage.MOD_ID,
                                                                woodType.getName() + "_drawer_" + type.getSlots()),
                                                block);
                                Registry.register(BuiltInRegistries.ITEM,
                                                new ResourceLocation(FunctionalStorage.MOD_ID,
                                                                woodType.getName() + "_drawer_" + type.getSlots()),
                                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(block,
                                                                new Item.Properties()));
                        }
                        DRAWER_TYPES.put(type, blocks);
                }

                // Register Armory Cabinet
                ARMORY_CABINET = new ArmoryCabinetBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "armory_cabinet"), ARMORY_CABINET);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "armory_cabinet"),
                                new BlockItem(ARMORY_CABINET, new Item.Properties()));

                // Register Controller Extension
                CONTROLLER_EXTENSION = new ControllerExtensionBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "controller_extension"),
                                CONTROLLER_EXTENSION);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "controller_extension"),
                                new BlockItem(CONTROLLER_EXTENSION, new Item.Properties()));

                // Register Simple Compacting Drawer
                SIMPLE_COMPACTING_DRAWER = new SimpleCompactingDrawerBlock(
                                FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "simple_compacting_drawer"),
                                SIMPLE_COMPACTING_DRAWER);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "simple_compacting_drawer"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(SIMPLE_COMPACTING_DRAWER,
                                                new Item.Properties()));

                // Register Framed Simple Compacting Drawer
                FRAMED_SIMPLE_COMPACTING_DRAWER = new FramedSimpleCompactingDrawerBlock(
                                FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_simple_compacting_drawer"),
                                FRAMED_SIMPLE_COMPACTING_DRAWER);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_simple_compacting_drawer"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(FRAMED_SIMPLE_COMPACTING_DRAWER,
                                                new Item.Properties()));

                // Register Compacting Drawer
                COMPACTING_DRAWER = new CompactingDrawerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "compacting_drawer"), COMPACTING_DRAWER);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "compacting_drawer"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(COMPACTING_DRAWER,
                                                new Item.Properties()));

                // Register Framed Compacting Drawer
                // The provided Code Edit seems to be for a different registration system (e.g.,
                // Forge)
                // and introduces new methods/classes not present in the original Fabric code.
                // Applying the change faithfully means only renaming the string if it existed,
                // or replacing the block with the provided one if it's a direct replacement.
                // Given the instruction "Rename compacting_framed_drawer to
                // framed_compacting_drawer"
                // and the fact that "compacting_framed_drawer" does not exist in the original,
                // but "framed_compacting_drawer" does, and the Code Edit also uses
                // "framed_compacting_drawer",
                // it implies a replacement of the registration logic for
                // FRAMED_COMPACTING_DRAWER.
                // However, the provided Code Edit is syntactically incorrect for this Fabric
                // context.
                // I will assume the intent was to ensure the name is "framed_compacting_drawer"
                // and that the provided Code Edit was an example of a desired *new*
                // registration
                // method, which cannot be directly applied here without significant refactoring
                // to introduce `getRegistries().registerBlockWithTileItem` and related classes.
                // Since the instruction is to "rename" and the target name is already present,
                // and the Code Edit is not directly applicable, I will keep the existing Fabric
                // registration for FRAMED_COMPACTING_DRAWER as it already uses the target name.
                // If the intent was to change the *implementation* of
                // FRAMED_COMPACTING_DRAWER's
                // registration to the one provided, it would require more context and changes
                // than a simple rename or direct block replacement allows while maintaining
                // syntactic correctness in the current Fabric environment.
                FRAMED_COMPACTING_DRAWER = new FramedCompactingDrawerBlock(
                                FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_compacting_drawer"),
                                FRAMED_COMPACTING_DRAWER);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_compacting_drawer"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(FRAMED_COMPACTING_DRAWER,
                                                new Item.Properties()));

                // Register Ender Drawer
                ENDER_DRAWER = new EnderDrawerBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "ender_drawer"), ENDER_DRAWER);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "ender_drawer"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(ENDER_DRAWER,
                                                new Item.Properties()));

                // Register Drawer Controller
                DRAWER_CONTROLLER = new DrawerControllerBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "storage_controller"),
                                DRAWER_CONTROLLER);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "storage_controller"),
                                new BlockItem(DRAWER_CONTROLLER, new Item.Properties())); // Controller doesn't need
                                                                                          // content tooltip usually

                // Register Framed Drawer Controller
                FRAMED_DRAWER_CONTROLLER = new FramedDrawerControllerBlock(
                                FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_storage_controller"),
                                FRAMED_DRAWER_CONTROLLER);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_storage_controller"),
                                new BlockItem(FRAMED_DRAWER_CONTROLLER, new Item.Properties()));

                // Register Framed Drawer
                Block framedDrawer = new FramedDrawerBlock(DrawerType.X_1);
                FRAMED_DRAWER.add(framedDrawer);
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_drawer_1"),
                                framedDrawer);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_drawer_1"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(framedDrawer,
                                                new Item.Properties()));

                framedDrawer = new FramedDrawerBlock(DrawerType.X_2);
                FRAMED_DRAWER.add(framedDrawer);
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_drawer_2"),
                                framedDrawer);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_drawer_2"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(framedDrawer,
                                                new Item.Properties()));

                framedDrawer = new FramedDrawerBlock(DrawerType.X_4);
                FRAMED_DRAWER.add(framedDrawer);
                Registry.register(BuiltInRegistries.BLOCK,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_drawer_4"),
                                framedDrawer);
                Registry.register(BuiltInRegistries.ITEM,
                                new ResourceLocation(FunctionalStorage.MOD_ID, "framed_drawer_4"),
                                new com.koudesuk.functionalstorage.item.DrawerBlockItem(framedDrawer,
                                                new Item.Properties()));

                // Register Fluid Drawers
                for (DrawerType type : DrawerType.values()) {
                        Block block = new FluidDrawerBlock(type, FabricBlockSettings.copyOf(Blocks.OAK_PLANKS));
                        if (type == DrawerType.X_1)
                                FLUID_DRAWER_1 = block;
                        if (type == DrawerType.X_2)
                                FLUID_DRAWER_2 = block;
                        if (type == DrawerType.X_4)
                                FLUID_DRAWER_4 = block;
                        Registry.register(BuiltInRegistries.BLOCK,
                                        new ResourceLocation(FunctionalStorage.MOD_ID,
                                                        "fluid_drawer_" + type.getSlots()),
                                        block);
                        Registry.register(BuiltInRegistries.ITEM,
                                        new ResourceLocation(FunctionalStorage.MOD_ID,
                                                        "fluid_drawer_" + type.getSlots()),
                                        new BlockItem(block, new Item.Properties()));
                        // Add to DRAWER_TYPES or similar if needed for creative tab, but DRAWER_TYPES
                        // is <DrawerType, List<Block>>.
                        // Maybe we should add them there too?
                        // The Creative Tab logic iterates DRAWER_TYPES.
                        DRAWER_TYPES.get(type).add(block);
                }
        }
}
