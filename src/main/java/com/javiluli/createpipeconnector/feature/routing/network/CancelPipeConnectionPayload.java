package com.javiluli.createpipeconnector.feature.routing.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Cancela la ruta actual sin desactivar el modo Pipe Connector.
 */
public record CancelPipeConnectionPayload() implements CustomPacketPayload {
    public static final Type<CancelPipeConnectionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "cancel_pipe_connection"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CancelPipeConnectionPayload> STREAM_CODEC = StreamCodec.unit(new CancelPipeConnectionPayload());

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
