package com.javiluli.createpipeconnector;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.bootstrap.NeoForgeClientBootstrap;
import com.javiluli.createpipeconnector.feature.connector.server.ServerPipeConnectorEvents;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.javiluli.createpipeconnector.platform.network.CreatePipeConnectorNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Punto de entrada NeoForge que registra eventos, configuracion y red.
 */
@Mod(Constants.MOD_ID)
public class CreatePipeConnector {
    /** Inicializa los componentes comunes y exclusivos del cliente. */
    public CreatePipeConnector(IEventBus eventBus, ModContainer modContainer) {
        modContainer.registerConfig(
                ModConfig.Type.CLIENT,
                PlacementAnimationClientConfig.SPEC,
                Constants.MOD_ID + "-client.toml"
        );
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForgeClientBootstrap.register(modContainer);
        }
        eventBus.addListener(CreatePipeConnectorNetwork::register);
        NeoForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onPlayerLoggedOut);
    }
}
