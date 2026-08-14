package com.javiluli.createpipeconnector.feature.pump.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sincroniza el sentido compartido por bombas automaticas y manuales. */
public record PumpDirectionPayload(boolean reversed) implements CustomPacketPayload {
    public static final Type<PumpDirectionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pump_direction")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PumpDirectionPayload> STREAM_CODEC =
            StreamCodec.ofMember(PumpDirectionPayload::write, PumpDirectionPayload::read);

    /** Decodifica el sentido solicitado. */
    private static PumpDirectionPayload read(RegistryFriendlyByteBuf buffer) {
        return new PumpDirectionPayload(buffer.readBoolean());
    }

    /** Codifica el sentido solicitado. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(reversed);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
