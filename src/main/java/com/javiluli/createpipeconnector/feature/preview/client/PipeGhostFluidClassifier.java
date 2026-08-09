package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import net.minecraft.client.Camera;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import java.util.List;

/** Clasifica fluidos de camara y mundo con la misma regla de render. */
final class PipeGhostFluidClassifier {
    private static final int AIR = 0;
    private static final int WATER = 1;
    private static final int LAVA = 2;
    private static final int OTHER = 3;

    /** Impide crear instancias del clasificador. */
    private PipeGhostFluidClassifier() {
    }

    /** Devuelve el grupo de fluido ocupado por la camara. */
    static int cameraGroup(Camera camera) {
        return switch (camera.getFluidInCamera()) {
            case NONE -> AIR;
            case WATER -> WATER;
            case LAVA -> LAVA;
            case POWDER_SNOW -> OTHER;
        };
    }

    /** Devuelve el grupo de un estado de fluido del mundo. */
    static int worldGroup(FluidState fluidState) {
        if (fluidState.isEmpty()) {
            return AIR;
        }
        if (fluidState.is(FluidTags.WATER)) {
            return WATER;
        }
        if (fluidState.is(FluidTags.LAVA)) {
            return LAVA;
        }
        return OTHER;
    }

    /** Indica si una seccion pertenece al mismo fluido que la camara. */
    static boolean matchesCamera(int fluidMask, int cameraGroup) {
        return fluidMask == 0 || (fluidMask & (1 << cameraGroup)) != 0;
    }

    /** Resume todos los grupos de fluido ocupados por una ruta. */
    static int routeMask(Level level, List<PreviewPipe> previewPipes) {
        int mask = 0;
        for (PreviewPipe previewPipe : previewPipes) {
            mask |= 1 << worldGroup(level.getFluidState(previewPipe.position()));
        }
        return mask;
    }
}
