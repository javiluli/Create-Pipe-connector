package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Updates automatic pump placement state.
 */
public record ToggleAutoPumpsPayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleAutoPumpsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_TOGGLE_AUTO_PUMPS));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAutoPumpsPayload> STREAM_CODEC = StreamCodec.ofMember(ToggleAutoPumpsPayload::write, ToggleAutoPumpsPayload::read);

    private static ToggleAutoPumpsPayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleAutoPumpsPayload(buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
