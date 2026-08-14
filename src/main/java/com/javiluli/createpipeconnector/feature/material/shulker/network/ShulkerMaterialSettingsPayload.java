package com.javiluli.createpipeconnector.feature.material.shulker.network;

import net.minecraft.network.FriendlyByteBuf;

/** Sincroniza con el servidor si un jugador permite consumir materiales de shulkers. */
public record ShulkerMaterialSettingsPayload(boolean enabled) {
    /** Decodifica la preferencia enviada por el cliente. */
    public static ShulkerMaterialSettingsPayload decode(FriendlyByteBuf buffer) {
        return new ShulkerMaterialSettingsPayload(buffer.readBoolean());
    }

    /** Codifica la preferencia para enviarla al servidor. */
    public static void encode(ShulkerMaterialSettingsPayload payload, FriendlyByteBuf buffer) {
        buffer.writeBoolean(payload.enabled());
    }
}
