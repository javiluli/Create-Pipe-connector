package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Removes the most recently added route anchor.
 */
public record RemoveLastAnchorPayload() {
    public static RemoveLastAnchorPayload decode(FriendlyByteBuf buffer) {
        return new RemoveLastAnchorPayload();
    }

    public static void encode(RemoveLastAnchorPayload payload, FriendlyByteBuf buffer) {
    }
}
