package com.javiluli.createpipeconnector.feature.connector.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Sincroniza si el jugador tiene activo el modo Pipe Connector.
 */
public record ToggleConnectorModePayload(boolean enabled) {
    /** Decodifica el estado del modo Pipe Connector. */
    public static ToggleConnectorModePayload decode(FriendlyByteBuf buffer) {
        return new ToggleConnectorModePayload(buffer.readBoolean());
    }

    /** Codifica el estado del modo Pipe Connector. */
    public static void encode(ToggleConnectorModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.enabled());
    }
}
