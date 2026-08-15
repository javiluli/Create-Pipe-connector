package com.javiluli.createpipeconnector.feature.placement;

/** Comparte la temporizacion del zoom en cascada entre cliente y servidor. */
public final class PlacementCascadeTiming {
    private static final float ZOOM_DURATION_INTERVALS = 2.0F;

    /** Impide crear instancias del calculador temporal. */
    private PlacementCascadeTiming() {
    }

    /** Devuelve el intervalo medio entre el inicio de dos piezas consecutivas. */
    public static float pieceIntervalTicks(int delayMilliseconds) {
        return (float) sanitizeDelay(delayMilliseconds)
                / PlacementAnimationSettings.MILLISECONDS_PER_GAME_TICK;
    }

    /**
     * Mantiene un solapamiento fijo del cincuenta por ciento: la siguiente
     * pieza comienza a mitad del zoom actual y la anterior termina al comenzar
     * la tercera.
     */
    public static float zoomDurationTicks(int delayMilliseconds) {
        return pieceIntervalTicks(delayMilliseconds) * ZOOM_DURATION_INTERVALS;
    }

    /** Evita intervalos invalidos si una preferencia externa envia otro valor. */
    private static int sanitizeDelay(int delayMilliseconds) {
        return Math.max(
                PlacementAnimationSettings.MINIMUM_DELAY_MILLISECONDS,
                Math.min(PlacementAnimationSettings.MAXIMUM_DELAY_MILLISECONDS, delayMilliseconds)
        );
    }
}
