package com.javiluli.createpipeconnector.client.render;

import com.javiluli.createpipeconnector.Constants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

public final class PipeConnectorRenderTypes extends RenderType {
    private static final int GHOST_BUFFER_SIZE = 2_097_152;
    private static final RenderType GHOST_TRANSLUCENT = RenderType.create(
            Constants.MOD_ID + "_ghost_translucent",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            GHOST_BUFFER_SIZE,
            true,
            true,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(BLOCK_SHEET_MIPPED)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(LIGHTMAP)
                    .setOutputState(TRANSLUCENT_TARGET)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true)
    );
    private static final RenderType ANCHOR_FILLED_BOX = RenderType.create(
            Constants.MOD_ID + "_anchor_filled_box",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP,
            1536,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(ITEM_ENTITY_TARGET)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)
    );

    private PipeConnectorRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType anchorFilledBox() {
        return ANCHOR_FILLED_BOX;
    }

    public static RenderType ghostTranslucent() {
        return GHOST_TRANSLUCENT;
    }
}
