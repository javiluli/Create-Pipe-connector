package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.feature.connector.server.ServerConnectorSessionValidator;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Aplica opciones automaticas y marcas manuales de bombas. */
public final class ServerPumpPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerPumpPayloadHandler() {
    }

    /** Cambia el modo de espaciado automatico. */
    public static void handlePumpMode(PumpModePayload payload, IPayloadContext context) {
        ConnectorSessionStore.setPumpMode(context.player().getUUID(), payload.mode());
    }

    /** Sincroniza la inversion del sentido de todas las bombas de la ruta. */
    public static void handlePumpDirection(PumpDirectionPayload payload, IPayloadContext context) {
        ConnectorSessionStore.setPumpDirectionReversed(context.player().getUUID(), payload.reversed());
    }

    /** Alterna una marca manual si pertenece a una seleccion valida y cercana. */
    public static void handleToggleManualPump(ToggleManualPumpPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (ServerConnectorSessionValidator.canModifyRoute(player)) {
            ConnectorSessionStore.toggleManualPump(player.getUUID(), payload.position());
        }
    }

    /** Quita la ultima marca manual valida. */
    public static void handleRemoveLastManualPump(RemoveLastManualPumpPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
            ConnectorSessionStore.removeLastManualPump(player.getUUID());
        }
    }
}
