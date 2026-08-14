package com.javiluli.createpipeconnector.feature.casing;

/** Determina si el revestimiento automatico esta desactivado o cubre toda la ruta. */
public enum CopperCasingMode {
    NONE,
    ALL;

    /** Devuelve el siguiente modo de forma circular. */
    public CopperCasingMode next() {
        CopperCasingMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

}
