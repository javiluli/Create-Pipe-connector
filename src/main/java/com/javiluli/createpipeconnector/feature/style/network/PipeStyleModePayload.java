package com.javiluli.createpipeconnector.feature.style.network;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sincroniza el estilo normal o de cristal.
 */
public record PipeStyleModePayload(PipeStyleMode mode) implements CustomPacketPayload {
    public static final Type<PipeStyleModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pipe_style_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeStyleModePayload> STREAM_CODEC = StreamCodec.ofMember(PipeStyleModePayload::write, PipeStyleModePayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static PipeStyleModePayload read(RegistryFriendlyByteBuf buffer) {
        return new PipeStyleModePayload(buffer.readEnum(PipeStyleMode.class));
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

