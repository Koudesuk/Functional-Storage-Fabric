package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class FunctionalStorageRecipeProvider extends FabricRecipeProvider {

        // Fabric convention tags (c: namespace) - these are provided by Fabric API's
        // convention tag JSON files
        // c:stones includes stone, granite, diorite, andesite, deepslate, tuff,
        // calcite, etc.
        // c:wooden_chests includes chest and trapped_chest (excludes ender_chest)
        private static final TagKey<Item> STONES = TagKey.create(Registries.ITEM, new ResourceLocation("c", "stones"));
        private static final TagKey<Item> WOODEN_CHESTS = TagKey.create(Registries.ITEM,
                        new ResourceLocation("c", "wooden_chests"));

        public FunctionalStorageRecipeProvider(FabricDataOutput output) {
                super(output);
        }

        @Override
        public void buildRecipes(Consumer<FinishedRecipe> exporter) {
                TagKey<Item> DRAWER = TagKey.create(Registries.ITEM,
                                new ResourceLocation("functionalstorage", "drawer"));

                // ===== UPGRADES =====

                // Iron Upgrade
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON))
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', Items.IRON_INGOT)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Void Upgrade
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.VOID_UPGRADE)
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', Items.OBSIDIAN)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Configuration Tool
                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FunctionalStorageItems.CONFIGURATION_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', Items.GOLD_INGOT)
                                .define('D', DRAWER)
                                .define('E', Items.EMERALD)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Linking Tool
                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FunctionalStorageItems.LINKING_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', Items.GOLD_INGOT)
                                .define('D', DRAWER)
                                .define('E', Items.DIAMOND)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Copper Upgrade (uses CHESTS_WOODEN tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER))
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.COPPER_INGOT)
                                .define('B', Items.COPPER_BLOCK)
                                .define('C', WOODEN_CHESTS)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Gold Upgrade (uses WOODEN_CHESTS tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD))
                                .pattern("IBI").pattern("CDC").pattern("BIB")
                                .define('I', Items.GOLD_INGOT)
                                .define('B', Items.GOLD_BLOCK)
                                .define('C', WOODEN_CHESTS)
                                .define('D', FunctionalStorageItems.STORAGE_UPGRADES
                                                .get(StorageUpgradeItem.StorageTier.COPPER))
                                .unlockedBy("has_copper_upgrade",
                                                has(FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.COPPER)))
                                .save(exporter);

                // Diamond Upgrade (uses WOODEN_CHESTS tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND))
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.DIAMOND)
                                .define('B', Items.DIAMOND_BLOCK)
                                .define('C', WOODEN_CHESTS)
                                .define('D', FunctionalStorageItems.STORAGE_UPGRADES
                                                .get(StorageUpgradeItem.StorageTier.GOLD))
                                .unlockedBy("has_gold_upgrade",
                                                has(FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.GOLD)))
                                .save(exporter);

                // Redstone Upgrade
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.REDSTONE_UPGRADE)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.REDSTONE)
                                .define('B', Items.REDSTONE_BLOCK)
                                .define('C', Items.COMPARATOR)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Netherite Upgrade (smithing)
                net.minecraft.data.recipes.SmithingTransformRecipeBuilder.smithing(
                                net.minecraft.world.item.crafting.Ingredient
                                                .of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                                net.minecraft.world.item.crafting.Ingredient.of(
                                                FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.DIAMOND)),
                                net.minecraft.world.item.crafting.Ingredient.of(Items.NETHERITE_INGOT),
                                RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE))
                                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                                .save(exporter, new ResourceLocation("functionalstorage",
                                                "netherite_upgrade_smithing"));

                // Armory Cabinet (uses STONE tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageBlocks.ARMORY_CABINET)
                                .pattern("ICI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Items.NETHERITE_INGOT)
                                .define('C', DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pulling Upgrade (uses STONES tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.PULLING_UPGRADE)
                                .pattern("ICI").pattern("IDI").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Items.REDSTONE)
                                .define('C', Items.HOPPER)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pushing Upgrade (uses STONES tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.PUSHING_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IRI")
                                .define('I', STONES)
                                .define('B', Items.REDSTONE)
                                .define('R', Items.HOPPER)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Collector Upgrade (uses STONES tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.COLLECTOR_UPGRADE)
                                .pattern("IBI").pattern("RDR").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Items.HOPPER)
                                .define('R', Items.REDSTONE)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Ender Drawer
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageBlocks.ENDER_DRAWER)
                                .pattern("PPP").pattern("LCL").pattern("PPP")
                                .define('P', ItemTags.PLANKS)
                                .define('C', Items.ENDER_CHEST)
                                .define('L', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== WOODEN DRAWERS (uses WOODEN_CHESTS tag) =====
                for (com.koudesuk.functionalstorage.util.DrawerType type : com.koudesuk.functionalstorage.util.DrawerType
                                .values()) {
                        for (net.minecraft.world.level.block.Block block : FunctionalStorageBlocks.DRAWER_TYPES
                                        .get(type)) {
                                if (block instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
                                        net.minecraft.world.level.block.Block planks = drawerBlock.getWoodType()
                                                        .getPlanks();
                                        if (type == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                                .pattern("PPP").pattern("PCP").pattern("PPP")
                                                                .define('P', planks)
                                                                .define('C', WOODEN_CHESTS)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
                                                                .pattern("PCP").pattern("PPP").pattern("PCP")
                                                                .define('P', planks)
                                                                .define('C', WOODEN_CHESTS)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 4)
                                                                .pattern("CPC").pattern("PPP").pattern("CPC")
                                                                .define('P', planks)
                                                                .define('C', WOODEN_CHESTS)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        }
                                }
                        }
                }

                // ===== FLUID DRAWERS =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_1)
                                .pattern("PPP").pattern("PBP").pattern("PPP")
                                .define('P', ItemTags.PLANKS)
                                .define('B', Items.BUCKET)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_2, 2)
                                .pattern("PBP").pattern("PPP").pattern("PBP")
                                .define('P', ItemTags.PLANKS)
                                .define('B', Items.BUCKET)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_4, 4)
                                .pattern("BPB").pattern("PPP").pattern("BPB")
                                .define('P', ItemTags.PLANKS)
                                .define('B', Items.BUCKET)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                // ===== FRAMED DRAWERS (uses WOODEN_CHESTS tag) =====
                for (net.minecraft.world.level.block.Block block : FunctionalStorageBlocks.FRAMED_DRAWER) {
                        if (block instanceof com.koudesuk.functionalstorage.block.FramedDrawerBlock framedDrawerBlock) {
                                if (framedDrawerBlock.getType() == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                        .pattern("PPP").pattern("PCP").pattern("PPP")
                                                        .define('P', Items.IRON_NUGGET)
                                                        .define('C', WOODEN_CHESTS)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                } else if (framedDrawerBlock
                                                .getType() == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
                                                        .pattern("PCP").pattern("PPP").pattern("PCP")
                                                        .define('P', Items.IRON_NUGGET)
                                                        .define('C', WOODEN_CHESTS)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                } else if (framedDrawerBlock
                                                .getType() == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 4)
                                                        .pattern("CPC").pattern("PPP").pattern("CPC")
                                                        .define('P', Items.IRON_NUGGET)
                                                        .define('C', WOODEN_CHESTS)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                }
                        }
                }

                // ===== COMPACTING DRAWERS (correct pattern: SSS/PDP/SIS) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.COMPACTING_DRAWER)
                                .pattern("SSS").pattern("PDP").pattern("SIS")
                                .define('S', Blocks.STONE)
                                .define('P', Blocks.PISTON)
                                .define('D', DRAWER)
                                .define('I', Items.IRON_INGOT)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== SIMPLE COMPACTING DRAWER (correct pattern: SSS/SDP/SIS) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("SDP").pattern("SIS")
                                .define('S', Blocks.STONE)
                                .define('P', Blocks.PISTON)
                                .define('D', DRAWER)
                                .define('I', Items.IRON_INGOT)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED COMPACTING DRAWER (pattern: SSS/PDP/SIS with iron nugget) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("PDP").pattern("SIS")
                                .define('S', Items.IRON_NUGGET)
                                .define('P', Blocks.PISTON)
                                .define('D', DRAWER)
                                .define('I', Items.IRON_INGOT)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED SIMPLE COMPACTING DRAWER (pattern: SSS/SDP/SIS with iron nugget)
                // =====
                ShapedRecipeBuilder
                                .shaped(RecipeCategory.DECORATIONS,
                                                FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("SDP").pattern("SIS")
                                .define('S', Items.IRON_NUGGET)
                                .define('P', Blocks.PISTON)
                                .define('D', DRAWER)
                                .define('I', Items.IRON_INGOT)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== STORAGE CONTROLLER (correct pattern: IBI/CDC/IBI) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.DRAWER_CONTROLLER)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== CONTROLLER EXTENSION (correct pattern: IBI/CDC/IBI) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.CONTROLLER_EXTENSION)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', DRAWER)
                                .define('D', Items.REPEATER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED STORAGE CONTROLLER =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.IRON_NUGGET)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);
        }
}
