package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Cancels the current route while leaving connector mode enabled.
 */
public record CancelPipeConnectionPayload() {
    public static CancelPipeConnectionPayload decode(FriendlyByteBuf buffer) {
        return new CancelPipeConnectionPayload();
    }

    public static void encode(CancelPipeConnectionPayload payload, FriendlyByteBuf buffer) {
    }
}
