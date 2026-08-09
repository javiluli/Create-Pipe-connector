package com.javiluli.createpipeconnector.feature.pump;

import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Anade bombas a tramos rectos de un plan inmutable. */
public final class AutoPumpPlanner {
    private static final String FLUID_PROPAGATOR_CLASS = "com.simibubi.create.content.fluids.FluidPropagator";
    private static final String GET_PUMP_RANGE_METHOD = "getPumpRange";
    private static final int FALLBACK_PUMP_SUCTION_PIPE_GAP = 15;
    private static final int FALLBACK_PUMP_PUSH_PIPE_GAP = 15;
    private static Integer cachedPumpSuctionPipeGap;

    /** Impide crear instancias del planificador. */
    private AutoPumpPlanner() {
    }

    /** Aplica el modo eficiente con el sentido de flujo normal. */
    public static ConnectionPlan apply(ConnectionPlan plan) {
        return apply(plan, PumpMode.EFFICIENT, false);
    }

    /** Aplica el modo eficiente con el sentido de flujo indicado. */
    public static ConnectionPlan apply(ConnectionPlan plan, boolean reversed) {
        return apply(plan, PumpMode.EFFICIENT, reversed);
    }

    /** Aplica al plan la estrategia y el sentido de bombas seleccionados. */
    public static ConnectionPlan apply(ConnectionPlan plan, PumpMode mode, boolean reversed) {
        if (mode == null || !mode.isAutomatic()) {
            return plan;
        }
        if (CreatePipeBlocks.getMechanicalPumpBlock() == null || plan.placementPositions().isEmpty()) {
            return plan;
        }

        Map<BlockPos, Direction> pumpPlacements = calculatePumpPlacements(plan, pipeGapFor(mode));
        if (pumpPlacements.isEmpty()) {
            return plan;
        }

        Map<BlockPos, Direction> resolvedPumpPlacements = reversed ? reverseDirections(pumpPlacements) : pumpPlacements;
        return new ConnectionPlan(plan.path(), plan.placementPositions(), resolvedPumpPlacements, withoutPumpCasings(plan, resolvedPumpPlacements), withoutPumpGlassPipes(plan, resolvedPumpPlacements));
    }

    /** Calcula posiciones rectas separadas por la distancia requerida. */
    private static Map<BlockPos, Direction> calculatePumpPlacements(ConnectionPlan plan, int pipeGap) {
        List<BlockPos> path = plan.path();
        Set<BlockPos> placementPositions = new HashSet<>(plan.placementPositions());
        Map<BlockPos, Direction> pumpPlacements = new HashMap<>();
        int normalizedPipeGap = Math.max(1, pipeGap);

        int nextPumpIndex = firstPlacementPathIndex(path, placementPositions);
        int minimumIndex = nextPumpIndex;
        while (nextPumpIndex >= 0 && nextPumpIndex < path.size()) {
            PumpSlot pumpSlot = findPumpSlotAtOrBefore(path, placementPositions, nextPumpIndex, minimumIndex);
            if (pumpSlot == null && pumpPlacements.isEmpty()) {
                pumpSlot = findPumpSlotAfter(path, placementPositions, nextPumpIndex);
            }
            if (pumpSlot == null) {
                break;
            }

            pumpPlacements.put(pumpSlot.position(), pumpSlot.facing());
            minimumIndex = pumpSlot.pathIndex() + 1;
            nextPumpIndex = pumpSlot.pathIndex() + normalizedPipeGap + 1;
        }

        return pumpPlacements;
    }

    /** Resuelve cuantas tuberias pueden quedar entre bombas para cada modo. */
    private static int pipeGapFor(PumpMode mode) {
        int suctionPipeGap = Math.max(1, getPumpSuctionPipeGap());
        int pushPipeGap = Math.max(0, getPumpPushPipeGap());
        return switch (mode) {
            case EFFICIENT -> suctionPipeGap + pushPipeGap;
            case SAFE -> suctionPipeGap;
            default -> suctionPipeGap;
        };
    }

    /** Localiza el primer bloque de la ruta que realmente se colocara. */
    private static int firstPlacementPathIndex(List<BlockPos> path, Set<BlockPos> placementPositions) {
        for (int index = 0; index < path.size(); index++) {
            if (placementPositions.contains(path.get(index))) {
                return index;
            }
        }
        return -1;
    }

    /** Busca hacia atras el hueco recto mas proximo al indice objetivo. */
    private static PumpSlot findPumpSlotAtOrBefore(List<BlockPos> path, Set<BlockPos> placementPositions, int targetIndex, int minimumIndex) {
        int upperIndex = Math.min(targetIndex, path.size() - 1);
        for (int index = upperIndex; index >= Math.max(0, minimumIndex); index--) {
            PumpSlot pumpSlot = pumpSlotAt(path, placementPositions, index);
            if (pumpSlot != null) {
                return pumpSlot;
            }
        }
        return null;
    }

    /** Busca hacia delante el primer hueco recto disponible. */
    private static PumpSlot findPumpSlotAfter(List<BlockPos> path, Set<BlockPos> placementPositions, int targetIndex) {
        for (int index = Math.max(0, targetIndex); index < path.size(); index++) {
            PumpSlot pumpSlot = pumpSlotAt(path, placementPositions, index);
            if (pumpSlot != null) {
                return pumpSlot;
            }
        }
        return null;
    }

    /** Convierte un indice de ruta en un hueco de bomba si el tramo es recto. */
    private static PumpSlot pumpSlotAt(List<BlockPos> path, Set<BlockPos> placementPositions, int index) {
        BlockPos position = path.get(index);
        if (!placementPositions.contains(position)) {
            return null;
        }

        if (path.size() < 2) {
            return null;
        }
        if (index == 0) {
            return new PumpSlot(index, position, PipeRouteGeometry.directionBetween(position, path.get(1)));
        }
        if (index == path.size() - 1) {
            return new PumpSlot(index, position, PipeRouteGeometry.directionBetween(path.get(index - 1), position));
        }

        Direction fromPrevious = PipeRouteGeometry.directionBetween(path.get(index - 1), position);
        Direction toNext = PipeRouteGeometry.directionBetween(position, path.get(index + 1));
        if (fromPrevious.getAxis() != toNext.getAxis()) {
            return null;
        }

        return new PumpSlot(index, position, toNext);
    }

    /** Obtiene y memoriza el alcance de succion expuesto por Create. */
    private static int getPumpSuctionPipeGap() {
        if (cachedPumpSuctionPipeGap != null) {
            return cachedPumpSuctionPipeGap;
        }

        // Create cambia esta API entre versiones; la reflexion mantiene Create 6.x.
        try {
            Class<?> fluidPropagator = Class.forName(FLUID_PROPAGATOR_CLASS);
            Method getPumpRange = fluidPropagator.getMethod(GET_PUMP_RANGE_METHOD);
            Object range = getPumpRange.invoke(null);
            if (range instanceof Integer pumpRange) {
                cachedPumpSuctionPipeGap = Math.max(1, pumpRange - 1);
                return cachedPumpSuctionPipeGap;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        cachedPumpSuctionPipeGap = FALLBACK_PUMP_SUCTION_PIPE_GAP;
        return cachedPumpSuctionPipeGap;
    }

    /** Devuelve el alcance de impulsion compatible con Create 6.x. */
    private static int getPumpPushPipeGap() {
        return FALLBACK_PUMP_PUSH_PIPE_GAP;
    }

    /** Invierte todas las orientaciones sin modificar el mapa recibido. */
    private static Map<BlockPos, Direction> reverseDirections(Map<BlockPos, Direction> pumpPlacements) {
        Map<BlockPos, Direction> reversedPlacements = new HashMap<>(pumpPlacements.size());
        pumpPlacements.forEach((position, direction) -> reversedPlacements.put(position, direction.getOpposite()));
        return reversedPlacements;
    }

    /** Retira revestimientos de las posiciones reservadas para bombas. */
    private static Set<BlockPos> withoutPumpCasings(ConnectionPlan plan, Map<BlockPos, Direction> pumpPlacements) {
        Set<BlockPos> casingPlacements = new HashSet<>(plan.copperCasingPlacements());
        casingPlacements.removeAll(pumpPlacements.keySet());
        return casingPlacements;
    }

    /** Retira estilos de cristal de las posiciones reservadas para bombas. */
    private static Set<BlockPos> withoutPumpGlassPipes(ConnectionPlan plan, Map<BlockPos, Direction> pumpPlacements) {
        Set<BlockPos> glassPipePlacements = new HashSet<>(plan.glassPipePlacements());
        glassPipePlacements.removeAll(pumpPlacements.keySet());
        return glassPipePlacements;
    }

    /** Describe una posicion valida de bomba dentro del recorrido. */
    private record PumpSlot(int pathIndex, BlockPos position, Direction facing) {
    }
}
