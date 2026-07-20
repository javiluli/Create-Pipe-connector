package com.javiluli.createpipeconnector.client.render;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.render.overlay.AnchorOverlayRenderer;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class PipeGhostRenderer {
    private static final float GHOST_RED = 1.00F;
    private static final float GHOST_GREEN = 1.00F;
    private static final float GHOST_BLUE = 1.00F;
    private static final float GHOST_ALPHA = 0.42F;
    private static final float OUTLINE_RED = 0.15F;
    private static final float OUTLINE_GREEN = 0.85F;
    private static final float OUTLINE_BLUE = 1.00F;
    private static final float OUTLINE_ALPHA = 0.95F;
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
    private static Level cachedLevel;
    private static int cachedPreviewVersion = -1;
    private static Map<RenderType, SuperByteBuffer> cachedBufferCache = Map.of();

    private PipeGhostRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || minecraft.player == null) {
            clearBufferCache();
            return;
        }

        Selection selection = ClientPipeConnectorState.getSelection();
        if (selection == null) {
            clearBufferCache();
            return;
        }

        List<PreviewPipe> previewPipes = ClientPipeConnectorState.getPreviewPipes();
        List<PlacementTarget> anchors = ClientPipeConnectorState.getAnchors();
        if (previewPipes.isEmpty() && anchors.isEmpty()) {
            clearBufferCache();
            return;
        }

        Map<RenderType, SuperByteBuffer> bufferCache = getBufferCache(minecraft, level, previewPipes, ClientPipeConnectorState.getPreviewVersion());
        if (bufferCache.isEmpty() && anchors.isEmpty()) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 cameraPosition = event.getCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-cameraPosition.x, -cameraPosition.y, -cameraPosition.z);

        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(GHOST_RED, GHOST_GREEN, GHOST_BLUE, GHOST_ALPHA);

        try {
            renderPipeGhosts(poseStack, bufferSource, level, previewPipes, bufferCache);
            AnchorOverlayRenderer.render(poseStack, bufferSource, anchors);
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            poseStack.popPose();
        }
    }

    private static Map<RenderType, SuperByteBuffer> getBufferCache(Minecraft minecraft, Level level, List<PreviewPipe> previewPipes, int previewVersion) {
        if (cachedLevel == level && cachedPreviewVersion == previewVersion) {
            return cachedBufferCache;
        }

        SchematicLevel schematicLevel = buildPreviewWorld(level, previewPipes);
        cachedLevel = level;
        cachedPreviewVersion = previewVersion;
        cachedBufferCache = redrawPreview(minecraft, schematicLevel, previewPipes);
        return cachedBufferCache;
    }

    private static void clearBufferCache() {
        cachedLevel = null;
        cachedPreviewVersion = -1;
        cachedBufferCache = Map.of();
    }

    private static void renderPipeGhosts(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Level level, List<PreviewPipe> previewPipes, Map<RenderType, SuperByteBuffer> bufferCache) {
        if (previewPipes.isEmpty()) {
            return;
        }

        bufferCache.values().forEach(buffer -> buffer.renderInto(poseStack, bufferSource.getBuffer(RenderType.translucent())));
        bufferSource.endBatch(RenderType.translucent());
        renderPipeOutlines(poseStack, bufferSource, level, previewPipes);
    }

    private static SchematicLevel buildPreviewWorld(Level level, List<PreviewPipe> previewPipes) {
        SchematicLevel schematicLevel = new SchematicLevel(BlockPos.ZERO, level);
        schematicLevel.renderMode = true;
        for (PreviewPipe previewPipe : previewPipes) {
            schematicLevel.setBlock(previewPipe.position(), previewPipe.state(), 3);
        }
        return schematicLevel;
    }

    private static Map<RenderType, SuperByteBuffer> redrawPreview(Minecraft minecraft, SchematicLevel schematicLevel, List<PreviewPipe> previewPipes) {
        Map<RenderType, SuperByteBuffer> bufferCache = new LinkedHashMap<>(RenderType.chunkBufferLayers().size());
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        ModelBlockRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        schematicLevel.renderMode = true;
        ModelBlockRenderer.enableCaching();
        try {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                SuperByteBuffer buffer = drawLayer(layer, dispatcher, renderer, schematicLevel, previewPipes, objects);
                if (!buffer.isEmpty()) {
                    bufferCache.put(layer, buffer);
                }
            }
        } finally {
            ModelBlockRenderer.clearCache();
            schematicLevel.renderMode = false;
        }

        return bufferCache;
    }

    private static SuperByteBuffer drawLayer(RenderType layer, BlockRenderDispatcher dispatcher, ModelBlockRenderer renderer, SchematicLevel schematicLevel, List<PreviewPipe> previewPipes, ThreadLocalObjects objects) {
        PoseStack poseStack = objects.poseStack;
        RandomSource random = objects.random;
        BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;

        sbbBuilder.begin();

        for (PreviewPipe previewPipe : previewPipes) {
            BlockPos localPos = previewPipe.position();
            BlockPos worldPos = mutableBlockPos.set(localPos.getX(), localPos.getY(), localPos.getZ());
            BlockState state = schematicLevel.getBlockState(worldPos);

            if (state.getRenderShape() != RenderShape.MODEL) {
                continue;
            }

            BakedModel model = dispatcher.getBlockModel(state);
            BlockEntity blockEntity = schematicLevel.getBlockEntity(worldPos);
            ModelData modelData = blockEntity != null ? blockEntity.getModelData() : ModelData.EMPTY;
            modelData = model.getModelData(schematicLevel, worldPos, state, modelData);

            long seed = state.getSeed(worldPos);
            random.setSeed(seed);
            if (!model.getRenderTypes(state, random, modelData).contains(layer)) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());
            renderer.tesselateBlock(
                    schematicLevel,
                    model,
                    state,
                    worldPos,
                    poseStack,
                    sbbBuilder,
                    true,
                    random,
                    seed,
                    OverlayTexture.NO_OVERLAY,
                    modelData,
                    layer
            );
            poseStack.popPose();
        }

        return sbbBuilder.end();
    }

    private static void renderPipeOutlines(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Level level, List<PreviewPipe> previewPipes) {
        VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lines());
        for (PreviewPipe previewPipe : previewPipes) {
            BlockPos position = previewPipe.position();
            BlockState state = previewPipe.state();
            VoxelShape shape = state.getShape(level, position, CollisionContext.empty());
            if (shape.isEmpty()) {
                continue;
            }

            LevelRenderer.renderVoxelShape(
                    poseStack,
                    lineBuffer,
                    shape,
                    position.getX(),
                    position.getY(),
                    position.getZ(),
                    OUTLINE_RED,
                    OUTLINE_GREEN,
                    OUTLINE_BLUE,
                    OUTLINE_ALPHA,
                    true
            );
        }
        bufferSource.endBatch(RenderType.lines());
    }

    private static final class ThreadLocalObjects {
        private final PoseStack poseStack = new PoseStack();
        private final RandomSource random = RandomSource.createNewThreadLocalInstance();
        private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        private final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
