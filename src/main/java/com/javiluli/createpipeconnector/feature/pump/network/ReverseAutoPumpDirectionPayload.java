package com.javiluli.createpipeconnector.feature.pump.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Sincroniza si las bombas automaticas deben orientarse al reves.
 */
public record ReverseAutoPumpDirectionPayload(boolean reversed) {
    /** Decodifica el estado de inversion. */
    public static ReverseAutoPumpDirectionPayload decode(FriendlyByteBuf buffer) {
        return new ReverseAutoPumpDirectionPayload(buffer.readBoolean());
    }

    /** Codifica el estado de inversion. */
    public static void encode(ReverseAutoPumpDirectionPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.reversed());
    }
}
