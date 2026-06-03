package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public class ControllerRenderer<T extends StorageControllerTile>
        implements BlockEntityRenderer<T, FunctionalStorageBlockEntityRenderState<T>> {
    @Override
    public FunctionalStorageBlockEntityRenderState<T> createRenderState() {
        return new FunctionalStorageBlockEntityRenderState<>();
    }

    @Override
    public void extractRenderState(T blockEntity, FunctionalStorageBlockEntityRenderState<T> state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(FunctionalStorageBlockEntityRenderState<T> state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
