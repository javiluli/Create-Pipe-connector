package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;

/**
 * Define capas de renderizado para bloques fantasma y volumenes de ancla.
 */
public final class PipeConnectorRenderTypes extends RenderType {
    private static final RenderType GHOST_TRANSLUCENT = RenderType.create(
            Constants.GHOST_RENDER_TYPE,
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            Constants.GHOST_BUFFER_SIZE,
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
            Constants.ANCHOR_RENDER_TYPE,
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP,
            Constants.ANCHOR_BUFFER_SIZE,
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

    /** Construye internamente una capa mediante la API base de Minecraft. */
    private PipeConnectorRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    /** Devuelve la capa translucida empleada por las caras del ancla. */
    public static RenderType anchorFilledBox() {
        return ANCHOR_FILLED_BOX;
    }

    /** Devuelve la capa translucida empleada por la geometria fantasma. */
    public static RenderType ghostTranslucent() {
        return GHOST_TRANSLUCENT;
    }
}
