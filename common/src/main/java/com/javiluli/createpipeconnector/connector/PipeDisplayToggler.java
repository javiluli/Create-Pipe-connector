package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeDisplayToggleResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Converts one connected pipe segment between Create's regular and glass
 * display variants without crossing mechanical pumps.
 */
final class PipeDisplayToggler {
    private static final int MAX_TOGGLE_BLOCKS = 512;
    private static final Direction[] DIRECTIONS = Direction.values();

    private PipeDisplayToggler() {
    }

    static PipeDisplayToggleResult toggleSegment(ServerLevel level, BlockPos origin) {
        BlockState originState = level.getBlockState(origin);
        if (!CreatePipeBlocks.isPipeDisplayToggleTarget(originState)) {
            return PipeDisplayToggleResult.empty(false);
        }

        List<BlockPos> segment = collectSegment(level, origin);
        if (segment.isEmpty()) {
            return PipeDisplayToggleResult.empty(false);
        }

        boolean convertToGlass = CreatePipeBlocks.isFluidPipe(originState);
        int changed = 0;
        int skipped = 0;

        for (BlockPos position : segment) {
            BlockState currentState = level.getBlockState(position);
            BlockState newState = convertToGlass
                    ? CreatePipeBlocks.createGlassPipeState(currentState)
                    : CreatePipeBlocks.createRegularPipeState(level, position, currentState);
            if (newState == null || newState.equals(currentState)) {
                if (convertToGlass && CreatePipeBlocks.isFluidPipe(currentState)) {
                    skipped++;
                }
                continue;
            }

            cacheFluidFlows(level, position);
            level.setBlockAndUpdate(position, newState);
            loadFluidFlows(level, position);
            changed++;
        }

        PipeConnectorLogic.refreshPipeStates(level, segment);
        return new PipeDisplayToggleResult(convertToGlass, changed, skipped, segment.size());
    }

    private static List<BlockPos> collectSegment(Level level, BlockPos origin) {
        List<BlockPos> segment = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> openSet = new ArrayDeque<>();
        openSet.add(origin);

        // Prevent malformed or enormous networks from stalling the server tick.
        while (!openSet.isEmpty() && segment.size() < MAX_TOGGLE_BLOCKS) {
            BlockPos position = openSet.removeFirst();
            if (!visited.add(position)) {
                continue;
            }

            BlockState state = level.getBlockState(position);
            if (!CreatePipeBlocks.isPipeDisplayToggleTarget(state)) {
                continue;
            }

            segment.add(position);
            for (Direction direction : DIRECTIONS) {
                if (!CreatePipeBlocks.isPipeOpenAt(state, direction)) {
                    continue;
                }

                BlockPos neighbourPos = position.relative(direction);
                BlockState neighbourState = level.getBlockState(neighbourPos);
                if (CreatePipeBlocks.isMechanicalPump(neighbourState)) {
                    continue;
                }
                if (CreatePipeBlocks.isPipeDisplayToggleTarget(neighbourState)
                        && CreatePipeBlocks.isPipeOpenAt(neighbourState, direction.getOpposite())) {
                    openSet.add(neighbourPos);
                }
            }
        }

        return segment;
    }

    private static void cacheFluidFlows(LevelAccessor level, BlockPos position) {
        invokeFluidTransportMethod(Constants.CACHE_FLOWS, level, position);
    }

    private static void loadFluidFlows(LevelAccessor level, BlockPos position) {
        invokeFluidTransportMethod(Constants.LOAD_FLOWS, level, position);
    }

    private static void invokeFluidTransportMethod(String methodName, LevelAccessor level, BlockPos position) {
        try {
            Class<?> fluidTransport = Class.forName(Constants.CREATE_FLUID_TRANSPORT);
            Method method = fluidTransport.getMethod(methodName, LevelAccessor.class, BlockPos.class);
            method.invoke(null, level, position);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }
}
