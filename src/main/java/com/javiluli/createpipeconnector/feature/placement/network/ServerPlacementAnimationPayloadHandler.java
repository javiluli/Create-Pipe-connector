package com.javiluli.createpipeconnector.feature.placement.network;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import com.javiluli.createpipeconnector.feature.placement.server.IncrementalPipePlacementService;
import com.javiluli.createpipeconnector.feature.placement.server.PlacementAnimationPreferenceStore;
import com.javiluli.createpipeconnector.platform.network.ServerPayloadContext;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Valida y guarda preferencias de animacion recibidas desde los clientes. */
public final class ServerPlacementAnimationPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerPlacementAnimationPayloadHandler() {
    }

    /** Limita la velocidad solicitada antes de asociarla al jugador remitente. */
    public static void handleSettings(
            PlacementAnimationSettingsPayload payload,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        ServerPayloadContext.enqueue(contextSupplier, player -> {
            PlacementAnimationSettings settings = new PlacementAnimationSettings(
                    payload.enabled(),
                    payload.zoomEnabled(),
                    payload.delayMilliseconds()
            );
            PlacementAnimationPreferenceStore.set(player.getUUID(), settings);
            IncrementalPipePlacementService.applySettings(player, settings);
        });
    }
}
