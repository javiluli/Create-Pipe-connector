package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard.DepthTestStateShard;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL11;

/**
 * Define capas de renderizado para bloques fantasma y volumenes de ancla.
 */
public final class PipeConnectorRenderTypes extends RenderType {
    private static final String GHOST_RENDER_TYPE = Constants.MOD_ID + "_ghost_translucent";
    private static final String PLACEMENT_GHOST_RENDER_TYPE = Constants.MOD_ID + "_placement_ghost_translucent";
    private static final String ANCHOR_RENDER_TYPE = Constants.MOD_ID + "_anchor_filled_box";
    private static final int GHOST_BUFFER_SIZE = 2_097_152;
    private static final int ANCHOR_BUFFER_SIZE = 1_536;
    private static final DepthTestStateShard STRICT_DEPTH_TEST = new DepthTestStateShard(
            "strict_depth_test",
            GL11.GL_LESS
    );
    private static final RenderType GHOST_TRANSLUCENT = RenderType.create(
            GHOST_RENDER_TYPE,
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
    private static final RenderType PLACEMENT_GHOST_TRANSLUCENT = RenderType.create(
            PLACEMENT_GHOST_RENDER_TYPE,
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
                    .setDepthTestState(STRICT_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(true)
    );
    private static final RenderType ANCHOR_FILLED_BOX = RenderType.create(
            ANCHOR_RENDER_TYPE,
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP,
            ANCHOR_BUFFER_SIZE,
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

    /** Devuelve la capa que evita repintar piezas ya materializadas. */
    public static RenderType placementGhostTranslucent() {
        return PLACEMENT_GHOST_TRANSLUCENT;
    }
}
