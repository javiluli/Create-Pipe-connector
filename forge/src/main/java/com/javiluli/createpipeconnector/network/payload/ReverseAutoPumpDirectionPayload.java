package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record ReverseAutoPumpDirectionPayload(boolean reversed) {
    public static ReverseAutoPumpDirectionPayload decode(FriendlyByteBuf buffer) {
        return new ReverseAutoPumpDirectionPayload(buffer.readBoolean());
    }

    public static void encode(ReverseAutoPumpDirectionPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.reversed());
    }
}
