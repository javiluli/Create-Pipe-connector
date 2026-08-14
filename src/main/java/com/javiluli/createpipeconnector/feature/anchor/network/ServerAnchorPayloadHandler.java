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
     * Elimina el ancla indicada de la ruta activa.
     *
     * @param payload posicion exacta del ancla que debe retirarse
     * @param contextSupplier contexto de red del paquete
     */
    public static void handleRemoveAnchor(
            RemoveAnchorPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
                ConnectorSessionStore.removeAnchor(player.getUUID(), payload.position());
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

    /**
     * Comprueba ocupacion y compatibilidad del bloque usado como ancla.
     *
     * <p>El alcance no se vuelve a exigir porque el objetivo pudo fijarse cerca
     * y conservarse mediante freecam mientras el jugador se desplaza.</p>
     */
    private static boolean isAnchorValid(
            ServerLevel level,
            Selection selection,
            PlacementTarget anchor
    ) {
        if (selection.position().equals(anchor.position())) {
            return false;
        }
        if (!anchor.existingPipe()) {
            return PipeConnectorLogic.canPlacePipeAt(level, anchor.position());
        }

        BlockState anchorState = level.getBlockState(anchor.position());
        return PipeConnectorLogic.isConnectablePipe(anchorState) && anchorState.getBlock() == selection.pipeBlock();
    }
}
