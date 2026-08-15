package com.javiluli.createpipeconnector.feature.anchor.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/** Elimina un ancla concreta de la ruta activa. */
public record RemoveAnchorPayload(BlockPos position) {
    /** Decodifica la posicion del ancla que debe retirarse. */
    public static RemoveAnchorPayload decode(FriendlyByteBuf buffer) {
        return new RemoveAnchorPayload(buffer.readBlockPos());
    }

    /** Codifica la posicion del ancla que debe retirarse. */
    public static void encode(RemoveAnchorPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
