package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class FluidDrawerRenderer
        implements BlockEntityRenderer<FluidDrawerTile, FunctionalStorageBlockEntityRenderState<FluidDrawerTile>> {
    @Override
    public FunctionalStorageBlockEntityRenderState<FluidDrawerTile> createRenderState() {
        return new FunctionalStorageBlockEntityRenderState<>();
    }

    @Override
    public void extractRenderState(FluidDrawerTile blockEntity,
            FunctionalStorageBlockEntityRenderState<FluidDrawerTile> state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(FunctionalStorageBlockEntityRenderState<FluidDrawerTile> state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    }
}
