package com.javiluli.createpipeconnector.feature.style.network;

import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sincroniza el estilo normal o de cristal seleccionado.
 */
public record PipeStyleModePayload(PipeStyleMode mode) {
    /** Decodifica el estilo de tuberia. */
    public static PipeStyleModePayload decode(FriendlyByteBuf buffer) {
        return new PipeStyleModePayload(buffer.readEnum(PipeStyleMode.class));
    }

    /** Codifica el estilo de tuberia. */
    public static void encode(PipeStyleModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.mode());
    }
}
