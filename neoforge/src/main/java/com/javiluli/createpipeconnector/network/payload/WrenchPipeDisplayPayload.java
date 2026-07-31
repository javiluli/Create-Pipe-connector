package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Requests a wrench display change for a pipe segment.
 */
public record WrenchPipeDisplayPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<WrenchPipeDisplayPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_WRENCH_DISPLAY));
    public static final StreamCodec<RegistryFriendlyByteBuf, WrenchPipeDisplayPayload> STREAM_CODEC = StreamCodec.ofMember(WrenchPipeDisplayPayload::write, WrenchPipeDisplayPayload::read);

    private static WrenchPipeDisplayPayload read(RegistryFriendlyByteBuf buffer) {
        return new WrenchPipeDisplayPayload(buffer.readBlockPos());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
