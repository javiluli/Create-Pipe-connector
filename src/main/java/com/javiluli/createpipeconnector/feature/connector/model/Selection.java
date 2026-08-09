package com.javiluli.createpipeconnector.feature.connector.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

/** Conserva el primer extremo y el tipo de tuberia mientras se edita una ruta. */
public record Selection(BlockPos position, Block pipeBlock, Direction face, boolean existingPipe) {
    /** Valida los datos obligatorios de la seleccion. */
    public Selection {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(pipeBlock, "pipeBlock");
        Objects.requireNonNull(face, "face");
    }
}
