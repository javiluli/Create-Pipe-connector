package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

public record SelectPipeTargetPayload(BlockPos position, Direction face, boolean existingPipe) {
    public static SelectPipeTargetPayload decode(FriendlyByteBuf buffer) {
        return new SelectPipeTargetPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
    }

    public static void encode(SelectPipeTargetPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
        buffer.writeEnum(payload.face());
        buffer.writeBoolean(payload.existingPipe());
    }
}
