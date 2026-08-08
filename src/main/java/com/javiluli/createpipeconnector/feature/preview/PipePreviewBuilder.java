package com.javiluli.createpipeconnector.feature.preview;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Produce estados conectados para la vista fantasma sin modificar el mundo real.
 */
public final class PipePreviewBuilder {
    /** Impide crear instancias del constructor de vista previa. */
    private PipePreviewBuilder() {
    }

    /** Construye las piezas renderizables de un plan de conexion. */
    public static List<PreviewPipe> buildPreview(Level level, ConnectionPlan plan, Block pipeBlock) {
        Map<BlockPos, BlockState> connectionStates = buildConnectionStates(level, plan, pipeBlock);

        List<PreviewPipe> previewPipes = new ArrayList<>(plan.placementPositions().size());
        Block pumpBlock = CreatePipeBlocks.getMechanicalPumpBlock();
        for (BlockPos position : plan.placementPositions()) {
            Direction pumpFacing = plan.pumpPlacements().get(position);
            BlockState sourceState = level.getBlockState(position);
            BlockState connectedPipeState = connectionStates.getOrDefault(position, CreatePipeBlocks.createPipeState(pipeBlock, sourceState));
            BlockState renderState = pumpFacing != null && pumpBlock != null
                    ? CreatePipeBlocks.createPumpState(pumpBlock, sourceState, pumpFacing)
                    : createPipeRenderState(connectedPipeState, sourceState, plan.copperCasingPlacements().contains(position), plan.glassPipePlacements().contains(position));
            previewPipes.add(new PreviewPipe(position, renderState, pumpFacing));
        }
        return previewPipes;
    }

    /** Calcula estados de tuberia coherentes para todo el recorrido. */
    public static Map<BlockPos, BlockState> buildConnectionStates(Level level, ConnectionPlan plan, Block pipeBlock) {
        Map<BlockPos, BlockState> connectionStates = new HashMap<>(plan.path().size());
        for (BlockPos position : plan.path()) {
            BlockState currentState = level.getBlockState(position);
            connectionStates.put(position, CreatePipeBlocks.isConnectablePipe(currentState) ? currentState : CreatePipeBlocks.createPipeState(pipeBlock, currentState));
        }

        BlockAndTintGetter previewWorld = createPreviewWorld(level, connectionStates);
        Map<BlockPos, Direction> preferredDirections = preferredDirectionsForPath(plan.path());
        // Create deriva los brazos de cada tuberia de sus vecinos. Repetir hasta
        // estabilizar permite que los codos usen cambios del mismo preview.
        for (int pass = 0; pass < 3; pass++) {
            boolean changed = false;
            for (BlockPos position : plan.path()) {
                BlockState currentState = connectionStates.get(position);
                BlockState updatedState = CreatePipeBlocks.updatePipeState(currentState, preferredDirections.getOrDefault(position, Direction.NORTH), previewWorld, position);
                if (!updatedState.equals(currentState)) {
                    connectionStates.put(position, updatedState);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }
        return connectionStates;
    }

    /** Aplica revestimiento o cristal al estado conectado que se renderizara. */
    private static BlockState createPipeRenderState(BlockState pipeState, BlockState sourceState, boolean copperCasing, boolean glassPipe) {
        if (copperCasing) {
            BlockState encasedState = CreatePipeBlocks.createEncasedPipeState(pipeState, sourceState);
            return encasedState == null ? pipeState : encasedState;
        }

        if (glassPipe) {
            BlockState glassState = CreatePipeBlocks.createGlassPipeState(pipeState);
            return glassState == null ? pipeState : glassState;
        }

        return pipeState;
    }

    /** Crea una vista del nivel que prioriza los estados fantasma calculados. */
    public static BlockAndTintGetter createPreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
        return new PreviewWorld(level, previewStates);
    }

    /** Asigna a cada posicion una direccion vecina estable para Create. */
    private static Map<BlockPos, Direction> preferredDirectionsForPath(List<BlockPos> path) {
        Map<BlockPos, Direction> preferredDirections = new HashMap<>(path.size());
        for (int index = 0; index < path.size(); index++) {
            BlockPos position = path.get(index);
            if (preferredDirections.containsKey(position)) {
                continue;
            }

            if (index + 1 < path.size()) {
                preferredDirections.put(position, PipeRouteGeometry.directionBetween(position, path.get(index + 1)));
            } else if (index > 0) {
                preferredDirections.put(position, PipeRouteGeometry.directionBetween(path.get(index - 1), position));
            } else {
                preferredDirections.put(position, Direction.NORTH);
            }
        }
        return preferredDirections;
    }

    /**
     * Adapta un nivel real para que las consultas de vecinos vean primero el preview.
     */
    private static final class PreviewWorld implements BlockAndTintGetter {
        private final Level level;
        private final Map<BlockPos, BlockState> previewStates;

        /** Crea una vista combinada del nivel y sus estados fantasma. */
        private PreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
            this.level = level;
            this.previewStates = previewStates;
        }

        /** Devuelve la entidad de bloque real, ya que el preview no crea entidades. */
        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos position) {
            return level.getBlockEntity(position);
        }

        /** Devuelve el estado fantasma cuando existe y el real en caso contrario. */
        @Override
        public BlockState getBlockState(BlockPos position) {
            return previewStates.getOrDefault(position, level.getBlockState(position));
        }

        /** Devuelve el fluido asociado al estado visible por la vista combinada. */
        @Override
        public FluidState getFluidState(BlockPos position) {
            BlockState previewState = previewStates.get(position);
            return previewState == null ? level.getFluidState(position) : previewState.getFluidState();
        }

        /** Conserva la altura total del nivel real. */
        @Override
        public int getHeight() {
            return level.getHeight();
        }

        /** Conserva la altura minima de construccion del nivel real. */
        @Override
        public int getMinBuildHeight() {
            return level.getMinBuildHeight();
        }

        /** Delega el sombreado direccional en el nivel real. */
        @Override
        public float getShade(Direction direction, boolean shade) {
            return level.getShade(direction, shade);
        }

        /** Delega el motor de iluminacion en el nivel real. */
        @Override
        public LevelLightEngine getLightEngine() {
            return level.getLightEngine();
        }

        /** Delega los tintes de bioma en el nivel real. */
        @Override
        public int getBlockTint(BlockPos position, ColorResolver colorResolver) {
            return level.getBlockTint(position, colorResolver);
        }
    }
}
