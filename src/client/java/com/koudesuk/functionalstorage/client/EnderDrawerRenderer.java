package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.EnderDrawerTile;
import com.koudesuk.functionalstorage.inventory.EnderInventoryHandler;
import com.koudesuk.functionalstorage.util.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

/**
 * Renders Ender Drawer contents/upgrades on the block face.
 * Based on Forge EnderDrawerRenderer, adapted for Fabric.
 */
public class EnderDrawerRenderer implements BlockEntityRenderer<EnderDrawerTile> {

    @Override
    public void render(EnderDrawerTile tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn,
            int combinedLightIn, int combinedOverlayIn) {
        if (Minecraft.getInstance().player != null
                && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(),
                        FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)) {
            return;
        }
        matrixStack.pushPose();

        Direction facing = tile.getFacingDirection();
        matrixStack.mulPoseMatrix(MathUtils.createTransformMatrix(new Vector3f(0), new Vector3f(0, 180, 0), 1));

        if (facing == Direction.NORTH) {
            matrixStack.mulPoseMatrix(
                    MathUtils.createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0), 1));
        } else if (facing == Direction.EAST) {
            matrixStack.mulPoseMatrix(MathUtils.createTransformMatrix(new Vector3f(-1, 0, -1),
                    new Vector3f(0, -90, 0), 1));
        } else if (facing == Direction.SOUTH) {
            matrixStack.mulPoseMatrix(
                    MathUtils.createTransformMatrix(new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
        } else if (facing == Direction.WEST) {
            matrixStack.mulPoseMatrix(MathUtils.createTransformMatrix(new Vector3f(0, 0, 0),
                    new Vector3f(0, 90, 0), 1));
        }

        matrixStack.translate(0, 0, -0.5 / 16D);
        combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
        DrawerRenderer.renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
        matrixStack.popPose();
    }

    private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
            int combinedOverlayIn, EnderDrawerTile tile) {
        EnderInventoryHandler inventoryHandler = tile.getHandler();
        if (inventoryHandler == null) {
            return;
        }

        if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()) {
            matrixStack.translate(0.5, 0.5, 0.0005f);
            ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
            DrawerRenderer.renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                    inventoryHandler.getStoredStacks().get(0).getAmount(),
                    inventoryHandler.getSlotLimit(0), 0.015f,
                    tile.getDrawerOptions(), tile.getLevel());
        }
    }
}
