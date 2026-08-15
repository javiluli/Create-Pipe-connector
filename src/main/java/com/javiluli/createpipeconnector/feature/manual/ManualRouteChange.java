package com.javiluli.createpipeconnector.feature.manual;

import net.minecraft.core.BlockPos;

/** Describe la ultima accion manual retirada y si tambien elimino su ancla. */
public record ManualRouteChange(ManualAction action, BlockPos position, boolean anchorRemoved) {
}
