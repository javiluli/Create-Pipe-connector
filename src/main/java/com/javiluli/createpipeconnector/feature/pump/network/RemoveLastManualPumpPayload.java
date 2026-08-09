package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Solicita retirar la ultima bomba manual.
 */
public record RemoveLastManualPumpPayload() implements CustomPacketPayload {
    public static final Type<RemoveLastManualPumpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "remove_last_manual_pump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveLastManualPumpPayload> STREAM_CODEC = StreamCodec.unit(new RemoveLastManualPumpPayload());

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

