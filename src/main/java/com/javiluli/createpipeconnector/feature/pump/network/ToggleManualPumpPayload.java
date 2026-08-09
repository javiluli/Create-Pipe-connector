package com.javiluli.createpipeconnector.feature.pump.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Anade o retira una bomba manual en una posicion de la ruta.
 */
public record ToggleManualPumpPayload(BlockPos position) {
    /** Decodifica la posicion de bomba manual. */
    public static ToggleManualPumpPayload decode(FriendlyByteBuf buffer) {
        return new ToggleManualPumpPayload(buffer.readBlockPos());
    }

    /** Codifica la posicion de bomba manual. */
    public static void encode(ToggleManualPumpPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
