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
import com.mojang.math.Axis;
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
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PipeGhostRenderer {
    private static final ModelResourceLocation MECHANICAL_PUMP_ITEM_MODEL = new ModelResourceLocation(java.util.Objects.requireNonNull(ResourceLocation.tryParse("create:mechanical_pump")), "inventory");
    private static final float GHOST_RED = 1.00F;
    private static final float GHOST_GREEN = 1.00F;
    private static final float GHOST_BLUE = 1.00F;
    private static final float GHOST_ALPHA = 0.42F;
    private static final float MISSING_GHOST_RED = 1.00F;
    private static final float MISSING_GHOST_GREEN = 0.38F;
    private static final float MISSING_GHOST_BLUE = 0.34F;
    private static final float MISSING_GHOST_ALPHA = 0.16F;
    private static final float OUTLINE_RED = 0.15F;
    private static final float OUTLINE_GREEN = 0.85F;
    private static final float OUTLINE_BLUE = 1.00F;
    private static final float OUTLINE_ALPHA = 0.95F;
    private static final float MISSING_OUTLINE_RED = 1.00F;
    private static final float MISSING_OUTLINE_GREEN = 0.25F;
    private static final float MISSING_OUTLINE_BLUE = 0.20F;
    private static final float PUMP_OUTLINE_RED = 1.00F;
    private static final float PUMP_OUTLINE_GREEN = 0.82F;
    private static final float PUMP_OUTLINE_BLUE = 0.18F;
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
    private static Level cachedLevel;
    private static int cachedPreviewVersion = -1;
    private static PreviewBufferCache cachedBufferCache = PreviewBufferCache.empty();

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

        PreviewBufferCache bufferCache = getBufferCache(minecraft, level, previewPipes, ClientPipeConnectorState.getPreviewVersion());
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

    private static PreviewBufferCache getBufferCache(Minecraft minecraft, Level level, List<PreviewPipe> previewPipes, int previewVersion) {
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
        cachedBufferCache = PreviewBufferCache.empty();
    }

    private static void renderPipeGhosts(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Level level, List<PreviewPipe> previewPipes, PreviewBufferCache bufferCache) {
        if (previewPipes.isEmpty()) {
            return;
        }

        renderBufferCache(poseStack, bufferSource, bufferCache.base());
        RenderSystem.setShaderColor(MISSING_GHOST_RED, MISSING_GHOST_GREEN, MISSING_GHOST_BLUE, MISSING_GHOST_ALPHA);
        renderBufferCache(poseStack, bufferSource, bufferCache.missing());
        RenderSystem.setShaderColor(GHOST_RED, GHOST_GREEN, GHOST_BLUE, GHOST_ALPHA);
        renderPipeOutlines(poseStack, bufferSource, level, previewPipes);
    }

    private static void renderBufferCache(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Map<RenderType, SuperByteBuffer> bufferCache) {
        if (bufferCache.isEmpty()) {
            return;
        }

        bufferCache.values().forEach(buffer -> buffer.renderInto(poseStack, bufferSource.getBuffer(RenderType.translucent())));
        bufferSource.endBatch(RenderType.translucent());
    }

    private static SchematicLevel buildPreviewWorld(Level level, List<PreviewPipe> previewPipes) {
        SchematicLevel schematicLevel = new SchematicLevel(BlockPos.ZERO, level);
        schematicLevel.renderMode = true;
        for (PreviewPipe previewPipe : previewPipes) {
            schematicLevel.setBlock(previewPipe.position(), previewPipe.state(), 3);
        }
        return schematicLevel;
    }

    private static PreviewBufferCache redrawPreview(Minecraft minecraft, SchematicLevel schematicLevel, List<PreviewPipe> previewPipes) {
        Map<RenderType, SuperByteBuffer> baseCache = new LinkedHashMap<>(RenderType.chunkBufferLayers().size());
        boolean hasMissingMaterials = hasMissingMaterials(previewPipes);
        Map<RenderType, SuperByteBuffer> missingCache = hasMissingMaterials
                ? new LinkedHashMap<>(RenderType.chunkBufferLayers().size())
                : Map.of();
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        ModelBlockRenderer renderer = dispatcher.getModelRenderer();
        BakedModel pumpModel = minecraft.getModelManager().getModel(MECHANICAL_PUMP_ITEM_MODEL);
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        schematicLevel.renderMode = true;
        ModelBlockRenderer.enableCaching();
        try {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                SuperByteBuffer buffer = drawLayer(layer, dispatcher, renderer, pumpModel, schematicLevel, previewPipes, objects);
                if (!buffer.isEmpty()) {
                    baseCache.put(layer, buffer);
                }

                if (hasMissingMaterials) {
                    SuperByteBuffer missingBuffer = drawMissingLayer(layer, dispatcher, renderer, pumpModel, schematicLevel, previewPipes, objects);
                    if (!missingBuffer.isEmpty()) {
                        missingCache.put(layer, missingBuffer);
                    }
                }
            }
        } finally {
            ModelBlockRenderer.clearCache();
            schematicLevel.renderMode = false;
        }

        return new PreviewBufferCache(baseCache, missingCache);
    }

    private static SuperByteBuffer drawLayer(RenderType layer, BlockRenderDispatcher dispatcher, ModelBlockRenderer renderer, BakedModel pumpModel, SchematicLevel schematicLevel, List<PreviewPipe> previewPipes, ThreadLocalObjects objects) {
        return drawLayer(layer, dispatcher, renderer, pumpModel, schematicLevel, previewPipes, objects, false);
    }

    private static SuperByteBuffer drawMissingLayer(RenderType layer, BlockRenderDispatcher dispatcher, ModelBlockRenderer renderer, BakedModel pumpModel, SchematicLevel schematicLevel, List<PreviewPipe> previewPipes, ThreadLocalObjects objects) {
        return drawLayer(layer, dispatcher, renderer, pumpModel, schematicLevel, previewPipes, objects, true);
    }

    private static SuperByteBuffer drawLayer(RenderType layer, BlockRenderDispatcher dispatcher, ModelBlockRenderer renderer, BakedModel pumpModel, SchematicLevel schematicLevel, List<PreviewPipe> previewPipes, ThreadLocalObjects objects, boolean missingOnly) {
        PoseStack poseStack = objects.poseStack;
        RandomSource random = objects.random;
        BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;

        sbbBuilder.begin();

        for (PreviewPipe previewPipe : previewPipes) {
            if (missingOnly && !previewPipe.missingMaterial()) {
                continue;
            }

            BlockPos localPos = previewPipe.position();
            BlockPos worldPos = mutableBlockPos.set(localPos.getX(), localPos.getY(), localPos.getZ());
            BlockState state = schematicLevel.getBlockState(worldPos);

            if (state.getRenderShape() != RenderShape.MODEL) {
                continue;
            }

            boolean rendersMechanicalPump = isMechanicalPumpPreview(previewPipe);
            BakedModel model = rendersMechanicalPump ? pumpModel : dispatcher.getBlockModel(state);
            ModelData modelData = ModelData.EMPTY;
            if (!rendersMechanicalPump) {
                BlockEntity blockEntity = schematicLevel.getBlockEntity(worldPos);
                if (blockEntity != null) {
                    modelData = blockEntity.getModelData();
                }
            }
            modelData = model.getModelData(schematicLevel, worldPos, state, modelData);

            long seed = state.getSeed(worldPos);
            random.setSeed(seed);
            if (!model.getRenderTypes(state, random, modelData).contains(layer)) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(localPos.getX(), localPos.getY(), localPos.getZ());
            if (rendersMechanicalPump) {
                applyPumpFacingTransform(poseStack, previewPipe.mechanicalPumpFacing().getOpposite());
                renderer.renderModel(
                        poseStack.last(),
                        sbbBuilder,
                        state,
                        model,
                        1.0F,
                        1.0F,
                        1.0F,
                        LevelRenderer.getLightColor(schematicLevel, worldPos),
                        OverlayTexture.NO_OVERLAY,
                        modelData,
                        layer
                );
            } else {
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
            }
            poseStack.popPose();
        }

        return sbbBuilder.end();
    }

    private static void applyPumpFacingTransform(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5D, 0.5D, 0.5D);
        switch (facing) {
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            case UP -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case DOWN -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            case EAST -> {
            }
        }
        poseStack.translate(-0.5D, -0.5D, -0.5D);
    }

    private static void renderPipeOutlines(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Level level, List<PreviewPipe> previewPipes) {
        VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lines());
        for (PreviewPipe previewPipe : previewPipes) {
            BlockPos position = previewPipe.position();
            BlockState state = previewPipe.state();
            boolean rendersMechanicalPump = isMechanicalPumpPreview(previewPipe);
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
                    outlineRed(previewPipe, rendersMechanicalPump),
                    outlineGreen(previewPipe, rendersMechanicalPump),
                    outlineBlue(previewPipe, rendersMechanicalPump),
                    OUTLINE_ALPHA,
                    true
            );
        }
        bufferSource.endBatch(RenderType.lines());
    }

    private static boolean isMechanicalPumpPreview(PreviewPipe previewPipe) {
        return previewPipe.mechanicalPumpFacing() != null;
    }

    private static boolean hasMissingMaterials(List<PreviewPipe> previewPipes) {
        for (PreviewPipe previewPipe : previewPipes) {
            if (previewPipe.missingMaterial()) {
                return true;
            }
        }
        return false;
    }

    private static float outlineRed(PreviewPipe previewPipe, boolean rendersMechanicalPump) {
        if (previewPipe.missingMaterial()) {
            return MISSING_OUTLINE_RED;
        }
        return rendersMechanicalPump ? PUMP_OUTLINE_RED : OUTLINE_RED;
    }

    private static float outlineGreen(PreviewPipe previewPipe, boolean rendersMechanicalPump) {
        if (previewPipe.missingMaterial()) {
            return MISSING_OUTLINE_GREEN;
        }
        return rendersMechanicalPump ? PUMP_OUTLINE_GREEN : OUTLINE_GREEN;
    }

    private static float outlineBlue(PreviewPipe previewPipe, boolean rendersMechanicalPump) {
        if (previewPipe.missingMaterial()) {
            return MISSING_OUTLINE_BLUE;
        }
        return rendersMechanicalPump ? PUMP_OUTLINE_BLUE : OUTLINE_BLUE;
    }

    private record PreviewBufferCache(Map<RenderType, SuperByteBuffer> base, Map<RenderType, SuperByteBuffer> missing) {
        private static PreviewBufferCache empty() {
            return new PreviewBufferCache(Map.of(), Map.of());
        }

        private boolean isEmpty() {
            return base.isEmpty() && missing.isEmpty();
        }
    }

    private static final class ThreadLocalObjects {
        private final PoseStack poseStack = new PoseStack();
        private final RandomSource random = RandomSource.createNewThreadLocalInstance();
        private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        private final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
