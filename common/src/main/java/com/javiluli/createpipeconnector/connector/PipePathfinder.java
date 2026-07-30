package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.RoutePriority;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Finds traversable pipe routes without exposing pathfinding implementation details
 * through the public connector facade.
 */
final class PipePathfinder {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MIN_VISITED_NODES = 2_048;
    private static final int MAX_VISITED_NODES = 20_000;
    private static final int VISITED_NODES_PER_BLOCK = 160;
    private static final int MIN_SEARCH_PADDING = 8;
    private static final int MAX_SEARCH_PADDING = 32;

    private PipePathfinder() {
    }

    /**
     * Finds a route between two positions and optionally forces the route to leave
     * and enter through the supplied faces.
     *
     * @return an ordered path including both endpoints, or {@code null} when no
     * valid route can be found within the bounded search area
     */
    static List<BlockPos> findPath(
            Level level,
            BlockPos startPos,
            Direction startFace,
            BlockPos endPos,
            Direction endFace,
            RoutePriority routePriority
    ) {
        RoutePriority normalizedPriority = normalizePriority(routePriority);
        if (startFace != null && endFace != null) {
            return findFacedPath(level, startPos, startFace, endPos, endFace, normalizedPriority);
        }

        List<BlockPos> directPath = tryDirectAxisPaths(level, startPos, endPos, normalizedPriority);
        return directPath != null ? directPath : findAStarPath(level, startPos, endPos, normalizedPriority);
    }

    private static List<BlockPos> findFacedPath(
            Level level,
            BlockPos startPos,
            Direction startFace,
            BlockPos endPos,
            Direction endFace,
            RoutePriority routePriority
    ) {
        BlockPos startExitPos = startPos.relative(startFace);
        BlockPos endEntryPos = endPos.relative(endFace);
        if (startExitPos.equals(endPos) && endEntryPos.equals(startPos)) {
            return List.of(startPos, endPos);
        }
        if (startExitPos.equals(endPos) || endEntryPos.equals(startPos)) {
            return null;
        }
        if (!isTraversable(level, startExitPos, startPos, endPos)
                || !isTraversable(level, endEntryPos, startPos, endPos)) {
            return null;
        }

        List<BlockPos> middlePath = findPath(level, startExitPos, null, endEntryPos, null, routePriority);
        if (middlePath == null) {
            return null;
        }

        List<BlockPos> path = new ArrayList<>(middlePath.size() + 2);
        path.add(startPos);
        path.addAll(middlePath);
        path.add(endPos);
        return path;
    }

    /**
     * Tests every Manhattan axis order before invoking A*. Most ordinary routes
     * therefore avoid allocating the larger A* search structures.
     */
    private static List<BlockPos> tryDirectAxisPaths(Level level, BlockPos startPos, BlockPos endPos, RoutePriority routePriority) {
        Axis[] preferredOrder = preferredAxisOrder(startPos, endPos, routePriority);
        List<Axis[]> permutations = new ArrayList<>(List.of(
                new Axis[]{Axis.X, Axis.Y, Axis.Z},
                new Axis[]{Axis.X, Axis.Z, Axis.Y},
                new Axis[]{Axis.Y, Axis.X, Axis.Z},
                new Axis[]{Axis.Y, Axis.Z, Axis.X},
                new Axis[]{Axis.Z, Axis.X, Axis.Y},
                new Axis[]{Axis.Z, Axis.Y, Axis.X}
        ));
        permutations.sort(Comparator.comparingInt(order -> axisOrderDistance(order, preferredOrder)));

        for (Axis[] order : permutations) {
            List<BlockPos> path = new ArrayList<>();
            path.add(startPos);
            BlockPos current = startPos;
            boolean valid = true;

            for (Axis axis : order) {
                while (axis.distance(current, endPos) != 0) {
                    current = axis.stepTowards(current, endPos);
                    if (!isTraversable(level, current, startPos, endPos)) {
                        valid = false;
                        break;
                    }
                    path.add(current);
                }
                if (!valid) {
                    break;
                }
            }

            if (valid && current.equals(endPos)) {
                return path;
            }
        }

        return null;
    }

    /**
     * Runs a bounded A* search. The distance-scaled node budget prevents distant
     * or highly obstructed previews from monopolizing the client tick.
     */
    private static List<BlockPos> findAStarPath(Level level, BlockPos startPos, BlockPos endPos, RoutePriority routePriority) {
        int manhattanDistance = startPos.distManhattan(endPos);
        int padding = Math.max(MIN_SEARCH_PADDING, Math.min(MAX_SEARCH_PADDING, manhattanDistance / 2));
        int maxVisitedNodes = Math.max(
                MIN_VISITED_NODES,
                Math.min(MAX_VISITED_NODES, (manhattanDistance + 1) * VISITED_NODES_PER_BLOCK)
        );

        int minX = Math.min(startPos.getX(), endPos.getX()) - padding;
        int minY = Math.min(startPos.getY(), endPos.getY()) - padding;
        int minZ = Math.min(startPos.getZ(), endPos.getZ()) - padding;
        int maxX = Math.max(startPos.getX(), endPos.getX()) + padding;
        int maxY = Math.max(startPos.getY(), endPos.getY()) + padding;
        int maxZ = Math.max(startPos.getZ(), endPos.getZ()) + padding;

        Axis[] preferredAxes = preferredAxisOrder(startPos, endPos, routePriority);
        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator
                .comparingInt(PathNode::priority)
                .thenComparingInt(PathNode::turns)
                .thenComparingInt(PathNode::steps));
        Map<BlockPos, Integer> gScore = new HashMap<>();
        Map<BlockPos, Integer> turnScore = new HashMap<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();

        gScore.put(startPos, 0);
        turnScore.put(startPos, 0);
        openSet.add(new PathNode(startPos, null, 0, 0, heuristic(startPos, endPos, routePriority)));

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            if (!closedSet.add(current.position())) {
                continue;
            }
            if (closedSet.size() > maxVisitedNodes) {
                return null;
            }
            if (current.position().equals(endPos)) {
                return reconstructPath(cameFrom, current.position());
            }

            for (Direction direction : orderedDirections(current.position(), endPos, preferredAxes, current.direction())) {
                BlockPos nextPos = current.position().relative(direction);
                if (nextPos.getX() < minX || nextPos.getX() > maxX
                        || nextPos.getY() < minY || nextPos.getY() > maxY
                        || nextPos.getZ() < minZ || nextPos.getZ() > maxZ
                        || !isTraversable(level, nextPos, startPos, endPos)) {
                    continue;
                }

                int tentativeScore = gScore.get(current.position()) + movementCost(direction, routePriority);
                int tentativeTurns = current.turns() + (current.direction() != null && current.direction() != direction ? 1 : 0);
                int knownScore = gScore.getOrDefault(nextPos, Integer.MAX_VALUE);
                int knownTurns = turnScore.getOrDefault(nextPos, Integer.MAX_VALUE);
                if (tentativeScore > knownScore || tentativeScore == knownScore && tentativeTurns >= knownTurns) {
                    continue;
                }

                cameFrom.put(nextPos, current.position());
                gScore.put(nextPos, tentativeScore);
                turnScore.put(nextPos, tentativeTurns);
                openSet.add(new PathNode(
                        nextPos,
                        direction,
                        tentativeScore,
                        tentativeTurns,
                        tentativeScore + heuristic(nextPos, endPos, routePriority)
                ));
            }
        }

        return null;
    }

    private static List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> cameFrom, BlockPos endPos) {
        List<BlockPos> path = new ArrayList<>();
        BlockPos current = endPos;
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(current);
        }
        Collections.reverse(path);
        return path;
    }

    private static int heuristic(BlockPos firstPos, BlockPos secondPos, RoutePriority routePriority) {
        return Math.abs(firstPos.getX() - secondPos.getX())
                + Math.abs(firstPos.getZ() - secondPos.getZ())
                + Math.abs(firstPos.getY() - secondPos.getY()) * routePriority.verticalCost();
    }

    private static int movementCost(Direction direction, RoutePriority routePriority) {
        return direction.getAxis() == Direction.Axis.Y ? routePriority.verticalCost() : 1;
    }

    static RoutePriority normalizePriority(RoutePriority routePriority) {
        return routePriority == null ? RoutePriority.AUTO : routePriority;
    }

    private static Axis[] preferredAxisOrder(BlockPos startPos, BlockPos endPos, RoutePriority routePriority) {
        Axis primaryHorizontalAxis = Math.abs(Axis.X.distance(startPos, endPos)) >= Math.abs(Axis.Z.distance(startPos, endPos))
                ? Axis.X
                : Axis.Z;
        Axis secondaryHorizontalAxis = primaryHorizontalAxis == Axis.X ? Axis.Z : Axis.X;

        return switch (routePriority) {
            case HORIZONTAL_FIRST -> new Axis[]{primaryHorizontalAxis, secondaryHorizontalAxis, Axis.Y};
            case VERTICAL_FIRST -> new Axis[]{Axis.Y, primaryHorizontalAxis, secondaryHorizontalAxis};
            case X_FIRST -> new Axis[]{Axis.X, Axis.Z, Axis.Y};
            case Z_FIRST -> new Axis[]{Axis.Z, Axis.X, Axis.Y};
            case AVOID_VERTICAL -> new Axis[]{primaryHorizontalAxis, secondaryHorizontalAxis, Axis.Y};
            case AUTO -> automaticAxisOrder(startPos, endPos);
        };
    }

    private static Axis[] automaticAxisOrder(BlockPos startPos, BlockPos endPos) {
        List<Axis> axes = new ArrayList<>(List.of(Axis.X, Axis.Y, Axis.Z));
        axes.sort(Comparator
                .comparingInt((Axis axis) -> -Math.abs(axis.distance(startPos, endPos)))
                .thenComparingInt(Enum::ordinal));
        return axes.toArray(new Axis[0]);
    }

    private static int axisOrderDistance(Axis[] order, Axis[] preferredOrder) {
        int distance = 0;
        for (int index = 0; index < order.length; index++) {
            if (order[index] != preferredOrder[index]) {
                distance++;
            }
        }
        return distance;
    }

    private static List<Direction> orderedDirections(
            BlockPos currentPos,
            BlockPos endPos,
            Axis[] preferredAxes,
            Direction previousDirection
    ) {
        List<Direction> directions = new ArrayList<>(DIRECTIONS.length);
        for (Axis axis : preferredAxes) {
            addAxisDirections(directions, axis, currentPos, endPos);
        }
        for (Direction direction : DIRECTIONS) {
            if (!directions.contains(direction)) {
                directions.add(direction);
            }
        }
        if (previousDirection != null && directions.remove(previousDirection)) {
            directions.add(0, previousDirection);
        }
        return directions;
    }

    private static void addAxisDirections(List<Direction> directions, Axis axis, BlockPos currentPos, BlockPos endPos) {
        Direction preferredDirection = axis.directionTowards(currentPos, endPos);
        if (preferredDirection != null) {
            directions.add(preferredDirection);
            directions.add(preferredDirection.getOpposite());
            return;
        }
        directions.add(axis.positiveDirection());
        directions.add(axis.positiveDirection().getOpposite());
    }

    static boolean isTraversable(Level level, BlockPos position, BlockPos startPos, BlockPos endPos) {
        if (position.equals(startPos) || position.equals(endPos)) {
            return true;
        }
        return isTraversableBlock(level, position);
    }

    static boolean isTraversableBlock(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.isAir() || state.canBeReplaced() || CreatePipeBlocks.isConnectablePipe(state);
    }

    private record PathNode(BlockPos position, Direction direction, int steps, int turns, int priority) {
    }

    /**
     * Axis-specific movement is modeled polymorphically so the routing loops do
     * not need coordinate-specific conditionals.
     */
    private enum Axis {
        X {
            @Override
            int distance(BlockPos current, BlockPos target) {
                return target.getX() - current.getX();
            }

            @Override
            BlockPos stepTowards(BlockPos current, BlockPos target) {
                return current.offset(Integer.signum(target.getX() - current.getX()), 0, 0);
            }

            @Override
            Direction positiveDirection() {
                return Direction.EAST;
            }
        },
        Y {
            @Override
            int distance(BlockPos current, BlockPos target) {
                return target.getY() - current.getY();
            }

            @Override
            BlockPos stepTowards(BlockPos current, BlockPos target) {
                return current.offset(0, Integer.signum(target.getY() - current.getY()), 0);
            }

            @Override
            Direction positiveDirection() {
                return Direction.UP;
            }
        },
        Z {
            @Override
            int distance(BlockPos current, BlockPos target) {
                return target.getZ() - current.getZ();
            }

            @Override
            BlockPos stepTowards(BlockPos current, BlockPos target) {
                return current.offset(0, 0, Integer.signum(target.getZ() - current.getZ()));
            }

            @Override
            Direction positiveDirection() {
                return Direction.SOUTH;
            }
        };

        abstract int distance(BlockPos current, BlockPos target);

        abstract BlockPos stepTowards(BlockPos current, BlockPos target);

        abstract Direction positiveDirection();

        Direction directionTowards(BlockPos current, BlockPos target) {
            int distance = distance(current, target);
            if (distance == 0) {
                return null;
            }
            Direction positiveDirection = positiveDirection();
            return distance > 0 ? positiveDirection : positiveDirection.getOpposite();
        }
    }
}
