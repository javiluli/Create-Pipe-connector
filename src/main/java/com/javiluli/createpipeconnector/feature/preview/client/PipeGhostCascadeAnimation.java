package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.feature.placement.PlacementCascadeTiming;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.AnimatedPiece;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Calcula y aplica el zoom solapado de las piezas en construccion. */
final class PipeGhostCascadeAnimation {
    private static final float MINIMUM_SCALE = 0.02F;
    private static final float SETTLE_START_PROGRESS = 0.82F;
    private static final float SETTLE_PEAK_SCALE = 1.015F;

    /** Impide crear instancias del auxiliar visual. */
    private PipeGhostCascadeAnimation() {
    }

    /** Devuelve la escala animada de una pieza en construccion. */
    static float scale(Level level, AnimatedPiece piece, float partialTick) {
        int delayMilliseconds = PlacementAnimationClientConfig.get().delayMilliseconds();
        float durationTicks = PlacementCascadeTiming.zoomDurationTicks(delayMilliseconds);
        double elapsedTicks = level.getGameTime() + clamp(partialTick) - piece.startTick();
        float progress = clamp((float) (elapsedTicks / durationTicks));
        return settledScale(progress);
    }

    /** Escala geometria mundial alrededor del centro de su posicion. */
    static void applyCentered(PoseStack poseStack, BlockPos position, float scale) {
        double centerX = position.getX() + 0.5D;
        double centerY = position.getY() + 0.5D;
        double centerZ = position.getZ() + 0.5D;
        poseStack.translate(centerX, centerY, centerZ);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-centerX, -centerY, -centerZ);
    }

    /** Limita un valor al intervalo usado por la interpolacion. */
    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    /** Crece con rapidez y corrige solo un 1,5 por ciento al asentarse. */
    private static float settledScale(float progress) {
        if (progress < SETTLE_START_PROGRESS) {
            float growthProgress = progress / SETTLE_START_PROGRESS;
            float remaining = 1.0F - growthProgress;
            float easedGrowth = 1.0F - remaining * remaining * remaining;
            return MINIMUM_SCALE + (SETTLE_PEAK_SCALE - MINIMUM_SCALE) * easedGrowth;
        }

        float settleProgress = (progress - SETTLE_START_PROGRESS) / (1.0F - SETTLE_START_PROGRESS);
        return SETTLE_PEAK_SCALE + (1.0F - SETTLE_PEAK_SCALE) * smoothStep(settleProgress);
    }

    /** Interpolacion suave sin sobrepasar los extremos. */
    private static float smoothStep(float progress) {
        float value = clamp(progress);
        return value * value * (3.0F - 2.0F * value);
    }
}
