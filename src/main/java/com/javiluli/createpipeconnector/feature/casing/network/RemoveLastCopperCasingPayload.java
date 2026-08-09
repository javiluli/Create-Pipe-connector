package com.javiluli.createpipeconnector.feature.casing.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Solicita retirar el ultimo revestimiento manual.
 */
public record RemoveLastCopperCasingPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastCopperCasingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "remove_last_copper_casing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastCopperCasingPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastCopperCasingPayload());

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

