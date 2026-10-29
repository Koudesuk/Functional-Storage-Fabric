package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;

public class EnderDrawerRenderer implements BlockEntityRenderer<EnderDrawerTile, FunctionalStorageBlockEntityRenderState<EnderDrawerTile>> {
    @Override
    public FunctionalStorageBlockEntityRenderState<EnderDrawerTile> createRenderState() {
        return new FunctionalStorageBlockEntityRenderState<>();
    }

    @Override
    public void extractRenderState(EnderDrawerTile blockEntity,
            FunctionalStorageBlockEntityRenderState<EnderDrawerTile> state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(FunctionalStorageBlockEntityRenderState<EnderDrawerTile> state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        EnderDrawerTile tile = state.blockEntity;
        if (tile == null || tile.getLevel() == null || tile.getHandler() == null) {
            return;
        }
        if (Minecraft.getInstance().player != null
                && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(),
                        FunctionalStorageClientConfig.DRAWER_RENDER_RANGE.get())) {
            return;
        }

        BigInventoryHandler.BigStack stored = tile.getHandler().getStoredStacks().get(0);
        if (stored.getStack().isEmpty()) {
            return;
        }

        poseStack.pushPose();
        DrawerRenderer.transformToDrawerFace(poseStack, tile.getFacingDirection());
        int light = DrawerRenderer.getFrontLight(tile, state.lightCoords);
        DrawerRenderer.renderUpgrades(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, tile);
        poseStack.translate(0.5, 0.5, 0.0005f);
        DrawerRenderer.renderStack(poseStack, submitNodeCollector, light, stored.getStack(), stored.getAmount(),
                tile.getHandler().getSlotLimit(0), 0.015f, tile.getDrawerOptions(), tile.getLevel());
        poseStack.popPose();
    }
}
