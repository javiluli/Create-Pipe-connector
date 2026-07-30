package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class AutoPumpPlanner {
    private static final int FALLBACK_PUMP_SUCTION_PIPE_GAP = 15;
    private static final int FALLBACK_PUMP_PUSH_PIPE_GAP = 15;
    private static Integer cachedPumpSuctionPipeGap;

    private AutoPumpPlanner() {
    }

    static ConnectionPlan apply(ConnectionPlan plan) {
        return apply(plan, PipeConnectorLogic.PumpMode.EFFICIENT, false);
    }

    static ConnectionPlan apply(ConnectionPlan plan, boolean reversed) {
        return apply(plan, PipeConnectorLogic.PumpMode.EFFICIENT, reversed);
    }

    static ConnectionPlan apply(ConnectionPlan plan, PipeConnectorLogic.PumpMode mode, boolean reversed) {
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

    private static int pipeGapFor(PipeConnectorLogic.PumpMode mode) {
        int suctionPipeGap = Math.max(1, getPumpSuctionPipeGap());
        int pushPipeGap = Math.max(0, getPumpPushPipeGap());
        return switch (mode) {
            case EFFICIENT -> suctionPipeGap + pushPipeGap;
            case SAFE -> suctionPipeGap;
            default -> suctionPipeGap;
        };
    }

    private static int firstPlacementPathIndex(List<BlockPos> path, Set<BlockPos> placementPositions) {
        for (int index = 0; index < path.size(); index++) {
            if (placementPositions.contains(path.get(index))) {
                return index;
            }
        }
        return -1;
    }

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

    private static PumpSlot findPumpSlotAfter(List<BlockPos> path, Set<BlockPos> placementPositions, int targetIndex) {
        for (int index = Math.max(0, targetIndex); index < path.size(); index++) {
            PumpSlot pumpSlot = pumpSlotAt(path, placementPositions, index);
            if (pumpSlot != null) {
                return pumpSlot;
            }
        }
        return null;
    }

    private static PumpSlot pumpSlotAt(List<BlockPos> path, Set<BlockPos> placementPositions, int index) {
        BlockPos position = path.get(index);
        if (!placementPositions.contains(position)) {
            return null;
        }

        if (path.size() < 2) {
            return null;
        }
        if (index == 0) {
            return new PumpSlot(index, position, PipeConnectorLogic.directionBetween(position, path.get(1)));
        }
        if (index == path.size() - 1) {
            return new PumpSlot(index, position, PipeConnectorLogic.directionBetween(path.get(index - 1), position));
        }

        Direction fromPrevious = PipeConnectorLogic.directionBetween(path.get(index - 1), position);
        Direction toNext = PipeConnectorLogic.directionBetween(position, path.get(index + 1));
        if (fromPrevious.getAxis() != toNext.getAxis()) {
            return null;
        }

        return new PumpSlot(index, position, toNext);
    }

    private static int getPumpSuctionPipeGap() {
        if (cachedPumpSuctionPipeGap != null) {
            return cachedPumpSuctionPipeGap;
        }

        try {
            Class<?> fluidPropagator = Class.forName(Constants.CREATE_FLUID_PROPAGATOR);
            Method getPumpRange = fluidPropagator.getMethod(Constants.GET_PUMP_RANGE);
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

    private static int getPumpPushPipeGap() {
        return FALLBACK_PUMP_PUSH_PIPE_GAP;
    }

    private static Map<BlockPos, Direction> reverseDirections(Map<BlockPos, Direction> pumpPlacements) {
        Map<BlockPos, Direction> reversedPlacements = new HashMap<>(pumpPlacements.size());
        pumpPlacements.forEach((position, direction) -> reversedPlacements.put(position, direction.getOpposite()));
        return reversedPlacements;
    }

    private static Set<BlockPos> withoutPumpCasings(ConnectionPlan plan, Map<BlockPos, Direction> pumpPlacements) {
        Set<BlockPos> casingPlacements = new HashSet<>(plan.copperCasingPlacements());
        casingPlacements.removeAll(pumpPlacements.keySet());
        return casingPlacements;
    }

    private static Set<BlockPos> withoutPumpGlassPipes(ConnectionPlan plan, Map<BlockPos, Direction> pumpPlacements) {
        Set<BlockPos> glassPipePlacements = new HashSet<>(plan.glassPipePlacements());
        glassPipePlacements.removeAll(pumpPlacements.keySet());
        return glassPipePlacements;
    }

    private record PumpSlot(int pathIndex, BlockPos position, Direction facing) {
    }
}
