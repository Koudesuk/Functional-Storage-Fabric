package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;

/**
 * Renders the two outputs of the simple compacting drawer.
 * Generic type allows rendering both regular and framed simple compacting
 * drawers.
 */
public class SimpleCompactingDrawerRenderer<T extends SimpleCompactingDrawerTile> implements BlockEntityRenderer<T> {

        @Override
        public void render(T tile, float partialTicks, PoseStack matrixStack,
                        MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
                if (Minecraft.getInstance().player != null
                                && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(),
                                                FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)) {
                        return;
                }
                matrixStack.pushPose();

                Direction facing = tile.getBlockState()
                                .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
                matrixStack.mulPoseMatrix(com.koudesuk.functionalstorage.util.MathUtils.createTransformMatrix(
                                new org.joml.Vector3f(0),
                                new org.joml.Vector3f(0, 180, 0), 1));

                if (facing == Direction.NORTH) {
                        matrixStack.mulPoseMatrix(
                                        com.koudesuk.functionalstorage.util.MathUtils.createTransformMatrix(
                                                        new org.joml.Vector3f(-1, 0, 0),
                                                        new org.joml.Vector3f(0), 1));
                } else if (facing == Direction.EAST) {
                        matrixStack.mulPoseMatrix(
                                        com.koudesuk.functionalstorage.util.MathUtils.createTransformMatrix(
                                                        new org.joml.Vector3f(-1, 0, -1),
                                                        new org.joml.Vector3f(0, -90, 0), 1));
                } else if (facing == Direction.SOUTH) {
                        matrixStack.mulPoseMatrix(
                                        com.koudesuk.functionalstorage.util.MathUtils.createTransformMatrix(
                                                        new org.joml.Vector3f(0, 0, -1),
                                                        new org.joml.Vector3f(0, 180, 0), 1));
                } else if (facing == Direction.WEST) {
                        matrixStack.mulPoseMatrix(
                                        com.koudesuk.functionalstorage.util.MathUtils.createTransformMatrix(
                                                        new org.joml.Vector3f(0, 0, 0),
                                                        new org.joml.Vector3f(0, 90, 0), 1));
                }
                matrixStack.translate(0, 0, -0.5 / 16D);
                combinedLightIn = net.minecraft.client.renderer.LevelRenderer.getLightColor(tile.getLevel(),
                                tile.getBlockPos().relative(facing));
                DrawerRenderer.renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                renderSlots(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                matrixStack.popPose();
        }

        private void renderSlots(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, T tile) {
                var inventoryHandler = tile.handler;
                // Use resultList.get(i).getResult() to check item type existence,
                // so both slots render even when count is 0
                // Slot 0: Y=0.27 (bottom) - lower tier (e.g., nuggets or ingots)
                // Slot 1: Y=0.77 (top) - higher tier (e.g., ingots or blocks)
                var resultList = inventoryHandler.getResultList();

                // Slot 0 (bottom, lower tier)
                if (resultList.size() > 0) {
                        net.minecraft.world.item.ItemStack itemType0 = resultList.get(0).getResult();
                        if (!itemType0.isEmpty()) {
                                int count0 = inventoryHandler.getStackInSlot(0).getCount();
                                matrixStack.pushPose();
                                matrixStack.mulPoseMatrix(com.koudesuk.functionalstorage.util.MathUtils
                                                .createTransformMatrix(new org.joml.Vector3f(0.5f, 0.27f, 0.0005f),
                                                                new org.joml.Vector3f(0),
                                                                new org.joml.Vector3f(.5f, .5f, 1.0f)));
                                DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn,
                                                itemType0,
                                                count0, inventoryHandler.getSlotLimit(0), 0.02f,
                                                tile.getDrawerOptions(), tile.getLevel());
                                matrixStack.popPose();
                        }
                }

                // Slot 1 (top, higher tier)
                if (resultList.size() > 1) {
                        net.minecraft.world.item.ItemStack itemType1 = resultList.get(1).getResult();
                        if (!itemType1.isEmpty()) {
                                int count1 = inventoryHandler.getStackInSlot(1).getCount();
                                matrixStack.pushPose();
                                matrixStack.mulPoseMatrix(com.koudesuk.functionalstorage.util.MathUtils
                                                .createTransformMatrix(new org.joml.Vector3f(0.5f, 0.77f, 0.0005f),
                                                                new org.joml.Vector3f(0),
                                                                new org.joml.Vector3f(.5f, .5f, 1.0f)));
                                DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn,
                                                itemType1,
                                                count1, inventoryHandler.getSlotLimit(1), 0.02f,
                                                tile.getDrawerOptions(), tile.getLevel());
                                matrixStack.popPose();
                        }
                }
        }
}
