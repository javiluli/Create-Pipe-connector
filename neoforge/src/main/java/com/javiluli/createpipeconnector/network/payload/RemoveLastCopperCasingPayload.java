package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Removes the most recently marked manual casing.
 */
public record RemoveLastCopperCasingPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastCopperCasingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_REMOVE_CASING));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastCopperCasingPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastCopperCasingPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
