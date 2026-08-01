package com.javiluli.createpipeconnector.feature.pump.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Elimina la marca manual de bomba mas reciente.
 */
public record RemoveLastManualPumpPayload() {
    /** Decodifica la accion sin datos de eliminacion. */
    public static RemoveLastManualPumpPayload decode(FriendlyByteBuf buffer) {
        return new RemoveLastManualPumpPayload();
    }

    /** Codifica la accion sin datos de eliminacion. */
    public static void encode(RemoveLastManualPumpPayload payload, FriendlyByteBuf buffer) {
    }
}
