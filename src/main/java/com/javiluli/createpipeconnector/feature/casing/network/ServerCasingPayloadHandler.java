package com.javiluli.createpipeconnector.feature.casing.network;

import com.javiluli.createpipeconnector.feature.connector.server.ServerConnectorSessionValidator;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Aplica modos y marcas manuales de revestimiento de cobre. */
public final class ServerCasingPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerCasingPayloadHandler() {
    }

    /** Cambia el modo global de revestimiento para la ruta. */
    public static void handleCopperCasingMode(CopperCasingModePayload payload, IPayloadContext context) {
        ConnectorSessionStore.setCopperCasingMode(context.player().getUUID(), payload.mode());
    }

    /** Alterna una marca de revestimiento cercana. */
    public static void handleToggleCopperCasing(ToggleCopperCasingPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (ServerConnectorSessionValidator.canModifyRouteAt(player, payload.position())) {
            ConnectorSessionStore.toggleCopperCasing(player.getUUID(), payload.position());
        }
    }

    /** Quita la ultima marca de revestimiento valida. */
    public static void handleRemoveLastCopperCasing(RemoveLastCopperCasingPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (ServerConnectorSessionValidator.validatedSelection(player) != null) {
            ConnectorSessionStore.removeLastCopperCasing(player.getUUID());
        }
    }
}
