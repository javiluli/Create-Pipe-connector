package com.javiluli.createpipeconnector.network;

import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.ServerPipeConnectorPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class CreatePipeConnectorNetwork {
    private static final String NETWORK_VERSION = "1";

    private CreatePipeConnectorNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToServer(AddAnchorPayload.TYPE, AddAnchorPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleAddAnchor);
        registrar.playToServer(RemoveLastAnchorPayload.TYPE, RemoveLastAnchorPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleRemoveLastAnchor);
    }
}
