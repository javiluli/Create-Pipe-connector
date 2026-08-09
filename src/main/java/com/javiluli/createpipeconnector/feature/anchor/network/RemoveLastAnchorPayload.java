package com.javiluli.createpipeconnector.feature.anchor.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Solicita retirar la ultima ancla de la ruta.
 */
public record RemoveLastAnchorPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastAnchorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "remove_last_anchor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastAnchorPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastAnchorPayload());

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

