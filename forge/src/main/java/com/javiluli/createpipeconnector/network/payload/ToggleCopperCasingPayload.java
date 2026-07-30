package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Adds or removes a manual casing marker at a route position.
 */
public record ToggleCopperCasingPayload(BlockPos position) {
    public static ToggleCopperCasingPayload decode(FriendlyByteBuf buffer) {
        return new ToggleCopperCasingPayload(buffer.readBlockPos());
    }

    public static void encode(ToggleCopperCasingPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
