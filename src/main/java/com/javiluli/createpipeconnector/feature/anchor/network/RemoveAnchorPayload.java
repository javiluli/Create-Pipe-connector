package com.javiluli.createpipeconnector.feature.anchor.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Solicita retirar una ancla concreta de la ruta activa. */
public record RemoveAnchorPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<RemoveAnchorPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "remove_anchor")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, RemoveAnchorPayload> STREAM_CODEC =
            StreamCodec.ofMember(RemoveAnchorPayload::write, RemoveAnchorPayload::read);

    /** Decodifica la posicion del ancla. */
    private static RemoveAnchorPayload read(RegistryFriendlyByteBuf buffer) {
        return new RemoveAnchorPayload(buffer.readBlockPos());
    }

    /** Codifica la posicion del ancla. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
