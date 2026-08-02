package com.javiluli.createpipeconnector.feature.placement.config;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSpeed;
import net.createmod.catnip.config.ui.ConfigHelper;
import net.minecraftforge.common.ForgeConfigSpec;

/** Guarda las preferencias locales de animacion mediante el sistema TOML de Forge. */
public final class PlacementAnimationClientConfig {
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ANIMATION_ENABLED;
    private static final ForgeConfigSpec.BooleanValue FULL_ROUTE_PREVIEW;
    private static final ForgeConfigSpec.BooleanValue NEXT_PIECE_PREVIEW;
    private static final ForgeConfigSpec.EnumValue<PlacementAnimationSpeed> SPEED_PRESET;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("placementAnimation");
        ANIMATION_ENABLED = builder
                .comment("Build confirmed routes progressively instead of placing every piece immediately.")
                .translation("config.createpipeconnector.placement_animation.enabled")
                .define("animateRouteConstruction", PlacementAnimationSettings.DEFAULT_ENABLED);
        SPEED_PRESET = builder
                .comment(
                        "Closed construction speed preset.",
                        "Very slow: 1 piece/s, Slow: 5 pieces/s, Normal: 10 pieces/s,",
                        "Fast: 15 pieces/s, Very fast: 20 pieces/s."
                )
                .translation("config.createpipeconnector.placement_animation.speed_preset")
                .defineEnum("constructionSpeed", PlacementAnimationSpeed.VERY_FAST);
        FULL_ROUTE_PREVIEW = builder
                .comment(
                        "Keep the complete unbuilt route visible without outlines during construction.",
                        "Only used while animated route construction is enabled."
                )
                .translation("config.createpipeconnector.placement_animation.full_route_preview")
                .define("showFullRoutePreview", true);
        NEXT_PIECE_PREVIEW = builder
                .comment(
                        "Highlight the next piece immediately before it is placed.",
                        "Only used while animated route construction is enabled."
                )
                .translation("config.createpipeconnector.placement_animation.next_piece_preview")
                .define("showNextPiecePreview", true);
        builder.pop();
        SPEC = builder.build();
    }

    /** Impide crear instancias del adaptador de configuracion. */
    private PlacementAnimationClientConfig() {
    }

    /** Devuelve una copia saneada de las preferencias cargadas. */
    public static PlacementAnimationSettings get() {
        return new PlacementAnimationSettings(
                ANIMATION_ENABLED.get(),
                SPEED_PRESET.get().piecesPerSecond()
        );
    }

    /**
     * Devuelve el valor visible en la pantalla de Create, incluidos cambios
     * pendientes que el jugador todavia no ha guardado.
     */
    public static boolean isAnimationEnabledInConfigScreen() {
        return ConfigHelper.getValue(
                String.join(".", ANIMATION_ENABLED.getPath()),
                ANIMATION_ENABLED
        );
    }

    /** Indica si debe mantenerse visible todo el tramo aun no construido. */
    public static boolean showFullRoutePreview() {
        return ANIMATION_ENABLED.get() && FULL_ROUTE_PREVIEW.get();
    }

    /** Indica si debe resaltarse la pieza inmediatamente anterior a colocar. */
    public static boolean showNextPiecePreview() {
        return ANIMATION_ENABLED.get() && NEXT_PIECE_PREVIEW.get();
    }

    /** Guarda las preferencias elegidas desde la pantalla del mod. */
    public static void save(PlacementAnimationSettings settings) {
        ANIMATION_ENABLED.set(settings.enabled());
        SPEED_PRESET.set(PlacementAnimationSpeed.fromPiecesPerSecond(settings.piecesPerSecond()));
        SPEC.save();
    }
}
