package com.javiluli.createpipeconnector.feature.routing.network;

import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.server.ServerPipeConnectorEvents;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Gestiona la seleccion, cancelacion y prioridad de las rutas. */
public final class ServerRoutePayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerRoutePayloadHandler() {
    }

    /**
     * Inicia o confirma una ruta utilizando el objetivo validado por el servidor.
     *
     * @param payload objetivo senalado por el jugador
     * @param contextSupplier contexto de red del paquete
     */
    public static void handleSelectPipeTarget(
            SelectPipeTargetPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueueWithLevel(contextSupplier, (player, level) -> {
            PlacementTarget target = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
            ServerPipeConnectorEvents.handlePipeTarget(player, level, target);
        });
    }

    /**
     * Cancela la ruta activa sin desactivar el modo Pipe Connector.
     *
     * @param payload accion de cancelacion sin datos
     * @param contextSupplier contexto de red del paquete
     */
    public static void handleCancelPipeConnection(
            CancelPipeConnectionPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, ServerPipeConnectorEvents::cancelPipeConnection);
    }

    /**
     * Cambia la prioridad de ejes utilizada por el calculo de rutas.
     *
     * @param payload prioridad seleccionada
     * @param contextSupplier contexto de red del paquete
     */
    public static void handleRoutePriority(
            RoutePriorityPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier,
                player -> ConnectorSessionStore.setRoutePriority(player.getUUID(), payload.priority()));
    }
}
