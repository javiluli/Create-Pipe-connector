package com.javiluli.createpipeconnector.feature.style.network;

import com.javiluli.createpipeconnector.feature.connector.server.ServerPipeConnectorEvents;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Gestiona el estilo de ruta y las acciones remotas de la llave de Create. */
public final class ServerPipeStylePayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerPipeStylePayloadHandler() {
    }

    /** Sincroniza el estilo visual seleccionado para las tuberias. */
    public static void handlePipeStyleMode(
            PipeStyleModePayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier,
                player -> ConnectorSessionStore.setPipeStyleMode(player.getUUID(), payload.mode()));
    }

    /** Procesa el doble clic que alterna el aspecto de un tramo de tuberias. */
    public static void handleWrenchPipeDisplay(
            WrenchPipeDisplayPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueueWithLevel(contextSupplier,
                (player, level) -> ServerPipeConnectorEvents.handleWrenchPipeDisplayClick(
                        player,
                        level,
                        payload.position()
                ));
    }
}
