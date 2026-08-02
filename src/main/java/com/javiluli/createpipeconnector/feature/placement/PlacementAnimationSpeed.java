package com.javiluli.createpipeconnector.feature.placement;

/** Presets cerrados disponibles para la construccion progresiva. */
public enum PlacementAnimationSpeed {
    VERY_SLOW(1),
    SLOW(5),
    NORMAL(10),
    FAST(15),
    VERY_FAST(20);

    private final int piecesPerSecond;

    /** Asocia el preset con su velocidad real de colocacion. */
    PlacementAnimationSpeed(int piecesPerSecond) {
        this.piecesPerSecond = piecesPerSecond;
    }

    /** Devuelve las piezas colocadas durante cada segundo de juego. */
    public int piecesPerSecond() {
        return piecesPerSecond;
    }

    /** Convierte valores antiguos o externos al preset mas cercano. */
    public static PlacementAnimationSpeed fromPiecesPerSecond(int piecesPerSecond) {
        PlacementAnimationSpeed nearest = VERY_FAST;
        int nearestDistance = Integer.MAX_VALUE;
        for (PlacementAnimationSpeed speed : values()) {
            int distance = Math.abs(speed.piecesPerSecond - piecesPerSecond);
            if (distance < nearestDistance) {
                nearest = speed;
                nearestDistance = distance;
            }
        }
        return nearest;
    }
}
