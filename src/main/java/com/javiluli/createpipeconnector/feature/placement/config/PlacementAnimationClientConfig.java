package com.javiluli.createpipeconnector.feature.placement.config;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import net.createmod.catnip.config.ui.ConfigHelper;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Guarda las preferencias locales de animacion mediante el sistema TOML de NeoForge. */
public final class PlacementAnimationClientConfig {
    private static ModConfigSpec.BooleanValue animationEnabled;
    private static ModConfigSpec.BooleanValue zoomEnabled;
    private static ModConfigSpec.BooleanValue fullRoutePreview;
    private static ModConfigSpec.BooleanValue nextPiecePreview;
    private static ModConfigSpec.IntValue delayTime;

    /** Registra la seccion de animacion dentro del spec cliente compartido. */
    public static void define(ModConfigSpec.Builder builder) {
        builder.push("placementAnimation");
        animationEnabled = builder
                .comment("Build confirmed routes progressively instead of placing every piece immediately.")
                .translation("config.createpipeconnector.placement_animation.enabled")
                .define("animateRouteConstruction", PlacementAnimationSettings.DEFAULT_ENABLED);
        delayTime = builder
                .comment(
                        "Delay in milliseconds between the start of two consecutive pieces.",
                        "Lower values build the route faster. Range: 50-1000 ms.",
                        "Speed guide: 50 ms = Very fast, 67 ms = Fast, 100 ms = Normal,",
                        "200 ms = Slow, 1000 ms = Very slow."
                )
                .translation("config.createpipeconnector.placement_animation.delay_time")
                .defineInRange(
                        "delayTimeMilliseconds",
                        PlacementAnimationSettings.DEFAULT_DELAY_MILLISECONDS,
                        PlacementAnimationSettings.MINIMUM_DELAY_MILLISECONDS,
                        PlacementAnimationSettings.MAXIMUM_DELAY_MILLISECONDS
                );
        zoomEnabled = builder
                .comment(
                        "Scale each pending piece from minimum to full size before placement.",
                        "Disable this to keep progressive placement without the zoom effect."
                )
                .translation("config.createpipeconnector.placement_animation.zoom_enabled")
                .define("zoomAnimation", PlacementAnimationSettings.DEFAULT_ZOOM_ENABLED);
        fullRoutePreview = builder
                .comment(
                        "Keep the complete unbuilt route visible without outlines during construction.",
                        "Only used while animated route construction is enabled."
                )
                .translation("config.createpipeconnector.placement_animation.full_route_preview")
                .define("showFullRoutePreview", true);
        nextPiecePreview = builder
                .comment(
                        "Highlight the next piece immediately before it is placed.",
                        "Only used while animated route construction is enabled."
                )
                .translation("config.createpipeconnector.placement_animation.next_piece_preview")
                .define("showNextPiecePreview", true);
        builder.pop();
    }

    /** Impide crear instancias del adaptador de configuracion. */
    private PlacementAnimationClientConfig() {
    }

    /** Devuelve una copia saneada de las preferencias cargadas. */
    public static PlacementAnimationSettings get() {
        return new PlacementAnimationSettings(animationEnabled.get(), zoomEnabled.get(), delayTime.get());
    }

    /** Devuelve el valor visible en la pantalla de Create antes de guardar. */
    public static boolean isAnimationEnabledInConfigScreen() {
        return ConfigHelper.getValue(String.join(".", animationEnabled.getPath()), animationEnabled);
    }

    /** Indica si debe mantenerse visible todo el tramo aun no construido. */
    public static boolean showFullRoutePreview() {
        return animationEnabled.get() && fullRoutePreview.get();
    }

    /** Indica si debe resaltarse la pieza inmediatamente anterior a colocar. */
    public static boolean showNextPiecePreview() {
        return animationEnabled.get() && nextPiecePreview.get();
    }
}
