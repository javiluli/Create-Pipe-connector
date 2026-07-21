package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SelectPipeTargetPayload(BlockPos position, Direction face, boolean existingPipe) implements CustomPacketPayload {
    public static final Type<SelectPipeTargetPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "select_pipe_target"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectPipeTargetPayload> STREAM_CODEC = StreamCodec.ofMember(SelectPipeTargetPayload::write, SelectPipeTargetPayload::read);

    private static SelectPipeTargetPayload read(RegistryFriendlyByteBuf buffer) {
        return new SelectPipeTargetPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
        buffer.writeEnum(face);
        buffer.writeBoolean(existingPipe);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
