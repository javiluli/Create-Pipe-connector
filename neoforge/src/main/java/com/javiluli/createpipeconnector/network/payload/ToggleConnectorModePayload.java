package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ToggleConnectorModePayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleConnectorModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_connector_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleConnectorModePayload> STREAM_CODEC = StreamCodec.ofMember(ToggleConnectorModePayload::write, ToggleConnectorModePayload::read);

    private static ToggleConnectorModePayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleConnectorModePayload(buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
