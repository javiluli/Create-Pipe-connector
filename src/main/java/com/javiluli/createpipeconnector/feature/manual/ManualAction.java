package com.javiluli.createpipeconnector.feature.manual;

/** Acciones puntuales que comparten los controles contextuales de anadir y deshacer. */
public enum ManualAction {
    ANCHOR("anchor"),
    MECHANICAL_PUMP("mechanical_pump"),
    COPPER_CASING("copper_casing");

    private final String id;

    /** Crea una accion con un identificador estable para interfaz y traducciones. */
    ManualAction(String id) {
        this.id = id;
    }

    /** Devuelve el identificador estable de la accion. */
    public String id() {
        return id;
    }

    /** Devuelve la siguiente herramienta manual siguiendo el orden del menu radial. */
    public ManualAction next() {
        ManualAction[] actions = values();
        return actions[(ordinal() + 1) % actions.length];
    }
}
