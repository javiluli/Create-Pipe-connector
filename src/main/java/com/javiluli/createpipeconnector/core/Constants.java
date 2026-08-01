package com.javiluli.createpipeconnector.core;

/**
 * Reune los identificadores y valores de configuracion compartidos por el conector.
 *
 * <p>Centralizar estos valores evita dispersar nombres de protocolo, claves de
 * traduccion, objetivos de reflexion y limites de interaccion por el codigo.</p>
 */
public final class Constants {
    /** Identificador del mod empleado por Forge y por sus metadatos. */
    public static final String MOD_ID = "createpipeconnector";

    /** Espacio de nombres utilizado por los registros de Create. */
    public static final String NAMESPACE = "create";
    public static final String FLUID_PIPE = "fluid_pipe";
    public static final String GLASS_FLUID_PIPE = "glass_fluid_pipe";
    public static final String ENCASED_FLUID_PIPE = "encased_fluid_pipe";
    public static final String MECHANICAL_PUMP = "mechanical_pump";
    public static final String COPPER_CASING = "copper_casing";
    public static final String WRENCH = "wrench";

    /** Objetivos de reflexion compatibles con las versiones admitidas de Create y Forge. */
    public static final String CREATE_FLUID_PROPAGATOR = "com.simibubi.create.content.fluids.FluidPropagator";
    public static final String CREATE_FLUID_TRANSPORT = "com.simibubi.create.content.fluids.FluidTransportBehaviour";
    public static final String FORGE_MOD = "net.minecraftforge.common.ForgeMod";
    public static final String GET_PUMP_RANGE = "getPumpRange";
    public static final String UPDATE_BLOCK_STATE = "updateBlockState";
    public static final String CACHE_FLOWS = "cacheFlows";
    public static final String LOAD_FLOWS = "loadFlows";
    public static final String BLOCK_INTERACTION_RANGE = "blockInteractionRange";
    public static final String BLOCK_REACH = "BLOCK_REACH";
    public static final String GET = "get";

    /** Metadatos del canal de red de Forge. */
    public static final String PROTOCOL_VERSION = "2";
    public static final String CHANNEL_PATH = "main";

    /** Limites de interaccion expresados en ticks o bloques. */
    public static final int WRENCH_DOUBLE_CLICK_TICKS = 10;
    public static final int MANUAL_MARKER_SNAP_DISTANCE = 3;
    public static final double DEFAULT_BLOCK_REACH = 5.0D;

    /** Claves de traduccion utilizadas por mensajes, HUD y pantallas. */
    public static final String HUD_NO_ROUTE = "hud.createpipeconnector.no_route";
    public static final String HUD_MISSING_PIPES = "hud.createpipeconnector.missing_pipes";
    public static final String HUD_MISSING_PUMPS = "hud.createpipeconnector.missing_pumps";
    public static final String HUD_MISSING_CASINGS = "hud.createpipeconnector.missing_casings";
    public static final String HUD_MISSING_MATERIALS = "hud.createpipeconnector.missing_materials";
    public static final String HUD_FIRST_POINT_SELECTED = "hud.createpipeconnector.first_point_selected";
    public static final String HUD_PIPE_STYLE_CLICK_AGAIN = "hud.createpipeconnector.pipe_style_click_again";
    public static final String HUD_PIPE_STYLE_NO_CHANGES = "hud.createpipeconnector.pipe_style_no_changes";
    public static final String HUD_PIPE_STYLE_TO_GLASS = "hud.createpipeconnector.pipe_style_to_glass";
    public static final String HUD_PIPE_STYLE_TO_DEFAULT = "hud.createpipeconnector.pipe_style_to_default";
    public static final String HUD_CONTROL_CONNECTOR_MODE = "hud.createpipeconnector.control.connector_mode";
    public static final String HUD_CONTROL_START_CONFIRM = "hud.createpipeconnector.control.start_confirm";
    public static final String HUD_CONTROL_ROUTE_PRIORITY = "hud.createpipeconnector.control.cycle_route_priority";
    public static final String HUD_CONTROL_ADD_ANCHOR = "hud.createpipeconnector.control.add_anchor";
    public static final String HUD_CONTROL_LOCK_PREVIEW = "hud.createpipeconnector.control.lock_preview";
    public static final String OPTIONS_TITLE = "screen.createpipeconnector.options.title";
    public static final String OPTIONS_HINT = "screen.createpipeconnector.options.hint";
    public static final String OPTIONS_MECHANIC_PREFIX = "screen.createpipeconnector.options.mechanic.";
    public static final String OPTIONS_OPTION_PREFIX = "screen.createpipeconnector.options.option.";
    public static final String OPTIONS_DESCRIPTION_PREFIX = "screen.createpipeconnector.options.description.";

    /** Claves y categoria de los controles configurables de Minecraft. */
    public static final String CATEGORY = "key.categories.createpipeconnector";
    public static final String TOGGLE_CONNECTOR_MODE = "key.createpipeconnector.toggle_connector_mode";
    public static final String TOGGLE_PREVIEW_LOCK = "key.createpipeconnector.toggle_preview_lock";
    public static final String ADD_ANCHOR = "key.createpipeconnector.add_anchor";
    public static final String REMOVE_LAST_ANCHOR = "key.createpipeconnector.remove_last_anchor";
    public static final String TOGGLE_COPPER_CASING = "key.createpipeconnector.toggle_copper_casing";
    public static final String REMOVE_LAST_COPPER_CASING = "key.createpipeconnector.remove_last_copper_casing";
    public static final String TOGGLE_MANUAL_PUMP = "key.createpipeconnector.toggle_manual_pump";
    public static final String REMOVE_LAST_MANUAL_PUMP = "key.createpipeconnector.remove_last_manual_pump";
    public static final String TOGGLE_AUTO_PUMPS = "key.createpipeconnector.toggle_auto_pumps";
    public static final String CYCLE_COPPER_CASING_MODE = "key.createpipeconnector.cycle_copper_casing_mode";
    public static final String CYCLE_PIPE_STYLE_MODE = "key.createpipeconnector.cycle_pipe_style_mode";
    public static final String REVERSE_AUTO_PUMP_DIRECTION = "key.createpipeconnector.reverse_auto_pump_direction";
    public static final String CYCLE_ROUTE_PRIORITY = "key.createpipeconnector.cycle_route_priority";

    /** Nombres y tamanos registrados por los sistemas de renderizado y HUD. */
    public static final String GHOST_RENDER_TYPE = MOD_ID + "_ghost_translucent";
    public static final String ANCHOR_RENDER_TYPE = MOD_ID + "_anchor_filled_box";
    public static final String CONTROLS_OVERLAY = "pipe_connector_controls";
    public static final String ITEM_MODEL_VARIANT = "inventory";
    public static final int GHOST_BUFFER_SIZE = 2_097_152;
    public static final int ANCHOR_BUFFER_SIZE = 1_536;
    public static final int PREVIEW_BLOCK_UPDATE_FLAGS = 3;

    /**
     * Impide crear instancias de esta clase de constantes.
     */
    private Constants() {
    }
}
