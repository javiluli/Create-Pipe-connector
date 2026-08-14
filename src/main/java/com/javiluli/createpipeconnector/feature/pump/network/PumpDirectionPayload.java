package com.javiluli.createpipeconnector.feature.pump.network;

import net.minecraft.network.FriendlyByteBuf;

/** Sincroniza si las bombas de la ruta deben orientarse al reves. */
public record PumpDirectionPayload(boolean reversed) {
    /** Decodifica el estado de inversion. */
    public static PumpDirectionPayload decode(FriendlyByteBuf buffer) {
        return new PumpDirectionPayload(buffer.readBoolean());
    }

    /** Codifica el estado de inversion. */
    public static void encode(PumpDirectionPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.reversed());
    }
}
