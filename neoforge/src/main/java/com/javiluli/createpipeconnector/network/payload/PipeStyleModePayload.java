package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeStyleMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PipeStyleModePayload(PipeStyleMode mode) implements CustomPacketPayload {
    public static final Type<PipeStyleModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pipe_style_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeStyleModePayload> STREAM_CODEC = StreamCodec.ofMember(PipeStyleModePayload::write, PipeStyleModePayload::read);

    private static PipeStyleModePayload read(RegistryFriendlyByteBuf buffer) {
        return new PipeStyleModePayload(buffer.readEnum(PipeStyleMode.class));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(mode);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
