package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sincroniza la estrategia de separacion de bombas automaticas.
 */
public record PumpModePayload(PumpMode mode) {
    /** Decodifica el modo de bombas. */
    public static PumpModePayload decode(FriendlyByteBuf buffer) {
        return new PumpModePayload(buffer.readEnum(PumpMode.class));
    }

    /** Codifica el modo de bombas. */
    public static void encode(PumpModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.mode());
    }
}
