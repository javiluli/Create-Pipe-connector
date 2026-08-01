package com.javiluli.createpipeconnector.feature.casing.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Elimina la marca manual de revestimiento mas reciente.
 */
public record RemoveLastCopperCasingPayload() {
    /** Decodifica la accion sin datos de eliminacion. */
    public static RemoveLastCopperCasingPayload decode(FriendlyByteBuf buffer) {
        return new RemoveLastCopperCasingPayload();
    }

    /** Codifica la accion sin datos de eliminacion. */
    public static void encode(RemoveLastCopperCasingPayload payload, FriendlyByteBuf buffer) {
    }
}
