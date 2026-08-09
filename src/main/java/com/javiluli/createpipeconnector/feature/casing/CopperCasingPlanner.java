package com.javiluli.createpipeconnector.feature.casing;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Aplica al plan el modo de revestimiento de cobre seleccionado. */
public final class CopperCasingPlanner {
    /** Impide crear instancias del planificador estatico. */
    private CopperCasingPlanner() {
    }

    /** Resuelve las posiciones revestidas sin ocupar huecos reservados para bombas. */
    public static ConnectionPlan apply(
            ConnectionPlan plan,
            CopperCasingMode mode,
            List<BlockPos> casingPositions,
            Block pipeBlock
    ) {
        if (!CreatePipeBlocks.supportsCopperCasing(pipeBlock)) {
            return copyWithCasings(plan, Set.of());
        }

        CopperCasingMode normalizedMode = mode == null ? CopperCasingMode.MANUAL : mode;
        if (normalizedMode == CopperCasingMode.NONE) {
            return copyWithCasings(plan, Set.of());
        }

        Set<BlockPos> placementPositions = new HashSet<>(plan.placementPositions());
        Set<BlockPos> casingPlacements = new LinkedHashSet<>();
        if (normalizedMode == CopperCasingMode.ALL) {
            for (BlockPos position : plan.placementPositions()) {
                if (!plan.pumpPlacements().containsKey(position)) {
                    casingPlacements.add(position);
                }
            }
        } else if (casingPositions != null) {
            for (BlockPos position : casingPositions) {
                if (placementPositions.contains(position) && !plan.pumpPlacements().containsKey(position)) {
                    casingPlacements.add(position);
                }
            }
        }
        return copyWithCasings(plan, casingPlacements);
    }

    /** Crea una copia del plan sustituyendo unicamente sus revestimientos. */
    private static ConnectionPlan copyWithCasings(ConnectionPlan plan, Set<BlockPos> casingPlacements) {
        return new ConnectionPlan(
                plan.path(),
                plan.placementPositions(),
                plan.pumpPlacements(),
                casingPlacements,
                plan.glassPipePlacements()
        );
    }
}
