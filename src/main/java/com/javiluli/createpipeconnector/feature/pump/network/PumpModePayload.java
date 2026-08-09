package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sincroniza la estrategia de bombas mecanicas.
 */
public record PumpModePayload(PumpMode mode) implements CustomPacketPayload {
    public static final Type<PumpModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pump_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PumpModePayload> STREAM_CODEC = StreamCodec.ofMember(PumpModePayload::write, PumpModePayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static PumpModePayload read(RegistryFriendlyByteBuf buffer) {
        return new PumpModePayload(buffer.readEnum(PumpMode.class));
    }

    /** Codifica el payload en el bufer de red. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeEnum(mode);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

