package com.javiluli.createpipeconnector.feature.connector.server;

import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

/**
 * Comparte las validaciones de sesion que necesitan las features modificables de una ruta.
 */
public final class ServerConnectorSessionValidator {
    /** Impide crear instancias del validador estatico. */
    private ServerConnectorSessionValidator() {
    }

    /**
     * Devuelve la seleccion activa solo mientras el jugador conserva un estado compatible.
     *
     * @param player jugador cuya sesion se comprueba
     * @return seleccion valida o {@code null} cuando debe descartarse
     */
    public static Selection validatedSelection(ServerPlayer player) {
        if (!ConnectorSessionStore.isConnectorModeEnabled(player.getUUID())) {
            ConnectorSessionStore.clearSelection(player.getUUID());
            return null;
        }

        Selection selection = ConnectorSessionStore.getSelection(player.getUUID());
        if (selection != null && PipeConnectorLogic.isPlayerInPipeMode(player, selection)) {
            return selection;
        }

        ConnectorSessionStore.clearSelection(player.getUUID());
        return null;
    }

    /**
     * Comprueba que existe una ruta activa y que la posicion puede alcanzarse legitimamente.
     *
     * @param player jugador que solicita el cambio
     * @param position posicion afectada por la feature
     * @return {@code true} si la modificacion puede procesarse
     */
    public static boolean canModifyRouteAt(ServerPlayer player, BlockPos position) {
        return validatedSelection(player) != null
                && PipeConnectorLogic.isWithinInteractionRange(player, position);
    }
}
