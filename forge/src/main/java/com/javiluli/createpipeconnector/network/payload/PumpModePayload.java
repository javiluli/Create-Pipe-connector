package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PumpMode;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Synchronizes the automatic pump spacing strategy.
 */
public record PumpModePayload(PumpMode mode) {
    public static PumpModePayload decode(FriendlyByteBuf buffer) {
        return new PumpModePayload(buffer.readEnum(PumpMode.class));
    }

    public static void encode(PumpModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.mode());
    }
}
