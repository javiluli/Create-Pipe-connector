package com.javiluli.createpipeconnector.network;

import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.network.payload.ServerPipeConnectorPayloadHandler;
import com.javiluli.createpipeconnector.network.payload.ToggleConnectorModePayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class CreatePipeConnectorNetwork {
    private static final String NETWORK_VERSION = "1";

    private CreatePipeConnectorNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToServer(ToggleConnectorModePayload.TYPE, ToggleConnectorModePayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleToggleConnectorMode);
        registrar.playToServer(SelectPipeTargetPayload.TYPE, SelectPipeTargetPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleSelectPipeTarget);
        registrar.playToServer(CancelPipeConnectionPayload.TYPE, CancelPipeConnectionPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleCancelPipeConnection);
        registrar.playToServer(AddAnchorPayload.TYPE, AddAnchorPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleAddAnchor);
        registrar.playToServer(RemoveLastAnchorPayload.TYPE, RemoveLastAnchorPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleRemoveLastAnchor);
    }
}
