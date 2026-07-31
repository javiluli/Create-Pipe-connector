package com.javiluli.createpipeconnector.connector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless geometry helpers for ordered pipe paths.
 */
final class PipeRouteGeometry {
    private static final Direction[] DIRECTIONS = Direction.values();

    private PipeRouteGeometry() {
    }

    /**
     * Returns the cardinal direction from one aligned position to another.
     * Non-aligned positions fall back to north because Create requires a stable
     * direction when refreshing a pipe state.
     */
    static Direction directionBetween(BlockPos from, BlockPos to) {
        Direction directDirection = directDirectionBetween(from, to);
        return directDirection == null ? Direction.NORTH : directDirection;
    }

    /**
     * Returns the direction a pump can face at a straight path position.
     *
     * @return the route direction, or {@code null} when the position is not on
     * the path or belongs to a corner
     */
    static Direction straightPumpFacing(List<BlockPos> path, BlockPos position) {
        return straightPumpFacingAt(path, path.indexOf(position));
    }

    static Direction straightPumpFacingAt(List<BlockPos> path, int index) {
        if (index < 0 || path.size() < 2) {
            return null;
        }
        BlockPos position = path.get(index);
        if (index == 0) {
            return directionBetween(position, path.get(1));
        }
        if (index == path.size() - 1) {
            return directionBetween(path.get(index - 1), position);
        }

        Direction fromPrevious = directionBetween(path.get(index - 1), position);
        Direction toNext = directionBetween(position, path.get(index + 1));
        return fromPrevious.getAxis() == toNext.getAxis() ? toNext : null;
    }

    static Map<BlockPos, Integer> indexByPosition(List<BlockPos> path) {
        Map<BlockPos, Integer> indices = new HashMap<>(path.size());
        for (int index = 0; index < path.size(); index++) {
            indices.put(path.get(index), index);
        }
        return indices;
    }

    static Direction preferredDirection(List<BlockPos> path, BlockPos position) {
        int index = path.indexOf(position);
        if (index < 0) {
            Set<BlockPos> pathPositions = new HashSet<>(path);
            for (Direction direction : DIRECTIONS) {
                if (pathPositions.contains(position.relative(direction))) {
                    return direction;
                }
            }
            return Direction.NORTH;
        }
        if (index + 1 < path.size()) {
            return directionBetween(path.get(index), path.get(index + 1));
        }
        if (index > 0) {
            return directionBetween(path.get(index - 1), path.get(index));
        }
        return Direction.NORTH;
    }

    static Direction directDirectionBetween(BlockPos from, BlockPos to) {
        int deltaX = to.getX() - from.getX();
        int deltaY = to.getY() - from.getY();
        int deltaZ = to.getZ() - from.getZ();

        if (deltaX != 0 && deltaY == 0 && deltaZ == 0) {
            return deltaX > 0 ? Direction.EAST : Direction.WEST;
        }
        if (deltaY != 0 && deltaX == 0 && deltaZ == 0) {
            return deltaY > 0 ? Direction.UP : Direction.DOWN;
        }
        if (deltaZ != 0 && deltaX == 0 && deltaY == 0) {
            return deltaZ > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return null;
    }
}
