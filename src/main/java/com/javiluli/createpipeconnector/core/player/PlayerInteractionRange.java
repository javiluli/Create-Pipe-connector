package com.javiluli.createpipeconnector.core.player;

import net.minecraft.world.entity.player.Player;

/**
 * Resuelve el alcance efectivo de interaccion con bloques en Minecraft 1.21.1.
 */
public final class PlayerInteractionRange {
    /** Impide crear instancias del adaptador de alcance. */
    private PlayerInteractionRange() {
    }

    /**
     * Devuelve el alcance efectivo o utiliza el alcance historico de Minecraft
     * cuando ninguna API compatible esta disponible.
     */
    public static double resolve(Player player) {
        return player.blockInteractionRange();
    }
}
