package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveLastManualPumpPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastManualPumpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "remove_last_manual_pump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastManualPumpPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastManualPumpPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
