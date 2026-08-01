package com.javiluli.createpipeconnector.feature.routing.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Inicia o confirma una ruta mediante la posicion y cara senaladas.
 */
public record SelectPipeTargetPayload(BlockPos position, Direction face, boolean existingPipe) {
    /** Decodifica la posicion, cara y tipo de objetivo. */
    public static SelectPipeTargetPayload decode(FriendlyByteBuf buffer) {
        return new SelectPipeTargetPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
    }

    /** Codifica la posicion, cara y tipo de objetivo. */
    public static void encode(SelectPipeTargetPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
        buffer.writeEnum(payload.face());
        buffer.writeBoolean(payload.existingPipe());
    }
}
