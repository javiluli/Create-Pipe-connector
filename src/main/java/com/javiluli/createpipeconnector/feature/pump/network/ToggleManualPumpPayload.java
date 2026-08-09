package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Toggles a manual mechanical pump marker.
 */
public record ToggleManualPumpPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<ToggleManualPumpPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_manual_pump"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleManualPumpPayload> STREAM_CODEC = StreamCodec.ofMember(ToggleManualPumpPayload::write, ToggleManualPumpPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static ToggleManualPumpPayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleManualPumpPayload(buffer.readBlockPos());
    }

    /** Codifica el payload en el bufer de red. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(position);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

