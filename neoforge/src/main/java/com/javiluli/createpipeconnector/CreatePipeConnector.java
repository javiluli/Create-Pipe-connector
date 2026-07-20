package com.javiluli.createpipeconnector;

import com.javiluli.createpipeconnector.connector.ServerPipeConnectorEvents;
import com.javiluli.createpipeconnector.network.CreatePipeConnectorNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class CreatePipeConnector {
    public CreatePipeConnector(IEventBus eventBus) {
        eventBus.addListener(CreatePipeConnectorNetwork::register);
        NeoForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onPlayerTick);
    }
}
