package com.javiluli.createpipeconnector.feature.casing.network;

import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sincroniza con el servidor la estrategia de revestimiento seleccionada.
 */
public record CopperCasingModePayload(CopperCasingMode mode) {
    /** Decodifica el modo de revestimiento. */
    public static CopperCasingModePayload decode(FriendlyByteBuf buffer) {
        return new CopperCasingModePayload(buffer.readEnum(CopperCasingMode.class));
    }

    /** Codifica el modo de revestimiento. */
    public static void encode(CopperCasingModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.mode());
    }
}
