package com.javiluli.createpipeconnector.feature.placement.server;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Conserva en el servidor la preferencia de animacion enviada por cada jugador. */
public final class PlacementAnimationPreferenceStore {
    private static final Map<UUID, PlacementAnimationSettings> SETTINGS = new HashMap<>();

    /** Impide crear instancias del almacen global. */
    private PlacementAnimationPreferenceStore() {
    }

    /** Devuelve la preferencia del jugador o el comportamiento predeterminado. */
    public static PlacementAnimationSettings get(UUID playerId) {
        return SETTINGS.getOrDefault(playerId, PlacementAnimationSettings.DEFAULT);
    }

    /** Guarda valores ya saneados por el record de configuracion. */
    public static void set(UUID playerId, PlacementAnimationSettings settings) {
        if (PlacementAnimationSettings.DEFAULT.equals(settings)) {
            SETTINGS.remove(playerId);
        } else {
            SETTINGS.put(playerId, settings);
        }
    }

    /** Elimina la preferencia temporal cuando el jugador abandona el servidor. */
    public static void clear(UUID playerId) {
        SETTINGS.remove(playerId);
    }
}
