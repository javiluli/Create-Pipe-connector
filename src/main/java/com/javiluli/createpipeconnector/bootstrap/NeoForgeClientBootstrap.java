package com.javiluli.createpipeconnector.bootstrap;

import com.javiluli.createpipeconnector.core.Constants;
import net.createmod.catnip.config.ui.BaseConfigScreen;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Registra extensiones que solo existen en el cliente fisico de NeoForge. */
public final class NeoForgeClientBootstrap {
    /** Impide crear instancias del bootstrap de cliente. */
    private NeoForgeClientBootstrap() {
    }

    /** Expone la pantalla automatica de Catnip desde la ficha del mod. */
    public static void register(ModContainer modContainer) {
        IConfigScreenFactory factory = (container, parent) -> new BaseConfigScreen(parent, Constants.MOD_ID);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, factory);
    }
}
