package com.javiluli.createpipeconnector.config;

import com.javiluli.createpipeconnector.feature.manual.config.ManualAnchorClientConfig;
import com.javiluli.createpipeconnector.feature.material.shulker.config.ShulkerMaterialClientConfig;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Reune las preferencias individuales mostradas por la pantalla de Create.
 *
 * <p>El archivo es de tipo cliente porque cada jugador conserva sus propios
 * ajustes. Las features que afectan al servidor sincronizan su valor mediante
 * payloads y el servidor sigue siendo responsable de la colocacion final.</p>
 */
public final class CreatePipeConnectorClientConfig {
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        PlacementAnimationClientConfig.define(builder);
        ShulkerMaterialClientConfig.define(builder);
        ManualAnchorClientConfig.define(builder);
        SPEC = builder.build();
    }

    /** Impide crear instancias del registro de configuracion. */
    private CreatePipeConnectorClientConfig() {
    }
}
