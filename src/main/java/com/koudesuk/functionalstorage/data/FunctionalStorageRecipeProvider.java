package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.item.StorageUpgradeItem;
import com.koudesuk.functionalstorage.recipe.CopyComponentsRecipe;
import com.koudesuk.functionalstorage.recipe.CustomCompactingRecipe;
import com.koudesuk.functionalstorage.recipe.DrawerlessWoodIngredient;
import com.koudesuk.functionalstorage.recipe.TagWithoutComponentIngredient;
import com.koudesuk.functionalstorage.registry.FSAttachments;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.DrawerWoodType;
import com.koudesuk.functionalstorage.util.StorageTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.resources.Identifier;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;

public class FunctionalStorageRecipeProvider extends FabricRecipeProvider {

        // Fabric convention tags (c: namespace), the same tags the original takes through NeoForge's Tags.Items.
        // c:stones includes stone, granite, diorite, andesite, deepslate, tuff, calcite, etc.
        // c:chests/wooden includes chest and trapped_chest (excludes ender chest)
        private static final TagKey<Item> STONES = cTag("stones");
        private static final TagKey<Item> WOODEN_CHESTS = cTag("chests/wooden");
        private static final TagKey<Item> ENDER_CHESTS = cTag("chests/ender");
        private static final TagKey<Item> IRON_NUGGETS = cTag("nuggets/iron");
        private static final TagKey<Item> IRON_INGOTS = cTag("ingots/iron");
        private static final TagKey<Item> GOLD_INGOTS = cTag("ingots/gold");
        private static final TagKey<Item> GOLD_BLOCKS = cTag("storage_blocks/gold");
        private static final TagKey<Item> DIAMONDS = cTag("gems/diamond");
        private static final TagKey<Item> DIAMOND_BLOCKS = cTag("storage_blocks/diamond");
        private static final TagKey<Item> NETHERITE_INGOTS = cTag("ingots/netherite");
        private static final TagKey<Item> REDSTONE_DUSTS = cTag("dusts/redstone");
        private static final TagKey<Item> EMPTY_BUCKETS = cTag("buckets/empty");
        // c:obsidians includes obsidian and crying obsidian, plus whatever other mods add (same as the original)
        private static final TagKey<Item> OBSIDIANS = cTag("obsidians");

        public FunctionalStorageRecipeProvider(FabricPackOutput output,
                        CompletableFuture<HolderLookup.Provider> registriesFuture) {
                super(output, registriesFuture);
        }

        private static TagKey<Item> cTag(String path) {
                return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
        }

        @Override
        public String getName() {
                return "Functional Storage Recipes";
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput exporter) {
                return new RecipeProvider(registries, exporter) {
                        @Override
                        public void buildRecipes() {
                // Unlocks still use the plain tag, but a drawer is only used up while it is empty: like the
                // original's tag_without_component, a drawer with contents, upgrades or a lock never matches
                TagKey<Item> DRAWER = StorageTags.DRAWER;
                Ingredient EMPTY_DRAWER = new TagWithoutComponentIngredient(DRAWER).toVanilla();

                SpecialRecipeBuilder.special(com.koudesuk.functionalstorage.recipe.FramedDrawerRecipe::new)
                                .save(exporter, "functionalstorage:framed");

                // ===== UPGRADES =====

                // Iron Upgrade
                shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.IRON))
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', IRON_INGOTS)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Void Upgrade
                shaped(RecipeCategory.MISC, FunctionalStorageItems.VOID_UPGRADE)
                                .pattern("III").pattern("IDI").pattern("III")
                                .define('I', OBSIDIANS)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Configuration Tool
                shaped(RecipeCategory.TOOLS, FunctionalStorageItems.CONFIGURATION_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', GOLD_INGOTS)
                                .define('D', EMPTY_DRAWER)
                                .define('E', Items.EMERALD)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Linking Tool
                shaped(RecipeCategory.TOOLS, FunctionalStorageItems.LINKING_TOOL)
                                .pattern("PPG").pattern("PDG").pattern("PEP")
                                .define('P', Items.PAPER)
                                .define('G', GOLD_INGOTS)
                                .define('D', EMPTY_DRAWER)
                                .define('E', Items.DIAMOND)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Copper Upgrade (uses CHESTS_WOODEN tag)
                shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.COPPER))
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', Items.COPPER_INGOT)
                                .define('B', Items.COPPER_BLOCK)
                                .define('C', WOODEN_CHESTS)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Gold Upgrade (uses WOODEN_CHESTS tag)
                shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.GOLD))
                                .pattern("IBI").pattern("CDC").pattern("BIB")
                                .define('I', GOLD_INGOTS)
                                .define('B', GOLD_BLOCKS)
                                .define('C', WOODEN_CHESTS)
                                .define('D', FunctionalStorageItems.STORAGE_UPGRADES
                                                .get(StorageUpgradeItem.StorageTier.COPPER))
                                .unlockedBy("has_copper_upgrade",
                                                has(FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.COPPER)))
                                .save(exporter);

                // Diamond Upgrade (uses WOODEN_CHESTS tag)
                shaped(RecipeCategory.MISC,
                                FunctionalStorageItems.STORAGE_UPGRADES.get(StorageUpgradeItem.StorageTier.DIAMOND))
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', DIAMONDS)
                                .define('B', DIAMOND_BLOCKS)
                                .define('C', WOODEN_CHESTS)
                                .define('D', FunctionalStorageItems.STORAGE_UPGRADES
                                                .get(StorageUpgradeItem.StorageTier.GOLD))
                                .unlockedBy("has_gold_upgrade",
                                                has(FunctionalStorageItems.STORAGE_UPGRADES
                                                                .get(StorageUpgradeItem.StorageTier.GOLD)))
                                .save(exporter);

                // Redstone Upgrade
                shaped(RecipeCategory.MISC, FunctionalStorageItems.REDSTONE_UPGRADE)
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
                                .save(exporter, "functionalstorage:netherite_upgrade_smithing");

                // Armory Cabinet (uses STONE tag)
                shaped(RecipeCategory.MISC, FunctionalStorageBlocks.ARMORY_CABINET)
                                .pattern("ICI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', NETHERITE_INGOTS)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pulling Upgrade (uses STONES tag)
                shaped(RecipeCategory.MISC, FunctionalStorageItems.PULLING_UPGRADE)
                                .pattern("ICI").pattern("IDI").pattern("IBI")
                                .define('I', STONES)
                                .define('B', REDSTONE_DUSTS)
                                .define('C', Items.HOPPER)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pushing Upgrade (uses STONES tag)
                shaped(RecipeCategory.MISC, FunctionalStorageItems.PUSHING_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IRI")
                                .define('I', STONES)
                                .define('B', REDSTONE_DUSTS)
                                .define('R', Items.HOPPER)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Pulling and Pushing Upgrades turn into each other, like the original
                shapeless(RecipeCategory.MISC, FunctionalStorageItems.PUSHING_UPGRADE)
                                .requires(FunctionalStorageItems.PULLING_UPGRADE)
                                .unlockedBy("has_puller_upgrade", has(FunctionalStorageItems.PULLING_UPGRADE))
                                .save(exporter, "functionalstorage:pusher_upgrade_from_puller");
                shapeless(RecipeCategory.MISC, FunctionalStorageItems.PULLING_UPGRADE)
                                .requires(FunctionalStorageItems.PUSHING_UPGRADE)
                                .unlockedBy("has_pusher_upgrade", has(FunctionalStorageItems.PUSHING_UPGRADE))
                                .save(exporter, "functionalstorage:puller_upgrade_from_pusher");

                // Generator upgrades
                shaped(RecipeCategory.MISC, FunctionalStorageItems.DRIPPING_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IRI")
                                .define('I', STONES)
                                .define('B', Items.POINTED_DRIPSTONE)
                                .define('R', Items.LAVA_BUCKET)
                                .define('D', Items.CAULDRON)
                                .unlockedBy("has_stone", has(STONES))
                                .save(exporter);
                shaped(RecipeCategory.MISC, FunctionalStorageItems.WATER_GENERATOR_UPGRADE)
                                .pattern("IBI").pattern("IDI").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Items.WATER_BUCKET)
                                .define('D', Items.BUCKET)
                                .unlockedBy("has_stone", has(STONES))
                                .save(exporter);
                shapeless(RecipeCategory.MISC, FunctionalStorageItems.OBSIDIAN_UPGRADE)
                                .requires(FunctionalStorageItems.DRIPPING_UPGRADE, 4)
                                .requires(FunctionalStorageItems.WATER_GENERATOR_UPGRADE)
                                .unlockedBy("has_dripping_upgrade", has(FunctionalStorageItems.DRIPPING_UPGRADE))
                                .save(exporter);

                // Collector Upgrade (uses STONES tag)
                shaped(RecipeCategory.MISC, FunctionalStorageItems.COLLECTOR_UPGRADE)
                                .pattern("IBI").pattern("RDR").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Items.HOPPER)
                                .define('R', REDSTONE_DUSTS)
                                .define('D', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // Ender Drawer
                shaped(RecipeCategory.MISC, FunctionalStorageBlocks.ENDER_DRAWER)
                                .pattern("PPP").pattern("LCL").pattern("PPP")
                                .define('P', ItemTags.PLANKS)
                                .define('C', ENDER_CHESTS)
                                .define('L', EMPTY_DRAWER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== WOODEN DRAWERS (uses WOODEN_CHESTS tag) =====
                // Oak drawers also take the planks of every wood without a drawer of its own (other mods' woods),
                // like the original's oak_drawer_alternate recipes
                for (com.koudesuk.functionalstorage.util.DrawerType type : com.koudesuk.functionalstorage.util.DrawerType
                                .values()) {
                        for (net.minecraft.world.level.block.Block block : FunctionalStorageBlocks.DRAWER_TYPES
                                        .get(type)) {
                                if (block instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
                                        net.minecraft.world.level.block.Block planks = drawerBlock.getWoodType()
                                                        .getPlanks();
                                        Ingredient plankIngredient = drawerBlock.getWoodType() == DrawerWoodType.OAK
                                                        ? DrawerlessWoodIngredient.INSTANCE.toVanilla()
                                                        : Ingredient.of(planks);
                                        if (type == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                                shaped(RecipeCategory.DECORATIONS, block)
                                                                .pattern("PPP").pattern("PCP").pattern("PPP")
                                                                .define('P', plankIngredient)
                                                                .define('C', WOODEN_CHESTS)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                                shaped(RecipeCategory.DECORATIONS, block, 2)
                                                                .pattern("PCP").pattern("PPP").pattern("PCP")
                                                                .define('P', plankIngredient)
                                                                .define('C', WOODEN_CHESTS)
                                                                .unlockedBy("has_planks", has(planks))
                                                                .save(exporter);
                                        } else if (type == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                                shaped(RecipeCategory.DECORATIONS, block, 4)
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
                shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_1)
                                .pattern("PPP").pattern("PBP").pattern("PPP")
                                .define('P', ItemTags.PLANKS)
                                .define('B', EMPTY_BUCKETS)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_2, 2)
                                .pattern("PBP").pattern("PPP").pattern("PBP")
                                .define('P', ItemTags.PLANKS)
                                .define('B', EMPTY_BUCKETS)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.FLUID_DRAWER_4, 4)
                                .pattern("BPB").pattern("PPP").pattern("BPB")
                                .define('P', ItemTags.PLANKS)
                                .define('B', EMPTY_BUCKETS)
                                .unlockedBy("has_bucket", has(Items.BUCKET))
                                .save(exporter);

                // ===== FRAMED FLUID DRAWERS =====
                for (net.minecraft.world.level.block.Block block : FunctionalStorageBlocks.FRAMED_FLUID_DRAWER) {
                        DrawerType type = ((com.koudesuk.functionalstorage.block.FramedFluidDrawerBlock) block).getType();
                        ShapedRecipeBuilder builder = type == DrawerType.X_1
                                        ? shaped(RecipeCategory.MISC, block).pattern("PPP").pattern("PCP").pattern("PPP")
                                        : type == DrawerType.X_2
                                                        ? shaped(RecipeCategory.MISC, block, 2).pattern("PCP").pattern("PPP").pattern("PCP")
                                                        : shaped(RecipeCategory.MISC, block, 4).pattern("CPC").pattern("PPP").pattern("CPC");
                        builder.define('P', Items.IRON_NUGGET)
                                        .define('C', EMPTY_BUCKETS)
                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                        .save(exporter);
                }

                // ===== FRAMED DRAWERS =====
                for (net.minecraft.world.level.block.Block block : FunctionalStorageBlocks.FRAMED_DRAWER) {
                        if (block instanceof com.koudesuk.functionalstorage.block.FramedDrawerBlock framedDrawerBlock) {
                                if (framedDrawerBlock.getType() == com.koudesuk.functionalstorage.util.DrawerType.X_1) {
                                        shaped(RecipeCategory.MISC, block)
                                                        .pattern("PPP").pattern("PCP").pattern("PPP")
                                                        .define('P', IRON_NUGGETS)
                                                        .define('C', WOODEN_CHESTS)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                        simpleToFramed(framedDrawerBlock, shaped(RecipeCategory.MISC, block)
                                                        .pattern("PPP").pattern("PCP").pattern("PPP"), 4);
                                } else if (framedDrawerBlock
                                                .getType() == com.koudesuk.functionalstorage.util.DrawerType.X_2) {
                                        shaped(RecipeCategory.MISC, block, 2)
                                                        .pattern("PCP").pattern("PPP").pattern("PCP")
                                                        .define('P', IRON_NUGGETS)
                                                        .define('C', WOODEN_CHESTS)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                        simpleToFramed(framedDrawerBlock, shaped(RecipeCategory.MISC, block)
                                                        .pattern(" P ").pattern("PCP").pattern(" P "), 4);
                                } else if (framedDrawerBlock
                                                .getType() == com.koudesuk.functionalstorage.util.DrawerType.X_4) {
                                        shaped(RecipeCategory.MISC, block, 4)
                                                        .pattern("CPC").pattern("PPP").pattern("CPC")
                                                        .define('P', IRON_NUGGETS)
                                                        .define('C', WOODEN_CHESTS)
                                                        .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                                                        .save(exporter);
                                        simpleToFramed(framedDrawerBlock, shaped(RecipeCategory.MISC, block)
                                                        .pattern("PCP"), 1);
                                }
                        }
                }

                // ===== COMPACTING DRAWERS (correct pattern: SSS/PDP/SIS) =====
                shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.COMPACTING_DRAWER)
                                .pattern("SSS").pattern("PDP").pattern("SIS")
                                .define('S', STONES)
                                .define('P', Blocks.PISTON)
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== SIMPLE COMPACTING DRAWER (correct pattern: SSS/SDP/SIS) =====
                shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("SDP").pattern("SIS")
                                .define('S', STONES)
                                .define('P', Blocks.PISTON)
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED COMPACTING DRAWER =====
                shaped(RecipeCategory.MISC, FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("PDP").pattern("SIS")
                                .define('S', Items.IRON_NUGGET)
                                .define('P', Blocks.PISTON)
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);
                // A compacting drawer becomes a framed one and keeps what it holds
                shaped(RecipeCategory.MISC, FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER)
                                .pattern("S S").pattern("SDS").pattern(" S ")
                                .define('S', IRON_NUGGETS)
                                .define('D', FunctionalStorageBlocks.COMPACTING_DRAWER)
                                .unlockedBy("has_compacting_drawer", has(FunctionalStorageBlocks.COMPACTING_DRAWER))
                                .save(CopyComponentsRecipe.output(exporter, 4, FSAttachments.TILE),
                                                "functionalstorage:framed_compacting_drawer_from_simple");

                // ===== FRAMED SIMPLE COMPACTING DRAWER =====
                shaped(RecipeCategory.MISC,
                                                FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("SDP").pattern("SIS")
                                .define('S', IRON_NUGGETS)
                                .define('P', Blocks.PISTON)
                                .define('D', EMPTY_DRAWER)
                                .define('I', IRON_INGOTS)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);
                shaped(RecipeCategory.MISC, FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER)
                                .pattern("SSS").pattern("SDS").pattern(" S ")
                                .define('S', IRON_NUGGETS)
                                .define('D', FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER)
                                .unlockedBy("has_simple_compacting_drawer",
                                                has(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER))
                                .save(CopyComponentsRecipe.output(exporter, 4, FSAttachments.TILE),
                                                "functionalstorage:framed_simple_compacting_drawer_from_simple");

                // ===== STORAGE CONTROLLER (correct pattern: IBI/CDC/IBI) =====
                shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.DRAWER_CONTROLLER)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== CONTROLLER EXTENSION (correct pattern: IBI/CDC/IBI) =====
                shaped(RecipeCategory.DECORATIONS, FunctionalStorageBlocks.CONTROLLER_EXTENSION)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', STONES)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.REPEATER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED STORAGE CONTROLLER =====
                shaped(RecipeCategory.MISC, FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', IRON_NUGGETS)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.COMPARATOR)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== FRAMED CONTROLLER EXTENSION (Access Point) =====
                shaped(RecipeCategory.MISC, FunctionalStorageBlocks.FRAMED_CONTROLLER_EXTENSION)
                                .pattern("IBI").pattern("CDC").pattern("IBI")
                                .define('I', IRON_NUGGETS)
                                .define('B', Blocks.QUARTZ_BLOCK)
                                .define('C', EMPTY_DRAWER)
                                .define('D', Items.REPEATER)
                                .unlockedBy("has_drawer", has(DRAWER))
                                .save(exporter);

                // ===== CUSTOM COMPACTING (the original's custom_compacting recipes) =====
                compacting(new ItemStackTemplate(Items.GLOWSTONE_DUST, 4), new ItemStackTemplate(Items.GLOWSTONE), "glowstone");
                compacting(new ItemStackTemplate(Items.MELON_SLICE, 9), new ItemStackTemplate(Items.MELON), "melon");
                compacting(new ItemStackTemplate(Items.QUARTZ, 4), new ItemStackTemplate(Items.QUARTZ_BLOCK), "quartz");
                compacting(new ItemStackTemplate(Items.ICE, 9), new ItemStackTemplate(Items.PACKED_ICE), "ice");
                compacting(new ItemStackTemplate(Items.PACKED_ICE, 9), new ItemStackTemplate(Items.BLUE_ICE), "packed_ice");
                compacting(new ItemStackTemplate(Items.AMETHYST_SHARD, 4), new ItemStackTemplate(Items.AMETHYST_BLOCK), "amethyst");
                        }

                        /**
                         * Another drawer of the same size becomes this framed drawer and keeps its contents, like
                         * the original's framed_X_from_simple. Fluid drawers are left out: a framed item drawer
                         * cannot read their tanks, so the fluid would be lost.
                         */
                        private void simpleToFramed(com.koudesuk.functionalstorage.block.FramedDrawerBlock framed,
                                        ShapedRecipeBuilder recipe, int copyIndex) {
                                Block fluidDrawer = switch (framed.getType()) {
                                        case X_1 -> FunctionalStorageBlocks.FLUID_DRAWER_1;
                                        case X_2 -> FunctionalStorageBlocks.FLUID_DRAWER_2;
                                        case X_4 -> FunctionalStorageBlocks.FLUID_DRAWER_4;
                                };
                                Ingredient sameSize = Ingredient.of(registries.lookupOrThrow(Registries.ITEM)
                                                .getOrThrow(StorageTags.drawerOfSize(framed.getType())));
                                recipe.define('P', IRON_NUGGETS)
                                                .define('C', DefaultCustomIngredients.difference(sameSize,
                                                                Ingredient.of(framed, fluidDrawer)))
                                                .unlockedBy("has_drawer", has(StorageTags.DRAWER))
                                                .save(CopyComponentsRecipe.output(exporter, copyIndex, FSAttachments.TILE),
                                                                "functionalstorage:" + net.minecraft.core.registries.BuiltInRegistries.BLOCK
                                                                                .getKey(framed).getPath() + "_from_simple");
                        }

                        private void compacting(ItemStackTemplate lower, ItemStackTemplate higher, String name) {
                                new CustomCompactingRecipe(lower, higher).save(exporter,
                                                Identifier.fromNamespaceAndPath("functionalstorage", "compacting/" + name));
                        }
                };
        }
}
