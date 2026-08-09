package com.javiluli.createpipeconnector.feature.style.network;

import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.feature.connector.server.ServerPipeConnectorEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Aplica estilos de ruta y acciones de la llave de Create. */
public final class ServerPipeStylePayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerPipeStylePayloadHandler() {
    }

    /** Cambia el estilo de las tuberias planificadas. */
    public static void handlePipeStyleMode(PipeStyleModePayload payload, IPayloadContext context) {
        ConnectorSessionStore.setPipeStyleMode(context.player().getUUID(), payload.mode());
    }

    /** Delega el doble clic de llave en el controlador autoritativo. */
    public static void handleWrenchPipeDisplay(WrenchPipeDisplayPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (player.level() instanceof ServerLevel serverLevel) {
            ServerPipeConnectorEvents.handleWrenchPipeDisplayClick(player, serverLevel, payload.position());
        }
    }
}
