package com.javiluli.createpipeconnector.network;

import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.network.payload.CopperCasingModePayload;
import com.javiluli.createpipeconnector.network.payload.PipeStyleModePayload;
import com.javiluli.createpipeconnector.network.payload.PumpModePayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastCopperCasingPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastManualPumpPayload;
import com.javiluli.createpipeconnector.network.payload.ReverseAutoPumpDirectionPayload;
import com.javiluli.createpipeconnector.network.payload.RoutePriorityPayload;
import com.javiluli.createpipeconnector.network.payload.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.network.payload.ServerPipeConnectorPayloadHandler;
import com.javiluli.createpipeconnector.network.payload.ToggleAutoPumpsPayload;
import com.javiluli.createpipeconnector.network.payload.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.network.payload.ToggleCopperCasingPayload;
import com.javiluli.createpipeconnector.network.payload.ToggleManualPumpPayload;
import com.javiluli.createpipeconnector.network.payload.WrenchPipeDisplayPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers every NeoForge payload used by connector interactions.
 */
public final class CreatePipeConnectorNetwork {
    private static final String NETWORK_VERSION = "1";

    private CreatePipeConnectorNetwork() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToServer(ToggleConnectorModePayload.TYPE, ToggleConnectorModePayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleToggleConnectorMode);
        registrar.playToServer(PumpModePayload.TYPE, PumpModePayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handlePumpMode);
        registrar.playToServer(CopperCasingModePayload.TYPE, CopperCasingModePayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleCopperCasingMode);
        registrar.playToServer(PipeStyleModePayload.TYPE, PipeStyleModePayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handlePipeStyleMode);
        registrar.playToServer(ToggleAutoPumpsPayload.TYPE, ToggleAutoPumpsPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleToggleAutoPumps);
        registrar.playToServer(ReverseAutoPumpDirectionPayload.TYPE, ReverseAutoPumpDirectionPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleReverseAutoPumpDirection);
        registrar.playToServer(RoutePriorityPayload.TYPE, RoutePriorityPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleRoutePriority);
        registrar.playToServer(SelectPipeTargetPayload.TYPE, SelectPipeTargetPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleSelectPipeTarget);
        registrar.playToServer(CancelPipeConnectionPayload.TYPE, CancelPipeConnectionPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleCancelPipeConnection);
        registrar.playToServer(AddAnchorPayload.TYPE, AddAnchorPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleAddAnchor);
        registrar.playToServer(RemoveLastAnchorPayload.TYPE, RemoveLastAnchorPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleRemoveLastAnchor);
        registrar.playToServer(RemoveLastManualPumpPayload.TYPE, RemoveLastManualPumpPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleRemoveLastManualPump);
        registrar.playToServer(RemoveLastCopperCasingPayload.TYPE, RemoveLastCopperCasingPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleRemoveLastCopperCasing);
        registrar.playToServer(ToggleCopperCasingPayload.TYPE, ToggleCopperCasingPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleToggleCopperCasing);
        registrar.playToServer(ToggleManualPumpPayload.TYPE, ToggleManualPumpPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleToggleManualPump);
        registrar.playToServer(WrenchPipeDisplayPayload.TYPE, WrenchPipeDisplayPayload.STREAM_CODEC, ServerPipeConnectorPayloadHandler::handleWrenchPipeDisplay);
    }
}
