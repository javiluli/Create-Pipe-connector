package com.javiluli.createpipeconnector;

import com.javiluli.createpipeconnector.connector.ServerPipeConnectorEvents;
import com.javiluli.createpipeconnector.network.CreatePipeConnectorNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge entrypoint that registers networking and server gameplay events.
 */
@Mod(Constants.MOD_ID)
public final class CreatePipeConnector {
    public CreatePipeConnector() {
        CreatePipeConnectorNetwork.register();
        MinecraftForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onPlayerTick);
    }
}
