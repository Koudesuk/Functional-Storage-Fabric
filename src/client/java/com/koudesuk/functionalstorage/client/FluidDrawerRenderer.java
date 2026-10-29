package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Renders fluid contents and indicators on fluid drawer block faces.
 * Same coordinate system as the original FluidDrawerRenderer.
 */
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
        FluidDrawerTile tile = state.blockEntity;
        if (tile == null || tile.getLevel() == null) {
            return;
        }
        if (Minecraft.getInstance().player != null
                && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(),
                        FunctionalStorageClientConfig.DRAWER_RENDER_RANGE.get())) {
            return;
        }

        poseStack.pushPose();
        Direction facing = tile.getFacingDirection();
        poseStack.mulPose(Axis.YP.rotationDegrees(-180));
        if (facing == Direction.NORTH) {
            poseStack.translate(-1, 0, -1);
        } else if (facing == Direction.EAST) {
            poseStack.translate(0, 0, -1);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90));
        } else if (facing == Direction.SOUTH) {
            poseStack.mulPose(Axis.YP.rotationDegrees(-180));
        } else if (facing == Direction.WEST) {
            poseStack.translate(-1, 0, 0);
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
        int light = DrawerRenderer.getFrontLight(tile, state.lightCoords);
        submitContents(poseStack, submitNodeCollector, light, tile);

        // Upgrades after the fluids, at the front face
        poseStack.pushPose();
        poseStack.translate(0, 0, 0.9688);
        DrawerRenderer.renderUpgrades(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, tile);
        poseStack.popPose();
        poseStack.popPose();
    }

    /**
     * The tanks of a north facing drawer, shared with the drawer items (DrawerContentsItemRenderer).
     */
    static void submitContents(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light,
            FluidDrawerTile tile) {
        // Slot order follows DrawerBlock.getHit: bottom before top, right before left
        if (tile.getDrawerType() == DrawerType.X_1) {
            renderSlot(poseStack, submitNodeCollector, light, tile, 0, 0, 0, 15, 12.5, false);
        } else if (tile.getDrawerType() == DrawerType.X_2) {
            renderSlot(poseStack, submitNodeCollector, light, tile, 0, 0, 0, 15, 5.5, false);
            renderSlot(poseStack, submitNodeCollector, light, tile, 1, 0, 0.5, 15, 5.5, false);
        } else if (tile.getDrawerType() == DrawerType.X_4) {
            renderSlot(poseStack, submitNodeCollector, light, tile, 0, 0.5, 0, 8, 5.5, true);
            renderSlot(poseStack, submitNodeCollector, light, tile, 1, 0, 0, 8, 5.5, true);
            renderSlot(poseStack, submitNodeCollector, light, tile, 2, 0.5, 0.5, 8, 5.5, true);
            renderSlot(poseStack, submitNodeCollector, light, tile, 3, 0, 0.5, 8, 5.5, true);
        }
    }

    /**
     * One tank: offset to its quarter or half of the face, then a box from 1/16 up to its fill level.
     * A locked empty slot still shows its filter fluid, faded, with an amount of 0.
     */
    private static void renderSlot(PoseStack poseStack, SubmitNodeCollector collector, int light, FluidDrawerTile tile,
            int slot, double offsetX, double offsetY, double maxX, double fullHeight, boolean halfText) {
        FluidInventoryHandler handler = tile.getHandler();
        FluidVariant fluid = handler.getResource(slot);
        long amount = handler.getAmount(slot);
        if (fluid.isBlank()) {
            if (!tile.isLocked() || handler.getFilterStack(slot).isBlank()) {
                return;
            }
            fluid = handler.getFilterStack(slot);
            amount = 0;
        }
        long capacity = handler.getSlotLimit(slot);
        double fillRatio = capacity > 0 ? (double) amount / capacity : 0;

        poseStack.pushPose();
        poseStack.translate(offsetX, offsetY, 0);
        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, maxX / 16D,
                1.25 / 16D + fillRatio * (fullHeight / 16D), 15 / 16D);
        renderFluidStack(poseStack, collector, light, fluid, amount, capacity, 0.007f, tile.getDrawerOptions(), bounds,
                halfText);
        poseStack.popPose();
    }

    public static void renderFluidStack(PoseStack poseStack, SubmitNodeCollector collector, int light,
            FluidVariant fluid, long amount, long maxAmount, float scale, ControllableDrawerTile.DrawerOptions options,
            AABB bounds, boolean halfText) {
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
            // The same still texture the fluid uses in the world
            TextureAtlasSprite still = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                    .get(fluid.getFluid().defaultFluidState()).stillMaterial().sprite();

            int color = FluidVariantRendering.getColor(fluid);
            float red = (color >> 16 & 0xFF) / 255f;
            float green = (color >> 8 & 0xFF) / 255f;
            float blue = (color & 0xFF) / 255f;
            float tintAlpha = (color >> 24 & 0xFF) / 255f;
            // Faded when the slot only shows its lock filter; tints without alpha count as opaque
            float alpha = amount == 0 ? 0.3f : tintAlpha == 0 ? 1.0f : tintAlpha;

            float x1 = (float) bounds.minX;
            float x2 = (float) bounds.maxX;
            float y1 = (float) bounds.minY;
            float y2 = (float) bounds.maxY;
            float z1 = (float) bounds.minZ;
            float z2 = (float) bounds.maxZ;

            float u1 = still.getU(x1), u2 = still.getU(x2);
            float v1top = still.getV(z1), v2top = still.getV(z2);
            float v1front = still.getV(y1), v2front = still.getV(y2);

            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
                // TOP
                buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
                buffer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
                buffer.addVertex(pose, x2, y2, z1).setColor(red, green, blue, alpha).setUv(u2, v1top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
                buffer.addVertex(pose, x1, y2, z1).setColor(red, green, blue, alpha).setUv(u1, v1top).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 1f, 0f);
                // FRONT
                buffer.addVertex(pose, x2, y1, z2).setColor(red, green, blue, alpha).setUv(u2, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                buffer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha).setUv(u2, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                buffer.addVertex(pose, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
            });
        }

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
            poseStack.pushPose();
            poseStack.translate(0.5, 0.84, 0.97);
            if (halfText)
                poseStack.translate(-0.25, 0, 0);
            DrawerRenderer.renderText(poseStack, collector, light, Component.literal(ChatFormatting.WHITE + ""
                    + NumberUtils.getFormatedFluidBigNumber(amount)), Direction.NORTH, scale);
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0.5, 0.453, 0.97);
        if (halfText) {
            poseStack.scale(0.5f, 0.65f, 0.5f);
            poseStack.translate(-0.5, -0.18, 0);
        }
        DrawerRenderer.renderIndicator(poseStack, collector, light, Math.min(1, amount / (float) maxAmount), options);
        poseStack.popPose();
    }
}
