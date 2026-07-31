package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Toggles a manual copper casing marker.
 */
public record ToggleCopperCasingPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<ToggleCopperCasingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_TOGGLE_CASING));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCopperCasingPayload> STREAM_CODEC = StreamCodec.ofMember(ToggleCopperCasingPayload::write, ToggleCopperCasingPayload::read);

    private static ToggleCopperCasingPayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleCopperCasingPayload(buffer.readBlockPos());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
