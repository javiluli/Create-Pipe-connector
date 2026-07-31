package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Toggles a manual mechanical pump marker.
 */
public record ToggleManualPumpPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<ToggleManualPumpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.PAYLOAD_TOGGLE_MANUAL_PUMP));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleManualPumpPayload> STREAM_CODEC = StreamCodec.ofMember(ToggleManualPumpPayload::write, ToggleManualPumpPayload::read);

    private static ToggleManualPumpPayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleManualPumpPayload(buffer.readBlockPos());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
