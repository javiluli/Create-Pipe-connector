package com.javiluli.createpipeconnector.feature.routing.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Inicia o confirma una ruta sobre un objetivo validado.
 */
public record SelectPipeTargetPayload(BlockPos position, Direction face, boolean existingPipe) implements CustomPacketPayload {
    public static final Type<SelectPipeTargetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "select_pipe_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectPipeTargetPayload> STREAM_CODEC = StreamCodec.ofMember(SelectPipeTargetPayload::write, SelectPipeTargetPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static SelectPipeTargetPayload read(RegistryFriendlyByteBuf buffer) {
        return new SelectPipeTargetPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
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
