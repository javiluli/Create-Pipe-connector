package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Requests that the server append an anchor to the active route.
 */
public record AddAnchorPayload(BlockPos position, Direction face, boolean existingPipe) {
    public static AddAnchorPayload decode(FriendlyByteBuf buffer) {
        return new AddAnchorPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
    }

    public static void encode(AddAnchorPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
        buffer.writeEnum(payload.face());
        buffer.writeBoolean(payload.existingPipe());
    }
}
