package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record RemoveLastManualPumpPayload() {
    public static RemoveLastManualPumpPayload decode(FriendlyByteBuf buffer) {
        return new RemoveLastManualPumpPayload();
    }

    public static void encode(RemoveLastManualPumpPayload payload, FriendlyByteBuf buffer) {
    }
}
