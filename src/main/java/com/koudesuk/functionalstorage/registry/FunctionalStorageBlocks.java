package com.koudesuk.functionalstorage.registry;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.ArmoryCabinetBlock;
import com.koudesuk.functionalstorage.block.CompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.ControllerExtensionBlock;
import com.koudesuk.functionalstorage.block.DrawerBlock;
import com.koudesuk.functionalstorage.block.DrawerControllerBlock;
import com.koudesuk.functionalstorage.block.EnderDrawerBlock;
import com.koudesuk.functionalstorage.block.FluidDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedCompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedControllerExtensionBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerBlock;
import com.koudesuk.functionalstorage.block.FramedDrawerControllerBlock;
import com.koudesuk.functionalstorage.block.FramedSimpleCompactingDrawerBlock;
import com.koudesuk.functionalstorage.block.SimpleCompactingDrawerBlock;
import com.koudesuk.functionalstorage.item.DrawerBlockItem;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.DrawerWoodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

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
        public static Block FRAMED_CONTROLLER_EXTENSION;

        public static Block FLUID_DRAWER_1;
        public static Block FLUID_DRAWER_2;
        public static Block FLUID_DRAWER_4;

        public static List<Block> FRAMED_DRAWER = new ArrayList<>();

        public static void register() {
                for (DrawerType type : DrawerType.values()) {
                        List<Block> blocks = new ArrayList<>();
                        for (DrawerWoodType woodType : DrawerWoodType.values()) {
                                if (woodType == DrawerWoodType.FRAMED) {
                                        continue;
                                }

                                String name = woodType.getName() + "_drawer_" + type.getSlots();
                                Block block = registerBlock(name,
                                                properties -> new DrawerBlock(woodType, type, properties),
                                                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS));
                                blocks.add(block);
                                registerDrawerBlockItem(name, block);
                        }
                        DRAWER_TYPES.put(type, blocks);
                }

                ARMORY_CABINET = registerBlock("armory_cabinet", ArmoryCabinetBlock::new,
                                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
                registerBlockItem("armory_cabinet", ARMORY_CABINET);

                CONTROLLER_EXTENSION = registerBlock("controller_extension", ControllerExtensionBlock::new,
                                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
                registerBlockItem("controller_extension", CONTROLLER_EXTENSION);

                SIMPLE_COMPACTING_DRAWER = registerBlock("simple_compacting_drawer", SimpleCompactingDrawerBlock::new,
                                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
                registerDrawerBlockItem("simple_compacting_drawer", SIMPLE_COMPACTING_DRAWER);

                FRAMED_SIMPLE_COMPACTING_DRAWER = registerBlock("framed_simple_compacting_drawer",
                                FramedSimpleCompactingDrawerBlock::new,
                                framedIronProperties());
                registerDrawerBlockItem("framed_simple_compacting_drawer", FRAMED_SIMPLE_COMPACTING_DRAWER);

                COMPACTING_DRAWER = registerBlock("compacting_drawer", CompactingDrawerBlock::new,
                                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
                registerDrawerBlockItem("compacting_drawer", COMPACTING_DRAWER);

                FRAMED_COMPACTING_DRAWER = registerBlock("framed_compacting_drawer", FramedCompactingDrawerBlock::new,
                                framedIronProperties());
                registerDrawerBlockItem("framed_compacting_drawer", FRAMED_COMPACTING_DRAWER);

                ENDER_DRAWER = registerBlock("ender_drawer", EnderDrawerBlock::new,
                                BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN));
                registerDrawerBlockItem("ender_drawer", ENDER_DRAWER);

                DRAWER_CONTROLLER = registerBlock("storage_controller", DrawerControllerBlock::new,
                                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK));
                registerBlockItem("storage_controller", DRAWER_CONTROLLER);

                FRAMED_DRAWER_CONTROLLER = registerBlock("framed_storage_controller",
                                FramedDrawerControllerBlock::new,
                                framedIronProperties());
                registerBlockItem("framed_storage_controller", FRAMED_DRAWER_CONTROLLER);

                FRAMED_CONTROLLER_EXTENSION = registerBlock("framed_controller_extension",
                                FramedControllerExtensionBlock::new,
                                framedIronProperties());
                registerBlockItem("framed_controller_extension", FRAMED_CONTROLLER_EXTENSION);

                registerFramedDrawer(DrawerType.X_1);
                registerFramedDrawer(DrawerType.X_2);
                registerFramedDrawer(DrawerType.X_4);

                for (DrawerType type : DrawerType.values()) {
                        String name = "fluid_drawer_" + type.getSlots();
                        Block block = registerBlock(name, properties -> new FluidDrawerBlock(type, properties),
                                        BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS));
                        if (type == DrawerType.X_1) {
                                FLUID_DRAWER_1 = block;
                        }
                        if (type == DrawerType.X_2) {
                                FLUID_DRAWER_2 = block;
                        }
                        if (type == DrawerType.X_4) {
                                FLUID_DRAWER_4 = block;
                        }
                        registerBlockItem(name, block);
                }
        }

        private static void registerFramedDrawer(DrawerType type) {
                String name = "framed_drawer_" + type.getSlots();
                Block block = registerBlock(name, properties -> new FramedDrawerBlock(type, properties),
                                framedDrawerProperties());
                FRAMED_DRAWER.add(block);
                registerDrawerBlockItem(name, block);
        }

        private static BlockBehaviour.Properties framedIronProperties() {
                return BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion()
                                .isViewBlocking((state, level, pos) -> false);
        }

        private static BlockBehaviour.Properties framedDrawerProperties() {
                return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(1.5f, 1.5f).noOcclusion()
                                .isViewBlocking((state, level, pos) -> false);
        }

        private static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory,
                        BlockBehaviour.Properties properties) {
                T block = factory.apply(properties.setId(blockKey(name)));
                return Registry.register(BuiltInRegistries.BLOCK, id(name), block);
        }

        private static void registerBlockItem(String name, Block block) {
                registerItem(name, properties -> new BlockItem(block, properties));
        }

        private static void registerDrawerBlockItem(String name, Block block) {
                registerItem(name, properties -> new DrawerBlockItem(block, properties));
        }

        private static <T extends Item> T registerItem(String name, Function<Item.Properties, T> factory) {
                T item = factory.apply(itemProperties(name));
                return Registry.register(BuiltInRegistries.ITEM, id(name), item);
        }

        private static Item.Properties itemProperties(String name) {
                return new Item.Properties().setId(itemKey(name));
        }

        private static ResourceKey<Block> blockKey(String name) {
                return ResourceKey.create(Registries.BLOCK, id(name));
        }

        private static ResourceKey<Item> itemKey(String name) {
                return ResourceKey.create(Registries.ITEM, id(name));
        }

        private static Identifier id(String name) {
                return Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, name);
        }
}
