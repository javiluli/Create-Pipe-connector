package com.javiluli.createpipeconnector.feature.placement;

/** Preferencias inmutables que controlan la velocidad de construccion progresiva. */
public record PlacementAnimationSettings(boolean enabled, int piecesPerSecond) {
    public static final boolean DEFAULT_ENABLED = true;
    public static final int GAME_TICKS_PER_SECOND = 20;
    public static final PlacementAnimationSettings DEFAULT = new PlacementAnimationSettings(
            DEFAULT_ENABLED,
            PlacementAnimationSpeed.VERY_FAST.piecesPerSecond()
    );

    /** Ajusta valores antiguos o externos al preset de velocidad mas cercano. */
    public PlacementAnimationSettings {
        piecesPerSecond = PlacementAnimationSpeed.fromPiecesPerSecond(piecesPerSecond).piecesPerSecond();
    }
}
