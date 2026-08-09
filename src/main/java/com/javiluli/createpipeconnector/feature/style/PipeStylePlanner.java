package com.javiluli.createpipeconnector.feature.style;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Aplica al plan el aspecto normal o de cristal de las tuberias. */
public final class PipeStylePlanner {
    /** Impide crear instancias del planificador estatico. */
    private PipeStylePlanner() {
    }

    /** Marca como cristal unicamente posiciones rectas sin bomba ni revestimiento. */
    public static ConnectionPlan apply(ConnectionPlan plan, PipeStyleMode mode, Block pipeBlock) {
        if (mode != PipeStyleMode.GLASS || !CreatePipeBlocks.supportsGlassPipeStyle(pipeBlock)) {
            return copyWithGlassPipes(plan, Set.of());
        }

        Set<BlockPos> glassPipePlacements = new LinkedHashSet<>();
        Map<BlockPos, Integer> pathIndices = PipeRouteGeometry.indexByPosition(plan.path());
        for (BlockPos position : plan.placementPositions()) {
            if (plan.pumpPlacements().containsKey(position) || plan.copperCasingPlacements().contains(position)) {
                continue;
            }
            Integer pathIndex = pathIndices.get(position);
            if (pathIndex != null && PipeRouteGeometry.straightPumpFacingAt(plan.path(), pathIndex) != null) {
                glassPipePlacements.add(position);
            }
        }
        return copyWithGlassPipes(plan, glassPipePlacements);
    }

    /** Crea una copia del plan sustituyendo unicamente sus tuberias de cristal. */
    private static ConnectionPlan copyWithGlassPipes(ConnectionPlan plan, Set<BlockPos> glassPipePlacements) {
        return new ConnectionPlan(
                plan.path(),
                plan.placementPositions(),
                plan.pumpPlacements(),
                plan.copperCasingPlacements(),
                glassPipePlacements
        );
    }
}
