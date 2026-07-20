package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record AddAnchorPayload(BlockPos position, Direction face, boolean existingPipe) implements CustomPacketPayload {
    public static final Type<AddAnchorPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "add_anchor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AddAnchorPayload> STREAM_CODEC = StreamCodec.ofMember(AddAnchorPayload::write, AddAnchorPayload::read);

    private static AddAnchorPayload read(RegistryFriendlyByteBuf buffer) {
        return new AddAnchorPayload(buffer.readBlockPos(), buffer.readEnum(Direction.class), buffer.readBoolean());
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
