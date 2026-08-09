package com.javiluli.createpipeconnector.feature.anchor.network;

import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.server.ServerConnectorSessionValidator;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Valida la creacion y retirada de anclas de ruta. */
public final class ServerAnchorPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerAnchorPayloadHandler() {
    }

    /** Agrega el ancla solo si produce un plan valido. */
    public static void handleAddAnchor(AddAnchorPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Selection selection = ServerConnectorSessionValidator.validatedSelection(player);
        if (selection == null) {
            return;
        }

        PlacementTarget anchor = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
        if (!ServerConnectorSessionValidator.isAnchorValid(player, serverLevel, selection, anchor)) {
            return;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(
                serverLevel,
                selection,
                ConnectorSessionStore.getAnchors(player.getUUID()),
                anchor,
                ConnectorSessionStore.getRoutePriority(player.getUUID())
        );
        if (plan != null) {
            ConnectorSessionStore.addAnchor(player.getUUID(), anchor);
        }
    }

    /** Quita la ultima ancla de una seleccion valida. */
    public static void handleRemoveLastAnchor(RemoveLastAnchorPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
            ConnectorSessionStore.removeLastAnchor(player.getUUID());
        }
    }
}

