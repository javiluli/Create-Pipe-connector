package com.javiluli.createpipeconnector.feature.material.shulker.config;

import net.minecraftforge.common.ForgeConfigSpec;

/** Expone la preferencia local que permite usar materiales guardados en shulkers. */
public final class ShulkerMaterialClientConfig {
    private static final boolean DEFAULT_ENABLED = true;
    private static ForgeConfigSpec.BooleanValue enabled;

    /** Impide crear instancias del adaptador de configuracion. */
    private ShulkerMaterialClientConfig() {
    }

    /** Registra la seccion de fuentes de materiales en la pantalla de Create. */
    public static void define(ForgeConfigSpec.Builder builder) {
        builder.push("materialSources");
        enabled = builder
                .comment(
                        "Allow the connector to count and consume materials stored inside carried vanilla shulker boxes.",
                        "Loose inventory items are always used before shulker contents."
                )
                .translation("config.createpipeconnector.material_sources.use_shulkers")
                .define("useShulkerMaterials", DEFAULT_ENABLED);
        builder.pop();
    }

    /** Indica si el cliente permite usar sus shulkers como fuente de materiales. */
    public static boolean isEnabled() {
        return enabled == null ? DEFAULT_ENABLED : enabled.get();
    }
}
