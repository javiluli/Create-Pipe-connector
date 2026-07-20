package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RemoveLastAnchorPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastAnchorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "remove_last_anchor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastAnchorPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastAnchorPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
