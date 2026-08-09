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

    /** Conserva compatibilidad con el interruptor automatico directo. */
    public static void handleToggleAutoPumps(ToggleAutoPumpsPayload payload, IPayloadContext context) {
        ConnectorSessionStore.setAutoPumpsEnabled(context.player().getUUID(), payload.enabled());
    }

    /** Invierte la direccion de las bombas automaticas. */
    public static void handleReverseAutoPumpDirection(ReverseAutoPumpDirectionPayload payload, IPayloadContext context) {
        ConnectorSessionStore.setAutoPumpDirectionReversed(context.player().getUUID(), payload.reversed());
    }

    /** Alterna una marca manual si pertenece a una seleccion valida y cercana. */
    public static void handleToggleManualPump(ToggleManualPumpPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (ServerConnectorSessionValidator.canModifyRouteAt(player, payload.position())) {
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
