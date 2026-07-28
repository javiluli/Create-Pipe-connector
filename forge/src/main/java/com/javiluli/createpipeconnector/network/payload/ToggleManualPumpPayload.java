package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record ToggleManualPumpPayload(BlockPos position) {
    public static ToggleManualPumpPayload decode(FriendlyByteBuf buffer) {
        return new ToggleManualPumpPayload(buffer.readBlockPos());
    }

    public static void encode(ToggleManualPumpPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
