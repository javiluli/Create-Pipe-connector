package com.javiluli.createpipeconnector.feature.connector.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Enables or disables Connector Pipe mode.
 */
public record ToggleConnectorModePayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleConnectorModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_connector_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleConnectorModePayload> STREAM_CODEC = StreamCodec.ofMember(ToggleConnectorModePayload::write, ToggleConnectorModePayload::read);

    /** Decodifica el payload desde el bufer de red. */
    private static ToggleConnectorModePayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleConnectorModePayload(buffer.readBoolean());
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

