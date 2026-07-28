package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.RoutePriority;
import net.minecraft.network.FriendlyByteBuf;

public record RoutePriorityPayload(RoutePriority priority) {
    public static RoutePriorityPayload decode(FriendlyByteBuf buffer) {
        return new RoutePriorityPayload(buffer.readEnum(RoutePriority.class));
    }

    public static void encode(RoutePriorityPayload payload, FriendlyByteBuf buffer) {
        buffer.writeEnum(payload.priority());
    }
}
