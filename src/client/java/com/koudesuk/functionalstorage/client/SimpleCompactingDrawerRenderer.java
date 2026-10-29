package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.koudesuk.functionalstorage.util.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class SimpleCompactingDrawerRenderer<T extends SimpleCompactingDrawerTile>
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
        T tile = state.blockEntity;
        if (tile == null || tile.getLevel() == null) {
            return;
        }
        if (Minecraft.getInstance().player != null
                && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(),
                        FunctionalStorageClientConfig.DRAWER_RENDER_RANGE.get())) {
            return;
        }

        poseStack.pushPose();
        DrawerRenderer.transformToDrawerFace(poseStack, tile.getFacingDirection());
        int light = DrawerRenderer.getFrontLight(tile, state.lightCoords);
        DrawerRenderer.renderUpgrades(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, tile);
        submitContents(poseStack, submitNodeCollector, light, tile);
        poseStack.popPose();
    }

    /**
     * The stored items on the drawer face, shared with the drawer items (DrawerContentsItemRenderer).
     */
    static void submitContents(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light,
            SimpleCompactingDrawerTile tile) {
        // Lower tier at the bottom, higher tier at the top, matching SimpleCompactingDrawerBlock.getHit
        renderSlot(poseStack, submitNodeCollector, light, tile, 0, 0.5f, 0.27f);
        renderSlot(poseStack, submitNodeCollector, light, tile, 1, 0.5f, 0.77f);
    }

    private static void renderSlot(PoseStack poseStack, SubmitNodeCollector collector, int light,
            SimpleCompactingDrawerTile tile, int slot, float x, float y) {
        CompactingInventoryHandler handler = tile.handler;
        if (handler.getResultList().size() <= slot) {
            return;
        }
        ItemStack display = handler.getResultList().get(slot).getResult();
        if (display.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.last().pose().mul(MathUtils.createTransformMatrix(new Vector3f(x, y, 0.0005f), new Vector3f(0),
                new Vector3f(.5f, .5f, 1.0f)));
        DrawerRenderer.renderStack(poseStack, collector, light, display, handler.getStackInSlot(slot).getCount(),
                handler.getSlotLimit(slot), 0.02f, tile.getDrawerOptions(), tile.getLevel());
        poseStack.popPose();
    }
}
