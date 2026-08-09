package com.javiluli.createpipeconnector.feature.connector.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Objects;

/** Representa un extremo o ancla obtenido desde la mirada del jugador. */
public record PlacementTarget(BlockPos position, Direction face, boolean existingPipe) {
    /** Valida los datos obligatorios del objetivo. */
    public PlacementTarget {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(face, "face");
    }
}
