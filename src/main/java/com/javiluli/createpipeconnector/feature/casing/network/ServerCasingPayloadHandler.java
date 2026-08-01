package com.javiluli.createpipeconnector.feature.casing.network;

import com.javiluli.createpipeconnector.feature.connector.server.ServerConnectorSessionValidator;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Gestiona el modo y las marcas manuales de revestimiento de cobre. */
public final class ServerCasingPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerCasingPayloadHandler() {
    }

    /** Sincroniza la estrategia de revestimiento seleccionada. */
    public static void handleCopperCasingMode(
            CopperCasingModePayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier,
                player -> ConnectorSessionStore.setCopperCasingMode(player.getUUID(), payload.mode()));
    }

    /** Alterna un revestimiento manual en una posicion alcanzable de la ruta. */
    public static void handleToggleCopperCasing(
            ToggleCopperCasingPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            if (ServerConnectorSessionValidator.canModifyRouteAt(player, payload.position())) {
                ConnectorSessionStore.toggleCopperCasing(player.getUUID(), payload.position());
            }
        });
    }

    /** Retira el revestimiento manual anadido mas recientemente. */
    public static void handleRemoveLastCopperCasing(
            RemoveLastCopperCasingPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
                ConnectorSessionStore.removeLastCopperCasing(player.getUUID());
            }
        });
    }
}
