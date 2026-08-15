package com.javiluli.createpipeconnector.feature.connector.server;

import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
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
        if (selection != null && PipeConnectorLogic.isSelectionStillValid(player.level(), selection)) {
            return selection;
        }

        ConnectorSessionStore.clearSelection(player.getUUID());
        return null;
    }

    /**
     * Comprueba que existe una ruta activa antes de modificar sus marcas.
     *
     * <p>Las posiciones pueden pertenecer a un preview fijado anteriormente y
     * quedar fuera del alcance actual mientras el jugador usa freecam.</p>
     *
     * @param player jugador que solicita el cambio
     * @return {@code true} si la modificacion puede procesarse
     */
    public static boolean canModifyRoute(ServerPlayer player) {
        return validatedSelection(player) != null;
    }
}
