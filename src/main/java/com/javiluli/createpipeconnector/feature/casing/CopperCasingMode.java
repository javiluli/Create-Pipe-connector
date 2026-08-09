package com.javiluli.createpipeconnector.feature.casing;

/** Determina si el revestimiento esta desactivado, es manual o global. */
public enum CopperCasingMode {
    NONE,
    MANUAL,
    ALL;

    /** Devuelve el siguiente modo de forma circular. */
    public CopperCasingMode next() {
        CopperCasingMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    /** Devuelve el modo anterior de forma circular. */
    public CopperCasingMode previous() {
        CopperCasingMode[] modes = values();
        return modes[(ordinal() + modes.length - 1) % modes.length];
    }
}
