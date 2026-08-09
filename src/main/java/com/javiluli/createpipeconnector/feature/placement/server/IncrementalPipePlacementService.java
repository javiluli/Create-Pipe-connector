package com.javiluli.createpipeconnector.feature.placement.server;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.material.PipeInventory;
import com.javiluli.createpipeconnector.feature.pipe.PipeNetworkUpdater;
import com.javiluli.createpipeconnector.feature.preview.PipePreviewBuilder;
import com.javiluli.createpipeconnector.feature.routing.PipePathfinder;
import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

/**
 * Coloca rutas confirmadas a una velocidad estable sin agrupar piezas por tick.
 *
 * <p>Los materiales se reservan al confirmar. Si una ruta queda bloqueada,
 * se devuelven las piezas pendientes y se mantienen las ya colocadas.</p>
 */
public final class IncrementalPipePlacementService {
    private static final Map<UUID, Queue<PendingPlacement>> PENDING_PLACEMENTS = new HashMap<>();

    /** Impide crear instancias del servicio global. */
    private IncrementalPipePlacementService() {
    }

    /** Valida, reserva y encola un plan para su construccion progresiva. */
    public static boolean enqueue(Player player, ServerLevel level, ConnectionPlan plan, Block pipeBlock) {
        if (!isPlanPlaceable(level, plan)) {
            return false;
        }

        List<PlacementStep> steps = buildSteps(level, plan, pipeBlock);
        if (!PipeInventory.consumeItems(player, pipeBlock, plan)) {
            return false;
        }

        if (steps.isEmpty()) {
            PipeNetworkUpdater.refresh(level, plan.path());
            return true;
        }

        PlacementAnimationSettings settings = PlacementAnimationPreferenceStore.get(player.getUUID());
        PendingPlacement pendingPlacement = new PendingPlacement(
                level,
                plan.path(),
                pipeBlock,
                steps,
                settings.piecesPerSecond()
        );
        if (!settings.enabled()) {
            PlacementProgress progress = pendingPlacement.placeAll();
            pendingPlacement.finish();
            if (progress == PlacementProgress.BLOCKED) {
                pendingPlacement.refundRemaining(player);
            }
            return true;
        }

        PENDING_PLACEMENTS
                .computeIfAbsent(player.getUUID(), ignored -> new ArrayDeque<>())
                .add(pendingPlacement);
        return true;
    }

    /** Avanza de forma independiente todas las rutas pendientes del jugador. */
    public static void tick(Player player) {
        Queue<PendingPlacement> queue = PENDING_PLACEMENTS.get(player.getUUID());
        if (queue == null || queue.isEmpty()) {
            return;
        }

        Iterator<PendingPlacement> iterator = queue.iterator();
        while (iterator.hasNext()) {
            PendingPlacement pendingPlacement = iterator.next();
            PlacementProgress progress = pendingPlacement.placeNextTick();
            if (progress == PlacementProgress.CONTINUE) {
                continue;
            }
            iterator.remove();
            pendingPlacement.finish();
            if (progress == PlacementProgress.BLOCKED) {
                pendingPlacement.refundRemaining(player);
            }
        }

        if (queue.isEmpty()) {
            PENDING_PLACEMENTS.remove(player.getUUID());
        }
    }

    /** Aplica una preferencia nueva a todas las rutas que ya estan en marcha. */
    public static void applySettings(Player player, PlacementAnimationSettings settings) {
        if (!settings.enabled()) {
            completeAllImmediately(player);
            return;
        }
        Queue<PendingPlacement> queue = PENDING_PLACEMENTS.get(player.getUUID());
        if (queue == null) {
            return;
        }
        for (PendingPlacement pendingPlacement : queue) {
            pendingPlacement.updateSpeed(settings.piecesPerSecond());
        }
    }

    /** Cancela todas las rutas pendientes y devuelve sus materiales no colocados. */
    public static void cancelAndRefund(Player player) {
        Queue<PendingPlacement> queue = PENDING_PLACEMENTS.remove(player.getUUID());
        if (queue == null) {
            return;
        }
        for (PendingPlacement pendingPlacement : queue) {
            pendingPlacement.finish();
            pendingPlacement.refundRemaining(player);
        }
    }

    /** Finaliza al instante las rutas pendientes al desactivar la animacion. */
    private static void completeAllImmediately(Player player) {
        Queue<PendingPlacement> queue = PENDING_PLACEMENTS.remove(player.getUUID());
        if (queue == null) {
            return;
        }
        for (PendingPlacement pendingPlacement : queue) {
            PlacementProgress progress = pendingPlacement.placeAll();
            pendingPlacement.finish();
            if (progress == PlacementProgress.BLOCKED) {
                pendingPlacement.refundRemaining(player);
            }
        }
    }

    /** Comprueba que todas las posiciones siguen libres antes de reservar materiales. */
    private static boolean isPlanPlaceable(ServerLevel level, ConnectionPlan plan) {
        for (BlockPos position : plan.placementPositions()) {
            if (!level.isLoaded(position) || !PipePathfinder.isTraversableBlock(level, position)) {
                return false;
            }
        }
        return true;
    }

    /** Precalcula los estados finales para reducir trabajo durante los ticks. */
    private static List<PlacementStep> buildSteps(ServerLevel level, ConnectionPlan plan, Block pipeBlock) {
        Block pumpBlock = CreatePipeBlocks.getMechanicalPumpBlock();
        Map<BlockPos, BlockState> connectionStates = PipePreviewBuilder.buildConnectionStates(level, plan, pipeBlock);
        List<PlacementStep> steps = new ArrayList<>(plan.placementPositions().size());

        for (BlockPos position : plan.placementPositions()) {
            BlockState sourceState = level.getBlockState(position);
            Direction pumpFacing = plan.pumpPlacements().get(position);
            boolean pump = pumpFacing != null && pumpBlock != null;
            BlockState targetState;
            if (pump) {
                targetState = CreatePipeBlocks.createPumpState(pumpBlock, sourceState, pumpFacing);
            } else {
                BlockState connectedState = connectionStates.getOrDefault(
                        position,
                        CreatePipeBlocks.createPipeState(pipeBlock, sourceState)
                );
                targetState = createStyledPipeState(
                        connectedState,
                        sourceState,
                        plan.copperCasingPlacements().contains(position),
                        plan.glassPipePlacements().contains(position)
                );
            }
            steps.add(new PlacementStep(position.immutable(), targetState, pump));
        }
        return List.copyOf(steps);
    }

    /** Aplica revestimiento o cristal al estado de una tuberia normal. */
    private static BlockState createStyledPipeState(
            BlockState pipeState,
            BlockState sourceState,
            boolean copperCasing,
            boolean glassPipe
    ) {
        if (copperCasing) {
            BlockState encasedState = CreatePipeBlocks.createEncasedPipeState(pipeState, sourceState);
            return encasedState == null ? pipeState : encasedState;
        }
        if (glassPipe) {
            BlockState glassState = CreatePipeBlocks.createGlassPipeState(pipeState);
            return glassState == null ? pipeState : glassState;
        }
        return pipeState;
    }

    /** Estado interno de una ruta que se encuentra en construccion. */
    private static final class PendingPlacement {
        private final ServerLevel level;
        private final List<BlockPos> path;
        private final Block pipeBlock;
        private final List<PlacementStep> steps;
        private int piecesPerSecond;
        private int nextStepIndex;
        private int placementProgress;

        /** Crea una construccion progresiva con una copia estable del plan. */
        private PendingPlacement(
                ServerLevel level,
                List<BlockPos> path,
                Block pipeBlock,
                List<PlacementStep> steps,
                int piecesPerSecond
        ) {
            this.level = level;
            this.path = List.copyOf(path);
            this.pipeBlock = pipeBlock;
            this.steps = steps;
            this.piecesPerSecond = piecesPerSecond;
        }

        /** Cambia la velocidad sin perder el progreso parcial del intervalo. */
        private void updateSpeed(int piecesPerSecond) {
            this.piecesPerSecond = piecesPerSecond;
        }

        /** Distribuye piezas individuales entre los veinte ticks de cada segundo. */
        private PlacementProgress placeNextTick() {
            placementProgress += piecesPerSecond;
            if (placementProgress < PlacementAnimationSettings.GAME_TICKS_PER_SECOND) {
                return PlacementProgress.CONTINUE;
            }
            placementProgress -= PlacementAnimationSettings.GAME_TICKS_PER_SECOND;
            return placeNext();
        }

        /** Coloca la siguiente pieza o detecta que la ruta ya no es valida. */
        private PlacementProgress placeNext() {
            if (nextStepIndex >= steps.size()) {
                return PlacementProgress.COMPLETED;
            }
            PlacementStep step = steps.get(nextStepIndex);
            if (!level.isLoaded(step.position())) {
                return PlacementProgress.BLOCKED;
            }
            BlockState currentState = level.getBlockState(step.position());
            if (!PipePathfinder.isTraversableBlock(currentState)) {
                return PlacementProgress.BLOCKED;
            }
            BlockState waterloggedState = CreatePipeBlocks.applyCurrentWaterlogging(currentState, step.state());

            // Create usa estas flags al imprimir esquemas. Enviar solo el nuevo
            // estado evita recalculos de vecinos y remallados duplicados por pieza.
            int updateFlags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
            if (!level.setBlock(step.position(), waterloggedState, updateFlags)) {
                return PlacementProgress.BLOCKED;
            }
            nextStepIndex++;
            return nextStepIndex >= steps.size() ? PlacementProgress.COMPLETED : PlacementProgress.CONTINUE;
        }

        /** Coloca todas las piezas en el mismo tick para el modo instantaneo. */
        private PlacementProgress placeAll() {
            PlacementProgress progress;
            do {
                progress = placeNext();
            } while (progress == PlacementProgress.CONTINUE);
            return progress;
        }

        /** Reconstruye las conexiones reales de las piezas ya existentes. */
        private void finish() {
            PipeNetworkUpdater.refresh(level, path);
        }

        /** Devuelve solamente materiales que aun no se habian colocado. */
        private void refundRemaining(Player player) {
            int remainingPipes = 0;
            int remainingPumps = 0;
            for (int index = nextStepIndex; index < steps.size(); index++) {
                if (steps.get(index).pump()) {
                    remainingPumps++;
                } else {
                    remainingPipes++;
                }
            }
            PipeInventory.refundItems(player, pipeBlock, remainingPipes, remainingPumps);
        }
    }

    /** Describe un estado final y el material que fue reservado para el. */
    private record PlacementStep(BlockPos position, BlockState state, boolean pump) {
    }

    /** Resultado de intentar avanzar una ruta durante el tick actual. */
    private enum PlacementProgress {
        CONTINUE,
        COMPLETED,
        BLOCKED
    }
}
