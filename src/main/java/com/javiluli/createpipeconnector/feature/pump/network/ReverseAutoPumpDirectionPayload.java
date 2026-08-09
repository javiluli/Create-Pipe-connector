package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sincroniza el sentido de las bombas automaticas.
 */
public record ReverseAutoPumpDirectionPayload(boolean reversed) implements CustomPacketPayload {
    public static final Type<ReverseAutoPumpDirectionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "reverse_auto_pump_direction"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReverseAutoPumpDirectionPayload> STREAM_CODEC = StreamCodec.ofMember(ReverseAutoPumpDirectionPayload::write, ReverseAutoPumpDirectionPayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static ReverseAutoPumpDirectionPayload read(RegistryFriendlyByteBuf buffer) {
        return new ReverseAutoPumpDirectionPayload(buffer.readBoolean());
    }

    /** Codifica el payload en el bufer de red. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(reversed);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

