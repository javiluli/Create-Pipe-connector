package com.javiluli.createpipeconnector.feature.anchor.network;

import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.server.ServerConnectorSessionValidator;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Gestiona las anclas de ruta solicitadas por el cliente. */
public final class ServerAnchorPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerAnchorPayloadHandler() {
    }

    /** Anade un ancla si pertenece a una ruta valida. */
    public static void handleAddAnchor(AddAnchorPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (player.level() instanceof ServerLevel level) {
            addValidatedAnchor(player, level, payload);
        }
    }

    /** Elimina el ancla indicada de la ruta activa. */
    public static void handleRemoveAnchor(RemoveAnchorPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
            ConnectorSessionStore.removeAnchor(player.getUUID(), payload.position());
        }
    }

    /** Valida en servidor el ancla antes de incorporarla al recorrido. */
    private static void addValidatedAnchor(Player player, ServerLevel level, AddAnchorPayload payload) {
        Selection selection = ServerConnectorSessionValidator.validatedSelection(player);
        if (selection == null) {
            return;
        }

        PlacementTarget anchor = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
        if (!isAnchorValid(level, selection, anchor)) {
            return;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(
                level,
                selection,
                ConnectorSessionStore.getAnchors(player.getUUID()),
                anchor,
                ConnectorSessionStore.getRoutePriority(player.getUUID())
        );
        if (plan != null) {
            ConnectorSessionStore.addAnchor(player.getUUID(), anchor);
        }
    }

    /** Comprueba ocupacion y compatibilidad del bloque usado como ancla. */
    private static boolean isAnchorValid(ServerLevel level, Selection selection, PlacementTarget anchor) {
        if (selection.position().equals(anchor.position())) {
            return false;
        }
        if (!anchor.existingPipe()) {
            return PipeConnectorLogic.canPlacePipeAt(level, anchor.position());
        }

        BlockState anchorState = level.getBlockState(anchor.position());
        return PipeConnectorLogic.isConnectablePipe(anchorState)
                && anchorState.getBlock() == selection.pipeBlock();
    }
}
