package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.NumberUtils;
import com.koudesuk.functionalstorage.util.MathUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Renders drawer contents/upgrades and indicators on the block face.
 * Ported from the Forge renderer to Fabric.
 */
public class DrawerRenderer<T extends DrawerTile> implements BlockEntityRenderer<T> {

        @Override
        public void render(T tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn,
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
                                        new Vector3f(0, -90, 0),
                                        1));
                } else if (facing == Direction.SOUTH) {
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(0, 0, -1), new Vector3f(0, 180, 0),
                                                        1));
                } else if (facing == Direction.WEST) {
                        matrixStack.mulPoseMatrix(MathUtils.createTransformMatrix(new Vector3f(0, 0, 0),
                                        new Vector3f(0, 90, 0), 1));
                }
                matrixStack.translate(0, 0, -0.5 / 16D);
                combinedLightIn = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().relative(facing));
                renderUpgrades(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                if (tile.getDrawerType() == DrawerType.X_1)
                        render1Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                if (tile.getDrawerType() == DrawerType.X_2)
                        render2Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                if (tile.getDrawerType() == DrawerType.X_4)
                        render4Slot(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, tile);
                matrixStack.popPose();
        }

        public static void renderUpgrades(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, ControllableDrawerTile<?> tile) {
                float scale = 0.0625f;
                if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)) {
                        matrixStack.pushPose();
                        matrixStack.translate(0.031, 0.031f, 0.472 / 16D);
                        var upgrades = tile.getStorageUpgrades();
                        for (int i = 0; i < upgrades.getContainerSize(); i++) {
                                ItemStack stack = upgrades.getItem(i);
                                if (!stack.isEmpty()) {
                                        matrixStack.pushPose();
                                        matrixStack.scale(scale, scale, scale);
                                        Minecraft.getInstance().getItemRenderer().renderStatic(stack,
                                                        ItemDisplayContext.NONE, combinedLightIn,
                                                        combinedOverlayIn, matrixStack, bufferIn, tile.getLevel(), 0);
                                        matrixStack.popPose();
                                        matrixStack.translate(scale, 0, 0);
                                }
                        }
                        matrixStack.popPose();
                }
                if (tile.isVoid()) {
                        matrixStack.pushPose();
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(0.969f, 0.031f, 0.469f / 16.0f),
                                                        new Vector3f(0), scale));
                        Minecraft.getInstance().getItemRenderer().renderStatic(
                                        new ItemStack(FunctionalStorageItems.VOID_UPGRADE),
                                        ItemDisplayContext.NONE, combinedLightIn, combinedOverlayIn, matrixStack,
                                        bufferIn, tile.getLevel(), 0);
                        matrixStack.popPose();
                }
        }

        public static void renderIndicator(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, float progress, ControllableDrawerTile.DrawerOptions options) {
                var indicatorValue = options.getAdvancedValue(ConfigurationToolItem.ConfigurationAction.INDICATOR);
                if (indicatorValue != 0) {
                        TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                                        .apply(new ResourceLocation(
                                                        com.koudesuk.functionalstorage.FunctionalStorage.MOD_ID,
                                                        "block/indicator"));
                        VertexConsumer builder = bufferIn.getBuffer(RenderType.translucent());
                        Matrix4f posMat = matrixStack.last().pose();
                        float red = 1;
                        float green = 1;
                        float blue = 1;
                        float alpha = 1;
                        float x1 = -4 / 16F;
                        float x2 = x1 + 0.5f;
                        float y1 = -6.65F / 16F;
                        float y2 = y1 + 1.25f / 16F;
                        float z2 = 0;
                        double bx1 = 0;
                        double bx2 = 8;
                        double bz1 = 0;
                        double bz2 = 2;
                        float u1 = still.getU(bx1);
                        float u2 = still.getU(bx2);
                        float v1 = still.getV(bz1);
                        float v2 = still.getV(bz2);
                        if (indicatorValue != 3) { // hide background in mode 3
                                builder.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).uv(u2, v1)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                                builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                                builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                                builder.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).uv(u1, v1)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                        }

                        u2 = still.getU(bx2 * progress);
                        x2 = x1 + 0.5f * progress;
                        z2 = 0.0001f;
                        v1 = still.getV(8);
                        v2 = still.getV(10);
                        if (indicatorValue == 1 || progress >= 1) {
                                builder.vertex(posMat, x2, y1, z2).color(red, green, blue, alpha).uv(u2, v1)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                                builder.vertex(posMat, x2, y2, z2).color(red, green, blue, alpha).uv(u2, v2)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                                builder.vertex(posMat, x1, y2, z2).color(red, green, blue, alpha).uv(u1, v2)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                                builder.vertex(posMat, x1, y1, z2).color(red, green, blue, alpha).uv(u1, v1)
                                                .overlayCoords(combinedOverlayIn).uv2(combinedLightIn)
                                                .normal(0f, 0f, 1f).endVertex();
                        }
                }
        }

        private void render1Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, DrawerTile tile) {
                BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
                if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()) {
                        matrixStack.translate(0.5, 0.5, 0.0005f);
                        ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
                        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                                        inventoryHandler.getStoredStacks().get(0).getAmount(),
                                        inventoryHandler.getSlotLimit(0), 0.015f,
                                        tile.getDrawerOptions(), tile.getLevel());
                }
        }

        private void render2Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, DrawerTile tile) {
                BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
                if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()) {
                        matrixStack.pushPose();
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(0.5f, 0.27f, 0.0005f),
                                                        new Vector3f(0),
                                                        new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
                        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                                        inventoryHandler.getStoredStacks().get(0).getAmount(),
                                        inventoryHandler.getSlotLimit(0), 0.02f,
                                        tile.getDrawerOptions(), tile.getLevel());
                        matrixStack.popPose();
                }
                if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()) {
                        matrixStack.pushPose();
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(0.5f, 0.77f, 0.0005f),
                                                        new Vector3f(0),
                                                        new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
                        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                                        inventoryHandler.getStoredStacks().get(1).getAmount(),
                                        inventoryHandler.getSlotLimit(1), 0.02f,
                                        tile.getDrawerOptions(), tile.getLevel());
                        matrixStack.popPose();
                }
        }

        private void render4Slot(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, DrawerTile tile) {
                BigInventoryHandler inventoryHandler = (BigInventoryHandler) tile.getStorage();
                if (!inventoryHandler.getStoredStacks().get(0).getStack().isEmpty()) { // bottom right
                        matrixStack.pushPose();
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(.75f, .27f, .0005f),
                                                        new Vector3f(0),
                                                        new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack stack = inventoryHandler.getStoredStacks().get(0).getStack();
                        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                                        inventoryHandler.getStoredStacks().get(0).getAmount(),
                                        inventoryHandler.getSlotLimit(0), 0.02f,
                                        tile.getDrawerOptions(), tile.getLevel());
                        matrixStack.popPose();
                }
                if (!inventoryHandler.getStoredStacks().get(1).getStack().isEmpty()) { // bottom left
                        matrixStack.pushPose();
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(.25f, .27f, .0005f),
                                                        new Vector3f(0),
                                                        new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack stack = inventoryHandler.getStoredStacks().get(1).getStack();
                        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                                        inventoryHandler.getStoredStacks().get(1).getAmount(),
                                        inventoryHandler.getSlotLimit(1), 0.02f,
                                        tile.getDrawerOptions(), tile.getLevel());
                        matrixStack.popPose();
                }
                if (!inventoryHandler.getStoredStacks().get(2).getStack().isEmpty()) { // top right
                        matrixStack.pushPose();
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(.75f, .77f, .0005f),
                                                        new Vector3f(0),
                                                        new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack stack = inventoryHandler.getStoredStacks().get(2).getStack();
                        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                                        inventoryHandler.getStoredStacks().get(2).getAmount(),
                                        inventoryHandler.getSlotLimit(2), 0.02f,
                                        tile.getDrawerOptions(), tile.getLevel());
                        matrixStack.popPose();
                }
                if (!inventoryHandler.getStoredStacks().get(3).getStack().isEmpty()) { // top left
                        matrixStack.pushPose();
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(.25f, .77f, .0005f),
                                                        new Vector3f(0),
                                                        new Vector3f(.5f, .5f, 1.0f)));
                        ItemStack stack = inventoryHandler.getStoredStacks().get(3).getStack();
                        renderStack(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn, stack,
                                        inventoryHandler.getStoredStacks().get(3).getAmount(),
                                        inventoryHandler.getSlotLimit(3), 0.02f,
                                        tile.getDrawerOptions(), tile.getLevel());
                        matrixStack.popPose();
                }
        }

        public static void renderStack(PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn,
                        int combinedOverlayIn, ItemStack stack, int amount, int maxAmount, float scale,
                        ControllableDrawerTile.DrawerOptions options, Level level) {
                renderIndicator(matrixStack, bufferIn, combinedLightIn, combinedOverlayIn,
                                Math.min(1, amount / (float) maxAmount),
                                options);

                BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack,
                                Minecraft.getInstance().level, null,
                                0);
                if (model.isGui3d()) {
                        float thickness = (float) FunctionalStorageClientConfig.DRAWER_RENDER_THICKNESS;
                        // Avoid scaling normal matrix by using mulPoseMatrix instead of scale()
                        matrixStack.mulPoseMatrix(MathUtils.createTransformMatrix(new Vector3f(0), new Vector3f(0),
                                        new Vector3f(.75f, .75f, thickness)));
                } else {
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(0), new Vector3f(0), .4f));
                }

                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                // Always render the item on the drawer front
                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLightIn,
                                combinedOverlayIn, matrixStack, bufferIn, level, 0);

                matrixStack.mulPoseMatrix(MathUtils.createTransformMatrix(new Vector3f(0), new Vector3f(0, 180, 0), 1));
                if (!model.isGui3d()) {
                        matrixStack.mulPoseMatrix(MathUtils.createTransformMatrix(new Vector3f(0), new Vector3f(0),
                                        new Vector3f(0.5f / 0.4f, 0.5f / 0.4f, 1)));
                } else {
                        matrixStack.mulPoseMatrix(
                                        MathUtils.createTransformMatrix(new Vector3f(0), new Vector3f(0), .665f));
                }

                if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
                        renderText(matrixStack, bufferIn, combinedOverlayIn,
                                        Component.literal(ChatFormatting.WHITE + ""
                                                        + NumberUtils.getFormatedBigNumber(amount)),
                                        Direction.NORTH, scale);
                }
        }

        /** Thanks Mekanism */
        public static void renderText(PoseStack matrix, MultiBufferSource renderer, int overlayLight, Component text,
                        Direction side, float maxScale) {
                matrix.translate(0, -0.745, 0.01);

                float displayWidth = 1;
                float displayHeight = 1;

                Font font = Minecraft.getInstance().font;

                int requiredWidth = Math.max(font.width(text), 1);
                int requiredHeight = font.lineHeight + 2;
                float scaler = 0.4F;
                float scale = displayWidth / requiredWidth;
                scale = scale * scaler;
                if (maxScale > 0) {
                        scale = Math.min(scale, maxScale);
                }

                matrix.scale(scale, -scale, scale);
                int realHeight = (int) Math.floor(displayHeight / scale);
                int realWidth = (int) Math.floor(displayWidth / scale);
                int offsetX = (realWidth - requiredWidth) / 2;
                int offsetY = (realHeight - requiredHeight) / 2;
                font.drawInBatch(text, offsetX - realWidth / 2, 3 + offsetY - realHeight / 2, overlayLight, false,
                                matrix.last().pose(), renderer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
        }
}
