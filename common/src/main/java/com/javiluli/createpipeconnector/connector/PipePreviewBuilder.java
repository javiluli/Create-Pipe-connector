package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PipePreviewBuilder {
    private PipePreviewBuilder() {
    }

    static List<PreviewPipe> buildPreview(Level level, ConnectionPlan plan, Block pipeBlock) {
        Map<BlockPos, BlockState> connectionStates = new HashMap<>();
        for (BlockPos position : plan.path()) {
            BlockState currentState = level.getBlockState(position);
            connectionStates.put(position, CreatePipeBlocks.isConnectablePipe(currentState) ? currentState : CreatePipeBlocks.createPipeState(pipeBlock, currentState));
        }

        BlockAndTintGetter previewWorld = createPreviewWorld(level, connectionStates);
        Map<BlockPos, Direction> preferredDirections = preferredDirectionsForPath(plan.path());
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

        List<PreviewPipe> previewPipes = new ArrayList<>(plan.placementPositions().size());
        Block pumpBlock = CreatePipeBlocks.getMechanicalPumpBlock();
        for (BlockPos position : plan.placementPositions()) {
            Direction pumpFacing = plan.pumpPlacements().get(position);
            BlockState renderState = pumpFacing != null && pumpBlock != null
                    ? CreatePipeBlocks.createPumpState(pumpBlock, level.getBlockState(position), pumpFacing)
                    : connectionStates.get(position);
            previewPipes.add(new PreviewPipe(position, renderState, pumpFacing));
        }
        return previewPipes;
    }

    static BlockAndTintGetter createPreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
        ClassLoader classLoader = PipePreviewBuilder.class.getClassLoader();
        return (BlockAndTintGetter) Proxy.newProxyInstance(classLoader, new Class<?>[]{BlockAndTintGetter.class}, (proxy, method, args) -> {
            String methodName = method.getName();
            if ("getBlockState".equals(methodName) && args != null && args.length == 1 && args[0] instanceof BlockPos blockPos) {
                return previewStates.getOrDefault(blockPos, level.getBlockState(blockPos));
            }
            if ("getBlockEntity".equals(methodName) && args != null && args.length == 1 && args[0] instanceof BlockPos blockPos) {
                return level.getBlockEntity(blockPos);
            }
            if ("toString".equals(methodName)) {
                return "PreviewWorldProxy";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
                return proxy == args[0];
            }

            try {
                return method.invoke(level, args);
            } catch (ReflectiveOperationException exception) {
                Class<?> returnType = method.getReturnType();
                if (returnType == boolean.class) {
                    return false;
                }
                if (returnType == int.class || returnType == short.class || returnType == byte.class || returnType == long.class) {
                    return 0;
                }
                if (returnType == float.class || returnType == double.class) {
                    return 0.0;
                }
                if (returnType == char.class) {
                    return '\0';
                }
                return null;
            }
        });
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
}
