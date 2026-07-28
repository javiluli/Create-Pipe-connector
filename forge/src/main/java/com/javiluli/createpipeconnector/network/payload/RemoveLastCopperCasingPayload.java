package com.javiluli.createpipeconnector.network.payload;

import net.minecraft.network.FriendlyByteBuf;

public record RemoveLastCopperCasingPayload() {
    public static RemoveLastCopperCasingPayload decode(FriendlyByteBuf buffer) {
        return new RemoveLastCopperCasingPayload();
    }

    public static void encode(RemoveLastCopperCasingPayload payload, FriendlyByteBuf buffer) {
    }
}
