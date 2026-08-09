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
    private static final String GHOST_BEFORE_FLUIDS_RENDER_TYPE = Constants.MOD_ID + "_ghost_before_fluids";
    private static final String PLACEMENT_GHOST_RENDER_TYPE = Constants.MOD_ID + "_placement_ghost_translucent";
    private static final String PLACEMENT_GHOST_BEFORE_FLUIDS_RENDER_TYPE = Constants.MOD_ID + "_placement_ghost_before_fluids";
    private static final String ANCHOR_RENDER_TYPE = Constants.MOD_ID + "_anchor_filled_box";
    private static final String ANCHOR_BEFORE_FLUIDS_RENDER_TYPE = Constants.MOD_ID + "_anchor_before_fluids";
    private static final String OUTLINE_BEFORE_FLUIDS_RENDER_TYPE = Constants.MOD_ID + "_outline_before_fluids";
    private static final int GHOST_BUFFER_SIZE = 2_097_152;
    private static final int ANCHOR_BUFFER_SIZE = 1_536;
    private static final DepthTestStateShard STRICT_DEPTH_TEST = new DepthTestStateShard(
            "strict_depth_test",
            GL11.GL_LESS
    );
    private static final RenderType GHOST_TRANSLUCENT = createGhostRenderType(
            GHOST_RENDER_TYPE,
            LEQUAL_DEPTH_TEST,
            false
    );
    private static final RenderType GHOST_BEFORE_FLUIDS = createGhostRenderType(
            GHOST_BEFORE_FLUIDS_RENDER_TYPE,
            LEQUAL_DEPTH_TEST,
            true
    );
    private static final RenderType PLACEMENT_GHOST_TRANSLUCENT = createGhostRenderType(
            PLACEMENT_GHOST_RENDER_TYPE,
            STRICT_DEPTH_TEST,
            false
    );
    private static final RenderType PLACEMENT_GHOST_BEFORE_FLUIDS = createGhostRenderType(
            PLACEMENT_GHOST_BEFORE_FLUIDS_RENDER_TYPE,
            STRICT_DEPTH_TEST,
            true
    );
    private static final RenderType ANCHOR_FILLED_BOX = createAnchorRenderType(
            ANCHOR_RENDER_TYPE,
            false
    );
    private static final RenderType ANCHOR_BEFORE_FLUIDS = createAnchorRenderType(
            ANCHOR_BEFORE_FLUIDS_RENDER_TYPE,
            true
    );
    private static final RenderType OUTLINE_BEFORE_FLUIDS = RenderType.create(
            OUTLINE_BEFORE_FLUIDS_RENDER_TYPE,
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            ANCHOR_BUFFER_SIZE,
            false,
            false,
            CompositeState.builder()
                    .setShaderState(RENDERTYPE_LINES_SHADER)
                    .setLineState(DEFAULT_LINE)
                    .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setOutputState(MAIN_TARGET)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .setCullState(NO_CULL)
                    .createCompositeState(false)
    );

    /** Construye internamente una capa mediante la API base de Minecraft. */
    private PipeConnectorRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int bufferSize, boolean affectsCrumbling, boolean sortOnUpload, Runnable setupState, Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    /** Devuelve la capa de ancla adecuada para la fase de fluidos actual. */
    public static RenderType anchorFilledBox(boolean beforeFluids) {
        return beforeFluids ? ANCHOR_BEFORE_FLUIDS : ANCHOR_FILLED_BOX;
    }

    /** Devuelve la capa fantasma adecuada para la fase de fluidos actual. */
    public static RenderType ghostTranslucent(boolean beforeFluids) {
        return beforeFluids ? GHOST_BEFORE_FLUIDS : GHOST_TRANSLUCENT;
    }

    /** Devuelve la capa progresiva adecuada para la fase de fluidos actual. */
    public static RenderType placementGhostTranslucent(boolean beforeFluids) {
        return beforeFluids ? PLACEMENT_GHOST_BEFORE_FLUIDS : PLACEMENT_GHOST_TRANSLUCENT;
    }

    /** Devuelve la capa de contorno adecuada para la fase de fluidos actual. */
    public static RenderType outline(boolean beforeFluids) {
        return beforeFluids ? OUTLINE_BEFORE_FLUIDS : RenderType.lines();
    }

    /** Crea una capa fantasma para el target correspondiente a su fase. */
    private static RenderType createGhostRenderType(String name, DepthTestStateShard depthTest, boolean beforeFluids) {
        return RenderType.create(
                name,
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
                        .setOutputState(beforeFluids ? MAIN_TARGET : TRANSLUCENT_TARGET)
                        .setDepthTestState(depthTest)
                        .setWriteMaskState(COLOR_WRITE)
                        .createCompositeState(true)
        );
    }

    /** Crea una capa de ancla para el target correspondiente a su fase. */
    private static RenderType createAnchorRenderType(String name, boolean beforeFluids) {
        return RenderType.create(
                name,
                DefaultVertexFormat.POSITION_COLOR,
                VertexFormat.Mode.TRIANGLE_STRIP,
                ANCHOR_BUFFER_SIZE,
                false,
                false,
                CompositeState.builder()
                        .setShaderState(POSITION_COLOR_SHADER)
                        .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setOutputState(beforeFluids ? MAIN_TARGET : ITEM_ENTITY_TARGET)
                        .setDepthTestState(LEQUAL_DEPTH_TEST)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .createCompositeState(false)
        );
    }
}
