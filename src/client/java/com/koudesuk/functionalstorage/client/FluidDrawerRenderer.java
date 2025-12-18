package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.inventory.FluidInventoryHandler;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

/**
 * Renders fluid contents and indicators on fluid drawer block faces.
 * Ported from the Forge FluidDrawerRenderer to Fabric - matching coordinate
 * system exactly.
 */
public class FluidDrawerRenderer implements BlockEntityRenderer<FluidDrawerTile> {

        @Override
        public void render(FluidDrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn,
                        int combinedLightIn, int combinedOverlayIn) {
                if (Minecraft.getInstance().player != null
                                && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(),
                                                FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)) {
                        return;
                }
                matrixStack.pushPose();
                Direction facing = tile.getFacingDirection();

                // Match Forge's coordinate system exactly
                matrixStack.mulPose(Axis.YP.rotationDegrees(-180));

                if (facing == Direction.NORTH) {
                        matrixStack.translate(-1, 0, -1);
                } else if (facing == Direction.EAST) {
                        matrixStack.translate(0, 0, -1);
                        matrixStack.mulPose(Axis.YP.rotationDegrees(-90));
                } else if (facing == Direction.SOUTH) {
                        matrixStack.mulPose(Axis.YP.rotationDegrees(-180));
                } else if (facing == Direction.WEST) {
                        matrixStack.translate(-1, 0, 0);
                        matrixStack.mulPose(Axis.YP.rotationDegrees(90));
                }

                combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));

                // Render fluid contents based on drawer type
                if (tile.getDrawerType() == DrawerType.X_1)
                        render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                if (tile.getDrawerType() == DrawerType.X_2)
                        render2Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                if (tile.getDrawerType() == DrawerType.X_4)
                        render4Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);

                // Render upgrades after fluids, matching Forge position
                matrixStack.pushPose();
                matrixStack.translate(0, 0, 0.9688);
                DrawerRenderer.renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                matrixStack.popPose();

                matrixStack.popPose();
        }

        private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, FluidDrawerTile tile) {
                FluidInventoryHandler handler = tile.getHandler();
                FluidVariant resource = handler.getResource(0);
                long amount = handler.getAmount(0);
                long capacity = handler.getSlotLimit(0);

                // Use filterStack when locked and empty (matching Forge)
                if (!resource.isBlank() || (tile.isLocked() && !handler.getFilterStack(0).isBlank())) {
                        FluidVariant displayFluid = resource;
                        long displayAmount = amount;
                        if (resource.isBlank() && tile.isLocked() && !handler.getFilterStack(0).isBlank()) {
                                displayFluid = handler.getFilterStack(0);
                                displayAmount = 0;
                        }
                        float fillRatio = capacity > 0 ? (float) displayAmount / capacity : 0f;
                        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D,
                                        1.25 / 16D + fillRatio * (12.5 / 16D), 15 / 16D);
                        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, displayFluid,
                                        displayAmount,
                                        capacity,
                                        0.007f, tile.getDrawerOptions(), bounds, false, false);
                }
        }

        private void render2Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, FluidDrawerTile tile) {
                FluidInventoryHandler handler = tile.getHandler();

                // Slot 0 - bottom
                FluidVariant resource0 = handler.getResource(0);
                long amount0 = handler.getAmount(0);
                long capacity0 = handler.getSlotLimit(0);
                if (!resource0.isBlank() || (tile.isLocked() && !handler.getFilterStack(0).isBlank())) {
                        FluidVariant displayFluid = resource0;
                        long displayAmount = amount0;
                        if (resource0.isBlank() && tile.isLocked() && !handler.getFilterStack(0).isBlank()) {
                                displayFluid = handler.getFilterStack(0);
                                displayAmount = 0;
                        }
                        float fillRatio = capacity0 > 0 ? (float) displayAmount / capacity0 : 0f;
                        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D,
                                        1.25 / 16D + fillRatio * (5.5 / 16D), 15 / 16D);
                        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, displayFluid,
                                        displayAmount,
                                        capacity0,
                                        0.007f, tile.getDrawerOptions(), bounds, false, true);
                }

                // Slot 1 - top
                FluidVariant resource1 = handler.getResource(1);
                long amount1 = handler.getAmount(1);
                long capacity1 = handler.getSlotLimit(1);
                if (!resource1.isBlank() || (tile.isLocked() && !handler.getFilterStack(1).isBlank())) {
                        FluidVariant displayFluid = resource1;
                        long displayAmount = amount1;
                        if (resource1.isBlank() && tile.isLocked() && !handler.getFilterStack(1).isBlank()) {
                                displayFluid = handler.getFilterStack(1);
                                displayAmount = 0;
                        }
                        matrixStack.pushPose();
                        matrixStack.translate(0, 0.5, 0);
                        float fillRatio = capacity1 > 0 ? (float) displayAmount / capacity1 : 0f;
                        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 15 / 16D,
                                        1.25 / 16D + fillRatio * (5.5 / 16D), 15 / 16D);
                        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, displayFluid,
                                        displayAmount,
                                        capacity1,
                                        0.007f, tile.getDrawerOptions(), bounds, false, true);
                        matrixStack.popPose();
                }
        }

        private void render4Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, FluidDrawerTile tile) {
                FluidInventoryHandler handler = tile.getHandler();

                // Slot 0 - bottom right
                FluidVariant resource0 = handler.getResource(0);
                long amount0 = handler.getAmount(0);
                long capacity0 = handler.getSlotLimit(0);
                if (!resource0.isBlank() || (tile.isLocked() && !handler.getFilterStack(0).isBlank())) {
                        FluidVariant displayFluid = resource0;
                        long displayAmount = amount0;
                        if (resource0.isBlank() && tile.isLocked() && !handler.getFilterStack(0).isBlank()) {
                                displayFluid = handler.getFilterStack(0);
                                displayAmount = 0;
                        }
                        matrixStack.pushPose();
                        matrixStack.translate(0.5, 0, 0);
                        float fillRatio = capacity0 > 0 ? (float) displayAmount / capacity0 : 0f;
                        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D,
                                        1.25 / 16D + fillRatio * (5.5 / 16D), 15 / 16D);
                        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, displayFluid,
                                        displayAmount,
                                        capacity0,
                                        0.007f, tile.getDrawerOptions(), bounds, true, true);
                        matrixStack.popPose();
                }

                // Slot 1 - bottom left
                FluidVariant resource1 = handler.getResource(1);
                long amount1 = handler.getAmount(1);
                long capacity1 = handler.getSlotLimit(1);
                if (!resource1.isBlank() || (tile.isLocked() && !handler.getFilterStack(1).isBlank())) {
                        FluidVariant displayFluid = resource1;
                        long displayAmount = amount1;
                        if (resource1.isBlank() && tile.isLocked() && !handler.getFilterStack(1).isBlank()) {
                                displayFluid = handler.getFilterStack(1);
                                displayAmount = 0;
                        }
                        matrixStack.pushPose();
                        float fillRatio = capacity1 > 0 ? (float) displayAmount / capacity1 : 0f;
                        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D,
                                        1.25 / 16D + fillRatio * (5.5 / 16D), 15 / 16D);
                        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, displayFluid,
                                        displayAmount,
                                        capacity1,
                                        0.007f, tile.getDrawerOptions(), bounds, true, true);
                        matrixStack.popPose();
                }

                // Slot 2 - top right
                FluidVariant resource2 = handler.getResource(2);
                long amount2 = handler.getAmount(2);
                long capacity2 = handler.getSlotLimit(2);
                if (!resource2.isBlank() || (tile.isLocked() && !handler.getFilterStack(2).isBlank())) {
                        FluidVariant displayFluid = resource2;
                        long displayAmount = amount2;
                        if (resource2.isBlank() && tile.isLocked() && !handler.getFilterStack(2).isBlank()) {
                                displayFluid = handler.getFilterStack(2);
                                displayAmount = 0;
                        }
                        matrixStack.pushPose();
                        matrixStack.translate(0.5, 0.5, 0);
                        float fillRatio = capacity2 > 0 ? (float) displayAmount / capacity2 : 0f;
                        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D,
                                        1.25 / 16D + fillRatio * (5.5 / 16D), 15 / 16D);
                        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, displayFluid,
                                        displayAmount,
                                        capacity2,
                                        0.007f, tile.getDrawerOptions(), bounds, true, true);
                        matrixStack.popPose();
                }

                // Slot 3 - top left
                FluidVariant resource3 = handler.getResource(3);
                long amount3 = handler.getAmount(3);
                long capacity3 = handler.getSlotLimit(3);
                if (!resource3.isBlank() || (tile.isLocked() && !handler.getFilterStack(3).isBlank())) {
                        FluidVariant displayFluid = resource3;
                        long displayAmount = amount3;
                        if (resource3.isBlank() && tile.isLocked() && !handler.getFilterStack(3).isBlank()) {
                                displayFluid = handler.getFilterStack(3);
                                displayAmount = 0;
                        }
                        matrixStack.pushPose();
                        matrixStack.translate(0, 0.5, 0);
                        float fillRatio = capacity3 > 0 ? (float) displayAmount / capacity3 : 0f;
                        AABB bounds = new AABB(1 / 16D, 1.25 / 16D, 1 / 16D, 8 / 16D,
                                        1.25 / 16D + fillRatio * (5.5 / 16D), 15 / 16D);
                        renderFluidStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, displayFluid,
                                        displayAmount,
                                        capacity3,
                                        0.007f, tile.getDrawerOptions(), bounds, true, true);
                        matrixStack.popPose();
                }
        }

        public static void renderFluidStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLight,
                        int combinedOverlay, FluidVariant fluidVariant, long amount, long maxAmount, float scale,
                        ControllableDrawerTile.DrawerOptions options, AABB bounds, boolean halfText,
                        boolean isSmallBar) {

                matrixStack.pushPose();

                // Render fluid texture if enabled
                if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)
                                && !fluidVariant.isBlank()) {
                        TextureAtlasSprite sprite = FluidVariantRendering.getSprite(fluidVariant);
                        int color = FluidVariantRendering.getColor(fluidVariant);
                        boolean isMilkFallback = false;

                        // Fallback for fluids without textures (like Milk)
                        // Access raw water_still texture directly from block atlas
                        // to bypass Fabric's biome-dependent coloring
                        if (sprite == null) {
                                sprite = Minecraft.getInstance()
                                                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                                                .apply(new ResourceLocation("minecraft", "block/water_still"));
                                isMilkFallback = true;
                        }

                        if (sprite != null) {
                                float red, green, blue, alpha;
                                if (isMilkFallback) {
                                        // Water texture is GRAYSCALE, blue color comes from vertex tinting
                                        // Setting vertex to pure white = grayscale texture * white = white result
                                        // (Per hint: 把 Vertex Color 設回 0xFFFFFFFF 純白 即可)
                                        red = 1.0f;
                                        green = 1.0f;
                                        blue = 1.0f;
                                        alpha = amount == 0 ? 0.3f : 1.0f;
                                } else {
                                        red = ((color >> 16) & 0xFF) / 255f;
                                        green = ((color >> 8) & 0xFF) / 255f;
                                        blue = (color & 0xFF) / 255f;
                                        alpha = amount == 0 ? 0.3f : ((color >> 24) & 0xFF) / 255f;
                                        if (alpha == 0)
                                                alpha = 1.0f;
                                }

                                VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());

                                float x1 = (float) bounds.minX;
                                float x2 = (float) bounds.maxX;
                                float y1 = (float) bounds.minY;
                                float y2 = (float) bounds.maxY;
                                float z1 = (float) bounds.minZ;
                                float z2 = (float) bounds.maxZ;
                                double bx1 = bounds.minX * 16;
                                double bx2 = bounds.maxX * 16;
                                double by1 = bounds.minY * 16;
                                double by2 = bounds.maxY * 16;
                                double bz1 = bounds.minZ * 16;
                                double bz2 = bounds.maxZ * 16;

                                Matrix4f posMat = matrixStack.last().pose();

                                // TOP face
                                float u1 = sprite.getU(bx1);
                                float u2 = sprite.getU(bx2);
                                float v1 = sprite.getV(bz1);
                                float v2 = sprite.getV(bz2);
                                builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f)
                                                .endVertex();
                                builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f)
                                                .endVertex();
                                builder.vertex(posMat, x2, y2, z1).color(red, green, blue, alpha).uv(u2, v1)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f)
                                                .endVertex();
                                builder.vertex(posMat, x1, y2, z1).color(red, green, blue, alpha).uv(u1, v1)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 1f, 0f)
                                                .endVertex();

                                // FRONT face
                                u1 = sprite.getU(bx1);
                                u2 = sprite.getU(bx2);
                                v1 = sprite.getV(by1);
                                v2 = sprite.getV(by2);
                                builder.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).uv(u2, v1)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f)
                                                .endVertex();
                                builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f)
                                                .endVertex();
                                builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f)
                                                .endVertex();
                                builder.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).uv(u1, v1)
                                                .overlayCoords(combinedOverlay).uv2(combinedLight).normal(0f, 0f, 1f)
                                                .endVertex();
                        }
                }

                matrixStack.popPose();

                // Render amount text
                if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
                        matrixStack.pushPose();
                        matrixStack.translate(0.5, 0.84, 0.97);
                        if (halfText)
                                matrixStack.translate(-0.25, 0, 0);
                        DrawerRenderer.renderText(matrixStack, bufferIn, combinedOverlay,
                                        Component.literal(ChatFormatting.WHITE + ""
                                                        + NumberUtils.getFormatedFluidBigNumber(amount)),
                                        Direction.NORTH, scale);
                        matrixStack.popPose();
                }

                // Render fill indicator bar
                matrixStack.pushPose();
                matrixStack.translate(0.5, 0.453, 0.97);
                if (halfText) {
                        matrixStack.scale(0.5f, 0.65f, 0.5f);
                        matrixStack.translate(-0.5, -0.18, 0);
                }
                DrawerRenderer.renderIndicator(matrixStack, bufferIn, combinedLight, combinedOverlay,
                                Math.min(1, (float) amount / maxAmount), options);
                matrixStack.popPose();
        }
}
