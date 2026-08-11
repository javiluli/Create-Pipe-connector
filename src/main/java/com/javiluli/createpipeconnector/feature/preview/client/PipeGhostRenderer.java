package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.anchor.client.AnchorOverlayRenderer;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.ActivePreview;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.AnimatedPiece;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;

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
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PipeGhostRenderer {
    private static final RenderType GHOST_RENDER_TYPE = PipeConnectorRenderTypes.ghostTranslucent();
    private static final RenderType PLACEMENT_GHOST_RENDER_TYPE = PipeConnectorRenderTypes.placementGhostTranslucent();
    private static final GhostTint GHOST_TINT = GhostTint.fromNormalized(1.00F, 1.00F, 1.00F, 0.42F);
    private static final GhostTint MISSING_GHOST_TINT = GhostTint.fromNormalized(1.00F, 0.38F, 0.34F, 0.16F);
    private static Level cachedLevel;
    private static int cachedPreviewVersion = -1;
    private static PipeGhostGeometryCache cachedBufferCache = PipeGhostGeometryCache.empty();
    private static Level cachedLeadLevel;
    private static final Map<Integer, PipeGhostGeometryCache> CACHED_LEAD_BUFFER_CACHES = new HashMap<>();

    // Forge ejecuta estos eventos en el hilo de render. Reutilizar las
    // colecciones evita basura por frame sin exponer estado a otros hilos.
    private static final Set<Integer> ACTIVE_LEAD_VERSIONS = new HashSet<>();
    private static final List<PipeGhostGeometryCache.Section> VISIBLE_PREVIEW_SECTIONS = new ArrayList<>();

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
        List<ActivePreview> leadPreviews = ClientPlacementLeadPreview.getActivePreviews(
                level,
                event.getPartialTick()
        );
        boolean showFullRoutePreview = PlacementAnimationClientConfig.showFullRoutePreview();
        boolean showNextPiecePreview = PlacementAnimationClientConfig.showNextPiecePreview();
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

        // Renderizar en el lado de la camara respecto al fluido mantiene el
        // preview visible tanto desde fuera como desde dentro del agua o lava.
        boolean renderBeforeFluids = shouldRenderBeforeFluids(
                event.getCamera(),
                level,
                bufferCache,
                leadPreviews,
                leadBufferCaches,
                showFullRoutePreview,
                showNextPiecePreview,
                anchors
        );
        RenderLevelStageEvent.Stage targetStage = renderBeforeFluids
                ? RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS
                : RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS;
        if (stage != targetStage) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPosition = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        Frustum frustum = event.getFrustum();
        List<PipeGhostGeometryCache.Section> visiblePreviewSections = collectVisibleSections(bufferCache, frustum);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        try {
            boolean renderedPreviewBodies = renderPipeBodies(
                    poseStack,
                    bufferSource,
                    visiblePreviewSections,
                    GHOST_RENDER_TYPE
            );
            if (renderedPreviewBodies) {
                bufferSource.endBatch(GHOST_RENDER_TYPE);
            }
            boolean renderedPlacementBodies = renderLeadBodies(
                    poseStack,
                    bufferSource,
                    level,
                    leadPreviews,
                    leadBufferCaches,
                    showFullRoutePreview,
                    showNextPiecePreview,
                    frustum,
                    event.getPartialTick()
            );
            if (renderedPlacementBodies) {
                bufferSource.endBatch(PLACEMENT_GHOST_RENDER_TYPE);
            }
            PipeGhostOutlineRenderer.renderVisible(poseStack, bufferSource, visiblePreviewSections);
            if (showNextPiecePreview) {
                PipeGhostOutlineRenderer.renderLeadPieces(
                        poseStack,
                        bufferSource,
                        level,
                        leadPreviews,
                        leadBufferCaches,
                        frustum,
                        event.getPartialTick()
                );
            }
            AnchorOverlayRenderer.renderFaces(poseStack, bufferSource, anchors);
            AnchorOverlayRenderer.renderOutlines(poseStack, bufferSource, anchors);
            PipeGhostOutlineRenderer.renderThroughAnchors(poseStack, bufferSource, bufferCache, anchors);
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }

    /** Decide en que fase dibujar cuando preview y camara estan en fluidos distintos. */
    private static boolean shouldRenderBeforeFluids(
            Camera camera,
            Level level,
            PipeGhostGeometryCache previewBufferCache,
            List<ActivePreview> leadPreviews,
            Map<Integer, PipeGhostGeometryCache> leadBufferCaches,
            boolean showFullRoutePreview,
            boolean showNextPiecePreview,
            List<PlacementTarget> anchors
    ) {
        int cameraFluidGroup = PipeGhostFluidClassifier.cameraGroup(camera);
        int cameraFluidMask = 1 << cameraFluidGroup;
        if ((previewBufferCache.fluidMask() & ~cameraFluidMask) != 0) {
            return true;
        }
        for (ActivePreview leadPreview : leadPreviews) {
            PipeGhostGeometryCache leadBufferCache = leadBufferCaches.get(leadPreview.version());
            if (leadBufferCache == null) {
                continue;
            }
            int fluidMask = showFullRoutePreview
                    ? leadBufferCache.fluidMaskFrom(leadPreview.pieceIndex())
                    : showNextPiecePreview
                    ? leadBufferCache.fluidMaskRange(
                            leadPreview.pieceIndex(),
                            leadPreview.firstUnstartedPieceIndex()
                    )
                    : 0;
            if ((fluidMask & ~cameraFluidMask) != 0) {
                return true;
            }
        }
        for (PlacementTarget anchor : anchors) {
            if (PipeGhostFluidClassifier.worldGroup(level.getFluidState(anchor.position())) != cameraFluidGroup) {
                return true;
            }
        }
        return false;
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
            Frustum frustum
    ) {
        VISIBLE_PREVIEW_SECTIONS.clear();
        for (PipeGhostGeometryCache.Section section : bufferCache.sections()) {
            if (frustum.isVisible(section.bounds())) {
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
            Frustum frustum,
            float partialTick
    ) {
        boolean rendered = false;
        for (ActivePreview leadPreview : leadPreviews) {
            PipeGhostGeometryCache bufferCache = bufferCaches.get(leadPreview.version());
            if (bufferCache != null) {
                if (showFullRoutePreview) {
                    rendered |= renderPipeBodiesRange(
                            poseStack,
                            bufferSource,
                            bufferCache,
                            leadPreview.firstUnstartedPieceIndex(),
                            bufferCache.sections().size(),
                            frustum,
                            PLACEMENT_GHOST_RENDER_TYPE
                    );
                }
                if (showFullRoutePreview || showNextPiecePreview) {
                    rendered |= renderAnimatedPieces(
                            poseStack,
                            bufferSource,
                            level,
                            leadPreview,
                            bufferCache,
                            frustum,
                            partialTick
                    );
                }
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
            Frustum frustum,
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
            if (!frustum.isVisible(section.bounds())) {
                continue;
            }

            BlockPos position = leadPreview.pieces().get(sectionIndex).position();
            float animatedScale = PipeGhostCascadeAnimation.scale(
                    level,
                    animatedPiece,
                    partialTick
            );
            poseStack.pushPose();
            try {
                PipeGhostCascadeAnimation.applyCentered(poseStack, position, animatedScale);
                rendered |= renderBufferCache(
                        poseStack,
                        bufferSource,
                        section.base(),
                        PLACEMENT_GHOST_RENDER_TYPE,
                        GHOST_TINT
                );
                rendered |= renderBufferCache(
                        poseStack,
                        bufferSource,
                        section.missing(),
                        PLACEMENT_GHOST_RENDER_TYPE,
                        MISSING_GHOST_TINT
                );
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
            RenderType renderType
    ) {
        boolean rendered = false;
        List<PipeGhostGeometryCache.Section> sections = bufferCache.sections();
        int endIndex = Math.min(lastSection, sections.size());
        for (int index = Math.max(0, firstSection); index < endIndex; index++) {
            PipeGhostGeometryCache.Section section = sections.get(index);
            if (!frustum.isVisible(section.bounds())) {
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
