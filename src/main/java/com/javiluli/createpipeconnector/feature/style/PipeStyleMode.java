package com.javiluli.createpipeconnector.feature.style;

/** Estilo visual aplicado a las posiciones ordinarias de la ruta. */
public enum PipeStyleMode {
    DEFAULT,
    GLASS;

    /** Devuelve el siguiente estilo de forma circular. */
    public PipeStyleMode next() {
        PipeStyleMode[] modes = values();
        return modes[(ordinal() + 1) % modes.length];
    }

    /** Devuelve el estilo anterior de forma circular. */
    public PipeStyleMode previous() {
        PipeStyleMode[] modes = values();
        return modes[(ordinal() + modes.length - 1) % modes.length];
    }
}
