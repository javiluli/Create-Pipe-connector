package com.javiluli.createpipeconnector.feature.placement;

/** Preferencias inmutables que controlan la construccion progresiva. */
public record PlacementAnimationSettings(boolean enabled, int delayMilliseconds) {
    public static final boolean DEFAULT_ENABLED = true;
    public static final int MILLISECONDS_PER_GAME_TICK = 50;
    public static final int MINIMUM_DELAY_MILLISECONDS = MILLISECONDS_PER_GAME_TICK;
    public static final int MAXIMUM_DELAY_MILLISECONDS = 1_000;
    public static final int DEFAULT_DELAY_MILLISECONDS = MINIMUM_DELAY_MILLISECONDS;
    public static final PlacementAnimationSettings DEFAULT = new PlacementAnimationSettings(
            DEFAULT_ENABLED,
            DEFAULT_DELAY_MILLISECONDS
    );

    /** Limita valores externos al intervalo ofrecido por la configuracion. */
    public PlacementAnimationSettings {
        delayMilliseconds = Math.max(
                MINIMUM_DELAY_MILLISECONDS,
                Math.min(MAXIMUM_DELAY_MILLISECONDS, delayMilliseconds)
        );
    }
}
