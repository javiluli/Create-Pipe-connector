package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record ToggleConnectorModePayload(boolean enabled) {
    public static ToggleConnectorModePayload decode(FriendlyByteBuf buffer) {
        return new ToggleConnectorModePayload(buffer.readBoolean());
    }

    public static void encode(ToggleConnectorModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.enabled());
    }
}
