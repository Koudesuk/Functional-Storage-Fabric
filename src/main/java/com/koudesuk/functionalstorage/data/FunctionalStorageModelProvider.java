package com.koudesuk.functionalstorage.data;

import com.koudesuk.functionalstorage.registry.FunctionalStorageBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.data.models.blockstates.Variant;
import net.minecraft.data.models.blockstates.VariantProperties;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class FunctionalStorageModelProvider extends FabricModelProvider {
    public FunctionalStorageModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockStateGenerator) {
        for (java.util.Map.Entry<com.koudesuk.functionalstorage.util.DrawerType, java.util.List<Block>> entry : FunctionalStorageBlocks.DRAWER_TYPES.entrySet()) {
            com.koudesuk.functionalstorage.util.DrawerType type = entry.getKey();
            for (Block block : entry.getValue()) {
                if (block instanceof com.koudesuk.functionalstorage.block.DrawerBlock drawerBlock) {
                    ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(block);
                    com.koudesuk.functionalstorage.util.IWoodType woodType = drawerBlock.getWoodType();
                    String woodName = woodType.getName();

                    // Generate Model
                    ResourceLocation parent = new ResourceLocation("functionalstorage", "block/base_x_" + type.getSlots());
                    TextureSlot front = TextureSlot.create("front");
                    TextureSlot side = TextureSlot.create("side");
                    TextureMapping mapping = new TextureMapping()
                            .put(TextureSlot.PARTICLE, new ResourceLocation("functionalstorage", "block/" + woodName + "_front_" + type.getSlots()))
                            .put(front, new ResourceLocation("functionalstorage", "block/" + woodName + "_front_" + type.getSlots()))
                            .put(side, new ResourceLocation("functionalstorage", "block/" + woodName + "_side"));

                    ModelTemplate template = new ModelTemplate(Optional.of(parent), Optional.empty(), TextureSlot.PARTICLE, front, side);
                    template.create(block, mapping, blockStateGenerator.modelOutput);

                    // Generate BlockState
                    blockStateGenerator.blockStateOutput.accept(MultiVariantGenerator
                            .multiVariant(block, Variant.variant().with(VariantProperties.MODEL, modelLocation))
                            .with(BlockModelGenerators.createBooleanModelDispatch(
                                    com.koudesuk.functionalstorage.block.DrawerBlock.LOCKED, modelLocation,
                                    modelLocation))
                            .with(BlockModelGenerators.createHorizontalFacingDispatch()));
                }
            }
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerator) {
        for (java.util.List<Block> blocks : FunctionalStorageBlocks.DRAWER_TYPES.values()) {
            for (Block block : blocks) {
                itemModelGenerator.generateFlatItem(block.asItem(), new ModelTemplate(Optional.of(ModelLocationUtils.getModelLocation(block)), Optional.empty()));
            }
        }
    }
}
