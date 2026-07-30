package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeStyleMode;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Synchronizes the selected regular or glass pipe style.
 */
public record PipeStyleModePayload(PipeStyleMode mode) {
    public static PipeStyleModePayload decode(FriendlyByteBuf buffer) {
        return new PipeStyleModePayload(buffer.readEnum(PipeStyleMode.class));
    }

    public static void encode(PipeStyleModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.mode());
    }
}
