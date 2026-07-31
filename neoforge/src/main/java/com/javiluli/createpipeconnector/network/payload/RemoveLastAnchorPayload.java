package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Removes the most recently added route anchor.
 */
public record RemoveLastAnchorPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastAnchorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_REMOVE_ANCHOR));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastAnchorPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastAnchorPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
