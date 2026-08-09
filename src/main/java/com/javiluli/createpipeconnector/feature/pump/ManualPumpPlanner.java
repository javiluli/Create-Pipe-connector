package com.javiluli.createpipeconnector.feature.pump;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Incorpora al plan las bombas marcadas manualmente por el jugador. */
public final class ManualPumpPlanner {
    /** Impide crear instancias del planificador estatico. */
    private ManualPumpPlanner() {
    }

    /** Conserva unicamente marcas colocables que pertenezcan a tramos rectos. */
    public static ConnectionPlan apply(ConnectionPlan plan, List<BlockPos> pumpPositions) {
        if (CreatePipeBlocks.getMechanicalPumpBlock() == null || pumpPositions == null || pumpPositions.isEmpty()) {
            return plan;
        }

        Set<BlockPos> placementPositions = new HashSet<>(plan.placementPositions());
        Map<BlockPos, Direction> pumpPlacements = new HashMap<>(plan.pumpPlacements());
        Map<BlockPos, Integer> pathIndices = PipeRouteGeometry.indexByPosition(plan.path());
        for (BlockPos position : pumpPositions) {
            if (!placementPositions.contains(position)) {
                continue;
            }

            Integer pathIndex = pathIndices.get(position);
            Direction facing = pathIndex == null ? null : PipeRouteGeometry.straightPumpFacingAt(plan.path(), pathIndex);
            if (facing != null) {
                pumpPlacements.put(position, facing);
            }
        }
        return new ConnectionPlan(
                plan.path(),
                plan.placementPositions(),
                pumpPlacements,
                plan.copperCasingPlacements(),
                plan.glassPipePlacements()
        );
    }
}
