package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.StorageTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class FunctionalStorageItemTagProvider extends FabricTagsProvider.ItemTagsProvider {

    // Fabric convention tags - we add items to these tags for recipe compatibility
    public static final TagKey<Item> STONES = TagKey.create(Registries.ITEM,
            Identifier.parse("c:stones"));
    public static final TagKey<Item> WOODEN_CHESTS = TagKey.create(Registries.ITEM,
            Identifier.parse("c:chests/wooden"));

    public FunctionalStorageItemTagProvider(FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        // === DRAWER TAG ===
        var drawerBuilder = valueLookupBuilder(
                TagKey.create(Registries.ITEM, Identifier.parse("functionalstorage:drawer")));

        for (java.util.List<Block> blocks : FunctionalStorageBlocks.DRAWER_TYPES.values()) {
            for (Block block : blocks) {
                drawerBuilder.add(block.asItem());
            }
        }
        drawerBuilder
                .add(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER.asItem())
                .add(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER.asItem())
                .add(FunctionalStorageBlocks.COMPACTING_DRAWER.asItem())
                .add(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER.asItem())
                .add(FunctionalStorageBlocks.FLUID_DRAWER_1.asItem())
                .add(FunctionalStorageBlocks.FLUID_DRAWER_2.asItem())
                .add(FunctionalStorageBlocks.FLUID_DRAWER_4.asItem());

        for (Block block : FunctionalStorageBlocks.FRAMED_DRAWER) {
            drawerBuilder.add(block.asItem());
        }
        for (Block block : FunctionalStorageBlocks.FRAMED_FLUID_DRAWER) {
            drawerBuilder.add(block.asItem());
        }

        // === DRAWERS BY SIZE AND FLUID DRAWERS === (the original's drawer_1x1/1x2/2x2 and fluid_drawer)
        for (DrawerType type : DrawerType.values()) {
            var sizeBuilder = valueLookupBuilder(StorageTags.drawerOfSize(type));
            for (Block block : FunctionalStorageBlocks.DRAWER_TYPES.get(type)) {
                sizeBuilder.add(block.asItem());
            }
            for (Block block : FunctionalStorageBlocks.FRAMED_DRAWER) {
                if (((com.koudesuk.functionalstorage.block.DrawerBlock) block).getType() == type)
                    sizeBuilder.add(block.asItem());
            }
            sizeBuilder.add(switch (type) {
                case X_1 -> FunctionalStorageBlocks.FLUID_DRAWER_1.asItem();
                case X_2 -> FunctionalStorageBlocks.FLUID_DRAWER_2.asItem();
                case X_4 -> FunctionalStorageBlocks.FLUID_DRAWER_4.asItem();
            });
        }
        valueLookupBuilder(StorageTags.FLUID_DRAWER)
                .add(FunctionalStorageBlocks.FLUID_DRAWER_1.asItem())
                .add(FunctionalStorageBlocks.FLUID_DRAWER_2.asItem())
                .add(FunctionalStorageBlocks.FLUID_DRAWER_4.asItem());

        // === C:STONES TAG ===
        // Populate c:stones with stone variants (Fabric convention tag)
        // This matches Forge Tags.Items.STONE
        valueLookupBuilder(STONES)
                .add(Items.STONE)
                .add(Items.GRANITE)
                .add(Items.DIORITE)
                .add(Items.ANDESITE)
                .add(Items.POLISHED_GRANITE)
                .add(Items.POLISHED_DIORITE)
                .add(Items.POLISHED_ANDESITE)
                .add(Items.DEEPSLATE)
                .add(Items.POLISHED_DEEPSLATE)
                .add(Items.TUFF)
                .add(Items.CALCITE);

        // === C:CHESTS/WOODEN TAG ===
        // Populate c:chests/wooden with wooden chests only (excludes ender chest)
        // This matches Forge Tags.Items.CHESTS_WOODEN
        valueLookupBuilder(WOODEN_CHESTS)
                .add(Items.CHEST)
                .add(Items.TRAPPED_CHEST);

        // === DATA PACK TAGS === (same contents as the original's FunctionalStorageItemTagsProvider)
        // Vanilla tags are not known to this provider, so addTag fails its reference check;
        // forceAddTag skips the check and still writes a plain required "#minecraft:swords"
        valueLookupBuilder(StorageTags.ARMORY_CABINET_INSERTABLE)
                .forceAddTag(ItemTags.SWORDS)
                .forceAddTag(ItemTags.AXES)
                .forceAddTag(ItemTags.HOES)
                .forceAddTag(ItemTags.PICKAXES)
                .forceAddTag(ItemTags.SHOVELS)
                .forceAddTag(ItemTags.HEAD_ARMOR)
                .forceAddTag(ItemTags.CHEST_ARMOR)
                .forceAddTag(ItemTags.LEG_ARMOR)
                .forceAddTag(ItemTags.FOOT_ARMOR)
                .forceAddTag(ItemTags.BOW_ENCHANTABLE)
                .forceAddTag(ItemTags.CROSSBOW_ENCHANTABLE)
                .forceAddTag(ItemTags.TRIDENT_ENCHANTABLE)
                .forceAddTag(ItemTags.MACE_ENCHANTABLE)
                .add(Items.ELYTRA, Items.SHIELD, Items.FISHING_ROD, Items.FLINT_AND_STEEL, Items.SHEARS, Items.BRUSH,
                        Items.ENCHANTED_BOOK);
        // Empty on purpose, for data packs to fill
        valueLookupBuilder(StorageTags.DRAWER_STORAGE_DENYLIST);
        valueLookupBuilder(StorageTags.CREATIVE_VENDING_UPGRADE_INCOMPATIBLE);
    }
}
