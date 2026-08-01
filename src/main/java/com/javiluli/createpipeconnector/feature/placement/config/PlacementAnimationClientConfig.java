package com.javiluli.createpipeconnector.feature.placement.config;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import net.minecraftforge.common.ForgeConfigSpec;

/** Guarda las preferencias locales de animacion mediante el sistema TOML de Forge. */
public final class PlacementAnimationClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ANIMATION_ENABLED;
    private static final ForgeConfigSpec.IntValue PIECES_PER_SECOND;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("placementAnimation");
        ANIMATION_ENABLED = builder
                .comment("Build confirmed routes progressively instead of placing every piece immediately.")
                .translation("config.createpipeconnector.placement_animation.enabled")
                .define("enabled", PlacementAnimationSettings.DEFAULT_ENABLED);
        PIECES_PER_SECOND = builder
                .comment("Pipes or pumps placed per game second. Pieces are always placed individually.")
                .translation("config.createpipeconnector.placement_animation.pieces_per_second")
                .defineInRange(
                        "piecesPerSecond",
                        PlacementAnimationSettings.DEFAULT_PIECES_PER_SECOND,
                        PlacementAnimationSettings.MIN_PIECES_PER_SECOND,
                        PlacementAnimationSettings.MAX_PIECES_PER_SECOND
                );
        builder.pop();
        SPEC = builder.build();
    }

    /** Impide crear instancias del adaptador de configuracion. */
    private PlacementAnimationClientConfig() {
    }

    /** Devuelve una copia saneada de las preferencias cargadas. */
    public static PlacementAnimationSettings get() {
        return new PlacementAnimationSettings(ANIMATION_ENABLED.get(), PIECES_PER_SECOND.get());
    }

    /** Guarda las preferencias elegidas desde la pantalla del mod. */
    public static void save(PlacementAnimationSettings settings) {
        ANIMATION_ENABLED.set(settings.enabled());
        PIECES_PER_SECOND.set(settings.piecesPerSecond());
        SPEC.save();
    }
}
