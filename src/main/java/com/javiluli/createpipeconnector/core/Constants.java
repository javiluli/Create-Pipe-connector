package com.javiluli.createpipeconnector.core;

/** Identificadores y valores compartidos por varias features del conector. */
public final class Constants {
    public static final String MOD_ID = "createpipeconnector";

    public static final String NAMESPACE = "create";
    public static final String MECHANICAL_PUMP = "mechanical_pump";

    public static final String UPDATE_BLOCK_STATE = "updateBlockState";

    public static final String HUD_MISSING_PIPES = "hud.createpipeconnector.missing_pipes";
    public static final String HUD_MISSING_PUMPS = "hud.createpipeconnector.missing_pumps";
    public static final String HUD_MISSING_CASINGS = "hud.createpipeconnector.missing_casings";
    public static final String HUD_MISSING_MATERIALS = "hud.createpipeconnector.missing_materials";
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

    /** Impide crear instancias del contenedor de constantes compartidas. */
    private Constants() {
    }
}
