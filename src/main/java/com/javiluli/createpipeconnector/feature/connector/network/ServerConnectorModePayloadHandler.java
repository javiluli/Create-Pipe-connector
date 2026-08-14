package com.javiluli.createpipeconnector.feature.connector.network;

import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Aplica cambios del modo conector en el servidor. */
public final class ServerConnectorModePayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerConnectorModePayloadHandler() {
    }

    /** Activa o desactiva el modo conector del jugador remitente. */
    public static void handleToggleConnectorMode(ToggleConnectorModePayload payload, IPayloadContext context) {
        Player player = context.player();
        ConnectorSessionStore.setConnectorModeEnabled(player.getUUID(), payload.enabled());
    }
}
