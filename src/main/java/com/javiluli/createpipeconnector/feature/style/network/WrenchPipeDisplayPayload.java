package com.javiluli.createpipeconnector.feature.style.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Solicita alternar el aspecto del tramo senalado mediante doble clic con la llave.
 */
public record WrenchPipeDisplayPayload(BlockPos position) {
    /** Decodifica la posicion utilizada con la llave. */
    public static WrenchPipeDisplayPayload decode(FriendlyByteBuf buffer) {
        return new WrenchPipeDisplayPayload(buffer.readBlockPos());
    }

    /** Codifica la posicion utilizada con la llave. */
    public static void encode(WrenchPipeDisplayPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
