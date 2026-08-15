package com.javiluli.createpipeconnector.feature.manual.config;

import com.javiluli.createpipeconnector.feature.manual.ManualAction;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Configura si las marcas manuales tambien guian el recorrido como anclas. */
public final class ManualAnchorClientConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static ModConfigSpec.BooleanValue enabled;

    /** Impide crear instancias del adaptador de configuracion. */
    private ManualAnchorClientConfig() {
    }

    /** Registra la preferencia individual en la pantalla de Create. */
    public static void define(ModConfigSpec.Builder builder) {
        builder.push("manualTools");
        enabled = builder
                .comment(
                        "Make manually marked pumps and copper casing positions act as route anchors.",
                        "Disable this to decorate the current route without changing its path."
                )
                .translation("config.createpipeconnector.manual_tools.markers_create_anchors")
                .define("manualMarkersCreateAnchors", DEFAULT_ENABLED);
        builder.pop();
    }

    /** Indica si una nueva marca manual debe crear un ancla auxiliar. */
    public static boolean isEnabled() {
        return enabled == null ? DEFAULT_ENABLED : enabled.get();
    }

    /** Indica si una pump o casing manual tambien fijara la ruta como ancla. */
    public static boolean willCreateSupportAnchor(ManualAction action) {
        return action != null && action != ManualAction.ANCHOR && isEnabled();
    }
}
