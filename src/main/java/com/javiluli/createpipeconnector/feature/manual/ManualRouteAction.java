package com.javiluli.createpipeconnector.feature.manual;

import net.minecraft.core.BlockPos;

/** Registra una accion manual en el orden en que modifica la ruta activa. */
public record ManualRouteAction(ManualAction action, BlockPos position, boolean ownsAnchor) {
    /** Devuelve una copia que asume la propiedad del ancla compartida. */
    public ManualRouteAction withAnchorOwnership() {
        return ownsAnchor ? this : new ManualRouteAction(action, position, true);
    }
}
