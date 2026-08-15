package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.anchor.client.AnchorOverlayRenderer;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.ActivePreview;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.AnimatedPiece;
import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import com.javiluli.createpipeconnector.feature.placement.PlacementCascadeTiming;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renderiza la ruta fantasma, los avisos de materiales y las anclas en el mundo.
 *
 * <p>La geometria se almacena por version para no reconstruir modelos de bloque
 * en cada frame cuando la ruta no ha cambiado.</p>
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class PipeGhostRenderer {
    private static final GhostTint GHOST_TINT = GhostTint.fromNormalized(1.00F, 1.00F, 1.00F, 0.42F);
    private static final GhostTint MISSING_GHOST_TINT = GhostTint.fromNormalized(1.00F, 0.38F, 0.34F, 0.16F);
    private static Level cachedLevel;
    private static int cachedPreviewVersion = -1;
    private static PipeGhostGeometryCache cachedBufferCache = PipeGhostGeometryCache.empty();
    private static Level cachedLeadLevel;
    private static final Map<Integer, PipeGhostGeometryCache> CACHED_LEAD_BUFFER_CACHES = new HashMap<>();

    // NeoForge ejecuta estos eventos en el hilo de render. Reutilizar las
    // colecciones evita basura por frame sin exponer estado a otros hilos.
    private static final Set<Integer> ACTIVE_LEAD_VERSIONS = new HashSet<>();
    private static final List<PipeGhostGeometryCache.Section> VISIBLE_PREVIEW_SECTIONS = new ArrayList<>();
    private static final List<PlacementTarget> VISIBLE_ANCHORS = new ArrayList<>();

    /** Impide crear instancias del renderizador global. */
    private PipeGhostRenderer() {
    }

    /**
     * Dibuja el preview en la fase que lo mantiene visible a traves de la
     * frontera de fluido actual de la camara.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        RenderLevelStageEvent.Stage stage = event.getStage();
        if (stage != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS
                && stage != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || minecraft.player == null) {
            clearBufferCache();
            clearLeadBufferCache();
            return;
        }

        Selection selection = ClientPipeConnectorState.getSelection();
        List<PreviewPipe> previewPipes = selection == null
                ? List.of()
                : ClientPipeConnectorState.getPreviewPipes();
        List<PlacementTarget> anchors = selection == null
                ? List.of()
                : ClientPipeConnectorState.getAnchors();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        PlacementAnimationSettings animationSettings = PlacementAnimationClientConfig.get();
        List<ActivePreview> leadPreviews = ClientPlacementLeadPreview.getActivePreviews(
                level,
                partialTick,
                animationSettings
        );
        boolean showFullRoutePreview = PlacementAnimationClientConfig.showFullRoutePreview();
        boolean showNextPiecePreview = PlacementAnimationClientConfig.showNextPiecePreview();
        boolean zoomAnimationEnabled = animationSettings.enabled() && animationSettings.zoomEnabled();
        float zoomDurationTicks = zoomAnimationEnabled
                ? PlacementCascadeTiming.zoomDurationTicks(animationSettings.delayMilliseconds())
                : 0.0F;
        boolean showPlacementPreview = showFullRoutePreview || showNextPiecePreview;

        if (selection == null) {
            clearBufferCache();
        }
        if (leadPreviews.isEmpty() || !showPlacementPreview) {
            clearLeadBufferCache();
        }
        if (previewPipes.isEmpty() && anchors.isEmpty() && leadPreviews.isEmpty()) {
            return;
        }

        PipeGhostGeometryCache bufferCache = previewPipes.isEmpty()
                ? PipeGhostGeometryCache.empty()
                : getBufferCache(minecraft, level, previewPipes, ClientPipeConnectorState.getPreviewVersion());
        Map<Integer, PipeGhostGeometryCache> leadBufferCaches = leadPreviews.isEmpty() || !showPlacementPreview
                ? Map.of()
                : getLeadBufferCaches(minecraft, level, leadPreviews);
        if (bufferCache.isEmpty() && anchors.isEmpty() && allBuffersEmpty(leadBufferCaches)) {
            return;
        }

        boolean renderBeforeFluids = stage == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS;
        int cameraFluidGroup = PipeGhostFluidClassifier.cameraGroup(event.getCamera());
        Frustum frustum = event.getFrustum();
        List<PipeGhostGeometryCache.Section> visiblePreviewSections = collectVisibleSections(
                bufferCache,
                frustum,
                cameraFluidGroup,
                renderBeforeFluids
        );
        List<PlacementTarget> visibleAnchors = collectAnchorsForPhase(
                level,
                anchors,
                cameraFluidGroup,
                renderBeforeFluids
        );
        if (visiblePreviewSections.isEmpty()
                && visibleAnchors.isEmpty()
                && !hasLeadGeometryForPhase(
                        leadPreviews,
                        leadBufferCaches,
                        showFullRoutePreview,
                        showNextPiecePreview,
                        zoomAnimationEnabled,
                        frustum,
                        cameraFluidGroup,
                        renderBeforeFluids
                )) {
            return;
        }

        RenderType ghostRenderType = PipeConnectorRenderTypes.ghostTranslucent(renderBeforeFluids);
        RenderType placementGhostRenderType = PipeConnectorRenderTypes.placementGhostTranslucent(renderBeforeFluids);
        RenderType anchorRenderType = PipeConnectorRenderTypes.anchorFilledBox(renderBeforeFluids);
        RenderType outlineRenderType = PipeConnectorRenderTypes.outline(renderBeforeFluids);

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPosition = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        Matrix4fStack modelViewStack = null;
        if (renderBeforeFluids) {
            // Las fases de chunks se publican antes de que LevelRenderer aplique
            // la matriz de camara global. Sin ella, el preview queda ligado a la
            // pantalla al cambiar entre aire y agua.
            modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix();
            modelViewStack.identity();
            modelViewStack.mul(event.getModelViewMatrix());
            RenderSystem.applyModelViewMatrix();
        }

        try {
            MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            boolean renderedPreviewBodies = renderPipeBodies(
                    poseStack,
                    bufferSource,
                    visiblePreviewSections,
                    ghostRenderType
            );
            if (renderedPreviewBodies) {
                bufferSource.endBatch(ghostRenderType);
            }
            boolean renderedPlacementBodies = renderLeadBodies(
                    poseStack,
                    bufferSource,
                    level,
                    leadPreviews,
                    leadBufferCaches,
                    showFullRoutePreview,
                    showNextPiecePreview,
                    zoomAnimationEnabled,
                    zoomDurationTicks,
                    frustum,
                    placementGhostRenderType,
                    cameraFluidGroup,
                    renderBeforeFluids,
                    partialTick
            );
            if (renderedPlacementBodies) {
                bufferSource.endBatch(placementGhostRenderType);
            }
            PipeGhostOutlineRenderer.renderVisible(poseStack, bufferSource, visiblePreviewSections, outlineRenderType);
            if (showNextPiecePreview) {
                PipeGhostOutlineRenderer.renderLeadPieces(
                        poseStack,
                        bufferSource,
                        level,
                        leadPreviews,
                        leadBufferCaches,
                        zoomAnimationEnabled,
                        zoomDurationTicks,
                        frustum,
                        outlineRenderType,
                        cameraFluidGroup,
                        renderBeforeFluids,
                        partialTick
                );
            }
            AnchorOverlayRenderer.renderFaces(poseStack, bufferSource, visibleAnchors, anchorRenderType);
            AnchorOverlayRenderer.renderOutlines(poseStack, bufferSource, visibleAnchors, outlineRenderType);
            PipeGhostOutlineRenderer.renderThroughAnchors(
                    poseStack,
                    bufferSource,
                    bufferCache,
                    visibleAnchors,
                    outlineRenderType
            );
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            poseStack.popPose();
            if (modelViewStack != null) {
                modelViewStack.popMatrix();
                RenderSystem.applyModelViewMatrix();
            }
        }
    }

    /** Indica si una ruta progresiva contiene geometria visible en esta fase. */
    private static boolean hasLeadGeometryForPhase(
            List<ActivePreview> leadPreviews,
            Map<Integer, PipeGhostGeometryCache> leadBufferCaches,
            boolean showFullRoutePreview,
            boolean showNextPiecePreview,
            boolean zoomAnimationEnabled,
            Frustum frustum,
            int cameraFluidGroup,
            boolean beforeFluids
    ) {
        for (ActivePreview leadPreview : leadPreviews) {
            PipeGhostGeometryCache leadBufferCache = leadBufferCaches.get(leadPreview.version());
            if (leadBufferCache == null) {
                continue;
            }
            int firstSection = leadPreview.pieceIndex();
            int lastSection;
            if (showFullRoutePreview) {
                lastSection = leadBufferCache.sections().size();
            } else if (!showNextPiecePreview) {
                lastSection = firstSection;
            } else if (zoomAnimationEnabled) {
                lastSection = leadPreview.firstUnstartedPieceIndex();
            } else {
                lastSection = firstSection + 1;
            }
            int endIndex = Math.min(lastSection, leadBufferCache.sections().size());
            for (int index = Math.max(0, firstSection); index < endIndex; index++) {
                PipeGhostGeometryCache.Section section = leadBufferCache.sections().get(index);
                boolean sameFluid = PipeGhostFluidClassifier.matchesCamera(section.fluidMask(), cameraFluidGroup);
                if (beforeFluids != sameFluid && frustum.isVisible(section.bounds())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Reutiliza una lista temporal con las anclas correspondientes a la fase. */
    private static List<PlacementTarget> collectAnchorsForPhase(
            Level level,
            List<PlacementTarget> anchors,
            int cameraFluidGroup,
            boolean beforeFluids
    ) {
        VISIBLE_ANCHORS.clear();
        for (PlacementTarget anchor : anchors) {
            boolean sameFluid = PipeGhostFluidClassifier.worldGroup(level.getFluidState(anchor.position())) == cameraFluidGroup;
            if (beforeFluids != sameFluid) {
                VISIBLE_ANCHORS.add(anchor);
            }
        }
        return VISIBLE_ANCHORS;
    }

    /** Devuelve una cache valida o reconstruye la geometria del preview. */
    private static PipeGhostGeometryCache getBufferCache(Minecraft minecraft, Level level, List<PreviewPipe> previewPipes, int previewVersion) {
        if (cachedLevel == level && cachedPreviewVersion == previewVersion) {
            return cachedBufferCache;
        }

        cachedLevel = level;
        cachedPreviewVersion = previewVersion;
        cachedBufferCache = PipeGhostGeometryBuilder.build(minecraft, level, previewPipes);
        return cachedBufferCache;
    }

    /** Libera la cache de geometria cuando cambia el mundo o desaparece el preview. */
    private static void clearBufferCache() {
        cachedLevel = null;
        cachedPreviewVersion = -1;
        cachedBufferCache = PipeGhostGeometryCache.empty();
    }

    /** Construye una sola vez el cuerpo fantasma de cada ruta confirmada. */
    private static Map<Integer, PipeGhostGeometryCache> getLeadBufferCaches(
            Minecraft minecraft,
            Level level,
            List<ActivePreview> leadPreviews
    ) {
        if (cachedLeadLevel != level) {
            cachedLeadLevel = level;
            CACHED_LEAD_BUFFER_CACHES.clear();
        }

        ACTIVE_LEAD_VERSIONS.clear();
        for (ActivePreview leadPreview : leadPreviews) {
            ACTIVE_LEAD_VERSIONS.add(leadPreview.version());
        }
        CACHED_LEAD_BUFFER_CACHES.keySet().removeIf(version -> !ACTIVE_LEAD_VERSIONS.contains(version));
        for (ActivePreview leadPreview : leadPreviews) {
            CACHED_LEAD_BUFFER_CACHES.computeIfAbsent(
                    leadPreview.version(),
                    version -> PipeGhostGeometryBuilder.buildProgressive(minecraft, level, leadPreview.pieces())
            );
        }
        return CACHED_LEAD_BUFFER_CACHES;
    }

    /** Libera los modelos de rutas confirmadas cuando termina la construccion. */
    private static void clearLeadBufferCache() {
        cachedLeadLevel = null;
        CACHED_LEAD_BUFFER_CACHES.clear();
        ACTIVE_LEAD_VERSIONS.clear();
    }

    /** Indica si ninguna ruta confirmada contiene geometria renderizable. */
    private static boolean allBuffersEmpty(Map<Integer, PipeGhostGeometryCache> bufferCaches) {
        for (PipeGhostGeometryCache bufferCache : bufferCaches.values()) {
            if (!bufferCache.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Dibuja solo los modelos fantasma sin anadir vertices ni contornos. */
    private static boolean renderPipeBodies(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            List<PipeGhostGeometryCache.Section> visibleSections,
            RenderType renderType
    ) {
        boolean rendered = false;
        for (PipeGhostGeometryCache.Section section : visibleSections) {
            rendered |= renderBufferCache(poseStack, bufferSource, section.base(), renderType, GHOST_TINT);
            rendered |= renderBufferCache(poseStack, bufferSource, section.missing(), renderType, MISSING_GHOST_TINT);
        }
        return rendered;
    }

    /** Reutiliza una lista temporal para comprobar cada seccion editable una sola vez. */
    private static List<PipeGhostGeometryCache.Section> collectVisibleSections(
            PipeGhostGeometryCache bufferCache,
            Frustum frustum,
            int cameraFluidGroup,
            boolean beforeFluids
    ) {
        VISIBLE_PREVIEW_SECTIONS.clear();
        for (PipeGhostGeometryCache.Section section : bufferCache.sections()) {
            boolean sameFluid = PipeGhostFluidClassifier.matchesCamera(section.fluidMask(), cameraFluidGroup);
            if (beforeFluids != sameFluid && frustum.isVisible(section.bounds())) {
                VISIBLE_PREVIEW_SECTIONS.add(section);
            }
        }
        return VISIBLE_PREVIEW_SECTIONS;
    }

    /** Dibuja los cuerpos de todas las rutas confirmadas en el lote compartido. */
    private static boolean renderLeadBodies(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Level level,
            List<ActivePreview> leadPreviews,
            Map<Integer, PipeGhostGeometryCache> bufferCaches,
            boolean showFullRoutePreview,
            boolean showNextPiecePreview,
            boolean zoomAnimationEnabled,
            float zoomDurationTicks,
            Frustum frustum,
            RenderType renderType,
            int cameraFluidGroup,
            boolean beforeFluids,
            float partialTick
    ) {
        boolean rendered = false;
        for (ActivePreview leadPreview : leadPreviews) {
            PipeGhostGeometryCache bufferCache = bufferCaches.get(leadPreview.version());
            if (bufferCache != null) {
                if (zoomAnimationEnabled) {
                    if (showFullRoutePreview) {
                        rendered |= renderPipeBodiesRange(
                                poseStack,
                                bufferSource,
                                bufferCache,
                                leadPreview.firstUnstartedPieceIndex(),
                                bufferCache.sections().size(),
                                frustum,
                                renderType,
                                cameraFluidGroup,
                                beforeFluids
                        );
                    }
                    if (showFullRoutePreview || showNextPiecePreview) {
                        rendered |= renderAnimatedPieces(
                                poseStack,
                                bufferSource,
                                level,
                                leadPreview,
                                bufferCache,
                                zoomDurationTicks,
                                frustum,
                                renderType,
                                cameraFluidGroup,
                                beforeFluids,
                                partialTick
                        );
                    }
                    continue;
                }
                int firstSection = leadPreview.pieceIndex();
                int lastSection = showFullRoutePreview
                        ? bufferCache.sections().size()
                        : showNextPiecePreview
                        ? firstSection + 1
                        : firstSection;
                rendered |= renderPipeBodiesRange(
                        poseStack,
                        bufferSource,
                        bufferCache,
                        firstSection,
                        lastSection,
                        frustum,
                        renderType,
                        cameraFluidGroup,
                        beforeFluids
                );
            }
        }
        return rendered;
    }

    /** Dibuja todas las piezas que crecen de forma simultanea en la cascada. */
    private static boolean renderAnimatedPieces(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            Level level,
            ActivePreview leadPreview,
            PipeGhostGeometryCache bufferCache,
            float zoomDurationTicks,
            Frustum frustum,
            RenderType renderType,
            int cameraFluidGroup,
            boolean beforeFluids,
            float partialTick
    ) {
        boolean rendered = false;
        List<PipeGhostGeometryCache.Section> sections = bufferCache.sections();
        for (AnimatedPiece animatedPiece : leadPreview.animatedPieces()) {
            int sectionIndex = animatedPiece.pieceIndex();
            if (sectionIndex < 0 || sectionIndex >= sections.size()) {
                continue;
            }

            PipeGhostGeometryCache.Section section = sections.get(sectionIndex);
            boolean sameFluid = PipeGhostFluidClassifier.matchesCamera(section.fluidMask(), cameraFluidGroup);
            if (beforeFluids == sameFluid || !frustum.isVisible(section.bounds())) {
                continue;
            }

            BlockPos position = leadPreview.pieces().get(sectionIndex).position();
            float animatedScale = PipeGhostCascadeAnimation.scale(
                    level,
                    animatedPiece,
                    partialTick,
                    zoomDurationTicks
            );
            poseStack.pushPose();
            try {
                PipeGhostCascadeAnimation.applyCentered(poseStack, position, animatedScale);
                rendered |= renderBufferCache(poseStack, bufferSource, section.base(), renderType, GHOST_TINT);
                rendered |= renderBufferCache(poseStack, bufferSource, section.missing(), renderType, MISSING_GHOST_TINT);
            } finally {
                poseStack.popPose();
            }
        }
        return rendered;
    }

    /** Dibuja un rango de piezas que el servidor aun no ha colocado. */
    private static boolean renderPipeBodiesRange(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            PipeGhostGeometryCache bufferCache,
            int firstSection,
            int lastSection,
            Frustum frustum,
            RenderType renderType,
            int cameraFluidGroup,
            boolean beforeFluids
    ) {
        boolean rendered = false;
        List<PipeGhostGeometryCache.Section> sections = bufferCache.sections();
        int endIndex = Math.min(lastSection, sections.size());
        for (int index = Math.max(0, firstSection); index < endIndex; index++) {
            PipeGhostGeometryCache.Section section = sections.get(index);
            boolean sameFluid = PipeGhostFluidClassifier.matchesCamera(section.fluidMask(), cameraFluidGroup);
            if (beforeFluids == sameFluid || !frustum.isVisible(section.bounds())) {
                continue;
            }
            rendered |= renderBufferCache(poseStack, bufferSource, section.base(), renderType, GHOST_TINT);
            rendered |= renderBufferCache(poseStack, bufferSource, section.missing(), renderType, MISSING_GHOST_TINT);
        }
        return rendered;
    }

    /** Dibuja cada bufer de capa aplicando un tinte y opacidad estables. */
    private static boolean renderBufferCache(
            PoseStack poseStack,
            MultiBufferSource.BufferSource bufferSource,
            List<SuperByteBuffer> bufferCache,
            RenderType renderType,
            GhostTint tint
    ) {
        if (bufferCache.isEmpty()) {
            return false;
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        bufferCache.forEach(buffer -> buffer
                .color(tint.red(), tint.green(), tint.blue(), tint.alpha())
                .renderInto(poseStack, vertexConsumer));
        return true;
    }

    /** Tinte de cuerpo convertido una sola vez al rango de ocho bits. */
    private record GhostTint(int red, int green, int blue, int alpha) {
        /** Crea un tinte desde canales normalizados. */
        private static GhostTint fromNormalized(float red, float green, float blue, float alpha) {
            return new GhostTint(
                    colorChannel(red),
                    colorChannel(green),
                    colorChannel(blue),
                    colorChannel(alpha)
            );
        }

        /** Convierte un canal normalizado al rango valido del buffer. */
        private static int colorChannel(float value) {
            return Math.max(0, Math.min(255, Math.round(value * 255.0F)));
        }
    }
}
