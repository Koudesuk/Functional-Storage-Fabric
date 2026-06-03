package com.koudesuk.functionalstorage.client;

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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
                        FunctionalStorageClientConfig.DRAWER_RENDER_RANGE)) {
            return;
        }

        poseStack.pushPose();
        transformToDrawerFace(poseStack, tile.getFacingDirection());
        int light = state.lightCoords;
        renderUpgrades(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, tile);

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
        poseStack.popPose();
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
        if (tile.getDrawerOptions().isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_UPGRADES)) {
            poseStack.pushPose();
            poseStack.translate(0.031, 0.031f, 0.472 / 16D);
            for (int i = 0; i < tile.getStorageUpgrades().getContainerSize(); i++) {
                ItemStack stack = tile.getStorageUpgrades().getItem(i);
                if (!stack.isEmpty()) {
                    renderItem(poseStack, collector, stack, tile.getLevel(), light, overlay, 0.0625f);
                    poseStack.translate(0.0625f, 0, 0);
                }
            }
            poseStack.popPose();
        }
        if (tile.isVoid()) {
            poseStack.pushPose();
            poseStack.last().pose().mul(MathUtils.createTransformMatrix(new Vector3f(0.969f, 0.031f, 0.469f / 16.0f),
                    new Vector3f(0), 0.0625f));
            renderItem(poseStack, collector, new ItemStack(FunctionalStorageItems.VOID_UPGRADE), tile.getLevel(), light,
                    overlay, 1.0f);
            poseStack.popPose();
        }
    }

    public static void renderStack(PoseStack poseStack, SubmitNodeCollector collector, int light, ItemStack stack,
            long amount, long limit, float textScale, ControllableDrawerTile.DrawerOptions options, Level level) {
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_RENDER)) {
            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            renderItem(poseStack, collector, stack, level, light, OverlayTexture.NO_OVERLAY, 0.45f);
            poseStack.popPose();
        }
        if (options.isActive(ConfigurationToolItem.ConfigurationAction.TOGGLE_NUMBERS)) {
            renderText(poseStack, collector, light,
                    NumberUtils.getFormatedBigNumber((int) Math.min(amount, Integer.MAX_VALUE)), textScale);
        }
    }

    private static void renderItem(PoseStack poseStack, SubmitNodeCollector collector, ItemStack stack, Level level,
            int light, int overlay, float scale) {
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        ItemStackRenderState itemState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver()
                .updateForTopItem(itemState, stack, ItemDisplayContext.FIXED, level, null, 0);
        itemState.submit(poseStack, collector, light, overlay, 0);
        poseStack.popPose();
    }

    public static void renderText(PoseStack poseStack, SubmitNodeCollector collector, int light, String amount,
            float maxScale) {
        poseStack.pushPose();
        poseStack.translate(0, -0.745, 0.01);
        Component text = Component.literal(amount);
        int requiredWidth = Math.max(Minecraft.getInstance().font.width(text), 1);
        float scale = Math.min(0.4f / requiredWidth, maxScale);
        poseStack.scale(scale, -scale, scale);
        collector.submitText(poseStack, -requiredWidth / 2.0f, -Minecraft.getInstance().font.lineHeight / 2.0f,
                text.getVisualOrderText(), false, Font.DisplayMode.NORMAL, light, 0xFFFFFFFF, 0, 0);
        poseStack.popPose();
    }
}
