package com.javiluli.createpipeconnector.feature.anchor.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Solicita anadir un ancla en el objetivo seleccionado.
 */
public record AddAnchorPayload(BlockPos position, Direction face, boolean existingPipe) implements CustomPacketPayload {
    public static final Type<AddAnchorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "add_anchor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddAnchorPayload> STREAM_CODEC = StreamCodec.ofMember(AddAnchorPayload::write, AddAnchorPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static AddAnchorPayload read(RegistryFriendlyByteBuf buffer) {
        return new AddAnchorPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
    }

    /** Codifica el payload en el bufer de red. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
        buffer.writeEnum(face);
        buffer.writeBoolean(existingPipe);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

