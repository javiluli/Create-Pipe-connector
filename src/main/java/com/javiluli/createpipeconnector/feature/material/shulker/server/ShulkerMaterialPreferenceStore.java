package com.javiluli.createpipeconnector.feature.material.shulker.server;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Conserva en servidor los jugadores que desactivaron el acceso a shulkers. */
public final class ShulkerMaterialPreferenceStore {
    private static final Set<UUID> DISABLED_PLAYERS = new HashSet<>();

    /** Impide crear instancias del almacen global. */
    private ShulkerMaterialPreferenceStore() {
    }

    /** Indica si las shulkers deben participar en el conteo y consumo. */
    public static boolean isEnabled(UUID playerId) {
        return !DISABLED_PLAYERS.contains(playerId);
    }

    /** Guarda solo las excepciones al valor predeterminado activado. */
    public static void setEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            DISABLED_PLAYERS.remove(playerId);
        } else {
            DISABLED_PLAYERS.add(playerId);
        }
    }

    /** Elimina el estado temporal cuando el jugador abandona el servidor. */
    public static void clear(UUID playerId) {
        DISABLED_PLAYERS.remove(playerId);
    }
}
