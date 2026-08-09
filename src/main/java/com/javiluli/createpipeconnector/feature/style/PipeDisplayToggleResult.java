package com.javiluli.createpipeconnector.feature.style;

/** Resume el resultado de alternar el aspecto de un tramo conectado. */
public record PipeDisplayToggleResult(boolean glassMode, int changed, int skipped, int total) {
    /** Crea un resultado sin bloques modificados. */
    public static PipeDisplayToggleResult empty(boolean glassMode) {
        return new PipeDisplayToggleResult(glassMode, 0, 0, 0);
    }
}
