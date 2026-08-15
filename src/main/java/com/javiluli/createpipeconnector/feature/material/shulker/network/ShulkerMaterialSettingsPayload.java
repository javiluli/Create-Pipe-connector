package com.javiluli.createpipeconnector.feature.material.shulker.network;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Sincroniza con el servidor si un jugador permite consumir materiales de shulkers. */
public record ShulkerMaterialSettingsPayload(boolean enabled) implements CustomPacketPayload {
    public static final Type<ShulkerMaterialSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "shulker_material_settings")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ShulkerMaterialSettingsPayload> STREAM_CODEC =
            StreamCodec.ofMember(ShulkerMaterialSettingsPayload::write, ShulkerMaterialSettingsPayload::read);

    /** Decodifica la preferencia enviada por el cliente. */
    private static ShulkerMaterialSettingsPayload read(RegistryFriendlyByteBuf buffer) {
        return new ShulkerMaterialSettingsPayload(buffer.readBoolean());
    }

    /** Codifica la preferencia para enviarla al servidor. */
    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(enabled);
    }

    /** Devuelve el tipo registrado para este payload. */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
