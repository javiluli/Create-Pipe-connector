package com.javiluli.createpipeconnector.bootstrap;

import com.javiluli.createpipeconnector.feature.anchor.network.AddAnchorPayload;
import com.javiluli.createpipeconnector.feature.anchor.network.RemoveAnchorPayload;
import com.javiluli.createpipeconnector.feature.anchor.network.ServerAnchorPayloadHandler;
import com.javiluli.createpipeconnector.feature.casing.network.CopperCasingModePayload;
import com.javiluli.createpipeconnector.feature.casing.network.RemoveLastCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.casing.network.ServerCasingPayloadHandler;
import com.javiluli.createpipeconnector.feature.casing.network.ToggleCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.connector.network.ServerConnectorModePayloadHandler;
import com.javiluli.createpipeconnector.feature.connector.network.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.feature.pump.network.PumpModePayload;
import com.javiluli.createpipeconnector.feature.pump.network.RemoveLastManualPumpPayload;
import com.javiluli.createpipeconnector.feature.pump.network.PumpDirectionPayload;
import com.javiluli.createpipeconnector.feature.pump.network.ServerPumpPayloadHandler;
import com.javiluli.createpipeconnector.feature.pump.network.ToggleManualPumpPayload;
import com.javiluli.createpipeconnector.feature.placement.network.PlacementAnimationSettingsPayload;
import com.javiluli.createpipeconnector.feature.placement.network.ServerPlacementAnimationPayloadHandler;
import com.javiluli.createpipeconnector.feature.material.shulker.network.ServerShulkerMaterialPayloadHandler;
import com.javiluli.createpipeconnector.feature.material.shulker.network.ShulkerMaterialSettingsPayload;
import com.javiluli.createpipeconnector.feature.routing.network.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.feature.routing.network.RoutePriorityPayload;
import com.javiluli.createpipeconnector.feature.routing.network.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.feature.routing.network.ServerRoutePayloadHandler;
import com.javiluli.createpipeconnector.feature.style.network.PipeStyleModePayload;
import com.javiluli.createpipeconnector.feature.style.network.ServerPipeStylePayloadHandler;
import com.javiluli.createpipeconnector.feature.style.network.WrenchPipeDisplayPayload;
import com.javiluli.createpipeconnector.platform.network.CreatePipeConnectorNetwork;

/** Registra en el canal Forge los payloads aportados por cada feature. */
public final class ForgePayloadRegistry {
    /** Impide crear instancias del bootstrap. */
    private ForgePayloadRegistry() {
    }

    /** Registra los payloads en el mismo orden utilizado por el protocolo actual. */
    public static void register() {
        CreatePipeConnectorNetwork.registerMessage(
                AddAnchorPayload.class,
                AddAnchorPayload::encode,
                AddAnchorPayload::decode,
                ServerAnchorPayloadHandler::handleAddAnchor
        );
        CreatePipeConnectorNetwork.registerMessage(
                RemoveAnchorPayload.class,
                RemoveAnchorPayload::encode,
                RemoveAnchorPayload::decode,
                ServerAnchorPayloadHandler::handleRemoveAnchor
        );
        CreatePipeConnectorNetwork.registerMessage(
                ToggleConnectorModePayload.class,
                ToggleConnectorModePayload::encode,
                ToggleConnectorModePayload::decode,
                ServerConnectorModePayloadHandler::handleToggleConnectorMode
        );
        CreatePipeConnectorNetwork.registerMessage(
                PumpModePayload.class,
                PumpModePayload::encode,
                PumpModePayload::decode,
                ServerPumpPayloadHandler::handlePumpMode
        );
        CreatePipeConnectorNetwork.registerMessage(
                CopperCasingModePayload.class,
                CopperCasingModePayload::encode,
                CopperCasingModePayload::decode,
                ServerCasingPayloadHandler::handleCopperCasingMode
        );
        CreatePipeConnectorNetwork.registerMessage(
                PipeStyleModePayload.class,
                PipeStyleModePayload::encode,
                PipeStyleModePayload::decode,
                ServerPipeStylePayloadHandler::handlePipeStyleMode
        );
        CreatePipeConnectorNetwork.registerMessage(
                PumpDirectionPayload.class,
                PumpDirectionPayload::encode,
                PumpDirectionPayload::decode,
                ServerPumpPayloadHandler::handlePumpDirection
        );
        CreatePipeConnectorNetwork.registerMessage(
                RoutePriorityPayload.class,
                RoutePriorityPayload::encode,
                RoutePriorityPayload::decode,
                ServerRoutePayloadHandler::handleRoutePriority
        );
        CreatePipeConnectorNetwork.registerMessage(
                SelectPipeTargetPayload.class,
                SelectPipeTargetPayload::encode,
                SelectPipeTargetPayload::decode,
                ServerRoutePayloadHandler::handleSelectPipeTarget
        );
        CreatePipeConnectorNetwork.registerMessage(
                CancelPipeConnectionPayload.class,
                CancelPipeConnectionPayload::encode,
                CancelPipeConnectionPayload::decode,
                ServerRoutePayloadHandler::handleCancelPipeConnection
        );
        CreatePipeConnectorNetwork.registerMessage(
                RemoveLastManualPumpPayload.class,
                RemoveLastManualPumpPayload::encode,
                RemoveLastManualPumpPayload::decode,
                ServerPumpPayloadHandler::handleRemoveLastManualPump
        );
        CreatePipeConnectorNetwork.registerMessage(
                RemoveLastCopperCasingPayload.class,
                RemoveLastCopperCasingPayload::encode,
                RemoveLastCopperCasingPayload::decode,
                ServerCasingPayloadHandler::handleRemoveLastCopperCasing
        );
        CreatePipeConnectorNetwork.registerMessage(
                ToggleCopperCasingPayload.class,
                ToggleCopperCasingPayload::encode,
                ToggleCopperCasingPayload::decode,
                ServerCasingPayloadHandler::handleToggleCopperCasing
        );
        CreatePipeConnectorNetwork.registerMessage(
                ToggleManualPumpPayload.class,
                ToggleManualPumpPayload::encode,
                ToggleManualPumpPayload::decode,
                ServerPumpPayloadHandler::handleToggleManualPump
        );
        CreatePipeConnectorNetwork.registerMessage(
                WrenchPipeDisplayPayload.class,
                WrenchPipeDisplayPayload::encode,
                WrenchPipeDisplayPayload::decode,
                ServerPipeStylePayloadHandler::handleWrenchPipeDisplay
        );
        CreatePipeConnectorNetwork.registerMessage(
                PlacementAnimationSettingsPayload.class,
                PlacementAnimationSettingsPayload::encode,
                PlacementAnimationSettingsPayload::decode,
                ServerPlacementAnimationPayloadHandler::handleSettings
        );
        CreatePipeConnectorNetwork.registerMessage(
                ShulkerMaterialSettingsPayload.class,
                ShulkerMaterialSettingsPayload::encode,
                ShulkerMaterialSettingsPayload::decode,
                ServerShulkerMaterialPayloadHandler::handleSettings
        );
    }
}
