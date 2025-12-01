package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class FunctionalStorageRecipeProvider extends FabricRecipeProvider {
        public FunctionalStorageRecipeProvider(FabricDataOutput output) {
                super(output);
        }

        @Override
        public void buildRecipes(Consumer<FinishedRecipe> exporter) {
                TagKey<Item> DRAWER = TagKey.create(net.minecraft.core.registries.Registries.ITEM,
                                new ResourceLocation("functionalstorage", "drawer"));

                // Upgrades
                ShapedRecipeBuilder
                                .shaped(RecipeCategory.MISC,
                                                FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.IRON))
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', Items.IRON_INGOT)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.VOID_UPGRADE)
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', Items.OBSIDIAN)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FunctionalStorageItems.CONFIGURATION_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', Items.GOLD_INGOT)
                                .define('D', DRAWER)
                                .define('E', Items.EMERALD)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FunctionalStorageItems.LINKING_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', Items.GOLD_INGOT)
                                .define('D', DRAWER)
                                .define('E', Items.DIAMOND)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder
                                .shaped(RecipeCategory.MISC,
                                                FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.COPPER))
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.COPPER_INGOT)
                                .define('B', Items.COPPER_BLOCK)
                                .define('C', Items.CHEST)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder
                                .shaped(RecipeCategory.MISC,
                                                FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.GOLD))
                                .pattern("IBI").pattern("CDC").pattern("BIB")
                                .define('I', Items.GOLD_INGOT)
                                .define('B', Items.GOLD_BLOCK)
                                .define('C', Items.CHEST)
                                .define('D', FunctionalStorageItems.STORAGE_UPGRADES
                                                .get(StorageUpgradeItem.StorageTier.COPPER))
                                .unlockedBy("has_copper_upgrade",
                                                has(FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.COPPER)))
                                .save(exporter);

                ShapedRecipeBuilder
                                .shaped(RecipeCategory.MISC,
                                                FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.DIAMOND))
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.DIAMOND)
                                .define('B', Items.DIAMOND_BLOCK)
                                .define('C', Items.CHEST)
                                .define('D', FunctionalStorageItems.STORAGE_UPGRADES
                                                .get(StorageUpgradeItem.StorageTier.GOLD))
                                .unlockedBy("has_gold_upgrade",
                                                has(FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.GOLD)))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.REDSTONE_UPGRADE)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.REDSTONE)
                                .define('B', Items.REDSTONE_BLOCK)
                                .define('C', Items.COMPARATOR)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                net.minecraft.data.recipes.SmithingTransformRecipeBuilder.smithing(
                                net.minecraft.world.item.crafting.Ingredient
                                                .of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                                net.minecraft.world.item.crafting.Ingredient.of(FunctionalStorageItems.STORAGE_UPGRADES
                                                .get(StorageUpgradeItem.StorageTier.DIAMOND)),
                                net.minecraft.world.item.crafting.Ingredient.of(Items.NETHERITE_INGOT),
                                RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.NETHERITE))
                                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                                .save(exporter, new ResourceLocation("functionalstorage",
                                                "netherite_upgrade_smithing"));

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.ARMORY_CABINET)
                                .pattern("ICI").pattern("CDC").pattern("IBI")
                                .define('I', Items.STONE)
                                .define('B', Items.NETHERITE_INGOT)
                                .define('C', DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.PULLING_UPGRADE)
                                .pattern("ICI").pattern("IDI").pattern("IBI")
                                .define('I', Items.STONE)
                                .define('B', Items.REDSTONE)
                                .define('C', Items.HOPPER)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.PUSHING_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IRI")
                                .define('I', Items.STONE)
                                .define('B', Items.REDSTONE)
                                .define('R', Items.HOPPER)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.COLLECTOR_UPGRADE)
                                .pattern("IBI").pattern("RDR").pattern("IBI")
                                .define('I', Items.STONE)
                                .define('B', Items.HOPPER)
                                .define('R', Items.REDSTONE)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.ENDER_DRAWER)
                                .pattern("PPP").pattern("LCL").pattern("PPP")
                                .define('P', ItemTags.PLANKS)
                                .define('C', Items.ENDER_CHEST)
                                .define('L', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Drawers
                for (com.koudesuk.functionalstorage.util.DrawerType type : com.koudesuk.functionalstorage.util.DrawerType
                                .values()) {
                        for (net.minecraft.world.level.block.Block block : com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.DRAWER_TYPES
                                        .get(type)) {
                                if (block instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
                                        net.minecraft.world.level.block.Block planks = drawerBlock.getWoodType()
                                                        .getPlanks();
                                        if (type == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                                .pattern("PPP").pattern("PCP").pattern("PPP")
                                                                .define('P', planks)
                                                                .define('C', Items.CHEST)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
                                                                .pattern("PCP").pattern("PPP").pattern("PCP")
                                                                .define('P', planks)
                                                                .define('C', Items.CHEST)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 4)
                                                                .pattern("CPC").pattern("PPP").pattern("CPC")
                                                                .define('P', planks)
                                                                .define('C', Items.CHEST)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        }
                                } else if (block instanceof com.koudesuk.functionalstorage.block.FluidDrawerBlock) {
                                        Item planks = Items.OAK_PLANKS;
                                        if (type == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                                .pattern("PPP").pattern("PBP").pattern("PPP")
                                                                .define('P', planks)
                                                                .define('B', Items.BUCKET)
                                                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
                                                                .pattern("PBP").pattern("PPP").pattern("PBP")
                                                                .define('P', planks)
                                                                .define('B', Items.BUCKET)
                                                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 4)
                                                                .pattern("BPB").pattern("PPP").pattern("BPB")
                                                                .define('P', planks)
                                                                .define('B', Items.BUCKET)
                                                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                                                .save(exporter);
                                        }
                                }
                        }
                }

                // Framed Drawers
                for (net.minecraft.world.level.block.Block block : com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.FRAMED_DRAWER) {
                        if (block instanceof com.koudesuk.functionalstorage.block.FramedDrawerBlock framedDrawerBlock) {
                                if (framedDrawerBlock.getType() == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                        .pattern("III").pattern("ICI").pattern("III")
                                                        .define('I', Items.IRON_NUGGET)
                                                        .define('C', Items.CHEST)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                } else if (framedDrawerBlock
                                                .getType() == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
                                                        .pattern("ICI").pattern("III").pattern("ICI")
                                                        .define('I', Items.IRON_NUGGET)
                                                        .define('C', Items.CHEST)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                } else if (framedDrawerBlock
                                                .getType() == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 4)
                                                        .pattern("CIC").pattern("III").pattern("CIC")
                                                        .define('I', Items.IRON_NUGGET)
                                                        .define('C', Items.CHEST)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                }
                        }
                }

                // Compacting Drawer
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.COMPACTING_DRAWER)
                                .pattern("SIS").pattern("PDP").pattern("SIS")
                                .define('S', Items.STONE)
                                .define('I', Items.IRON_INGOT)
                                .define('P', Items.PISTON)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Simple Compacting Drawer
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER)
                                .pattern("SIS").pattern("PDP").pattern("SIS")
                                .define('S', Items.STONE)
                                .define('I', Items.IRON_INGOT)
                                .define('P', Items.PISTON)
                                .define('D', Items.IRON_BLOCK)
                                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                                .save(exporter);

                // Controller
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.DRAWER_CONTROLLER)
                                .pattern("SRS").pattern("CDC").pattern("SRS")
                                .define('S', Items.STONE)
                                .define('R', Items.REDSTONE)
                                .define('C', Items.COMPARATOR)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Controller Extension
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS,
                                com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks.CONTROLLER_EXTENSION)
                                .pattern("SRS").pattern("CDC").pattern("SRS")
                                .define('S', Items.STONE)
                                .define('R', Items.REDSTONE)
                                .define('C', Items.REPEATER)
                                .define('D', DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);
        }
}
