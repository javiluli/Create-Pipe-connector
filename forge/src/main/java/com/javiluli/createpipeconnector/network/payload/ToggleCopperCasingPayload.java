package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record ToggleCopperCasingPayload(BlockPos position) {
    public static ToggleCopperCasingPayload decode(FriendlyByteBuf buffer) {
        return new ToggleCopperCasingPayload(buffer.readBlockPos());
    }

    public static void encode(ToggleCopperCasingPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
