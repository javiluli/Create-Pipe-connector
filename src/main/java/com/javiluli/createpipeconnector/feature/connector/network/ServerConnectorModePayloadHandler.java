package com.javiluli.createpipeconnector.feature.connector.network;

import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Gestiona la activacion remota del modo Pipe Connector. */
public final class ServerConnectorModePayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerConnectorModePayloadHandler() {
    }

    /**
     * Activa o desactiva el modo y limpia la ruta cuando deja de estar disponible.
     *
     * @param payload estado solicitado por el cliente
     * @param contextSupplier contexto de red del paquete
     */
    public static void handleToggleConnectorMode(
            ToggleConnectorModePayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            ConnectorSessionStore.setConnectorModeEnabled(player.getUUID(), payload.enabled());
        });
    }
}
