package com.javiluli.createpipeconnector.feature.routing.network;

import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sincroniza la prioridad de busqueda seleccionada por el jugador.
 */
public record RoutePriorityPayload(RoutePriority priority) {
    /** Decodifica la prioridad de ruta. */
    public static RoutePriorityPayload decode(FriendlyByteBuf buffer) {
        return new RoutePriorityPayload(buffer.readEnum(RoutePriority.class));
    }

    /** Codifica la prioridad de ruta. */
    public static void encode(RoutePriorityPayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.priority());
    }
}
