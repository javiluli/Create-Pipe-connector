package com.javiluli.createpipeconnector.feature.anchor.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Solicita al servidor anadir un ancla a la ruta activa.
 */
public record AddAnchorPayload(BlockPos position, Direction face, boolean existingPipe) {
    /** Decodifica la posicion, cara y tipo de extremo del ancla. */
    public static AddAnchorPayload decode(FriendlyByteBuf buffer) {
        return new AddAnchorPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
    }

    /** Codifica los datos del ancla en el bufer de red. */
    public static void encode(AddAnchorPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
        buffer.writeEnum(payload.face());
        buffer.writeBoolean(payload.existingPipe());
    }
}
