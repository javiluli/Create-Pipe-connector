package com.javiluli.createpipeconnector.feature.connector.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Sincroniza si el jugador tiene activo el modo Pipe Connector.
 */
public record ToggleConnectorModePayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ToggleConnectorModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "toggle_connector_mode"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleConnectorModePayload> STREAM_CODEC = StreamCodec.ofMember(ToggleConnectorModePayload::write, ToggleConnectorModePayload::read);

    /** Decodifica el estado del modo Pipe Connector. */
    private static ToggleConnectorModePayload read(RegistryFriendlyByteBuf buffer) {
        return new ToggleConnectorModePayload(buffer.readBoolean());
    }

    /** Codifica el estado del modo Pipe Connector. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
