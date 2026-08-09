package com.javiluli.createpipeconnector.feature.preview;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/** Describe una pieza renderizable con bomba opcional y estado de materiales. */
public record PreviewPipe(
        BlockPos position,
        BlockState state,
        Direction mechanicalPumpFacing,
        boolean missingMaterial
) {
    /** Crea una tuberia normal con materiales disponibles. */
    public PreviewPipe(BlockPos position, BlockState state) {
        this(position, state, null, false);
    }

    /** Crea una pieza con orientacion opcional de bomba. */
    public PreviewPipe(BlockPos position, BlockState state, Direction mechanicalPumpFacing) {
        this(position, state, mechanicalPumpFacing, false);
    }

    /** Devuelve una copia con el estado de disponibilidad indicado. */
    public PreviewPipe withMissingMaterial(boolean missingMaterial) {
        return new PreviewPipe(position, state, mechanicalPumpFacing, missingMaterial);
    }

    /** Indica si la pieza representa una bomba mecanica. */
    public boolean isMechanicalPump() {
        return mechanicalPumpFacing != null;
    }

    /** Valida los datos obligatorios de la pieza. */
    public PreviewPipe {
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(state, "state");
    }
}
