package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
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
 * Produces connected block states for the ghost preview without mutating the
 * real world.
 */
final class PipePreviewBuilder {
    private PipePreviewBuilder() {
    }

    static List<PreviewPipe> buildPreview(Level level, ConnectionPlan plan, Block pipeBlock) {
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

    static Map<BlockPos, BlockState> buildConnectionStates(Level level, ConnectionPlan plan, Block pipeBlock) {
        Map<BlockPos, BlockState> connectionStates = new HashMap<>();
        for (BlockPos position : plan.path()) {
            BlockState currentState = level.getBlockState(position);
            connectionStates.put(position, CreatePipeBlocks.isConnectablePipe(currentState) ? currentState : CreatePipeBlocks.createPipeState(pipeBlock, currentState));
        }

        BlockAndTintGetter previewWorld = createPreviewWorld(level, connectionStates);
        Map<BlockPos, Direction> preferredDirections = preferredDirectionsForPath(plan.path());
        // Create derives pipe arms from neighbouring states. Repeating until
        // stable lets corners observe updates made earlier in the same preview.
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

    static BlockAndTintGetter createPreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
        return new PreviewWorld(level, previewStates);
    }

    private static Map<BlockPos, Direction> preferredDirectionsForPath(List<BlockPos> path) {
        Map<BlockPos, Direction> preferredDirections = new HashMap<>(path.size());
        for (int index = 0; index < path.size(); index++) {
            BlockPos position = path.get(index);
            if (preferredDirections.containsKey(position)) {
                continue;
            }

            if (index + 1 < path.size()) {
                preferredDirections.put(position, PipeConnectorLogic.directionBetween(position, path.get(index + 1)));
            } else if (index > 0) {
                preferredDirections.put(position, PipeConnectorLogic.directionBetween(path.get(index - 1), position));
            } else {
                preferredDirections.put(position, Direction.NORTH);
            }
        }
        return preferredDirections;
    }

    private static final class PreviewWorld implements BlockAndTintGetter {
        private final Level level;
        private final Map<BlockPos, BlockState> previewStates;

        private PreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
            this.level = level;
            this.previewStates = previewStates;
        }

        @Nullable
        @Override
        public BlockEntity getBlockEntity(BlockPos position) {
            return level.getBlockEntity(position);
        }

        @Override
        public BlockState getBlockState(BlockPos position) {
            return previewStates.getOrDefault(position, level.getBlockState(position));
        }

        @Override
        public FluidState getFluidState(BlockPos position) {
            BlockState previewState = previewStates.get(position);
            return previewState == null ? level.getFluidState(position) : previewState.getFluidState();
        }

        @Override
        public int getHeight() {
            return level.getHeight();
        }

        @Override
        public int getMinBuildHeight() {
            return level.getMinBuildHeight();
        }

        @Override
        public float getShade(Direction direction, boolean shade) {
            return level.getShade(direction, shade);
        }

        @Override
        public LevelLightEngine getLightEngine() {
            return level.getLightEngine();
        }

        @Override
        public int getBlockTint(BlockPos position, ColorResolver colorResolver) {
            return level.getBlockTint(position, colorResolver);
        }
    }
}
