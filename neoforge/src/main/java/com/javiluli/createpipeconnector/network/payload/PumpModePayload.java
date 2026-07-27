package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PumpMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record PumpModePayload(PumpMode mode) implements CustomPacketPayload {
    public static final Type<PumpModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pump_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PumpModePayload> STREAM_CODEC = StreamCodec.ofMember(PumpModePayload::write, PumpModePayload::read);

    private static PumpModePayload read(RegistryFriendlyByteBuf buffer) {
        return new PumpModePayload(buffer.readEnum(PumpMode.class));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(mode);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
