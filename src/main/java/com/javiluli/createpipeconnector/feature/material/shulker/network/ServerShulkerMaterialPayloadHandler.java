package com.javiluli.createpipeconnector.feature.material.shulker.network;

import com.javiluli.createpipeconnector.feature.material.shulker.server.ShulkerMaterialPreferenceStore;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Recibe y guarda la preferencia de materiales en shulkers de cada jugador. */
public final class ServerShulkerMaterialPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerShulkerMaterialPayloadHandler() {
    }

    /** Aplica la preferencia al jugador que envio el payload. */
    public static void handleSettings(
            ShulkerMaterialSettingsPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(
                contextSupplier,
                player -> ShulkerMaterialPreferenceStore.setEnabled(player.getUUID(), payload.enabled())
        );
    }
}
