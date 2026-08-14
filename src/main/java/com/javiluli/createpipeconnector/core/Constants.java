package com.javiluli.createpipeconnector.core;

/**
 * Reune los identificadores y valores de configuracion compartidos por el conector.
 *
 * <p>Solo contiene valores compartidos por varias clases o necesarios para
 * registros y controles configurables.</p>
 */
public final class Constants {
    /** Identificador del mod empleado por Forge y por sus metadatos. */
    public static final String MOD_ID = "createpipeconnector";

    /** Espacio de nombres utilizado por los registros de Create. */
    public static final String NAMESPACE = "create";
    public static final String MECHANICAL_PUMP = "mechanical_pump";

    /** Objetivo de reflexion compartido entre adaptadores de Create. */
    public static final String UPDATE_BLOCK_STATE = "updateBlockState";

    /** Claves de traduccion compartidas por cliente y servidor. */
    public static final String HUD_MISSING_PIPES = "hud.createpipeconnector.missing_pipes";
    public static final String HUD_MISSING_PUMPS = "hud.createpipeconnector.missing_pumps";
    public static final String HUD_MISSING_CASINGS = "hud.createpipeconnector.missing_casings";
    public static final String HUD_MISSING_MATERIALS = "hud.createpipeconnector.missing_materials";

    /** Claves y categoria de los controles configurables de Minecraft. */
    public static final String CATEGORY = "key.categories.createpipeconnector";
    public static final String TOGGLE_PIPE_CONNECTOR_MODE = "key.createpipeconnector.toggle_connector_mode";
    public static final String TOGGLE_PREVIEW_LOCK = "key.createpipeconnector.toggle_preview_lock";
    public static final String APPLY_MANUAL_ACTION = "key.createpipeconnector.apply_manual_action";
    public static final String CYCLE_MANUAL_ACTION = "key.createpipeconnector.cycle_manual_action";
    public static final String UNDO_LAST_ROUTE_ACTION = "key.createpipeconnector.undo_last_route_action";
    public static final String TOGGLE_COPPER_CASING = "key.createpipeconnector.toggle_copper_casing";
    public static final String REMOVE_LAST_COPPER_CASING = "key.createpipeconnector.remove_last_copper_casing";
    public static final String TOGGLE_MANUAL_PUMP = "key.createpipeconnector.toggle_manual_pump";
    public static final String REMOVE_LAST_MANUAL_PUMP = "key.createpipeconnector.remove_last_manual_pump";
    public static final String CYCLE_PUMP_MODE = "key.createpipeconnector.cycle_pump_mode";
    public static final String CYCLE_COPPER_CASING_MODE = "key.createpipeconnector.cycle_copper_casing_mode";
    public static final String CYCLE_PIPE_STYLE_MODE = "key.createpipeconnector.cycle_pipe_style_mode";
    public static final String REVERSE_PUMP_DIRECTION = "key.createpipeconnector.reverse_pump_direction";
    public static final String OPEN_PIPE_CONNECTOR_OPTIONS = "key.createpipeconnector.open_connector_options";

    /**
     * Impide crear instancias de esta clase de constantes.
     */
    private Constants() {
    }
}
