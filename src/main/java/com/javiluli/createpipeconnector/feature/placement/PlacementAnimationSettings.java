package com.javiluli.createpipeconnector.feature.placement;

/** Preferencias inmutables que controlan la velocidad de construccion progresiva. */
public record PlacementAnimationSettings(boolean enabled, int piecesPerSecond) {
    public static final boolean DEFAULT_ENABLED = true;
    public static final int GAME_TICKS_PER_SECOND = 20;
    public static final int DEFAULT_PIECES_PER_SECOND = 20;
    public static final int MIN_PIECES_PER_SECOND = 1;
    public static final int MAX_PIECES_PER_SECOND = 20;
    public static final PlacementAnimationSettings DEFAULT = new PlacementAnimationSettings(
            DEFAULT_ENABLED,
            DEFAULT_PIECES_PER_SECOND
    );

    /** Limita la velocidad para impedir valores abusivos recibidos desde la red. */
    public PlacementAnimationSettings {
        piecesPerSecond = Math.max(
                MIN_PIECES_PER_SECOND,
                Math.min(MAX_PIECES_PER_SECOND, piecesPerSecond)
        );
    }
}
