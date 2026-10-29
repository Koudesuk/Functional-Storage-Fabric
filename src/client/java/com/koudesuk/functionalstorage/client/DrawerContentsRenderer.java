package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.CompactingDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.block.tile.FluidDrawerTile;
import com.koudesuk.functionalstorage.block.tile.SimpleCompactingDrawerTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.Direction;

/**
 * The contents of a north facing drawer, as its block entity renderer draws them, for the drawer items
 * (client.item.DrawerContentsItemRenderer), whose model faces north. Upgrades are left out, like the original.
 */
public final class DrawerContentsRenderer {

    private DrawerContentsRenderer() {
    }

    public static void submitItems(PoseStack poseStack, SubmitNodeCollector collector, int light, DrawerTile tile) {
        DrawerRenderer.transformToDrawerFace(poseStack, Direction.NORTH);
        DrawerRenderer.submitContents(poseStack, collector, light, tile);
    }

    public static void submitCompacting(PoseStack poseStack, SubmitNodeCollector collector, int light,
            CompactingDrawerTile tile) {
        DrawerRenderer.transformToDrawerFace(poseStack, Direction.NORTH);
        CompactingDrawerRenderer.submitContents(poseStack, collector, light, tile);
    }

    public static void submitSimpleCompacting(PoseStack poseStack, SubmitNodeCollector collector, int light,
            SimpleCompactingDrawerTile tile) {
        DrawerRenderer.transformToDrawerFace(poseStack, Direction.NORTH);
        SimpleCompactingDrawerRenderer.submitContents(poseStack, collector, light, tile);
    }

    public static void submitFluids(PoseStack poseStack, SubmitNodeCollector collector, int light,
            FluidDrawerTile tile) {
        // FluidDrawerRenderer's transform for a north facing drawer
        poseStack.mulPose(Axis.YP.rotationDegrees(-180));
        poseStack.translate(-1, 0, -1);
        FluidDrawerRenderer.submitContents(poseStack, collector, light, tile);
    }
}
