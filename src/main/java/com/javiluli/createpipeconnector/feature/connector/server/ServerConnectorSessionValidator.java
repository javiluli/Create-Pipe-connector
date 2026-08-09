package com.javiluli.createpipeconnector.feature.connector.server;

import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/** Reune validaciones compartidas por los payloads autoritativos del servidor. */
public final class ServerConnectorSessionValidator {
    /** Impide crear instancias del helper de validacion. */
    private ServerConnectorSessionValidator() {
    }

    /** Devuelve la seleccion activa si el jugador sigue en un estado valido. */
    public static Selection validatedSelection(Player player) {
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

    /** Comprueba que existe una ruta activa y que la posicion esta al alcance. */
    public static boolean canModifyRouteAt(Player player, BlockPos position) {
        return validatedSelection(player) != null
                && PipeConnectorLogic.isWithinInteractionRange(player, position);
    }

    /** Comprueba alcance, ocupacion y tipo de una nueva ancla. */
    public static boolean isAnchorValid(Player player, ServerLevel level, Selection selection, PlacementTarget anchor) {
        if (selection.position().equals(anchor.position())) {
            return false;
        }
        if (!PipeConnectorLogic.isWithinInteractionRange(player, anchor.position())) {
            return false;
        }
        if (!anchor.existingPipe()) {
            return PipeConnectorLogic.canPlacePipeAt(level, anchor.position());
        }

        BlockState anchorState = level.getBlockState(anchor.position());
        return PipeConnectorLogic.isConnectablePipe(anchorState)
                && anchorState.getBlock() == selection.pipeBlock();
    }
}
