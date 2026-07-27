package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.CopperCasingMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record CopperCasingModePayload(CopperCasingMode mode) implements CustomPacketPayload {
    public static final Type<CopperCasingModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "copper_casing_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CopperCasingModePayload> STREAM_CODEC = StreamCodec.ofMember(CopperCasingModePayload::write, CopperCasingModePayload::read);

    private static CopperCasingModePayload read(RegistryFriendlyByteBuf buffer) {
        return new CopperCasingModePayload(buffer.readEnum(CopperCasingMode.class));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(mode);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
