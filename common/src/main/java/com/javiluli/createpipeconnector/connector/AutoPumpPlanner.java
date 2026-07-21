package com.javiluli.createpipeconnector.connector;

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
    private static final int FALLBACK_PUMP_PIPE_GAP = 15;
    private static Integer cachedPumpPipeGap;

    private AutoPumpPlanner() {
    }

    static ConnectionPlan apply(ConnectionPlan plan) {
        return apply(plan, false);
    }

    static ConnectionPlan apply(ConnectionPlan plan, boolean reversed) {
        if (CreatePipeBlocks.getMechanicalPumpBlock() == null || plan.placementPositions().isEmpty()) {
            return plan;
        }

        Map<BlockPos, Direction> pumpPlacements = calculatePumpPlacements(plan);
        if (pumpPlacements.isEmpty()) {
            return plan;
        }

        return new ConnectionPlan(plan.path(), plan.placementPositions(), reversed ? reverseDirections(pumpPlacements) : pumpPlacements);
    }

    private static Map<BlockPos, Direction> calculatePumpPlacements(ConnectionPlan plan) {
        List<BlockPos> path = plan.path();
        Set<BlockPos> placementPositions = new HashSet<>(plan.placementPositions());
        Map<BlockPos, Direction> pumpPlacements = new HashMap<>();
        int pipeGap = Math.max(1, getPumpPipeGap());

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
            nextPumpIndex = pumpSlot.pathIndex() + pipeGap + 1;
        }

        return pumpPlacements;
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

    private static int getPumpPipeGap() {
        if (cachedPumpPipeGap != null) {
            return cachedPumpPipeGap;
        }

        try {
            Class<?> fluidPropagator = Class.forName("com.simibubi.create.content.fluids.FluidPropagator");
            Method getPumpRange = fluidPropagator.getMethod("getPumpRange");
            Object range = getPumpRange.invoke(null);
            if (range instanceof Integer pumpRange) {
                cachedPumpPipeGap = Math.max(1, pumpRange - 1);
                return cachedPumpPipeGap;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
        cachedPumpPipeGap = FALLBACK_PUMP_PIPE_GAP;
        return cachedPumpPipeGap;
    }

    private static Map<BlockPos, Direction> reverseDirections(Map<BlockPos, Direction> pumpPlacements) {
        Map<BlockPos, Direction> reversedPlacements = new HashMap<>(pumpPlacements.size());
        pumpPlacements.forEach((position, direction) -> reversedPlacements.put(position, direction.getOpposite()));
        return reversedPlacements;
    }

    private record PumpSlot(int pathIndex, BlockPos position, Direction facing) {
    }
}
