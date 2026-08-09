package com.javiluli.createpipeconnector.feature.pump;

/** Estrategias de separacion automatica de bombas mecanicas. */
public enum PumpMode {
    OFF,
    EFFICIENT,
    SAFE;

    /** Indica si el modo coloca bombas automaticamente. */
    public boolean isAutomatic() {
        return this == EFFICIENT || this == SAFE;
    }

    /** Devuelve el siguiente modo de forma circular. */
    public PumpMode next() {
        PumpMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    /** Devuelve el modo anterior de forma circular. */
    public PumpMode previous() {
        PumpMode[] modes = values();
        return modes[(ordinal() + modes.length - 1) % modes.length];
    }
}
