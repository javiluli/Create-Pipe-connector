package com.javiluli.createpipeconnector.feature.manual;

import net.minecraft.core.BlockPos;

/** Describe el cambio aplicado a una marca manual y a su ancla vinculada. */
public record ManualMarkerChange(BlockPos position, boolean added, boolean anchorChanged) {
}
