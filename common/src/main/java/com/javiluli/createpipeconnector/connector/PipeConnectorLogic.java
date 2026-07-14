package com.javiluli.createpipeconnector.connector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;

public final class PipeConnectorLogic {
    private static final Set<ResourceLocation> CONNECTABLE_PIPES = Set.of(
            ResourceLocation.fromNamespaceAndPath("create", "fluid_pipe"),
            ResourceLocation.fromNamespaceAndPath("create", "smart_fluid_pipe")
    );
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();

    private PipeConnectorLogic() {
    }

    public static boolean isConnectablePipe(BlockState state) {
        return CONNECTABLE_PIPES.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    public static Selection getSelection(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    public static void setSelection(UUID playerId, Selection selection) {
        SELECTIONS.put(playerId, selection);
    }

    public static void clearSelection(UUID playerId) {
        SELECTIONS.remove(playerId);
    }

    public static boolean connect(ServerLevel level, BlockPos startPos, BlockPos endPos, Block pipeBlock) {
        List<BlockPos> path = findPath(level, startPos, endPos);
        if (path == null || path.size() < 2) {
            return false;
        }

        BlockState pipeState = createPipeState(pipeBlock, level.getBlockState(startPos));

        for (int index = 1; index < path.size() - 1; index++) {
            BlockPos position = path.get(index);
            BlockState currentState = level.getBlockState(position);
            if (isConnectablePipe(currentState)) {
                continue;
            }
            if (!isTraversable(level, position, startPos, endPos)) {
                return false;
            }
            level.setBlockAndUpdate(position, pipeState);
        }

        refreshPipeStates(level, path);
        return true;
    }

    public static BlockState createPipeState(Block pipeBlock, BlockState sourceState) {
        BlockState pipeState = pipeBlock.defaultBlockState();
        if (pipeState.hasProperty(BlockStateProperties.WATERLOGGED) && sourceState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            pipeState = pipeState.setValue(BlockStateProperties.WATERLOGGED, sourceState.getValue(BlockStateProperties.WATERLOGGED));
        }
        return pipeState;
    }

    public static List<PreviewPipe> buildPreview(Level level, BlockPos startPos, BlockPos endPos, Block pipeBlock) {
        List<BlockPos> path = findPath(level, startPos, endPos);
        if (path == null || path.isEmpty()) {
            return List.of();
        }

        Map<BlockPos, BlockState> previewStates = new HashMap<>();
        for (BlockPos position : path) {
            previewStates.put(position, createPipeState(pipeBlock, level.getBlockState(position)));
        }

        BlockAndTintGetter previewWorld = createPreviewWorld(level, previewStates);
        for (int pass = 0; pass < 3; pass++) {
            boolean changed = false;
            for (BlockPos position : path) {
                BlockState currentState = previewStates.get(position);
                BlockState updatedState = updatePreviewState(currentState, preferredDirectionForPosition(path, position), previewWorld, position);
                if (!updatedState.equals(currentState)) {
                    previewStates.put(position, updatedState);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }

        List<PreviewPipe> previewPipes = new ArrayList<>(path.size());
        for (BlockPos position : path) {
            previewPipes.add(new PreviewPipe(position, previewStates.get(position)));
        }
        return previewPipes;
    }

    public static List<BlockPos> findPath(Level level, BlockPos startPos, BlockPos endPos) {
        List<BlockPos> directPath = tryDirectAxisPaths(level, startPos, endPos);
        if (directPath != null) {
            return directPath;
        }
        return findAStarPath(level, startPos, endPos);
    }

    private static List<BlockPos> tryDirectAxisPaths(Level level, BlockPos startPos, BlockPos endPos) {
        Axis[] preferredOrder = preferredAxisOrder(startPos, endPos);
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

    private static List<BlockPos> findAStarPath(Level level, BlockPos startPos, BlockPos endPos) {
        int manhattanDistance = startPos.distManhattan(endPos);
        int padding = Math.max(8, Math.min(32, manhattanDistance / 2));

        int minX = Math.min(startPos.getX(), endPos.getX()) - padding;
        int minY = Math.min(startPos.getY(), endPos.getY()) - padding;
        int minZ = Math.min(startPos.getZ(), endPos.getZ()) - padding;
        int maxX = Math.max(startPos.getX(), endPos.getX()) + padding;
        int maxY = Math.max(startPos.getY(), endPos.getY()) + padding;
        int maxZ = Math.max(startPos.getZ(), endPos.getZ()) + padding;

        Axis[] preferredAxes = preferredAxisOrder(startPos, endPos);
        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator
                .comparingInt(PathNode::priority)
                .thenComparingInt(PathNode::turns)
                .thenComparingInt(node -> directionPreference(node.direction(), node.position(), endPos, preferredAxes)));
        Map<BlockPos, Integer> gScore = new HashMap<>();
        Map<BlockPos, Integer> turnScore = new HashMap<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Set<BlockPos> closedSet = new HashSet<>();

        gScore.put(startPos, 0);
        turnScore.put(startPos, 0);
        openSet.add(new PathNode(startPos, null, 0, 0, heuristic(startPos, endPos)));

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            if (!closedSet.add(current.position())) {
                continue;
            }

            if (current.position().equals(endPos)) {
                return reconstructPath(cameFrom, current.position());
            }

            for (Direction direction : orderedDirections(current.position(), endPos, preferredAxes, current.direction())) {
                BlockPos nextPos = current.position().relative(direction);
                if (nextPos.getX() < minX || nextPos.getX() > maxX
                        || nextPos.getY() < minY || nextPos.getY() > maxY
                        || nextPos.getZ() < minZ || nextPos.getZ() > maxZ) {
                    continue;
                }
                if (!isTraversable(level, nextPos, startPos, endPos)) {
                    continue;
                }

                int tentativeScore = gScore.get(current.position()) + 1;
                int tentativeTurns = current.turns() + (current.direction() != null && current.direction() != direction ? 1 : 0);
                int knownScore = gScore.getOrDefault(nextPos, Integer.MAX_VALUE);
                int knownTurns = turnScore.getOrDefault(nextPos, Integer.MAX_VALUE);
                if (tentativeScore > knownScore || (tentativeScore == knownScore && tentativeTurns >= knownTurns)) {
                    continue;
                }

                cameFrom.put(nextPos, current.position());
                gScore.put(nextPos, tentativeScore);
                turnScore.put(nextPos, tentativeTurns);
                openSet.add(new PathNode(nextPos, direction, tentativeScore, tentativeTurns, tentativeScore + heuristic(nextPos, endPos)));
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

        java.util.Collections.reverse(path);
        return path;
    }

    private static void refreshPipeStates(ServerLevel level, List<BlockPos> path) {
        Set<BlockPos> candidates = new LinkedHashSet<>(path);
        for (BlockPos position : path) {
            for (Direction direction : DIRECTIONS) {
                candidates.add(position.relative(direction));
            }
        }

        for (BlockPos position : candidates) {
            BlockState currentState = level.getBlockState(position);
            if (!isConnectablePipe(currentState)) {
                continue;
            }
            BlockState refreshedState = refreshPipeState(level, position, currentState, path);
            if (!refreshedState.equals(currentState)) {
                level.setBlockAndUpdate(position, refreshedState);
            }
        }
    }

    private static BlockState refreshPipeState(BlockAndTintGetter level, BlockPos position, BlockState state, List<BlockPos> path) {
        try {
            Object block = state.getBlock();
            Method updateBlockState = block.getClass().getMethod(
                    "updateBlockState",
                    BlockState.class,
                    Direction.class,
                    Direction.class,
                    BlockAndTintGetter.class,
                    BlockPos.class
            );
            Direction preferredDirection = preferredDirectionForPosition(path, position);
            return (BlockState) updateBlockState.invoke(block, state, preferredDirection, null, level, position);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return state;
        }
    }

    private static BlockState updatePreviewState(BlockState state, Direction preferredDirection, BlockAndTintGetter world, BlockPos position) {
        try {
            Method updateBlockState = state.getBlock().getClass().getMethod(
                    "updateBlockState",
                    BlockState.class,
                    Direction.class,
                    Direction.class,
                    BlockAndTintGetter.class,
                    BlockPos.class
            );
            return (BlockState) updateBlockState.invoke(state.getBlock(), state, preferredDirection, null, world, position);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            return state;
        }
    }

    public static BlockAndTintGetter createPreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
        ClassLoader classLoader = PipeConnectorLogic.class.getClassLoader();
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

    private static int heuristic(BlockPos firstPos, BlockPos secondPos) {
        return firstPos.distManhattan(secondPos);
    }

    private static Axis[] preferredAxisOrder(BlockPos startPos, BlockPos endPos) {
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

    private static List<Direction> orderedDirections(BlockPos currentPos, BlockPos endPos, Axis[] preferredAxes, Direction previousDirection) {
        List<Direction> directions = new ArrayList<>(List.of(DIRECTIONS));
        directions.sort(Comparator
                .comparingInt((Direction direction) -> directionPreference(direction, currentPos, endPos, preferredAxes))
                .thenComparingInt(direction -> previousDirection != null && direction == previousDirection ? -1 : 0));
        return directions;
    }

    private static int directionPreference(Direction direction, BlockPos currentPos, BlockPos endPos, Axis[] preferredAxes) {
        if (direction == null) {
            return preferredAxes.length * 10;
        }

        int axisScore = switch (direction.getAxis()) {
            case X -> axisRank(preferredAxes, Axis.X);
            case Y -> axisRank(preferredAxes, Axis.Y);
            case Z -> axisRank(preferredAxes, Axis.Z);
        };
        int stepScore = 0;
        int delta = switch (direction.getAxis()) {
            case X -> endPos.getX() - currentPos.getX();
            case Y -> endPos.getY() - currentPos.getY();
            case Z -> endPos.getZ() - currentPos.getZ();
        };
        if (delta != 0 && Integer.signum(delta) == direction.getStepX() + direction.getStepY() + direction.getStepZ()) {
            stepScore -= 1;
        }
        return axisScore * 10 + stepScore;
    }

    private static int axisRank(Axis[] preferredAxes, Axis axis) {
        for (int index = 0; index < preferredAxes.length; index++) {
            if (preferredAxes[index] == axis) {
                return index;
            }
        }
        return preferredAxes.length;
    }

    private static Direction preferredDirectionForPosition(List<BlockPos> path, BlockPos position) {
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

    private static Direction directionBetween(BlockPos from, BlockPos to) {
        int deltaX = to.getX() - from.getX();
        if (deltaX != 0) {
            return deltaX > 0 ? Direction.EAST : Direction.WEST;
        }

        int deltaY = to.getY() - from.getY();
        if (deltaY != 0) {
            return deltaY > 0 ? Direction.UP : Direction.DOWN;
        }

        int deltaZ = to.getZ() - from.getZ();
        if (deltaZ != 0) {
            return deltaZ > 0 ? Direction.SOUTH : Direction.NORTH;
        }

        return Direction.NORTH;
    }

    private static boolean isTraversable(Level level, BlockPos position, BlockPos startPos, BlockPos endPos) {
        if (position.equals(startPos) || position.equals(endPos)) {
            return true;
        }

        BlockState state = level.getBlockState(position);
        return state.isAir() || state.canBeReplaced() || isConnectablePipe(state);
    }

    public record Selection(BlockPos position, Block pipeBlock) {
        public Selection {
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(pipeBlock, "pipeBlock");
        }
    }

    public record PreviewPipe(BlockPos position, BlockState state) {
        public PreviewPipe {
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(state, "state");
        }
    }

    private record PathNode(BlockPos position, Direction direction, int steps, int turns, int priority) {
    }

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
        };

        abstract int distance(BlockPos current, BlockPos target);

        abstract BlockPos stepTowards(BlockPos current, BlockPos target);
    }
}
