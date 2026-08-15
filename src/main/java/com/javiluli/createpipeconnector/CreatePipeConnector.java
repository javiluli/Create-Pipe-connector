package com.javiluli.createpipeconnector;

import com.javiluli.createpipeconnector.bootstrap.ForgePayloadRegistry;
import com.javiluli.createpipeconnector.bootstrap.ForgeClientBootstrap;
import com.javiluli.createpipeconnector.config.CreatePipeConnectorClientConfig;
import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.connector.server.ServerPipeConnectorEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * Punto de entrada de Forge que registra la red y los eventos de juego del servidor.
 */
@Mod(Constants.MOD_ID)
public final class CreatePipeConnector {
    /** Inicializa el canal de red y los eventos comunes del conector. */
    public CreatePipeConnector(FMLJavaModLoadingContext loadingContext) {
        loadingContext.registerConfig(
                ModConfig.Type.CLIENT,
                CreatePipeConnectorClientConfig.SPEC,
                Constants.MOD_ID + "-client.toml"
        );
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ForgeClientBootstrap.register(loadingContext));
        ForgePayloadRegistry.register();
        MinecraftForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onPlayerTick);
        MinecraftForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onPlayerLoggedOut);
    }
}
