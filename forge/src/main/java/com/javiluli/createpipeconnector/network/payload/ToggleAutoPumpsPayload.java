package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Enables or disables automatic pump placement.
 */
public record ToggleAutoPumpsPayload(boolean enabled) {
    public static ToggleAutoPumpsPayload decode(FriendlyByteBuf buffer) {
        return new ToggleAutoPumpsPayload(buffer.readBoolean());
    }

    public static void encode(ToggleAutoPumpsPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.enabled());
    }
}
