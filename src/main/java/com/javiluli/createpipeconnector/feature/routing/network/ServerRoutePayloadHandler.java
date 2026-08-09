package com.javiluli.createpipeconnector.feature.routing.network;

import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.server.ServerPipeConnectorEvents;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Aplica prioridad, destino y cancelacion de rutas. */
public final class ServerRoutePayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerRoutePayloadHandler() {
    }

    /** Cambia el criterio de busqueda del jugador. */
    public static void handleRoutePriority(RoutePriorityPayload payload, IPayloadContext context) {
        ConnectorSessionStore.setRoutePriority(context.player().getUUID(), payload.priority());
    }

    /** Valida y entrega un destino seleccionado al flujo del servidor. */
    public static void handleSelectPipeTarget(SelectPipeTargetPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        PlacementTarget target = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
        ServerPipeConnectorEvents.handlePipeTarget(player, serverLevel, target);
    }

    /** Cancela solamente la ruta activa del jugador. */
    public static void handleCancelPipeConnection(CancelPipeConnectionPayload payload, IPayloadContext context) {
        ServerPipeConnectorEvents.cancelPipeConnection(context.player());
    }
}
