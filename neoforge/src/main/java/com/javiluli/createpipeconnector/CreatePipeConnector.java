package com.javiluli.createpipeconnector;

import com.javiluli.createpipeconnector.connector.ServerPipeConnectorEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constants.MOD_ID)
public class CreatePipeConnector {
    public CreatePipeConnector(IEventBus eventBus) {
        NeoForge.EVENT_BUS.addListener(ServerPipeConnectorEvents::onRightClickBlock);
    }
}
