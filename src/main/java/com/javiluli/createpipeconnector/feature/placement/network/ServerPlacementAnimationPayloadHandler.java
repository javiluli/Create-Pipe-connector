package com.javiluli.createpipeconnector.feature.placement.network;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import com.javiluli.createpipeconnector.feature.placement.server.IncrementalPipePlacementService;
import com.javiluli.createpipeconnector.feature.placement.server.PlacementAnimationPreferenceStore;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Valida y guarda preferencias de animacion recibidas desde los clientes. */
public final class ServerPlacementAnimationPayloadHandler {
    /** Impide crear instancias del manejador estatico. */
    private ServerPlacementAnimationPayloadHandler() {
    }

    /** Limita la velocidad solicitada antes de asociarla al jugador remitente. */
    public static void handleSettings(PlacementAnimationSettingsPayload payload, IPayloadContext context) {
        Player player = context.player();
        PlacementAnimationSettings settings = new PlacementAnimationSettings(
                payload.enabled(),
                payload.zoomEnabled(),
                payload.delayMilliseconds()
        );
        PlacementAnimationPreferenceStore.set(player.getUUID(), settings);
        IncrementalPipePlacementService.applySettings(player, settings);
    }
}
