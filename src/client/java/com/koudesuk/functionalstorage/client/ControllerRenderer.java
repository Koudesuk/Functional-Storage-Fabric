package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.config.FunctionalStorageConfig;
import com.koudesuk.functionalstorage.block.tile.StorageControllerTile;
import com.koudesuk.functionalstorage.item.LinkingToolItem;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

import java.util.List;
import java.util.OptionalDouble;

import static com.koudesuk.functionalstorage.item.LinkingToolItem.NBT_CONTROLLER;
import static com.koudesuk.functionalstorage.item.LinkingToolItem.NBT_FIRST;

/**
 * Renders the linking tool's visual feedback:
 * - Connected drawer outlines (white boxes)
 * - Controller linking range (green translucent box)
 * - Selection area for multiple mode
 */
public class ControllerRenderer<T extends StorageControllerTile> implements BlockEntityRenderer<T> {

    // Use existing Minecraft render types instead of creating custom ones
    // (protected access)
    public static final RenderType TYPE = RenderType.lines();

    private static void renderShape(PoseStack poseStack, VertexConsumer vertexConsumer, VoxelShape shape,
            double x, double y, double z, float red, float green, float blue, float alpha) {
        PoseStack.Pose pose = poseStack.last();
        shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            float dx = (float) (x2 - x1);
            float dy = (float) (y2 - y1);
            float dz = (float) (z2 - z1);
            float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= length;
            dy /= length;
            dz /= length;
            vertexConsumer.vertex(pose.pose(), (float) (x1 + x), (float) (y1 + y), (float) (z1 + z))
                    .color(red, green, blue, alpha).normal(pose.normal(), dx, dy, dz).endVertex();
            vertexConsumer.vertex(pose.pose(), (float) (x2 + x), (float) (y2 + y), (float) (z2 + z))
                    .color(red, green, blue, alpha).normal(pose.normal(), dx, dy, dz).endVertex();
        });
    }

    @Override
    public void render(T tile, float partialTicks, PoseStack matrixStack,
            MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (tile == null || tile.getLevel() == null)
            return;

        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.isEmpty())
            return;
        if (stack.getItem() instanceof LinkingToolItem) {
            CompoundTag controllerNBT = stack.getOrCreateTag().getCompound(NBT_CONTROLLER);
            BlockPos controller = new BlockPos(controllerNBT.getInt("X"), controllerNBT.getInt("Y"),
                    controllerNBT.getInt("Z"));
            if (!controller.equals(tile.getBlockPos()))
                return;

            // Render selection box for MULTIPLE mode
            if (stack.getOrCreateTag().contains(NBT_FIRST)) {
                CompoundTag firstpos = stack.getOrCreateTag().getCompound(NBT_FIRST);
                BlockPos firstPos = new BlockPos(firstpos.getInt("X"), firstpos.getInt("Y"), firstpos.getInt("Z"));

                // Simple ray trace - get block player is looking at
                HitResult result = Minecraft.getInstance().hitResult;
                if (result != null && result.getType() == HitResult.Type.BLOCK) {
                    BlockPos hit = ((BlockHitResult) result).getBlockPos();
                    AABB aabb = new AABB(Math.min(firstPos.getX(), hit.getX()),
                            Math.min(firstPos.getY(), hit.getY()), Math.min(firstPos.getZ(), hit.getZ()),
                            Math.max(firstPos.getX(), hit.getX()) + 1, Math.max(firstPos.getY(), hit.getY()) + 1,
                            Math.max(firstPos.getZ(), hit.getZ()) + 1);
                    VoxelShape shape = Shapes.create(aabb);
                    renderShape(matrixStack, bufferIn.getBuffer(TYPE), shape, -controller.getX(), -controller.getY(),
                            -controller.getZ(), 1f, 1f, 1f, 1f);
                    return;
                }
            }

            if (tile.getConnectedDrawers() == null)
                return;

            // Render all connected drawers as white outlines
            VoxelShape shape = tile.getConnectedDrawers().getCachedVoxelShape();
            if (shape == null || tile.getLevel().getGameTime() % 400 == 0) {
                tile.getConnectedDrawers().rebuildShapes();
                shape = tile.getConnectedDrawers().getCachedVoxelShape();
            }

            if (shape != null) {
                List<AABB> list = shape.toAabbs();
                int colorCount = Mth.ceil((double) list.size() / 3.0D);

                for (int j = 0; j < list.size(); ++j) {
                    AABB aabb = list.get(j);
                    renderShape(matrixStack, bufferIn.getBuffer(TYPE), Shapes.create(aabb.move(0.0D, 0.0D, 0.0D)),
                            -tile.getBlockPos().getX(), -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), 1f, 1f,
                            1f, 1.0F);
                }
            }

            // Render controller linking range as green translucent box
            var extraRange = tile.getStorageMultiplier();
            if (extraRange == 1) {
                extraRange = 0;
            }
            var area = new AABB(tile.getBlockPos())
                    .inflate(FunctionalStorageConfig.DRAWER_CONTROLLER_LINKING_RANGE + extraRange);
            renderShape(matrixStack, bufferIn.getBuffer(TYPE), Shapes.create(area), -tile.getBlockPos().getX(),
                    -tile.getBlockPos().getY(), -tile.getBlockPos().getZ(), 0.5f, 1, 0.5f, 1.0F);
            renderFaces(matrixStack, bufferIn, area, -tile.getBlockPos().getX(), -tile.getBlockPos().getY(),
                    -tile.getBlockPos().getZ(), 0.5f, 1, 0.5f, 0.25f);
        }
    }

    @Override
    public boolean shouldRender(T tile, Vec3 cameraPos) {
        return true;
    }

    @Override
    public boolean shouldRenderOffScreen(T tile) {
        return true;
    }

    private static final RenderType AREA_TYPE = FunctionalStorageRenderType.AREA_TYPE;

    private void renderFaces(PoseStack stack, MultiBufferSource renderTypeBuffer, AABB pos, double x, double y,
            double z, float red, float green, float blue, float alpha) {

        float x1 = (float) (pos.minX + x);
        float x2 = (float) (pos.maxX + x);
        float y1 = (float) (pos.minY + y);
        float y2 = (float) (pos.maxY + y);
        float z1 = (float) (pos.minZ + z);
        float z2 = (float) (pos.maxZ + z);

        Matrix4f matrix = stack.last().pose();
        VertexConsumer buffer = renderTypeBuffer.getBuffer(AREA_TYPE);

        // Front face (z1)
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).endVertex();

        // Back face (z2)
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).endVertex();

        // Bottom face (y1)
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).endVertex();

        // Top face (y2)
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).endVertex();

        // Left face (x1)
        buffer.vertex(matrix, x1, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y1, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x1, y2, z1).color(red, green, blue, alpha).endVertex();

        // Right face (x2)
        buffer.vertex(matrix, x2, y1, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z2).color(red, green, blue, alpha).endVertex();
        buffer.vertex(matrix, x2, y2, z1).color(red, green, blue, alpha).endVertex();
    }
}
