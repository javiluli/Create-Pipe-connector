package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.ActivePreview;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.AnimatedPiece;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Dibuja los contornos cacheados del preview y sus repintados especiales.
 *
 * <p>Las cajas y colores se calculan al construir la geometria. Esta clase
 * solo selecciona piezas visibles y emite sus lineas.</p>
 */
final class PipeGhostOutlineRenderer {
    private static final float OUTLINE_ALPHA = 0.95F;
    private static final Direction[] DIRECTIONS = Direction.values();

    // Esta coleccion pertenece al hilo de render y se limpia antes de usarla.
    private static final Set<BlockPos> ANCHOR_NEIGHBOURHOOD = new HashSet<>();

    /** Impide crear instancias del renderizador auxiliar. */
    private PipeGhostOutlineRenderer() {
    }

    /** Dibuja los contornos de las secciones editables visibles. */
    static void renderVisible(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            List<PipeGhostGeometryCache.Section> visibleSections
    ) {
        if (visibleSections.isEmpty()) {
            return;
        }

        VertexConsumer lineBuffer = null;
        boolean rendered = false;
        for (PipeGhostGeometryCache.Section section : visibleSections) {
            for (PipeGhostGeometryCache.OutlinePiece outline : section.outlines()) {
                if (lineBuffer == null) {
                    lineBuffer = bufferSource.getBuffer(RenderType.lines());
                }
                renderPiece(poseStack, lineBuffer, outline);
                rendered = true;
            }
        }
        endLineBatch(bufferSource, rendered);
    }

    /** Dibuja la pieza que precede a cada construccion progresiva. */
    static void renderLeadPieces(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Level level,
            List<ActivePreview> leadPreviews,
            Map<Integer, PipeGhostGeometryCache> bufferCaches,
            boolean zoomAnimationEnabled,
            float zoomDurationTicks,
            Frustum frustum,
            float partialTick
    ) {
        if (leadPreviews.isEmpty()) {
            return;
        }

        VertexConsumer lineBuffer = null;
        boolean rendered = false;
        for (ActivePreview leadPreview : leadPreviews) {
            PipeGhostGeometryCache bufferCache = bufferCaches.get(leadPreview.version());
            if (bufferCache == null) {
                continue;
            }

            List<PipeGhostGeometryCache.Section> sections = bufferCache.sections();
            if (!zoomAnimationEnabled) {
                int pieceIndex = leadPreview.pieceIndex();
                if (pieceIndex < 0 || pieceIndex >= sections.size()) {
                    continue;
                }
                PipeGhostGeometryCache.Section section = sections.get(pieceIndex);
                if (!frustum.isVisible(section.bounds())) {
                    continue;
                }
                for (PipeGhostGeometryCache.OutlinePiece outline : section.outlines()) {
                    if (lineBuffer == null) {
                        lineBuffer = bufferSource.getBuffer(RenderType.lines());
                    }
                    renderPiece(poseStack, lineBuffer, outline);
                    rendered = true;
                }
                continue;
            }
            for (AnimatedPiece animatedPiece : leadPreview.animatedPieces()) {
                int pieceIndex = animatedPiece.pieceIndex();
                if (pieceIndex < 0 || pieceIndex >= sections.size()) {
                    continue;
                }

                PipeGhostGeometryCache.Section section = sections.get(pieceIndex);
                if (!frustum.isVisible(section.bounds())) {
                    continue;
                }

                BlockPos position = leadPreview.pieces().get(pieceIndex).position();
                float animatedScale = PipeGhostCascadeAnimation.scale(
                        level,
                        animatedPiece,
                        partialTick,
                        zoomDurationTicks
                );
                poseStack.pushPose();
                try {
                    PipeGhostCascadeAnimation.applyCentered(poseStack, position, animatedScale);
                    for (PipeGhostGeometryCache.OutlinePiece outline : section.outlines()) {
                        if (lineBuffer == null) {
                            lineBuffer = bufferSource.getBuffer(RenderType.lines());
                        }
                        renderPiece(poseStack, lineBuffer, outline);
                        rendered = true;
                    }
                } finally {
                    poseStack.popPose();
                }
            }
        }
        endLineBatch(bufferSource, rendered);
    }

    /** Redibuja sin profundidad las tuberias ocultas por una cara de ancla. */
    static void renderThroughAnchors(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            PipeGhostGeometryCache bufferCache,
            List<PlacementTarget> anchors
    ) {
        if (bufferCache.isEmpty() || anchors.isEmpty()) {
            return;
        }

        Set<BlockPos> positions = anchorNeighbourhood(anchors);
        RenderSystem.disableDepthTest();
        try {
            VertexConsumer lineBuffer = null;
            boolean rendered = false;
            for (BlockPos position : positions) {
                PipeGhostGeometryCache.OutlinePiece outline = bufferCache.outlineAt(position);
                if (outline != null) {
                    if (lineBuffer == null) {
                        lineBuffer = bufferSource.getBuffer(RenderType.lines());
                    }
                    renderPiece(poseStack, lineBuffer, outline);
                    rendered = true;
                }
            }
            endLineBatch(bufferSource, rendered);
        } finally {
            RenderSystem.enableDepthTest();
        }
    }

    /** Reune cada ancla y sus seis vecinos sin posiciones duplicadas. */
    private static Set<BlockPos> anchorNeighbourhood(List<PlacementTarget> anchors) {
        ANCHOR_NEIGHBOURHOOD.clear();
        for (PlacementTarget anchor : anchors) {
            BlockPos anchorPosition = anchor.position();
            ANCHOR_NEIGHBOURHOOD.add(anchorPosition);
            for (Direction direction : DIRECTIONS) {
                ANCHOR_NEIGHBOURHOOD.add(anchorPosition.relative(direction));
            }
        }
        return ANCHOR_NEIGHBOURHOOD;
    }

    /** Finaliza la capa lineal solo cuando se emitio alguna caja. */
    private static void endLineBatch(MultiBufferSource.BufferSource bufferSource, boolean rendered) {
        if (rendered) {
            bufferSource.endBatch(RenderType.lines());
        }
    }

    /** Dibuja todas las cajas de una pieza con sus colores precalculados. */
    private static void renderPiece(
            PoseStack poseStack,
            VertexConsumer lineBuffer,
            PipeGhostGeometryCache.OutlinePiece outline
    ) {
        for (PipeGhostGeometryCache.OutlineBox box : outline.boxes()) {
            LevelRenderer.renderLineBox(
                    poseStack,
                    lineBuffer,
                    box.bounds(),
                    box.red(),
                    box.green(),
                    box.blue(),
                    OUTLINE_ALPHA
            );
        }
    }
}
