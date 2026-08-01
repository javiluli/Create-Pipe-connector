package com.javiluli.createpipeconnector;

import com.javiluli.createpipeconnector.bootstrap.ForgePayloadRegistry;
import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.connector.server.ServerPipeConnectorEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

/**
 * Punto de entrada de Forge que registra la red y los eventos de juego del servidor.
 */
@Mod(Constants.MOD_ID)
public final class CreatePipeConnector {
    /** Inicializa el canal de red y los eventos comunes del conector. */
    public CreatePipeConnector() {
        ForgePayloadRegistry.register();
        MinecraftForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onPlayerTick);
    }
}
