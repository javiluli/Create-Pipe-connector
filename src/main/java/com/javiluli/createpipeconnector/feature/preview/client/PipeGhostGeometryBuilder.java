package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.createmod.catnip.client.render.model.BakedModelBufferer;
import net.createmod.catnip.client.render.model.ShadeSeparatedResultConsumer;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Construye y secciona la geometria compartida por todos los previews. */
final class PipeGhostGeometryBuilder {
    private static final int PREVIEW_BLOCK_UPDATE_FLAGS = 3;
    private static final int SECTION_SIZE = 16;
    private static final ModelResourceLocation MECHANICAL_PUMP_ITEM_MODEL = ModelResourceLocation.inventory(
            ResourceLocation.fromNamespaceAndPath(Constants.NAMESPACE, Constants.MECHANICAL_PUMP)
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
     * ya materializadas sin volver a procesar el recorrido durante la animacion.
     */
    static PipeGhostGeometryCache buildProgressive(Minecraft minecraft, Level level, List<PreviewPipe> previewPipes) {
        return build(minecraft, level, previewPipes, true);
    }

    /** Construye geometria espacial o progresiva segun su uso de render. */
    private static PipeGhostGeometryCache build(
            Minecraft minecraft,
            Level level,
            List<PreviewPipe> previewPipes,
            boolean progressive
    ) {
        SchematicLevel schematicLevel = buildPreviewWorld(level, previewPipes);
        BakedModel pumpModel = minecraft.getModelManager().getModel(MECHANICAL_PUMP_ITEM_MODEL);
        ReusableObjects objects = REUSABLE_OBJECTS.get();

        schematicLevel.renderMode = true;
        try {
            int expectedSections = progressive
                    ? previewPipes.size()
                    : Math.max(1, (previewPipes.size() + SECTION_SIZE - 1) / SECTION_SIZE);
            List<PipeGhostGeometryCache.Section> sections = new ArrayList<>(expectedSections);
            if (progressive) {
                List<PreviewPipe> singlePiece = objects.singlePiece;
                for (PreviewPipe previewPipe : previewPipes) {
                    singlePiece.clear();
                    singlePiece.add(previewPipe);
                    sections.add(buildSection(
                            pumpModel,
                            schematicLevel,
                            level,
                            singlePiece,
                            objects,
                            true
                    ));
                }
            } else {
                for (List<PreviewPipe> sectionPipes : partitionSpatially(level, previewPipes)) {
                    sections.add(buildSection(
                            pumpModel,
                            schematicLevel,
                            level,
                            sectionPipes,
                            objects,
                            false
                    ));
                }
            }
            List<PipeGhostGeometryCache.Section> immutableSections = List.copyOf(sections);
            return progressive
                    ? PipeGhostGeometryCache.progressive(immutableSections)
                    : PipeGhostGeometryCache.editable(immutableSections);
        } finally {
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

    /** Divide el preview editable por regiones descartables mediante frustum. */
    private static List<List<PreviewPipe>> partitionSpatially(Level level, List<PreviewPipe> previewPipes) {
        Map<SectionKey, List<PreviewPipe>> sections = new LinkedHashMap<>();
        for (PreviewPipe previewPipe : previewPipes) {
            BlockPos position = previewPipe.position();
            SectionKey key = new SectionKey(
                    Math.floorDiv(position.getX(), SECTION_SIZE),
                    Math.floorDiv(position.getY(), SECTION_SIZE),
                    Math.floorDiv(position.getZ(), SECTION_SIZE),
                    PipeGhostFluidClassifier.worldGroup(level.getFluidState(position))
            );
            sections.computeIfAbsent(key, ignored -> new ArrayList<>()).add(previewPipe);
        }
        return List.copyOf(sections.values());
    }

    /** Construye las capas normal e insuficiente de una seccion. */
    private static PipeGhostGeometryCache.Section buildSection(
            BakedModel pumpModel,
            SchematicLevel schematicLevel,
            Level level,
            List<PreviewPipe> previewPipes,
            ReusableObjects objects,
            boolean progressive
    ) {
        List<SuperByteBuffer> baseCache = buildBuffers(
                pumpModel,
                schematicLevel,
                previewPipes,
                objects,
                false
        );
        List<SuperByteBuffer> missingCache = hasMissingMaterials(previewPipes)
                ? buildBuffers(pumpModel, schematicLevel, previewPipes, objects, true)
                : List.of();
        return new PipeGhostGeometryCache.Section(
                sectionBounds(previewPipes),
                PipeGhostOutlineBuilder.build(level, previewPipes),
                baseCache,
                missingCache,
                PipeGhostFluidClassifier.routeMask(level, previewPipes)
        );
    }

    /** Procesa cada modelo una vez y agrupa la geometria por capa de render. */
    private static List<SuperByteBuffer> buildBuffers(
            BakedModel pumpModel,
            SchematicLevel schematicLevel,
            List<PreviewPipe> previewPipes,
            ReusableObjects objects,
            boolean missingOnly
    ) {
        GeometryCollector collector = objects.collector;
        collector.reset();
        List<BlockPos> regularPipePositions = objects.regularPipePositions;
        regularPipePositions.clear();

        for (PreviewPipe previewPipe : previewPipes) {
            if (missingOnly && !previewPipe.missingMaterial()) {
                continue;
            }

            if (previewPipe.isMechanicalPump()) {
                bufferPump(pumpModel, schematicLevel, previewPipe, objects.poseStack, collector);
            } else {
                regularPipePositions.add(previewPipe.position());
            }
        }

        if (!regularPipePositions.isEmpty()) {
            BakedModelBufferer.bufferBlocks(
                    regularPipePositions.iterator(),
                    schematicLevel,
                    null,
                    false,
                    collector
            );
        }
        return collector.build();
    }

    /** Incorpora el modelo de item de una bomba con la orientacion prevista. */
    private static void bufferPump(
            BakedModel pumpModel,
            SchematicLevel schematicLevel,
            PreviewPipe previewPipe,
            PoseStack poseStack,
            GeometryCollector collector
    ) {
        BlockPos position = previewPipe.position();
        BlockState state = schematicLevel.getBlockState(position);
        poseStack.pushPose();
        try {
            poseStack.translate(position.getX(), position.getY(), position.getZ());
            applyPumpFacingTransform(poseStack, previewPipe.mechanicalPumpFacing().getOpposite());
            BakedModelBufferer.bufferModel(
                    pumpModel,
                    position,
                    schematicLevel,
                    state,
                    poseStack,
                    collector
            );
        } finally {
            poseStack.popPose();
        }
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
    private record SectionKey(int x, int y, int z, int fluidGroup) {
    }

    /** Reutiliza objetos mutables temporales entre secciones reconstruidas. */
    private static final class ReusableObjects {
        private final PoseStack poseStack = new PoseStack();
        private final List<PreviewPipe> singlePiece = new ArrayList<>(1);
        private final List<BlockPos> regularPipePositions = new ArrayList<>();
        private final GeometryCollector collector = new GeometryCollector();
    }

    /** Acumula las salidas modernas de Catnip y conserva sus grupos de sombreado. */
    private static final class GeometryCollector implements ShadeSeparatedResultConsumer {
        private final Map<RenderType, LayerBufferBuilder> builders = new LinkedHashMap<>();

        /** Recoge una malla de Catnip y la agrupa por capa de renderizado. */
        @Override
        public void accept(RenderType renderType, boolean shaded, MeshData data) {
            builders.computeIfAbsent(renderType, ignored -> new LayerBufferBuilder())
                    .add(data, shaded);
        }

        /** Prepara los acumuladores para otra seccion sin volver a crearlos. */
        private void reset() {
            builders.values().forEach(LayerBufferBuilder::reset);
        }

        /** Finaliza cada capa y descarta las que no contienen vertices. */
        private List<SuperByteBuffer> build() {
            if (builders.isEmpty()) {
                return List.of();
            }

            List<SuperByteBuffer> buffers = new ArrayList<>(builders.size());
            builders.forEach((ignored, builder) -> {
                if (!builder.hasGeometry()) {
                    return;
                }
                SuperByteBuffer buffer = builder.build();
                if (!buffer.isEmpty()) {
                    buffers.add(buffer);
                }
            });
            return List.copyOf(buffers);
        }
    }

    /** Inicializa el acumulador con el mismo estado de sombreado que Catnip. */
    private static final class LayerBufferBuilder extends SuperByteBufferBuilder {
        private boolean hasGeometry;

        /** Prepara el acumulador mutable usado para una capa del modelo. */
        private LayerBufferBuilder() {
            prepare();
        }

        /** Agrega una malla y registra que la capa contiene geometria. */
        @Override
        public void add(MeshData data, boolean shaded) {
            super.add(data, shaded);
            hasGeometry = true;
        }

        /** Limpia la malla mutable despues de copiarla al buffer inmutable. */
        private void reset() {
            prepare();
            hasGeometry = false;
        }

        /** Indica si esta capa recibio vertices durante la reconstruccion actual. */
        private boolean hasGeometry() {
            return hasGeometry;
        }
    }
}
