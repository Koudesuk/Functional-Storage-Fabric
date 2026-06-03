package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;

public class FunctionalStorageBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public FunctionalStorageBlockTagProvider(FabricPackOutput output,
            CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg) {
        var drawerBuilder = valueLookupBuilder(
                net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                        Identifier.fromNamespaceAndPath("functionalstorage", "drawer")));

        // Add wooden drawers to drawer tag and MINEABLE_WITH_AXE
        for (java.util.List<Block> blocks : FunctionalStorageBlocks.DRAWER_TYPES.values()) {
            for (Block block : blocks) {
                drawerBuilder.add(block);
                valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE).add(block);
            }
        }

        // Add framed drawers to MINEABLE_WITH_AXE (they are wood-based)
        for (Block framedDrawer : FunctionalStorageBlocks.FRAMED_DRAWER) {
            valueLookupBuilder(BlockTags.MINEABLE_WITH_AXE).add(framedDrawer);
        }

        // Add pickaxe-mineable blocks (metal/stone-based) - matches Forge
        // implementation
        valueLookupBuilder(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(FunctionalStorageBlocks.COMPACTING_DRAWER)
                .add(FunctionalStorageBlocks.DRAWER_CONTROLLER)
                .add(FunctionalStorageBlocks.ARMORY_CABINET)
                .add(FunctionalStorageBlocks.ENDER_DRAWER)
                .add(FunctionalStorageBlocks.FRAMED_COMPACTING_DRAWER)
                .add(FunctionalStorageBlocks.FLUID_DRAWER_1)
                .add(FunctionalStorageBlocks.FLUID_DRAWER_2)
                .add(FunctionalStorageBlocks.FLUID_DRAWER_4)
                .add(FunctionalStorageBlocks.CONTROLLER_EXTENSION)
                .add(FunctionalStorageBlocks.SIMPLE_COMPACTING_DRAWER)
                .add(FunctionalStorageBlocks.FRAMED_DRAWER_CONTROLLER)
                .add(FunctionalStorageBlocks.FRAMED_SIMPLE_COMPACTING_DRAWER)
                .add(FunctionalStorageBlocks.FRAMED_CONTROLLER_EXTENSION);
    }
}
