package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public class FunctionalStorageBlockTagProvider extends FabricTagProvider.BlockTagProvider {
    public FunctionalStorageBlockTagProvider(FabricDataOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        FabricTagProvider<Block>.FabricTagBuilder drawerBuilder = getOrCreateTagBuilder(
                net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                        new net.minecraft.resources.ResourceLocation("functionalstorage", "drawer")));

        for (java.util.List<Block> blocks : FunctionalStorageBlocks.DRAWER_TYPES.values()) {
            for (Block block : blocks) {
                drawerBuilder.add(block);
                getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE).add(block);
            }
        }
    }
}
