package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.feature.connector.server.ServerConnectorSessionValidator;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Gestiona los modos y marcas manuales de bombas mecanicas. */
public final class ServerPumpPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerPumpPayloadHandler() {
    }

    /** Sincroniza el interruptor booleano heredado de bombas automaticas. */
    public static void handleToggleAutoPumps(
            ToggleAutoPumpsPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier,
                player -> ConnectorSessionStore.setAutoPumpsEnabled(player.getUUID(), payload.enabled()));
    }

    /** Sincroniza la estrategia de separacion de bombas automaticas. */
    public static void handlePumpMode(PumpModePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPayloadContext.enqueue(contextSupplier,
                player -> ConnectorSessionStore.setPumpMode(player.getUUID(), payload.mode()));
    }

    /** Sincroniza la inversion del sentido de las bombas automaticas. */
    public static void handleReverseAutoPumpDirection(
            ReverseAutoPumpDirectionPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier,
                player -> ConnectorSessionStore.setAutoPumpDirectionReversed(player.getUUID(), payload.reversed()));
    }

    /** Alterna una bomba manual en una posicion alcanzable de la ruta. */
    public static void handleToggleManualPump(
            ToggleManualPumpPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            if (ServerConnectorSessionValidator.canModifyRouteAt(player, payload.position())) {
                ConnectorSessionStore.toggleManualPump(player.getUUID(), payload.position());
            }
        });
    }

    /** Retira la bomba manual anadida mas recientemente. */
    public static void handleRemoveLastManualPump(
            RemoveLastManualPumpPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
                ConnectorSessionStore.removeLastManualPump(player.getUUID());
            }
        });
    }
}
