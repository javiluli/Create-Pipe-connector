package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sincroniza el estado heredado de bombas automaticas.
 */
public record ToggleAutoPumpsPayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleAutoPumpsPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_auto_pumps"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAutoPumpsPayload> STREAM_CODEC = StreamCodec.ofMember(ToggleAutoPumpsPayload::write, ToggleAutoPumpsPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static ToggleAutoPumpsPayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleAutoPumpsPayload(buffer.readBoolean());
    }

    /** Codifica el payload en el bufer de red. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

