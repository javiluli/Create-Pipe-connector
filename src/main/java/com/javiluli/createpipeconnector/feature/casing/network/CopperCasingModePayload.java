package com.javiluli.createpipeconnector.feature.casing.network;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sincroniza el modo de revestimiento de cobre.
 */
public record CopperCasingModePayload(CopperCasingMode mode) implements CustomPacketPayload {
    public static final Type<CopperCasingModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "copper_casing_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CopperCasingModePayload> STREAM_CODEC = StreamCodec.ofMember(CopperCasingModePayload::write, CopperCasingModePayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static CopperCasingModePayload read(RegistryFriendlyByteBuf buffer) {
        return new CopperCasingModePayload(buffer.readEnum(CopperCasingMode.class));
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

