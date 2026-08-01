package com.javiluli.createpipeconnector.feature.routing.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Cancela la ruta actual sin desactivar el modo conector.
 */
public record CancelPipeConnectionPayload() {
    /** Decodifica la accion sin datos de cancelacion. */
    public static CancelPipeConnectionPayload decode(FriendlyByteBuf buffer) {
        return new CancelPipeConnectionPayload();
    }

    /** Codifica la accion sin datos de cancelacion. */
    public static void encode(CancelPipeConnectionPayload payload, FriendlyByteBuf buffer) {
    }
}
