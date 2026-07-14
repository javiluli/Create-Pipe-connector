package com.javiluli.createpipeconnector.client.render;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PipeGhostRenderer {
    private static final float GHOST_RED = 0.84F;
    private static final float GHOST_GREEN = 0.95F;
    private static final float GHOST_BLUE = 1.00F;
    private static final float GHOST_ALPHA = 0.80F;
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

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
            return;
        }

        Selection selection = ClientPipeConnectorState.getSelection();
        if (selection == null) {
            return;
        }

        List<PreviewPipe> previewPipes = ClientPipeConnectorState.getPreviewPipes();
        if (previewPipes.isEmpty()) {
            return;
        }

        SchematicLevel schematicLevel = buildPreviewWorld(level, previewPipes);
        Map<RenderType, SuperByteBuffer> bufferCache = redrawPreview(minecraft, schematicLevel);
        if (bufferCache.isEmpty()) {
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
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor(GHOST_RED, GHOST_GREEN, GHOST_BLUE, GHOST_ALPHA);

        bufferCache.forEach((layer, buffer) -> buffer.renderInto(poseStack, bufferSource.getBuffer(layer)));

        bufferSource.endBatch();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static SchematicLevel buildPreviewWorld(Level level, List<PreviewPipe> previewPipes) {
        SchematicLevel schematicLevel = new SchematicLevel(BlockPos.ZERO, level);
        schematicLevel.renderMode = true;
        for (PreviewPipe previewPipe : previewPipes) {
            schematicLevel.setBlock(previewPipe.position(), previewPipe.state(), 3);
        }
        return schematicLevel;
    }

    private static Map<RenderType, SuperByteBuffer> redrawPreview(Minecraft minecraft, SchematicLevel schematicLevel) {
        Map<RenderType, SuperByteBuffer> bufferCache = new LinkedHashMap<>(RenderType.chunkBufferLayers().size());
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        ModelBlockRenderer renderer = dispatcher.getModelRenderer();
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

        schematicLevel.renderMode = true;
        ModelBlockRenderer.enableCaching();
        try {
            for (RenderType layer : RenderType.chunkBufferLayers()) {
                SuperByteBuffer buffer = drawLayer(layer, dispatcher, renderer, schematicLevel, objects);
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

    private static SuperByteBuffer drawLayer(RenderType layer, BlockRenderDispatcher dispatcher, ModelBlockRenderer renderer, SchematicLevel schematicLevel, ThreadLocalObjects objects) {
        PoseStack poseStack = objects.poseStack;
        RandomSource random = objects.random;
        BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
        ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;

        sbbBuilder.begin();

        for (BlockPos localPos : BlockPos.betweenClosed(
                schematicLevel.getBounds().minX(),
                schematicLevel.getBounds().minY(),
                schematicLevel.getBounds().minZ(),
                schematicLevel.getBounds().maxX(),
                schematicLevel.getBounds().maxY(),
                schematicLevel.getBounds().maxZ()
        )) {
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

    private static final class ThreadLocalObjects {
        private final PoseStack poseStack = new PoseStack();
        private final RandomSource random = RandomSource.createNewThreadLocalInstance();
        private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        private final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
    }
}
