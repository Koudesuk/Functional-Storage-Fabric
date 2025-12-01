package com.koudesuk.functionalstorage.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public class FunctionalStorageRenderType extends RenderType {

    public FunctionalStorageRenderType(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize,
            boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static final RenderType AREA_TYPE = create("controller_area",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(true));
}
