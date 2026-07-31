package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Cancels the active route while preserving connector mode.
 */
public record CancelPipeConnectionPayload() implements CustomPacketPayload {
    public static final Type<CancelPipeConnectionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_CANCEL_CONNECTION));
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelPipeConnectionPayload> STREAM_CODEC = StreamCodec.unit(new CancelPipeConnectionPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
