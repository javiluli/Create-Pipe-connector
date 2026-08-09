package com.javiluli.createpipeconnector.core.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Resultado inmutable de una ruta con todos sus modificadores de colocacion. */
public record ConnectionPlan(
        List<BlockPos> path,
        List<BlockPos> placementPositions,
        Map<BlockPos, Direction> pumpPlacements,
        Set<BlockPos> copperCasingPlacements,
        Set<BlockPos> glassPipePlacements
) {
    /** Crea un plan sin bombas, revestimientos ni cristal. */
    public ConnectionPlan(List<BlockPos> path, List<BlockPos> placementPositions) {
        this(path, placementPositions, Map.of());
    }

    /** Crea un plan con bombas y sin estilos adicionales. */
    public ConnectionPlan(List<BlockPos> path, List<BlockPos> placementPositions, Map<BlockPos, Direction> pumpPlacements) {
        this(path, placementPositions, pumpPlacements, Set.of());
    }

    /** Crea un plan con bombas y revestimientos, pero sin cristal. */
    public ConnectionPlan(
            List<BlockPos> path,
            List<BlockPos> placementPositions,
            Map<BlockPos, Direction> pumpPlacements,
            Set<BlockPos> copperCasingPlacements
    ) {
        this(path, placementPositions, pumpPlacements, copperCasingPlacements, Set.of());
    }

    /** Copia las colecciones para garantizar la inmutabilidad del plan. */
    public ConnectionPlan {
        path = List.copyOf(path);
        placementPositions = List.copyOf(placementPositions);
        pumpPlacements = Map.copyOf(pumpPlacements);
        copperCasingPlacements = Set.copyOf(copperCasingPlacements);
        glassPipePlacements = Set.copyOf(glassPipePlacements);
    }

    /** Devuelve cuantas tuberias normales consume el plan. */
    public int requiredPipes() {
        return placementPositions.size() - requiredPumps();
    }

    /** Devuelve cuantas bombas mecanicas consume el plan. */
    public int requiredPumps() {
        return pumpPlacements.size();
    }

    /** Devuelve si el plan requiere el objeto habilitador de revestimiento. */
    public int requiredCopperCasings() {
        return copperCasingPlacements.isEmpty() ? 0 : 1;
    }
}
