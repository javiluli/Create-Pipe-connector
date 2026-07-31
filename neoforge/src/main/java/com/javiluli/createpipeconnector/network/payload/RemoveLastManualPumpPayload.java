package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Removes the most recently marked manual pump.
 */
public record RemoveLastManualPumpPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastManualPumpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_REMOVE_PUMP));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastManualPumpPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastManualPumpPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
