package com.javiluli.createpipeconnector.feature.casing.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Toggles a manual copper casing marker.
 */
public record ToggleCopperCasingPayload(BlockPos position) implements CustomPacketPayload {
    public static final Type<ToggleCopperCasingPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_copper_casing"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleCopperCasingPayload> STREAM_CODEC = StreamCodec.ofMember(ToggleCopperCasingPayload::write, ToggleCopperCasingPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static ToggleCopperCasingPayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleCopperCasingPayload(buffer.readBlockPos());
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

