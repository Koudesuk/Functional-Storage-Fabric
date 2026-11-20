package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.recipe.CopyComponentsRecipe;
import com.koudesuk.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.koudesuk.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.DrawerWoodType;
import com.koudesuk.functionalstorage.util.StorageTags;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;
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
        // The original takes Forge's tags for these; Fabric API 1.20.1 ships the same c: tags
        private static final TagKey<Item> IRON_INGOTS = ConventionalItemTags.IRON_INGOTS;
        private static final TagKey<Item> GOLD_INGOTS = ConventionalItemTags.GOLD_INGOTS;
        private static final TagKey<Item> DIAMONDS = ConventionalItemTags.DIAMONDS;
        private static final TagKey<Item> NETHERITE_INGOTS = ConventionalItemTags.NETHERITE_INGOTS;
        private static final TagKey<Item> REDSTONE_DUSTS = ConventionalItemTags.REDSTONE_DUSTS;
        private static final TagKey<Item> EMPTY_BUCKETS = ConventionalItemTags.EMPTY_BUCKETS;

        public FunctionalStorageRecipeProvider(FabricDataOutput output) {
                super(output);
        }

        @Override
        public void buildRecipes(Consumer<FinishedRecipe> exporter) {
                TagKey<Item> DRAWER = StorageTags.DRAWER;
                // Unlocks still use the plain tag, but a drawer is only used up while it is empty: like the
                // original's tag_without_component, a drawer with contents, upgrades or a lock never matches
                Ingredient EMPTY_DRAWER = new TagWithoutComponentIngredient(DRAWER).toVanilla();

                // ===== UPGRADES =====

                // Iron Upgrade
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON))
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', IRON_INGOTS)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Void Upgrade
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.VOID_UPGRADE)
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', Items.OBSIDIAN)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Configuration Tool
                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FunctionalStorageItems.CONFIGURATION_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', GOLD_INGOTS)
                                .define('D', EMPTY_DRAWER)
                                .define('E', Items.EMERALD)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Linking Tool
                ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, FunctionalStorageItems.LINKING_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', GOLD_INGOTS)
                                .define('D', EMPTY_DRAWER)
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
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Gold Upgrade (uses WOODEN_CHESTS tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD))
                                .pattern("IBI").pattern("CDC").pattern("BIB")
                                .define('I', GOLD_INGOTS)
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
                                .define('I', DIAMONDS)
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
                                .define('D', EMPTY_DRAWER)
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
                                .define('B', NETHERITE_INGOTS)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pulling Upgrade (uses STONES tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.PULLING_UPGRADE)
                                .pattern("ICI").pattern("IDI").pattern("IBI")
                                .define('I', STONES)
                                .define('B', REDSTONE_DUSTS)
                                .define('C', Items.HOPPER)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pushing Upgrade (uses STONES tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.PUSHING_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IRI")
                                .define('I', STONES)
                                .define('B', REDSTONE_DUSTS)
                                .define('R', Items.HOPPER)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Collector Upgrade (uses STONES tag)
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.COLLECTOR_UPGRADE)
                                .pattern("IBI").pattern("RDR").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Items.HOPPER)
                                .define('R', REDSTONE_DUSTS)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Ender Drawer
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageBlocks.ENDER_DRAWER)
                                .pattern("PPP").pattern("LCL").pattern("PPP")
                                .define('P', ItemTags.PLANKS)
                                .define('C', Items.ENDER_CHEST)
                                .define('L', EMPTY_DRAWER)
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
                                        // Oak drawers also take the planks of every wood without a drawer of its own (other mods' woods),
                                        // like the original's oak_drawer_alternate recipes
                                        Ingredient plankIngredient = drawerBlock.getWoodType() == DrawerWoodType.OAK
                                                        ? DrawerlessWoodIngredient.INSTANCE.toVanilla()
                                                        : Ingredient.of(planks);
                                        if (type == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                                .pattern("PPP").pattern("PCP").pattern("PPP")
                                                                .define('P', plankIngredient)
                                                                .define('C', WOODEN_CHESTS)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
                                                                .pattern("PCP").pattern("PPP").pattern("PCP")
                                                                .define('P', plankIngredient)
                                                                .define('C', WOODEN_CHESTS)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 4)
                                                                .pattern("CPC").pattern("PPP").pattern("CPC")
                                                                .define('P', plankIngredient)
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
                                .define('B', EMPTY_BUCKETS)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_2, 2)
                                .pattern("PBP").pattern("PPP").pattern("PBP")
                                .define('P', ItemTags.PLANKS)
                                .define('B', EMPTY_BUCKETS)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_4, 4)
                                .pattern("BPB").pattern("PPP").pattern("BPB")
                                .define('P', ItemTags.PLANKS)
                                .define('B', EMPTY_BUCKETS)
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
                                .define('S', STONES)
                                .define('P', Blocks.PISTON)
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== SIMPLE COMPACTING DRAWER (correct pattern: SSS/SDP/SIS) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("SDP").pattern("SIS")
                                .define('S', STONES)
                                .define('P', Blocks.PISTON)
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED COMPACTING DRAWER (pattern: SSS/PDP/SIS with iron nugget) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("PDP").pattern("SIS")
                                .define('S', Items.IRON_NUGGET)
                                .define('P', Blocks.PISTON)
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
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
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== STORAGE CONTROLLER (correct pattern: IBI/CDC/IBI) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.DRAWER_CONTROLLER)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== CONTROLLER EXTENSION (correct pattern: IBI/CDC/IBI) =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.CONTROLLER_EXTENSION)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.REPEATER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED STORAGE CONTROLLER =====
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.IRON_NUGGET)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED CONTROLLER EXTENSION (Access Point) =====
                ShapedRecipeBuilder
                                .shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FRAMED_CONTROLLER_EXTENSION)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.IRON_NUGGET)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.REPEATER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pulling and Pushing Upgrades turn into each other, like the original
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, FunctionalStorageItems.PUSHING_UPGRADE)
                                .requires(FunctionalStorageItems.PULLING_UPGRADE)
                                .unlockedBy("has_puller_upgrade", has(FunctionalStorageItems.PULLING_UPGRADE))
                                .save(exporter, "functionalstorage:pusher_upgrade_from_puller");
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, FunctionalStorageItems.PULLING_UPGRADE)
                                .requires(FunctionalStorageItems.PUSHING_UPGRADE)
                                .unlockedBy("has_pusher_upgrade", has(FunctionalStorageItems.PUSHING_UPGRADE))
                                .save(exporter, "functionalstorage:puller_upgrade_from_pusher");

                // Generator upgrades, like the original's 1.6 ones
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.DRIPPING_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IRI")
                                .define('I', STONES)
                                .define('B', Items.POINTED_DRIPSTONE)
                                .define('R', Items.LAVA_BUCKET)
                                .define('D', Items.CAULDRON)
                                .unlockedBy("has_stone", has(STONES))
                                .save(exporter);
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, FunctionalStorageItems.WATER_GENERATOR_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Items.WATER_BUCKET)
                                .define('D', Items.BUCKET)
                                .unlockedBy("has_stone", has(STONES))
                                .save(exporter);
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, FunctionalStorageItems.OBSIDIAN_UPGRADE)
                                .requires(FunctionalStorageItems.DRIPPING_UPGRADE, 4)
                                .requires(FunctionalStorageItems.WATER_GENERATOR_UPGRADE)
                                .unlockedBy("has_dripping_upgrade", has(FunctionalStorageItems.DRIPPING_UPGRADE))
                                .save(exporter);

                // ===== FRAMED FLUID DRAWERS =====
                for (Block block : FunctionalStorageBlocks.FRAMED_FLUID_DRAWER) {
                        DrawerType type = ((com.koudesuk.functionalstorage.block.FramedFluidDrawerBlock) block).getType();
                        ShapedRecipeBuilder builder = type == DrawerType.X_1
                                        ? ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                        .pattern("PPP").pattern("PCP").pattern("PPP")
                                        : type == DrawerType.X_2
                                                        ? ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 2)
                                                                        .pattern("PCP").pattern("PPP").pattern("PCP")
                                                        : ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block, 4)
                                                                        .pattern("CPC").pattern("PPP").pattern("CPC");
                        builder.define('P', Items.IRON_NUGGET)
                                        .define('C', EMPTY_BUCKETS)
                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                        .save(exporter);
                }

                // ===== DRAWERS TURNED INTO FRAMED ONES, KEEPING WHAT THEY HOLD =====
                for (Block block : FunctionalStorageBlocks.FRAMED_DRAWER) {
                        com.koudesuk.functionalstorage.block.FramedDrawerBlock framed = (com.koudesuk.functionalstorage.block.FramedDrawerBlock) block;
                        if (framed.getType() == DrawerType.X_1) {
                                simpleToFramed(exporter, framed, ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                .pattern("PPP").pattern("PCP").pattern("PPP"), 4);
                        } else if (framed.getType() == DrawerType.X_2) {
                                simpleToFramed(exporter, framed, ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                .pattern(" P ").pattern("PCP").pattern(" P "), 4);
                        } else {
                                simpleToFramed(exporter, framed, ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, block)
                                                .pattern("PCP"), 1);
                        }
                }
                // A compacting drawer becomes a framed one and keeps what it holds
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER)
                                .pattern("S S").pattern("SDS").pattern(" S ")
                                .define('S', Items.IRON_NUGGET)
                                .define('D', FunctionalStorageBlocks.COMPACTING_DRAWER)
                                .unlockedBy("has_compacting_drawer", has(FunctionalStorageBlocks.COMPACTING_DRAWER))
                                .save(CopyComponentsRecipe.output(exporter, 4, "Tile"),
                                                "functionalstorage:framed_compacting_drawer_from_simple");
                ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("SDS").pattern(" S ")
                                .define('S', Items.IRON_NUGGET)
                                .define('D', FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER)
                                .unlockedBy("has_simple_compacting_drawer",
                                                has(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER))
                                .save(CopyComponentsRecipe.output(exporter, 4, "Tile"),
                                                "functionalstorage:framed_simple_compacting_drawer_from_simple");

                // ===== CUSTOM COMPACTING (the original's custom_compacting recipes) =====
                compacting(exporter, Items.CLAY_BALL, 4, Items.CLAY, "clay");
                compacting(exporter, Items.GLOWSTONE_DUST, 4, Items.GLOWSTONE, "glowstone");
                compacting(exporter, Items.MELON_SLICE, 9, Items.MELON, "melon");
                compacting(exporter, Items.QUARTZ, 4, Items.QUARTZ_BLOCK, "quartz");
                compacting(exporter, Items.ICE, 9, Items.PACKED_ICE, "ice");
                compacting(exporter, Items.PACKED_ICE, 9, Items.BLUE_ICE, "packed_ice");
                compacting(exporter, Items.AMETHYST_SHARD, 4, Items.AMETHYST_BLOCK, "amethyst");
        }

        /**
         * Another drawer of the same size becomes this framed drawer and keeps its contents, like the original's
         * framed_X_from_simple. Fluid drawers are left out: a framed item drawer cannot read their tanks, so the
         * fluid would be lost.
         */
        private static void simpleToFramed(Consumer<FinishedRecipe> exporter,
                        com.koudesuk.functionalstorage.block.FramedDrawerBlock framed, ShapedRecipeBuilder recipe,
                        int copyIndex) {
                Block fluidDrawer = switch (framed.getType()) {
                        case X_1 -> FunctionalStorageBlocks.FLUID_DRAWER_1;
                        case X_2 -> FunctionalStorageBlocks.FLUID_DRAWER_2;
                        case X_4 -> FunctionalStorageBlocks.FLUID_DRAWER_4;
                };
                Ingredient sameSize = Ingredient.of(StorageTags.drawerOfSize(framed.getType()));
                recipe.define('P', Items.IRON_NUGGET)
                                .define('C', DefaultCustomIngredients.difference(sameSize,
                                                Ingredient.of(framed, fluidDrawer)))
                                .unlockedBy("has_drawer", has(StorageTags.DRAWER))
                                .save(CopyComponentsRecipe.output(exporter, copyIndex, "Tile"),
                                                "functionalstorage:" + BuiltInRegistries.BLOCK.getKey(framed).getPath()
                                                                + "_from_simple");
        }

        /**
         * One of the original's custom_compacting recipes: {@code count} of {@code lower} compact into one
         * {@code higher}, in the original's JSON format.
         */
        private static void compacting(Consumer<FinishedRecipe> exporter, Item lower, int count, Item higher,
                        String name) {
                ResourceLocation id = new ResourceLocation("functionalstorage", "custom_compacting/" + name);
                exporter.accept(new FinishedRecipe() {
                        @Override
                        public void serializeRecipeData(JsonObject json) {
                                JsonObject lowerInput = new JsonObject();
                                lowerInput.addProperty("item", BuiltInRegistries.ITEM.getKey(lower).toString());
                                lowerInput.addProperty("count", count);
                                JsonObject higherInput = new JsonObject();
                                higherInput.addProperty("item", BuiltInRegistries.ITEM.getKey(higher).toString());
                                higherInput.addProperty("count", 1);
                                json.add("lower_input", lowerInput);
                                json.add("higher_input", higherInput);
                        }

                        @Override
                        public ResourceLocation getId() {
                                return id;
                        }

                        @Override
                        public RecipeSerializer<?> getType() {
                                return com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe.Serializer.CUSTOM_INSTANCE;
                        }

                        @Nullable
                        @Override
                        public JsonObject serializeAdvancement() {
                                return null;
                        }

                        @Nullable
                        @Override
                        public ResourceLocation getAdvancementId() {
                                return null;
                        }
                });
        }
}
