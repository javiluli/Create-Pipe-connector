package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record RemoveLastAnchorPayload() {
    public static RemoveLastAnchorPayload decode(FriendlyByteBuf buffer) {
        return new RemoveLastAnchorPayload();
    }

    public static void encode(RemoveLastAnchorPayload payload, FriendlyByteBuf buffer) {
    }
}
