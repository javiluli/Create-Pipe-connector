package com.javiluli.createpipeconnector.platform.network;

import com.javiluli.createpipeconnector.feature.anchor.network.AddAnchorPayload;
import com.javiluli.createpipeconnector.feature.anchor.network.ServerAnchorPayloadHandler;
import com.javiluli.createpipeconnector.feature.routing.network.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.feature.casing.network.CopperCasingModePayload;
import com.javiluli.createpipeconnector.feature.casing.network.ServerCasingPayloadHandler;
import com.javiluli.createpipeconnector.feature.style.network.PipeStyleModePayload;
import com.javiluli.createpipeconnector.feature.style.network.ServerPipeStylePayloadHandler;
import com.javiluli.createpipeconnector.feature.pump.network.PumpModePayload;
import com.javiluli.createpipeconnector.feature.pump.network.ServerPumpPayloadHandler;
import com.javiluli.createpipeconnector.feature.anchor.network.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.feature.casing.network.RemoveLastCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.pump.network.RemoveLastManualPumpPayload;
import com.javiluli.createpipeconnector.feature.pump.network.ReverseAutoPumpDirectionPayload;
import com.javiluli.createpipeconnector.feature.routing.network.RoutePriorityPayload;
import com.javiluli.createpipeconnector.feature.routing.network.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.feature.routing.network.ServerRoutePayloadHandler;
import com.javiluli.createpipeconnector.feature.pump.network.ToggleAutoPumpsPayload;
import com.javiluli.createpipeconnector.feature.connector.network.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.feature.connector.network.ServerConnectorModePayloadHandler;
import com.javiluli.createpipeconnector.feature.casing.network.ToggleCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.pump.network.ToggleManualPumpPayload;
import com.javiluli.createpipeconnector.feature.style.network.WrenchPipeDisplayPayload;
import com.javiluli.createpipeconnector.feature.placement.network.PlacementAnimationSettingsPayload;
import com.javiluli.createpipeconnector.feature.placement.network.ServerPlacementAnimationPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registra los payloads NeoForge usados por las interacciones del conector.
 */
public final class CreatePipeConnectorNetwork {
    private static final String NETWORK_VERSION = "1";

    /** Impide crear instancias del canal global. */
    private CreatePipeConnectorNetwork() {
    }

    /** Asocia cada payload de cliente con su manejador autoritativo. */
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION);
        registrar.playToServer(ToggleConnectorModePayload.TYPE, ToggleConnectorModePayload.STREAM_CODEC, ServerConnectorModePayloadHandler::handleToggleConnectorMode);
        registrar.playToServer(PumpModePayload.TYPE, PumpModePayload.STREAM_CODEC, ServerPumpPayloadHandler::handlePumpMode);
        registrar.playToServer(CopperCasingModePayload.TYPE, CopperCasingModePayload.STREAM_CODEC, ServerCasingPayloadHandler::handleCopperCasingMode);
        registrar.playToServer(PipeStyleModePayload.TYPE, PipeStyleModePayload.STREAM_CODEC, ServerPipeStylePayloadHandler::handlePipeStyleMode);
        registrar.playToServer(ToggleAutoPumpsPayload.TYPE, ToggleAutoPumpsPayload.STREAM_CODEC, ServerPumpPayloadHandler::handleToggleAutoPumps);
        registrar.playToServer(ReverseAutoPumpDirectionPayload.TYPE, ReverseAutoPumpDirectionPayload.STREAM_CODEC, ServerPumpPayloadHandler::handleReverseAutoPumpDirection);
        registrar.playToServer(RoutePriorityPayload.TYPE, RoutePriorityPayload.STREAM_CODEC, ServerRoutePayloadHandler::handleRoutePriority);
        registrar.playToServer(SelectPipeTargetPayload.TYPE, SelectPipeTargetPayload.STREAM_CODEC, ServerRoutePayloadHandler::handleSelectPipeTarget);
        registrar.playToServer(CancelPipeConnectionPayload.TYPE, CancelPipeConnectionPayload.STREAM_CODEC, ServerRoutePayloadHandler::handleCancelPipeConnection);
        registrar.playToServer(AddAnchorPayload.TYPE, AddAnchorPayload.STREAM_CODEC, ServerAnchorPayloadHandler::handleAddAnchor);
        registrar.playToServer(RemoveLastAnchorPayload.TYPE, RemoveLastAnchorPayload.STREAM_CODEC, ServerAnchorPayloadHandler::handleRemoveLastAnchor);
        registrar.playToServer(RemoveLastManualPumpPayload.TYPE, RemoveLastManualPumpPayload.STREAM_CODEC, ServerPumpPayloadHandler::handleRemoveLastManualPump);
        registrar.playToServer(RemoveLastCopperCasingPayload.TYPE, RemoveLastCopperCasingPayload.STREAM_CODEC, ServerCasingPayloadHandler::handleRemoveLastCopperCasing);
        registrar.playToServer(ToggleCopperCasingPayload.TYPE, ToggleCopperCasingPayload.STREAM_CODEC, ServerCasingPayloadHandler::handleToggleCopperCasing);
        registrar.playToServer(ToggleManualPumpPayload.TYPE, ToggleManualPumpPayload.STREAM_CODEC, ServerPumpPayloadHandler::handleToggleManualPump);
        registrar.playToServer(WrenchPipeDisplayPayload.TYPE, WrenchPipeDisplayPayload.STREAM_CODEC, ServerPipeStylePayloadHandler::handleWrenchPipeDisplay);
        registrar.playToServer(
                PlacementAnimationSettingsPayload.TYPE,
                PlacementAnimationSettingsPayload.STREAM_CODEC,
                ServerPlacementAnimationPayloadHandler::handleSettings
        );
    }
}

