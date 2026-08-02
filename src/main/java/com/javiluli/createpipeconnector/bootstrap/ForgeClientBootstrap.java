package com.javiluli.createpipeconnector.bootstrap;

import com.javiluli.createpipeconnector.core.Constants;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/** Registra extensiones de Forge que solo existen en el cliente fisico. */
public final class ForgeClientBootstrap {
    /** Impide crear instancias del bootstrap de cliente. */
    private ForgeClientBootstrap() {
    }

    /** Expone la pantalla de configuracion desde la ficha del mod. */
    public static void register(FMLJavaModLoadingContext loadingContext) {
        loadingContext.registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new BaseConfigScreen(parent, Constants.MOD_ID)
                )
        );
    }
}
