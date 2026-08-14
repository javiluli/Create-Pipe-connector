package com.javiluli.createpipeconnector.feature.connector.interaction;

import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Resuelve de forma comun la prioridad entre una ruta y la interaccion vanilla.
 */
public final class RouteInteractionResolver {
    /** Impide crear instancias del resolvedor estatico. */
    private RouteInteractionResolver() {
    }

    /**
     * Decide la accion de clic derecho sin modificar el estado del jugador.
     *
     * <p>La tuberia seleccionada confirma sobre bloques normales. Inventarios,
     * maquinas y otros objetos conservan su uso vanilla, salvo que el jugador
     * mantenga Shift para forzar la confirmacion.</p>
     */
    public static boolean shouldConnectorHandle(
            Player player,
            @Nullable Selection selection,
            Level level,
            @Nullable BlockPos blockPosition
    ) {
        if (selection == null) {
            if (PipeConnectorLogic.getHeldPipeBlock(player) == null || blockPosition == null) {
                return false;
            }
            if (!player.isShiftKeyDown()
                    && shouldPrioritizeBlockUse(level, blockPosition)) {
                return false;
            }
            return true;
        }

        if (player.isShiftKeyDown() || blockPosition == null) {
            return true;
        }
        if (shouldPrioritizeBlockUse(level, blockPosition)) {
            return false;
        }
        return PipeConnectorLogic.isUsingSelectedPipe(player, selection);
    }

    /**
     * Indica si el bloque expone un menu o utiliza una entidad interactiva.
     *
     * <p>Las entidades de bloque cubren terminales y maquinas de mods que abren
     * interfaces mediante redes propias. Las tuberias compatibles quedan fuera
     * para que puedan seguir siendo objetivos directos del conector.</p>
     */
    private static boolean shouldPrioritizeBlockUse(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        if (state.getMenuProvider(level, position) != null) {
            return true;
        }

        BlockEntity blockEntity = level.getBlockEntity(position);
        if (blockEntity == null) {
            return false;
        }
        if (blockEntity instanceof MenuProvider) {
            return true;
        }
        return !PipeConnectorLogic.isConnectablePipe(state);
    }
}
