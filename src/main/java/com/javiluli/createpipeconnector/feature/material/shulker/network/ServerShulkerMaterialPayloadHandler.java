package com.javiluli.createpipeconnector.feature.material.shulker.network;

import com.javiluli.createpipeconnector.feature.material.shulker.server.ShulkerMaterialPreferenceStore;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Recibe y guarda la preferencia de materiales en shulkers de cada jugador. */
public final class ServerShulkerMaterialPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerShulkerMaterialPayloadHandler() {
    }

    /** Aplica la preferencia al jugador que envio el payload. */
    public static void handleSettings(ShulkerMaterialSettingsPayload payload, IPayloadContext context) {
        ShulkerMaterialPreferenceStore.setEnabled(context.player().getUUID(), payload.enabled());
    }
}
