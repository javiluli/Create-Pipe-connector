package com.javiluli.createpipeconnector.feature.style.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Solicita cambiar con la llave el aspecto de un tramo de tuberias.
 */
public record WrenchPipeDisplayPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<WrenchPipeDisplayPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "wrench_pipe_display"));
    public static final StreamCodec<RegistryFriendlyByteBuf, WrenchPipeDisplayPayload> STREAM_CODEC = StreamCodec.ofMember(WrenchPipeDisplayPayload::write, WrenchPipeDisplayPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static WrenchPipeDisplayPayload read(RegistryFriendlyByteBuf buffer) {
        return new WrenchPipeDisplayPayload(buffer.readBlockPos());
    }

    /** Codifica el payload en el bufer de red. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
