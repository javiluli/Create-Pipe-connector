package com.javiluli.createpipeconnector.feature.pump.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Activa o desactiva la colocacion automatica de bombas.
 */
public record ToggleAutoPumpsPayload(boolean enabled) {
    /** Decodifica el estado de bombas automaticas. */
    public static ToggleAutoPumpsPayload decode(FriendlyByteBuf buffer) {
        return new ToggleAutoPumpsPayload(buffer.readBoolean());
    }

    /** Codifica el estado de bombas automaticas. */
    public static void encode(ToggleAutoPumpsPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.enabled());
    }
}
