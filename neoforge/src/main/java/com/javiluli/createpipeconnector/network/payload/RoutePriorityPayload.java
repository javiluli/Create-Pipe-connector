package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.RoutePriority;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RoutePriorityPayload(RoutePriority priority) implements CustomPacketPayload {
    public static final Type<RoutePriorityPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "route_priority"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RoutePriorityPayload> STREAM_CODEC = StreamCodec.ofMember(RoutePriorityPayload::write, RoutePriorityPayload::read);

    private static RoutePriorityPayload read(RegistryFriendlyByteBuf buffer) {
        return new RoutePriorityPayload(buffer.readEnum(RoutePriority.class));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(priority);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
