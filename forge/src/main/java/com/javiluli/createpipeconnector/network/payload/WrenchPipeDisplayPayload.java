package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Requests a double-wrench display toggle for the targeted pipe segment.
 */
public record WrenchPipeDisplayPayload(BlockPos position) {
    public static WrenchPipeDisplayPayload decode(FriendlyByteBuf buffer) {
        return new WrenchPipeDisplayPayload(buffer.readBlockPos());
    }

    public static void encode(WrenchPipeDisplayPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
