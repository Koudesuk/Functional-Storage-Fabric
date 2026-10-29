package com.koudesuk.functionalstorage.client;

import com.koudesuk.functionalstorage.FunctionalStorage;
import com.koudesuk.functionalstorage.block.tile.ControllableDrawerTile;
import com.koudesuk.functionalstorage.block.tile.DrawerTile;
import com.koudesuk.functionalstorage.inventory.BigInventoryHandler;
import com.koudesuk.functionalstorage.item.ConfigurationToolItem;
import com.koudesuk.functionalstorage.registry.FunctionalStorageItems;
import com.koudesuk.functionalstorage.util.DrawerType;
import com.koudesuk.functionalstorage.util.MathUtils;
import com.koudesuk.functionalstorage.util.NumberUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.data.AtlasIds;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class DrawerRenderer<T extends DrawerTile> implements BlockEntityRenderer<T, FunctionalStorageBlockEntityRenderState<T>> {
    @Override
    public FunctionalStorageBlockEntityRenderState<T> createRenderState() {
        return new FunctionalStorageBlockEntityRenderState<>();
    }

    @Override
    public void extractRenderState(T blockEntity, FunctionalStorageBlockEntityRenderState<T> state,
            float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(FunctionalStorageBlockEntityRenderState<T> state, PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        T tile = state.blockEntity;
        if (tile == null || tile.getLevel() == null) {
            return;
        }
        if (Minecraft.getInstance().player != null
                && !tile.getBlockPos().closerThan(Minecraft.getInstance().player.getOnPos(),
                        FunctionalStorageClientConfig.DRAWER_RENDER_RANGE.get())) {
            return;
        }

        poseStack.pushPose();
        transformToDrawerFace(poseStack, tile.getFacingDirection());
        int light = getFrontLight(tile, state.lightCoords);
        renderUpgrades(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, tile);
        submitContents(poseStack, submitNodeCollector, light, tile);
        poseStack.popPose();
    }

    /**
     * The stored items on the drawer face, shared with the drawer items (DrawerContentsItemRenderer).
     */
    static void submitContents(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light,
            DrawerTile tile) {
        if (tile.getDrawerType() == DrawerType.X_1) {
            renderSlot(poseStack, submitNodeCollector, light, tile, 0, 0.5f, 0.5f, 1.0f, 0.015f);
        } else if (tile.getDrawerType() == DrawerType.X_2) {
            renderSlot(poseStack, submitNodeCollector, light, tile, 0, 0.5f, 0.27f, 0.5f, 0.02f);
            renderSlot(poseStack, submitNodeCollector, light, tile, 1, 0.5f, 0.77f, 0.5f, 0.02f);
        } else if (tile.getDrawerType() == DrawerType.X_4) {
            renderSlot(poseStack, submitNodeCollector, light, tile, 0, 0.75f, 0.27f, 0.5f, 0.02f);
            renderSlot(poseStack, submitNodeCollector, light, tile, 1, 0.25f, 0.27f, 0.5f, 0.02f);
            renderSlot(poseStack, submitNodeCollector, light, tile, 2, 0.75f, 0.77f, 0.5f, 0.02f);
            renderSlot(poseStack, submitNodeCollector, light, tile, 3, 0.25f, 0.77f, 0.5f, 0.02f);
        }
    }

    /**
     * The drawer block itself is solid, so its own light is always dark. Like the original, the
     * contents are lit by the block in front of the drawer face.
     */
    static int getFrontLight(ControllableDrawerTile<?> tile, int fallback) {
        return tile.getLevel() != null
                ? LevelRenderer.getLightCoords(tile.getLevel(), tile.getBlockPos().relative(tile.getFacingDirection()))
                : fallback;
    }

    static void transformToDrawerFace(PoseStack poseStack, Direction facing) {
        poseStack.last().pose().mul(MathUtils.createTransformMatrix(new Vector3f(0), new Vector3f(0, 180, 0), 1));
        if (facing == Direction.NORTH) {
            poseStack.last().pose().mul(MathUtils.createTransformMatrix(new Vector3f(-1, 0, 0), new Vector3f(0), 1));
        } else if (facing == Direction.EAST) {
            poseStack.last().pose()
                    .mul(MathUtils.createTransformMatrix(new Vector3f(-1, 0, -1), new Vector3f(0, -90, 0), 1));
        } else if (facing == Direction.SOUTH) {
            poseStack.last().pose()
                    .mul(MathUtils.createTransformMatrix(new Vector3f(0, 0, -1), new Vector3f(0, 180, 0), 1));
        } else if (facing == Direction.WEST) {
            poseStack.last().pose().mul(MathUtils.createTransformMatrix(new Vector3f(0, 0, 0), new Vector3f(0, 90, 0), 1));
        }
        poseStack.translate(0, 0, -0.5 / 16D);
    }

    private static void renderSlot(PoseStack poseStack, SubmitNodeCollector collector, int light, DrawerTile tile,
            int slot, float x, float y, float scale, float textScale) {
        BigInventoryHandler handler = (BigInventoryHandler) tile.getStorage();
        if (slot >= handler.getStoredStacks().size()) {
            return;
        }
        BigInventoryHandler.BigStack stored = handler.getStoredStacks().get(slot);
        if (stored.getStack().isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.last().pose().mul(MathUtils.createTransformMatrix(new Vector3f(x, y, 0.0005f), new Vector3f(0),
                new Vector3f(scale, scale, 1.0f)));
        renderStack(poseStack, collector, light, stored.getStack(), stored.getAmount(), handler.getSlotLimit(slot),
                textScale, tile.getDrawerOptions(), tile.getLevel());
        poseStack.popPose();
    }

    public static void renderUpgrades(PoseStack poseStack, SubmitNodeCollector collector, int light,
            int overlay, ControllableDrawerTile<?> tile) {
        float scale = 0.0625f;
        if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)) {
            poseStack.pushPose();
            poseStack.translate(0.031, 0.031f, 0.472 / 16D);
            for (int i = 0; i < tile.getStorageUpgrades().getContainerSize(); i++) {
                ItemStack stack = tile.getStorageUpgrades().getItem(i);
                if (!stack.isEmpty()) {
                    poseStack.pushPose();
                    poseStack.scale(scale, scale, scale);
                    ItemStackRenderState itemState = new ItemStackRenderState();
                    Minecraft.getInstance().getItemModelResolver()
                            .updateForTopItem(itemState, stack, ItemDisplayContext.NONE, tile.getLevel(), null, 0);
                    itemState.submit(poseStack, collector, light, overlay, 0);
                    poseStack.popPose();
                    poseStack.translate(scale, 0, 0);
                }
            }
            poseStack.popPose();
        }
        if (tile.isVoid()) {
            poseStack.pushPose();
            poseStack.mulPose(MathUtils.createTransformMatrix(new Vector3f(0.969f, 0.031f, 0.469f / 16.0f),
                    new Vector3f(0), scale));
            ItemStackRenderState itemState = new ItemStackRenderState();
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemState,
                    new ItemStack(FunctionalStorageItems.VOID_UPGRADE), ItemDisplayContext.NONE, tile.getLevel(), null, 0);
            itemState.submit(poseStack, collector, light, overlay, 0);
            poseStack.popPose();
        }
    }

    public static void renderIndicator(PoseStack poseStack, SubmitNodeCollector collector, int light, float progress,
            ControllableDrawerTile.DrawerOptions options) {
        int indicatorValue = options.getAdvancedValue(ConfigurationToolItem.ConfigurationAction.INDICATOR);
        if (indicatorValue != 0) {
            TextureAtlasSprite still = Minecraft.getInstance().getAtlasManager()
                    .getAtlasOrThrow(AtlasIds.BLOCKS)
                    .getSprite(Identifier.fromNamespaceAndPath(FunctionalStorage.MOD_ID, "block/indicator"));

            float red = 1, green = 1, blue = 1, alpha = 1;
            float x1 = -4 / 16F;
            float x2bg = x1 + 0.5f;
            float y1 = -6.65F / 16F;
            float y2 = y1 + 1.25f / 16F;
            float z2 = 0;
            float bx1 = 0 / 16F, bx2 = 8 / 16F, bz1 = 0 / 16F, bz2 = 2 / 16F;
            float u1 = still.getU(bx1);
            float u2bg = still.getU(bx2);
            float v1bg = still.getV(bz1);
            float v2bg = still.getV(bz2);

            float u2progress = still.getU(bx2 * progress);
            float x2progress = x1 + 0.5f * progress;
            float v1front = still.getV(8 / 16F);
            float v2front = still.getV(10 / 16F);

            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), (pose, buffer) -> {
                // Mode 3 hides the background bar
                if (indicatorValue != 3) {
                    buffer.addVertex(pose, x2bg, y1, z2).setColor(red, green, blue, alpha).setUv(u2bg, v1bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x2bg, y2, z2).setColor(red, green, blue, alpha).setUv(u2bg, v2bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha).setUv(u1, v2bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y1, z2).setColor(red, green, blue, alpha).setUv(u1, v1bg).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                }
                // Modes 2 and 3 only show the bar once the slot is full
                float zFront = 0.0001f;
                if (indicatorValue == 1 || progress >= 1) {
                    buffer.addVertex(pose, x2progress, y1, zFront).setColor(red, green, blue, alpha).setUv(u2progress, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x2progress, y2, zFront).setColor(red, green, blue, alpha).setUv(u2progress, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y2, zFront).setColor(red, green, blue, alpha).setUv(u1, v2front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                    buffer.addVertex(pose, x1, y1, zFront).setColor(red, green, blue, alpha).setUv(u1, v1front).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(0f, 0f, 1f);
                }
            });
        }
    }

    public static void renderStack(PoseStack poseStack, SubmitNodeCollector collector, int light, ItemStack stack,
            long amount, long limit, float textScale, ControllableDrawerTile.DrawerOptions options, Level level) {
        renderIndicator(poseStack, collector, light, Math.min(1, amount / (float) limit), options);

        ItemStackRenderState itemState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver()
                .updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, level, null, 0);
        boolean is3d = itemState.usesBlockLight();

        if (is3d) {
            float thickness = (float) FunctionalStorageClientConfig.DRAWER_RENDER_THICKNESS.get();
            poseStack.mulPose(MathUtils.createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), new Vector3f(.75f, .75f, thickness)));
        } else {
            poseStack.mulPose(MathUtils.createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), .4f));
        }

        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
            itemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        }

        // Undo the item scale so the amount sits at the same spot for flat and 3D items
        poseStack.mulPose(MathUtils.createTransformMatrix(
                new Vector3f(0), new Vector3f(0, 180, 0), 1));
        if (!is3d) {
            poseStack.mulPose(MathUtils.createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), new Vector3f(0.5f / 0.4f, 0.5f / 0.4f, 1)));
        } else {
            poseStack.mulPose(MathUtils.createTransformMatrix(
                    new Vector3f(0), new Vector3f(0), .665f));
        }

        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
            renderText(poseStack, collector, light, Component.literal(ChatFormatting.WHITE + ""
                    + NumberUtils.getFormatedBigNumber((int) Math.min(amount, Integer.MAX_VALUE))), Direction.NORTH,
                    textScale);
        }
    }

    /** Thanks Mekanism */
    public static void renderText(PoseStack poseStack, SubmitNodeCollector collector, int light, Component text,
            Direction side, float maxScale) {
        poseStack.translate(0, -0.745, 0.01);

        Font font = Minecraft.getInstance().font;

        int requiredWidth = Math.max(font.width(text), 1);
        int requiredHeight = font.lineHeight + 2;
        float scaler = 0.4F;
        float displayWidth = 1;
        float displayHeight = 1;
        float scale = displayWidth / requiredWidth * scaler;
        if (maxScale > 0) {
            scale = Math.min(scale, maxScale);
        }

        poseStack.scale(scale, -scale, scale);
        int realHeight = (int) Math.floor(displayHeight / scale);
        int realWidth = (int) Math.floor(displayWidth / scale);
        int offsetX = (realWidth - requiredWidth) / 2;
        int offsetY = (realHeight - requiredHeight) / 2;
        collector.submitText(poseStack, offsetX - realWidth / 2f, 3 + offsetY - realHeight / 2f,
                text.getVisualOrderText(), false, Font.DisplayMode.NORMAL, light, 0xFFFFFFFF, 0, 0);
    }
}
