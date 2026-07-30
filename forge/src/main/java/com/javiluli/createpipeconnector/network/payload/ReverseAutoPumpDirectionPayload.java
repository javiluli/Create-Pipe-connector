package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Synchronizes whether automatically placed pumps face in reverse.
 */
public record ReverseAutoPumpDirectionPayload(boolean reversed) {
    public static ReverseAutoPumpDirectionPayload decode(FriendlyByteBuf buffer) {
        return new ReverseAutoPumpDirectionPayload(buffer.readBoolean());
    }

    public static void encode(ReverseAutoPumpDirectionPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.reversed());
    }
}
