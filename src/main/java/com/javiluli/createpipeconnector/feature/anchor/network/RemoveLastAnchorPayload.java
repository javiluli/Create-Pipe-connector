package com.javiluli.createpipeconnector.feature.anchor.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Elimina el ancla anadida mas recientemente.
 */
public record RemoveLastAnchorPayload() {
    /** Decodifica la accion sin datos de eliminacion. */
    public static RemoveLastAnchorPayload decode(FriendlyByteBuf buffer) {
        return new RemoveLastAnchorPayload();
    }

    /** Codifica la accion sin datos de eliminacion. */
    public static void encode(RemoveLastAnchorPayload payload, FriendlyByteBuf buffer) {
    }
}
