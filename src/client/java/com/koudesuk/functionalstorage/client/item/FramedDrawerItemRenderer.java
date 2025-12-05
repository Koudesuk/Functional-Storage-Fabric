package com.koudesuk.functionalstorage.client.item;

import com.koudesuk.functionalstorage.client.model.FramedDrawerModelData;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import java.util.List;

/**
 * Fabric client-side item renderer for framed drawers.
 * Equivalent to Forge's DrawerISTER (BlockEntityWithoutLevelRenderer).
 * Reads NBT from ItemStack and renders with custom textures.
 * 
 * This renderer is registered via BuiltinItemRendererRegistry and handles
 * the dynamic texture rendering for framed drawer items in
 * inventory/hand/ground.
 */
public class FramedDrawerItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {

    private final Block block;

    public FramedDrawerItemRenderer(Block block) {
        this.block = block;
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices,
            MultiBufferSource vertexConsumers, int light, int overlay) {

        Minecraft minecraft = Minecraft.getInstance();
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        BlockState blockState = block.defaultBlockState();

        // Get the block model
        BakedModel model = blockRenderer.getBlockModel(blockState);

        // Read NBT data if present
        FramedDrawerModelData modelData = null;
        if (stack.hasTag() && stack.getTag().contains("Style")) {
            modelData = FramedDrawerModelData.fromNBT(stack.getTag().getCompound("Style"));
        }

        matrices.pushPose();
        // Apply standard item transformations - center the model
        matrices.translate(0.5, 0.5, 0.5);

        // Get the vertex consumer for solid rendering
        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderType.solid());
        RandomSource random = RandomSource.create();

        // If no custom textures, render normally
        if (modelData == null || modelData.getDesign().isEmpty()) {
            blockRenderer.getModelRenderer().renderModel(
                    matrices.last(),
                    vertexConsumer,
                    blockState,
                    model,
                    1.0f, 1.0f, 1.0f,
                    light,
                    overlay);
        } else {
            // Render with custom textures by retexturing quads
            renderWithCustomTextures(matrices, vertexConsumer, blockState, model, modelData, light, overlay, random);
        }

        matrices.popPose();
    }

    /**
     * Renders the model with custom textures based on the FramedDrawerModelData.
     * This method iterates through all quads and replaces textures based on the
     * design.
     */
    private void renderWithCustomTextures(PoseStack matrices, VertexConsumer vertexConsumer,
            BlockState blockState, BakedModel model, FramedDrawerModelData modelData,
            int light, int overlay, RandomSource random) {

        PoseStack.Pose pose = matrices.last();

        // Render quads for all directions (null = general, plus all 6 directions)
        for (Direction direction : new Direction[] { null, Direction.DOWN, Direction.UP,
                Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST }) {

            List<BakedQuad> quads = model.getQuads(blockState, direction, random);

            for (BakedQuad quad : quads) {
                TextureAtlasSprite originalSprite = quad.getSprite();
                TextureAtlasSprite newSprite = getReplacementSprite(originalSprite, modelData, random);

                if (newSprite != null && newSprite != originalSprite) {
                    // Render with retextured quad
                    renderRetexturedQuad(pose, vertexConsumer, quad, newSprite, light, overlay);
                } else {
                    // Render original quad
                    vertexConsumer.putBulkData(pose, quad, 1.0f, 1.0f, 1.0f, light, overlay);
                }
            }
        }
    }

    /**
     * Gets the replacement sprite for a given original sprite based on model data.
     */
    private TextureAtlasSprite getReplacementSprite(TextureAtlasSprite originalSprite,
            FramedDrawerModelData modelData, RandomSource random) {

        if (modelData == null || modelData.getDesign().isEmpty()) {
            return null;
        }

        String spriteName = originalSprite.contents().name().toString();
        String part = determineModelPart(spriteName);

        if (part != null && modelData.getDesign().containsKey(part)) {
            Item item = modelData.getDesign().get(part);
            if (item instanceof BlockItem blockItem) {
                BlockState frameState = blockItem.getBlock().defaultBlockState();
                BakedModel frameModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(frameState);

                // Try to get a quad from the frame model to extract its sprite
                List<BakedQuad> frameQuads = frameModel.getQuads(frameState, null, random);
                if (!frameQuads.isEmpty()) {
                    return frameQuads.get(0).getSprite();
                }

                // Fallback to particle icon
                return frameModel.getParticleIcon();
            }
        }

        return null;
    }

    /**
     * Determines which model part a sprite belongs to based on its name.
     */
    private String determineModelPart(String spriteName) {
        String lowerName = spriteName.toLowerCase();

        if (lowerName.contains("framed_front") ||
                lowerName.contains("framed_controller_front") ||
                (lowerName.contains("_front") && lowerName.contains("framed"))) {
            return "front";
        }

        if (lowerName.contains("framed_side") ||
                lowerName.contains("framed_controller_side") ||
                lowerName.contains("framed_top") ||
                (lowerName.contains("_side") && lowerName.contains("framed")) ||
                (lowerName.contains("_top") && lowerName.contains("framed"))) {
            return "side";
        }

        if (lowerName.contains("framed_trim") ||
                lowerName.contains("framed_divider") ||
                (lowerName.contains("_trim") && lowerName.contains("framed")) ||
                (lowerName.contains("_divider") && lowerName.contains("framed"))) {
            return "front_divider";
        }

        if (lowerName.contains("framed")) {
            return "particle";
        }

        return null;
    }

    /**
     * Renders a quad with a replacement texture.
     * This copies the quad geometry but uses a different sprite.
     */
    private void renderRetexturedQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer,
            BakedQuad originalQuad, TextureAtlasSprite newSprite, int light, int overlay) {

        // Get the original quad's vertices
        int[] vertices = originalQuad.getVertices();
        TextureAtlasSprite oldSprite = originalQuad.getSprite();

        // Calculate UV transformation factors
        float oldU0 = oldSprite.getU0();
        float oldV0 = oldSprite.getV0();
        float oldUScale = oldSprite.getU1() - oldSprite.getU0();
        float oldVScale = oldSprite.getV1() - oldSprite.getV0();

        float newU0 = newSprite.getU0();
        float newV0 = newSprite.getV0();
        float newUScale = newSprite.getU1() - newSprite.getU0();
        float newVScale = newSprite.getV1() - newSprite.getV0();

        // Process each of the 4 vertices
        for (int i = 0; i < 4; i++) {
            int offset = i * 8; // 8 ints per vertex (x, y, z, color, u, v, light, normal)

            // Extract position
            float x = Float.intBitsToFloat(vertices[offset]);
            float y = Float.intBitsToFloat(vertices[offset + 1]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);

            // Extract and transform UV coordinates
            float oldU = Float.intBitsToFloat(vertices[offset + 4]);
            float oldV = Float.intBitsToFloat(vertices[offset + 5]);

            // Normalize old UV to [0,1] range relative to old sprite
            float normalizedU = (oldU - oldU0) / oldUScale;
            float normalizedV = (oldV - oldV0) / oldVScale;

            // Transform to new sprite UV
            float newU = newU0 + normalizedU * newUScale;
            float newV = newV0 + normalizedV * newVScale;

            // Add vertex with new UV coordinates
            vertexConsumer
                    .vertex(pose.pose(), x, y, z)
                    .color(255, 255, 255, 255)
                    .uv(newU, newV)
                    .overlayCoords(overlay)
                    .uv2(light)
                    .normal(pose.normal(),
                            originalQuad.getDirection().getStepX(),
                            originalQuad.getDirection().getStepY(),
                            originalQuad.getDirection().getStepZ())
                    .endVertex();
        }
    }
}
