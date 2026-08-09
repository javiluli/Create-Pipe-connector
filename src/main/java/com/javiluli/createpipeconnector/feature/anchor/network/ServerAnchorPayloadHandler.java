package com.javiluli.createpipeconnector.feature.anchor.network;

import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.feature.connector.server.ServerConnectorSessionValidator;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Gestiona las anclas de ruta solicitadas por el cliente. */
public final class ServerAnchorPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerAnchorPayloadHandler() {
    }

    /**
     * Anade un ancla si pertenece a una ruta valida y esta al alcance del jugador.
     *
     * @param payload posicion y cara propuestas para el ancla
     * @param contextSupplier contexto de red del paquete
     */
    public static void handleAddAnchor(AddAnchorPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPayloadContext.enqueueWithLevel(contextSupplier,
                (player, level) -> addValidatedAnchor(player, level, payload));
    }

    /**
     * Elimina la ultima ancla de la ruta activa.
     *
     * @param payload accion de eliminacion sin datos
     * @param contextSupplier contexto de red del paquete
     */
    public static void handleRemoveLastAnchor(
            RemoveLastAnchorPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
                ConnectorSessionStore.removeLastAnchor(player.getUUID());
            }
        });
    }

    /** Valida en servidor el ancla antes de incorporarla al recorrido. */
    private static void addValidatedAnchor(ServerPlayer player, ServerLevel level, AddAnchorPayload payload) {
        Selection selection = ServerConnectorSessionValidator.validatedSelection(player);
        if (selection == null) {
            return;
        }

        PlacementTarget anchor = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
        if (!isAnchorValid(player, level, selection, anchor)) {
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

    /** Comprueba alcance, ocupacion y compatibilidad del bloque usado como ancla. */
    private static boolean isAnchorValid(
            ServerPlayer player,
            ServerLevel level,
            Selection selection,
            PlacementTarget anchor
    ) {
        if (selection.position().equals(anchor.position())
                || !PipeConnectorLogic.isWithinInteractionRange(player, anchor.position())) {
            return false;
        }
        if (!anchor.existingPipe()) {
            return PipeConnectorLogic.canPlacePipeAt(level, anchor.position());
        }

        BlockState anchorState = level.getBlockState(anchor.position());
        return PipeConnectorLogic.isConnectablePipe(anchorState) && anchorState.getBlock() == selection.pipeBlock();
    }
}
