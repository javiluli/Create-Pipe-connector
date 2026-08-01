package com.javiluli.createpipeconnector.feature.casing.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Anade o retira un revestimiento manual en una posicion de la ruta.
 */
public record ToggleCopperCasingPayload(BlockPos position) {
    /** Decodifica la posicion de revestimiento. */
    public static ToggleCopperCasingPayload decode(FriendlyByteBuf buffer) {
        return new ToggleCopperCasingPayload(buffer.readBlockPos());
    }

    /** Codifica la posicion de revestimiento. */
    public static void encode(ToggleCopperCasingPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(payload.position());
    }
}
