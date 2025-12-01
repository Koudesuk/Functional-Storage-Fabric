package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class FunctionalStorageRenderTypes extends RenderType {

    public FunctionalStorageRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
            boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType TYPE = createRenderType();

    private static RenderType createRenderType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeLinesShader))
                .setDepthTestState(new RenderStateShard.DepthTestStateShard("always", 519))
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.empty()))
                .setLayeringState(new RenderStateShard.LayeringStateShard("view_offset_z_layering", () -> {
                    com.mojang.blaze3d.vertex.PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.pushPose();
                    posestack.scale(0.99975586F, 0.99975586F, 0.99975586F);
                    RenderSystem.applyModelViewMatrix();
                }, () -> {
                    com.mojang.blaze3d.vertex.PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.popPose();
                    RenderSystem.applyModelViewMatrix();
                }))
                .setCullState(new RenderStateShard.CullStateShard(false))
                .createCompositeState(false);
        return RenderType.create("custom_lines", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES,
                256, false, false, state);
    }

    public static RenderType AREA_TYPE = createAreaRenderType();

    public static RenderType createAreaRenderType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorShader))
                .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }, () -> {
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                }))
                .setDepthTestState(new RenderStateShard.DepthTestStateShard("<=", 515)).createCompositeState(true);
        return RenderType.create("controller_area", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
                false, true, state);
    }
}
