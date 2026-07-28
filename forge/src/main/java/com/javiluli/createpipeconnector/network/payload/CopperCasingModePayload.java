package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.CopperCasingMode;
import net.minecraft.network.FriendlyByteBuf;

public record CopperCasingModePayload(CopperCasingMode mode) {
    public static CopperCasingModePayload decode(FriendlyByteBuf buffer) {
        return new CopperCasingModePayload(buffer.readEnum(CopperCasingMode.class));
    }

    public static void encode(CopperCasingModePayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.mode());
    }
}
