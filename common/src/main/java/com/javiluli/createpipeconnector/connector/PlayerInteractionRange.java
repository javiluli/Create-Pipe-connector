package com.javiluli.createpipeconnector.connector;

import net.minecraft.world.entity.player.Player;

/**
 * Resolves the player's effective block interaction range on Minecraft 1.21.1.
 */
final class PlayerInteractionRange {
    private PlayerInteractionRange() {
    }

    static double resolve(Player player) {
        return player.blockInteractionRange();
    }
}
