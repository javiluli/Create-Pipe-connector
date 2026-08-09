package com.javiluli.createpipeconnector.feature.pipe;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
        Map<BlockPos, Direction> preferredDirections = PipeRouteGeometry.preferredDirections(path, candidates);
        for (BlockPos position : candidates) {
            BlockState currentState = level.getBlockState(position);
            if (!CreatePipeBlocks.isConnectablePipe(currentState)) {
                continue;
            }
            BlockState refreshedState = CreatePipeBlocks.updatePipeState(
                    currentState,
                    preferredDirections.getOrDefault(position, Direction.NORTH),
                    level,
                    position
            );
            if (!refreshedState.equals(currentState)) {
                level.setBlockAndUpdate(position, refreshedState);
            }
        }
    }
}
