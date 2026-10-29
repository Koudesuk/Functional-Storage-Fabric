package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import static com.koudesuk.functionalstorage.registry.FSAttachments.CONTROLLER;
import static com.koudesuk.functionalstorage.registry.FSAttachments.FIRST_POSITION;

/**
 * Renders the linking tool's visual feedback while it is bound to this controller:
 * - Connected drawer outlines (white boxes)
 * - Controller linking range (green translucent box)
 * - Selection area for multiple mode
 */
public class ControllerRenderer<T extends StorageControllerTile>
        implements BlockEntityRenderer<T, ControllerRenderer.State> {

    public static final class State extends BlockEntityRenderState {
        private VoxelShape links = Shapes.empty();
        @Nullable
        private VoxelShape selection;
        @Nullable
        private AABB area;
        private BlockPos origin = BlockPos.ZERO;
        private boolean visible;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T tile, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.visible = false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || tile.getLevel() == null)
            return;

        ItemStack stack = minecraft.player.getMainHandItem();
        if (!(stack.getItem() instanceof LinkingToolItem))
            return;
        BlockPos controller = stack.get(CONTROLLER);
        if (controller == null || !controller.equals(tile.getBlockPos()))
            return;
        state.origin = controller;

        // Selection box for MULTIPLE mode, drawn on top of the linked drawers and the linking range
        // so the range stays visible while the second corner is being picked
        state.selection = null;
        BlockPos firstPos = stack.get(FIRST_POSITION);
        if (firstPos != null && minecraft.hitResult instanceof BlockHitResult blockHit
                && blockHit.getType() == HitResult.Type.BLOCK) {
            BlockPos hit = blockHit.getBlockPos();
            state.selection = Shapes.create(new AABB(Math.min(firstPos.getX(), hit.getX()),
                    Math.min(firstPos.getY(), hit.getY()), Math.min(firstPos.getZ(), hit.getZ()),
                    Math.max(firstPos.getX(), hit.getX()) + 1, Math.max(firstPos.getY(), hit.getY()) + 1,
                    Math.max(firstPos.getZ(), hit.getZ()) + 1));
        }

        if (tile.getConnectedDrawers() == null) {
            state.links = Shapes.empty();
            state.area = null;
            state.visible = state.selection != null;
            return;
        }

        // All connected drawers as white outlines
        VoxelShape links = tile.getConnectedDrawers().getCachedVoxelShape();
        if (links == null || tile.getLevel().getGameTime() % 400 == 0) {
            tile.getConnectedDrawers().rebuildShapes();
            links = tile.getConnectedDrawers().getCachedVoxelShape();
        }
        state.links = links == null ? Shapes.empty() : links;
        state.area = new AABB(controller).inflate(tile.getLinkingRange());
        state.visible = true;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.visible)
            return;
        double x = -state.origin.getX();
        double y = -state.origin.getY();
        double z = -state.origin.getZ();
        VoxelShape links = state.links;
        collector.submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(),
                (pose, consumer) -> renderShape(pose, consumer, links, x, y, z, 1, 1, 1, 1));
        VoxelShape selection = state.selection;
        if (selection != null) {
            collector.submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(),
                    (pose, consumer) -> renderShape(pose, consumer, selection, x, y, z, 1, 1, 1, 1));
        }
        AABB area = state.area;
        if (area != null) {
            collector.submitCustomGeometry(poseStack, RenderTypes.linesTranslucent(),
                    (pose, consumer) -> renderShape(pose, consumer, Shapes.create(area), x, y, z, 0.5F, 1, 0.5F, 1));
            collector.submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(),
                    (pose, consumer) -> renderFaces(pose, consumer, area, x, y, z, 0.5F, 1, 0.5F, 0.25F));
        }
    }

    private static void renderShape(PoseStack.Pose pose, VertexConsumer consumer, VoxelShape shape,
            double x, double y, double z, float red, float green, float blue, float alpha) {
        float lineWidth = Minecraft.getInstance().gameRenderer.getGameRenderState().windowRenderState.appropriateLineWidth;
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float nx = (float) (x2 - x1);
            float ny = (float) (y2 - y1);
            float nz = (float) (z2 - z1);
            float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
            nx /= length;
            ny /= length;
            nz /= length;
            consumer.addVertex(pose.pose(), (float) (x1 + x), (float) (y1 + y), (float) (z1 + z))
                    .setColor(red, green, blue, alpha)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);
            consumer.addVertex(pose.pose(), (float) (x2 + x), (float) (y2 + y), (float) (z2 + z))
                    .setColor(red, green, blue, alpha)
                    .setNormal(pose, nx, ny, nz)
                    .setLineWidth(lineWidth);
        });
    }

    /**
     * Every face is emitted with both windings, so the area stays visible from inside it too.
     */
    private static void renderFaces(PoseStack.Pose pose, VertexConsumer consumer, AABB box,
            double x, double y, double z, float red, float green, float blue, float alpha) {
        float x1 = (float) (box.minX + x), x2 = (float) (box.maxX + x);
        float y1 = (float) (box.minY + y), y2 = (float) (box.maxY + y);
        float z1 = (float) (box.minZ + z), z2 = (float) (box.maxZ + z);
        quad(pose, consumer, red, green, blue, alpha, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1);
        quad(pose, consumer, red, green, blue, alpha, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2);
        quad(pose, consumer, red, green, blue, alpha, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2);
        quad(pose, consumer, red, green, blue, alpha, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1);
        quad(pose, consumer, red, green, blue, alpha, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1);
        quad(pose, consumer, red, green, blue, alpha, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer consumer, float r, float g, float b, float a,
            float... xyz) {
        for (int i = 0; i < xyz.length; i += 3) {
            consumer.addVertex(pose.pose(), xyz[i], xyz[i + 1], xyz[i + 2]).setColor(r, g, b, a);
        }
        for (int i = xyz.length - 3; i >= 0; i -= 3) {
            consumer.addVertex(pose.pose(), xyz[i], xyz[i + 1], xyz[i + 2]).setColor(r, g, b, a);
        }
    }

    @Override
    public boolean shouldRender(T tile, Vec3 cameraPosition) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}
