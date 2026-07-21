package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ReverseAutoPumpDirectionPayload(boolean reversed) implements CustomPacketPayload {
    public static final Type<ReverseAutoPumpDirectionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "reverse_auto_pump_direction"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReverseAutoPumpDirectionPayload> STREAM_CODEC = StreamCodec.ofMember(ReverseAutoPumpDirectionPayload::write, ReverseAutoPumpDirectionPayload::read);

    private static ReverseAutoPumpDirectionPayload read(RegistryFriendlyByteBuf buffer) {
        return new ReverseAutoPumpDirectionPayload(buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(reversed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
