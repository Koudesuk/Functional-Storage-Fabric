package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class FunctionalStorageItemTagProvider extends FabricTagProvider.ItemTagProvider {

    // Fabric convention tags - we add items to these tags for recipe compatibility
    public static final TagKey<Item> STONES = TagKey.create(Registries.ITEM, new ResourceLocation("c", "stones"));
    public static final TagKey<Item> WOODEN_CHESTS = TagKey.create(Registries.ITEM,
            new ResourceLocation("c", "wooden_chests"));

    public FunctionalStorageItemTagProvider(FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        // === DRAWER TAG ===
        FabricTagProvider<Item>.FabricTagBuilder drawerBuilder = getOrCreateTagBuilder(
                TagKey.create(Registries.ITEM, new ResourceLocation("functionalstorage", "drawer")));

        for (java.util.List<Block> blocks : FunctionalStorageBlocks.DRAWER_TYPES.values()) {
            for (Block block : blocks) {
                drawerBuilder.add(block.asItem());
            }
        }

        // === C:STONES TAG ===
        // Populate c:stones with stone variants (Fabric convention tag)
        // This matches Forge Tags.Items.STONE
        getOrCreateTagBuilder(STONES)
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

        // === C:WOODEN_CHESTS TAG ===
        // Populate c:wooden_chests with wooden chests only (excludes ender_chest)
        // This matches Forge Tags.Items.CHESTS_WOODEN
        getOrCreateTagBuilder(WOODEN_CHESTS)
                .add(Items.CHEST)
                .add(Items.TRAPPED_CHEST);
    }
}
