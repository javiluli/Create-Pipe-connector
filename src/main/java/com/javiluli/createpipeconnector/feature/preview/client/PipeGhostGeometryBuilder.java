package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Construye y secciona la geometria compartida por todos los previews. */
final class PipeGhostGeometryBuilder {
    private static final String ITEM_MODEL_VARIANT = "inventory";
    private static final int PREVIEW_BLOCK_UPDATE_FLAGS = 3;
    private static final int SECTION_SIZE = 16;
    private static final ModelResourceLocation MECHANICAL_PUMP_ITEM_MODEL = new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath(Constants.NAMESPACE, Constants.MECHANICAL_PUMP),
            ITEM_MODEL_VARIANT
    );
    private static final ThreadLocal<ReusableObjects> REUSABLE_OBJECTS = ThreadLocal.withInitial(ReusableObjects::new);

    /** Impide crear instancias del constructor de geometria. */
    private PipeGhostGeometryBuilder() {
    }

    /** Construye una cache completa usando un unico nivel esquematico conectado. */
    static PipeGhostGeometryCache build(Minecraft minecraft, Level level, List<PreviewPipe> previewPipes) {
        return build(minecraft, level, previewPipes, false);
    }

    /**
     * Construye la ruta confirmada por pieza para poder ocultar las posiciones
     * ya materializadas sin volver a teselar el recorrido durante la animacion.
     */
    static PipeGhostGeometryCache buildProgressive(Minecraft minecraft, Level level, List<PreviewPipe> previewPipes) {
        return build(minecraft, level, previewPipes, true);
    }

    /** Construye geometria espacial o progresiva segun su uso de render. */
    private static PipeGhostGeometryCache build(
            Minecraft minecraft,
            Level level,
            List<PreviewPipe> previewPipes,
            boolean onePiecePerSection
    ) {
        SchematicLevel schematicLevel = buildPreviewWorld(level, previewPipes);
        BlockRenderDispatcher dispatcher = minecraft.getBlockRenderer();
        ModelBlockRenderer renderer = dispatcher.getModelRenderer();
        BakedModel pumpModel = minecraft.getModelManager().getModel(MECHANICAL_PUMP_ITEM_MODEL);
        ReusableObjects objects = REUSABLE_OBJECTS.get();

        schematicLevel.renderMode = true;
        ModelBlockRenderer.enableCaching();
        try {
            List<PipeGhostGeometryCache.Section> sections = new ArrayList<>();
            for (List<PreviewPipe> sectionPipes : partition(previewPipes, onePiecePerSection)) {
                sections.add(buildSection(
                        dispatcher,
                        renderer,
                        pumpModel,
                        schematicLevel,
                        level,
                        sectionPipes,
                        objects
                ));
            }
            return new PipeGhostGeometryCache(
                    List.copyOf(sections),
                    PipeGhostFluidClassifier.routeMask(level, previewPipes)
            );
        } finally {
            ModelBlockRenderer.clearCache();
            schematicLevel.renderMode = false;
        }
    }

    /** Crea el nivel virtual que permite a Create resolver conexiones vecinas. */
    private static SchematicLevel buildPreviewWorld(Level level, List<PreviewPipe> previewPipes) {
        SchematicLevel schematicLevel = new SchematicLevel(BlockPos.ZERO, level);
        schematicLevel.renderMode = true;
        for (PreviewPipe previewPipe : previewPipes) {
            schematicLevel.setBlock(
                    previewPipe.position(),
                    previewPipe.state(),
                    PREVIEW_BLOCK_UPDATE_FLAGS
            );
        }
        return schematicLevel;
    }

    /** Divide por pieza las rutas progresivas y por region el preview editable. */
    private static List<List<PreviewPipe>> partition(List<PreviewPipe> previewPipes, boolean onePiecePerSection) {
        if (onePiecePerSection) {
            List<List<PreviewPipe>> pieces = new ArrayList<>(previewPipes.size());
            for (PreviewPipe previewPipe : previewPipes) {
                pieces.add(List.of(previewPipe));
            }
            return pieces;
        }

        Map<SectionKey, List<PreviewPipe>> sections = new LinkedHashMap<>();
        for (PreviewPipe previewPipe : previewPipes) {
            BlockPos position = previewPipe.position();
            SectionKey key = new SectionKey(
                    Math.floorDiv(position.getX(), SECTION_SIZE),
                    Math.floorDiv(position.getY(), SECTION_SIZE),
                    Math.floorDiv(position.getZ(), SECTION_SIZE)
            );
            sections.computeIfAbsent(key, ignored -> new ArrayList<>()).add(previewPipe);
        }
        return List.copyOf(sections.values());
    }

    /** Construye las capas normal e insuficiente de una seccion. */
    private static PipeGhostGeometryCache.Section buildSection(
            BlockRenderDispatcher dispatcher,
            ModelBlockRenderer renderer,
            BakedModel pumpModel,
            SchematicLevel schematicLevel,
            Level level,
            List<PreviewPipe> previewPipes,
            ReusableObjects objects
    ) {
        boolean hasMissingMaterials = hasMissingMaterials(previewPipes);
        Map<RenderType, SuperByteBuffer> baseCache = new LinkedHashMap<>(RenderType.chunkBufferLayers().size());
        Map<RenderType, SuperByteBuffer> missingCache = hasMissingMaterials
                ? new LinkedHashMap<>(RenderType.chunkBufferLayers().size())
                : Map.of();
        for (RenderType layer : RenderType.chunkBufferLayers()) {
            SuperByteBuffer buffer = drawLayer(
                    layer,
                    dispatcher,
                    renderer,
                    pumpModel,
                    schematicLevel,
                    previewPipes,
                    objects,
                    false
            );
            if (!buffer.isEmpty()) {
                baseCache.put(layer, buffer);
            }
            if (hasMissingMaterials) {
                SuperByteBuffer missingBuffer = drawLayer(
                        layer,
                        dispatcher,
                        renderer,
                        pumpModel,
                        schematicLevel,
                        previewPipes,
                        objects,
                        true
                );
                if (!missingBuffer.isEmpty()) {
                    missingCache.put(layer, missingBuffer);
                }
            }
        }
        return new PipeGhostGeometryCache.Section(
                sectionBounds(previewPipes),
                List.copyOf(previewPipes),
                baseCache,
                missingCache,
                PipeGhostFluidClassifier.routeMask(level, previewPipes)
        );
    }

    /** Emite los modelos de una capa y filtra opcionalmente materiales faltantes. */
    @SuppressWarnings("removal")
    private static SuperByteBuffer drawLayer(
            RenderType layer,
            BlockRenderDispatcher dispatcher,
            ModelBlockRenderer renderer,
            BakedModel pumpModel,
            SchematicLevel schematicLevel,
            List<PreviewPipe> previewPipes,
            ReusableObjects objects,
            boolean missingOnly
    ) {
        PoseStack poseStack = objects.poseStack;
        RandomSource random = objects.random;
        BlockPos.MutableBlockPos mutableBlockPos = objects.mutableBlockPos;
        ShadedBlockSbbBuilder bufferBuilder = objects.bufferBuilder;
        bufferBuilder.begin();

        for (PreviewPipe previewPipe : previewPipes) {
            if (missingOnly && !previewPipe.missingMaterial()) {
                continue;
            }

            BlockPos localPosition = previewPipe.position();
            BlockPos worldPosition = mutableBlockPos.set(
                    localPosition.getX(),
                    localPosition.getY(),
                    localPosition.getZ()
            );
            BlockState state = schematicLevel.getBlockState(worldPosition);
            if (state.getRenderShape() != RenderShape.MODEL) {
                continue;
            }

            boolean mechanicalPump = previewPipe.isMechanicalPump();
            BakedModel model = mechanicalPump ? pumpModel : dispatcher.getBlockModel(state);
            ModelData modelData = modelData(model, schematicLevel, worldPosition, state, mechanicalPump);
            long seed = state.getSeed(worldPosition);
            random.setSeed(seed);
            if (!model.getRenderTypes(state, random, modelData).contains(layer)) {
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(localPosition.getX(), localPosition.getY(), localPosition.getZ());
            if (mechanicalPump) {
                renderPump(
                        poseStack,
                        bufferBuilder,
                        renderer,
                        model,
                        modelData,
                        schematicLevel,
                        worldPosition,
                        state,
                        layer,
                        previewPipe.mechanicalPumpFacing().getOpposite()
                );
            } else {
                renderer.tesselateBlock(
                        schematicLevel,
                        model,
                        state,
                        worldPosition,
                        poseStack,
                        bufferBuilder,
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
        return bufferBuilder.end();
    }

    /** Resuelve datos de modelo evitando consultar entidades para bombas parciales. */
    private static ModelData modelData(
            BakedModel model,
            SchematicLevel schematicLevel,
            BlockPos position,
            BlockState state,
            boolean mechanicalPump
    ) {
        ModelData modelData = ModelData.EMPTY;
        if (!mechanicalPump) {
            BlockEntity blockEntity = schematicLevel.getBlockEntity(position);
            if (blockEntity != null) {
                modelData = blockEntity.getModelData();
            }
        }
        return model.getModelData(schematicLevel, position, state, modelData);
    }

    /** Renderiza el modelo parcial de bomba con la orientacion del recorrido. */
    @SuppressWarnings("removal")
    private static void renderPump(
            PoseStack poseStack,
            ShadedBlockSbbBuilder bufferBuilder,
            ModelBlockRenderer renderer,
            BakedModel model,
            ModelData modelData,
            SchematicLevel schematicLevel,
            BlockPos position,
            BlockState state,
            RenderType layer,
            Direction facing
    ) {
        applyPumpFacingTransform(poseStack, facing);
        renderer.renderModel(
                poseStack.last(),
                bufferBuilder,
                state,
                model,
                1.0F,
                1.0F,
                1.0F,
                LevelRenderer.getLightColor(schematicLevel, position),
                OverlayTexture.NO_OVERLAY,
                modelData,
                layer
        );
    }

    /** Orienta la bomba dentro de su bloque fantasma. */
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

    /** Calcula el volumen minimo de una seccion. */
    private static AABB sectionBounds(List<PreviewPipe> previewPipes) {
        BlockPos first = previewPipes.get(0).position();
        int minX = first.getX();
        int minY = first.getY();
        int minZ = first.getZ();
        int maxX = minX;
        int maxY = minY;
        int maxZ = minZ;
        for (int index = 1; index < previewPipes.size(); index++) {
            BlockPos position = previewPipes.get(index).position();
            minX = Math.min(minX, position.getX());
            minY = Math.min(minY, position.getY());
            minZ = Math.min(minZ, position.getZ());
            maxX = Math.max(maxX, position.getX());
            maxY = Math.max(maxY, position.getY());
            maxZ = Math.max(maxZ, position.getZ());
        }
        return new AABB(minX, minY, minZ, maxX + 1.0D, maxY + 1.0D, maxZ + 1.0D).inflate(0.1D);
    }

    /** Indica si una seccion contiene alguna pieza sin materiales. */
    private static boolean hasMissingMaterials(List<PreviewPipe> previewPipes) {
        for (PreviewPipe previewPipe : previewPipes) {
            if (previewPipe.missingMaterial()) {
                return true;
            }
        }
        return false;
    }

    /** Coordenada estable de una seccion espacial. */
    private record SectionKey(int x, int y, int z) {
    }

    /** Reutiliza objetos mutables usados durante la teselacion. */
    private static final class ReusableObjects {
        private final PoseStack poseStack = new PoseStack();
        private final RandomSource random = RandomSource.createNewThreadLocalInstance();
        private final BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        // Ponder 1.0.91 solo ofrece este constructor compatible con sombreado de bloque.
        @SuppressWarnings("removal")
        private final ShadedBlockSbbBuilder bufferBuilder = ShadedBlockSbbBuilder.create();
    }
}
