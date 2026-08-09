package com.javiluli.createpipeconnector.feature.pipe;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Regenera las conexiones visuales y funcionales de las tuberias modificadas. */
public final class PipeNetworkUpdater {
    private static final Direction[] DIRECTIONS = Direction.values();

    /** Impide crear instancias del servicio estatico. */
    private PipeNetworkUpdater() {
    }

    /** Actualiza las tuberias colocadas y sus vecinas para regenerar conexiones. */
    public static void refresh(ServerLevel level, List<BlockPos> path) {
        Set<BlockPos> candidates = new LinkedHashSet<>(path);
        for (BlockPos position : path) {
            for (Direction direction : DIRECTIONS) {
                candidates.add(position.relative(direction));
            }
        }

        for (BlockPos position : candidates) {
            BlockState currentState = level.getBlockState(position);
            if (!CreatePipeBlocks.isConnectablePipe(currentState)) {
                continue;
            }
            BlockState refreshedState = refreshState(level, position, currentState, path);
            if (!refreshedState.equals(currentState)) {
                level.setBlockAndUpdate(position, refreshedState);
            }
        }
    }

    /** Solicita a Create el estado conectado actualizado de una tuberia. */
    private static BlockState refreshState(
            BlockAndTintGetter level,
            BlockPos position,
            BlockState state,
            List<BlockPos> path
    ) {
        try {
            Object block = state.getBlock();
            Method updateBlockState = block.getClass().getMethod(
                    Constants.UPDATE_BLOCK_STATE,
                    BlockState.class,
                    Direction.class,
                    Direction.class,
                    BlockAndTintGetter.class,
                    BlockPos.class
            );
            Direction preferredDirection = PipeRouteGeometry.preferredDirection(path, position);
            return (BlockState) updateBlockState.invoke(block, state, preferredDirection, null, level, position);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return state;
        }
    }
}
