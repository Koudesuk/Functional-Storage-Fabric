package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.inventory.CompactingInventoryHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;

/**
 * Renders the three compacted outputs on compacting drawers.
 * Generic type allows rendering both regular and framed compacting drawers.
 */
public class CompactingDrawerRenderer<T extends CompactingDrawerTile> implements BlockEntityRenderer<T> {

        @Override
        public void render(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn,
                        int combinedLightIn, int combinedOverlayIn) {
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
                render3Slots(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                matrixStack.popPose();
        }

        private void render3Slots(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, T tile) {
                CompactingInventoryHandler inventoryHandler = tile.handler;
                // Match Forge CompactingDrawerRenderer positions:
                // Slot 0: X=0.75 (right), Y=0.27 (bottom) - lowest tier (e.g., nuggets)
                // Slot 1: X=0.25 (left), Y=0.27 (bottom) - middle tier (e.g., ingots)
                // Slot 2: X=0.5 (center), Y=0.77 (top) - highest tier (e.g., blocks)
                // Use resultList.get(i).getResult() to check item type existence,
                // so all three slots render even when count is 0
                var resultList = inventoryHandler.getResultList();

                // Slot 0 (bottom-right, lowest tier)
                if (resultList.size() > 0) {
                        net.minecraft.world.item.ItemStack itemType0 = resultList.get(0).getResult();
                        if (!itemType0.isEmpty()) {
                                int count0 = inventoryHandler.getStackInSlot(0).getCount();
                                matrixStack.pushPose();
                                matrixStack.mulPoseMatrix(com.koudesuk.functionalstorage.util.MathUtils
                                                .createTransformMatrix(new org.joml.Vector3f(0.75f, 0.27f, 0.0005f),
                                                                new org.joml.Vector3f(0),
                                                                new org.joml.Vector3f(.5f, .5f, 1.0f)));
                                DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn,
                                                itemType0,
                                                count0, inventoryHandler.getSlotLimit(0), 0.02f,
                                                tile.getDrawerOptions(), tile.getLevel());
                                matrixStack.popPose();
                        }
                }

                // Slot 1 (bottom-left, middle tier)
                if (resultList.size() > 1) {
                        net.minecraft.world.item.ItemStack itemType1 = resultList.get(1).getResult();
                        if (!itemType1.isEmpty()) {
                                int count1 = inventoryHandler.getStackInSlot(1).getCount();
                                matrixStack.pushPose();
                                matrixStack.mulPoseMatrix(com.koudesuk.functionalstorage.util.MathUtils
                                                .createTransformMatrix(new org.joml.Vector3f(0.25f, 0.27f, 0.0005f),
                                                                new org.joml.Vector3f(0),
                                                                new org.joml.Vector3f(.5f, .5f, 1.0f)));
                                DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn,
                                                itemType1,
                                                count1, inventoryHandler.getSlotLimit(1), 0.02f,
                                                tile.getDrawerOptions(), tile.getLevel());
                                matrixStack.popPose();
                        }
                }

                // Slot 2 (top-center, highest tier)
                if (resultList.size() > 2) {
                        net.minecraft.world.item.ItemStack itemType2 = resultList.get(2).getResult();
                        if (!itemType2.isEmpty()) {
                                int count2 = inventoryHandler.getStackInSlot(2).getCount();
                                matrixStack.pushPose();
                                matrixStack.mulPoseMatrix(com.koudesuk.functionalstorage.util.MathUtils
                                                .createTransformMatrix(new org.joml.Vector3f(0.5f, 0.77f, 0.0005f),
                                                                new org.joml.Vector3f(0),
                                                                new org.joml.Vector3f(.5f, .5f, 1.0f)));
                                DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn,
                                                itemType2,
                                                count2, inventoryHandler.getSlotLimit(2), 0.02f,
                                                tile.getDrawerOptions(), tile.getLevel());
                                matrixStack.popPose();
                        }
                }
        }
}
