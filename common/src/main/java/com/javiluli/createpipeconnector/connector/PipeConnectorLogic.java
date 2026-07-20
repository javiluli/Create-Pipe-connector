package com.javiluli.createpipeconnector.connector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private static final Map<UUID, List<PlacementTarget>> ANCHORS = new HashMap<>();

    private PipeConnectorLogic() {
    }

    public static boolean isConnectablePipe(BlockState state) {
        return CONNECTABLE_PIPES.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    public static Block getPipeBlock(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && isConnectablePipe(blockItem.getBlock().defaultBlockState())) {
            return blockItem.getBlock();
        }
        return null;
    }

    public static PlacementTarget resolvePlacementTarget(Level level, BlockPos clickedPos, Direction clickedFace, Block pipeBlock) {
        BlockState clickedState = level.getBlockState(clickedPos);
        if (isConnectablePipe(clickedState)) {
            if (clickedState.getBlock() == pipeBlock) {
                return new PlacementTarget(clickedPos, clickedFace, true);
            }
            return null;
        }

        BlockPos placementPos = clickedState.isAir() || clickedState.canBeReplaced() ? clickedPos : clickedPos.relative(clickedFace);
        BlockState placementState = level.getBlockState(placementPos);
        if (isConnectablePipe(placementState)) {
            if (placementState.getBlock() == pipeBlock) {
                return new PlacementTarget(placementPos, clickedFace.getOpposite(), true);
            }
            return null;
        }
        if (!canPlacePipeAt(level, placementPos)) {
            return null;
        }

        return new PlacementTarget(placementPos, clickedFace, false);
    }

    public static boolean canPlacePipeAt(Level level, BlockPos position) {
        return isTraversableBlock(level, position);
    }

    public static boolean isSelectionStillValid(Level level, Selection selection) {
        BlockState selectionState = level.getBlockState(selection.position());
        if (selection.existingPipe()) {
            return isConnectablePipe(selectionState) && selection.pipeBlock() == selectionState.getBlock();
        }

        return canPlacePipeAt(level, selection.position());
    }

    public static boolean isPlayerInPipeMode(Player player, Selection selection) {
        Block heldPipeBlock = getPipeBlock(player.getOffhandItem());
        return player.getMainHandItem().isEmpty()
                && heldPipeBlock == selection.pipeBlock()
                && isSelectionStillValid(player.level(), selection);
    }

    public static Selection getSelection(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    public static void setSelection(UUID playerId, Selection selection) {
        SELECTIONS.put(playerId, selection);
        ANCHORS.remove(playerId);
    }

    public static void clearSelection(UUID playerId) {
        SELECTIONS.remove(playerId);
        ANCHORS.remove(playerId);
    }

    public static List<PlacementTarget> getAnchors(UUID playerId) {
        return List.copyOf(ANCHORS.getOrDefault(playerId, List.of()));
    }

    public static void addAnchor(UUID playerId, PlacementTarget anchor) {
        List<PlacementTarget> anchors = new ArrayList<>(ANCHORS.getOrDefault(playerId, List.of()));
        if (!anchors.isEmpty() && anchors.get(anchors.size() - 1).position().equals(anchor.position())) {
            anchors.set(anchors.size() - 1, anchor);
        } else {
            anchors.add(anchor);
        }
        ANCHORS.put(playerId, List.copyOf(anchors));
    }

    public static void removeLastAnchor(UUID playerId) {
        List<PlacementTarget> anchors = new ArrayList<>(ANCHORS.getOrDefault(playerId, List.of()));
        if (anchors.isEmpty()) {
            return;
        }

        anchors.remove(anchors.size() - 1);
        if (anchors.isEmpty()) {
            ANCHORS.remove(playerId);
        } else {
            ANCHORS.put(playerId, List.copyOf(anchors));
        }
    }

    public static void clearAnchors(UUID playerId) {
        ANCHORS.remove(playerId);
    }

    public static boolean connect(ServerLevel level, BlockPos startPos, BlockPos endPos, Block pipeBlock) {
        ConnectionPlan plan = buildConnectionPlan(level, startPos, endPos);
        if (plan == null) {
            return false;
        }

        return connect(level, plan, pipeBlock);
    }

    public static boolean connect(ServerLevel level, ConnectionPlan plan, Block pipeBlock) {
        BlockPos startPos = plan.path().get(0);
        BlockState pipeState = createPipeState(pipeBlock, level.getBlockState(startPos));

        for (BlockPos position : plan.placementPositions()) {
            if (!isTraversableBlock(level, position)) {
                return false;
            }
        }

        for (BlockPos position : plan.placementPositions()) {
            level.setBlockAndUpdate(position, pipeState);
        }

        refreshPipeStates(level, plan.path());
        return true;
    }

    public static int countAvailablePipes(Player player, Block pipeBlock) {
        if (player.getAbilities().instabuild) {
            return Integer.MAX_VALUE;
        }

        Item pipeItem = pipeBlock.asItem();
        if (pipeItem == Items.AIR) {
            return 0;
        }

        int count = 0;
        count += countMatchingStacks(player.getInventory().items, pipeItem);
        count += countMatchingStacks(player.getInventory().offhand, pipeItem);
        return count;
    }

    public static boolean hasEnoughPipes(Player player, Block pipeBlock, int requiredPipes) {
        return player.getAbilities().instabuild || countAvailablePipes(player, pipeBlock) >= requiredPipes;
    }

    public static boolean consumePipes(Player player, Block pipeBlock, int requiredPipes) {
        if (requiredPipes <= 0 || player.getAbilities().instabuild) {
            return true;
        }

        if (!hasEnoughPipes(player, pipeBlock, requiredPipes)) {
            return false;
        }

        Item pipeItem = pipeBlock.asItem();
        int remaining = requiredPipes;
        remaining = consumeMatchingStacks(player.getInventory().items, pipeItem, remaining);
        remaining = consumeMatchingStacks(player.getInventory().offhand, pipeItem, remaining);
        player.getInventory().setChanged();
        return remaining == 0;
    }

    public static BlockState createPipeState(Block pipeBlock, BlockState sourceState) {
        BlockState pipeState = pipeBlock.defaultBlockState();
        if (pipeState.hasProperty(BlockStateProperties.WATERLOGGED) && sourceState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            pipeState = pipeState.setValue(BlockStateProperties.WATERLOGGED, sourceState.getValue(BlockStateProperties.WATERLOGGED));
        }
        return pipeState;
    }

    public static List<PreviewPipe> buildPreview(Level level, BlockPos startPos, BlockPos endPos, Block pipeBlock) {
        ConnectionPlan plan = buildConnectionPlan(level, startPos, endPos);
        if (plan == null) {
            return List.of();
        }

        return buildPreview(level, plan, pipeBlock);
    }

    public static List<PreviewPipe> buildPreview(Level level, ConnectionPlan plan, Block pipeBlock) {
        Map<BlockPos, BlockState> previewStates = new HashMap<>();
        for (BlockPos position : plan.path()) {
            BlockState currentState = level.getBlockState(position);
            previewStates.put(position, isConnectablePipe(currentState) ? currentState : createPipeState(pipeBlock, currentState));
        }

        BlockAndTintGetter previewWorld = createPreviewWorld(level, previewStates);
        Map<BlockPos, Direction> preferredDirections = preferredDirectionsForPath(plan.path());
        for (int pass = 0; pass < 3; pass++) {
            boolean changed = false;
            for (BlockPos position : plan.path()) {
                BlockState currentState = previewStates.get(position);
                BlockState updatedState = updatePreviewState(currentState, preferredDirections.getOrDefault(position, Direction.NORTH), previewWorld, position);
                if (!updatedState.equals(currentState)) {
                    previewStates.put(position, updatedState);
                    changed = true;
                }
            }
            if (!changed) {
                break;
            }
        }

        List<PreviewPipe> previewPipes = new ArrayList<>(plan.requiredPipes());
        for (BlockPos position : plan.placementPositions()) {
            previewPipes.add(new PreviewPipe(position, previewStates.get(position)));
        }
        return previewPipes;
    }

    public static ConnectionPlan buildConnectionPlan(Level level, BlockPos startPos, BlockPos endPos) {
        return buildConnectionPlan(level, startPos, null, endPos, null);
    }

    public static ConnectionPlan buildConnectionPlan(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace) {
        return buildPlacementPlan(level, startPos, startFace, true, endPos, endFace, true);
    }

    public static ConnectionPlan buildPlacementPreviewPlan(Level level, BlockPos startPos, Direction startFace, BlockPos targetPos) {
        return buildPlacementPlan(level, startPos, startFace, true, targetPos, null, false);
    }

    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, PlacementTarget target) {
        return buildPlacementPlan(
                level,
                selection.position(),
                selection.face(),
                selection.existingPipe(),
                target.position(),
                target.face(),
                target.existingPipe()
        );
    }

    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, List<PlacementTarget> anchors, PlacementTarget target) {
        List<PlacementTarget> waypoints = new ArrayList<>();
        if (anchors != null) {
            waypoints.addAll(anchors);
        }
        waypoints.add(target);

        SegmentEndpoint start = new SegmentEndpoint(selection.position(), selection.face(), selection.existingPipe());
        List<BlockPos> mergedPath = new ArrayList<>();
        Set<BlockPos> placementPositions = new LinkedHashSet<>();

        for (PlacementTarget waypoint : waypoints) {
            if (start.position().equals(waypoint.position())) {
                start = new SegmentEndpoint(waypoint.position(), waypoint.face(), waypoint.existingPipe());
                continue;
            }

            ConnectionPlan segment = buildPlacementPlan(
                    level,
                    start.position(),
                    start.face(),
                    start.existingPipe(),
                    waypoint.position(),
                    waypoint.face(),
                    waypoint.existingPipe()
            );
            if (segment == null) {
                return null;
            }

            appendSegmentPath(mergedPath, segment.path());
            placementPositions.addAll(segment.placementPositions());
            start = new SegmentEndpoint(waypoint.position(), waypoint.face(), waypoint.existingPipe());
        }

        if (mergedPath.size() < 2) {
            return null;
        }

        return new ConnectionPlan(mergedPath, new ArrayList<>(placementPositions));
    }

    public static ConnectionPlan buildPlacementPlan(
            Level level,
            BlockPos startPos,
            Direction startFace,
            boolean startIsExistingPipe,
            BlockPos endPos,
            Direction endFace,
            boolean endIsExistingPipe
    ) {
        Objects.requireNonNull(startFace, "startFace");
        if (endIsExistingPipe) {
            Objects.requireNonNull(endFace, "endFace");
        }
        if (startPos.equals(endPos)) {
            return null;
        }
        if (startIsExistingPipe && !isConnectablePipe(level.getBlockState(startPos))) {
            return null;
        }
        if (endIsExistingPipe && !isConnectablePipe(level.getBlockState(endPos))) {
            return null;
        }
        if (!startIsExistingPipe && !canPlacePipeAt(level, startPos)) {
            return null;
        }
        if (!endIsExistingPipe && !canPlacePipeAt(level, endPos)) {
            return null;
        }

        Direction resolvedStartFace = startIsExistingPipe ? resolveStraightLineFace(startPos, startFace, endPos) : startFace;
        Direction resolvedEndFace = endIsExistingPipe ? resolveStraightLineFace(endPos, endFace, startPos) : endFace;
        List<BlockPos> path = findPlacementPath(level, startPos, resolvedStartFace, startIsExistingPipe, endPos, resolvedEndFace, endIsExistingPipe);
        if (path == null || path.size() < 2) {
            return null;
        }

        return buildConnectionPlan(level, path, startIsExistingPipe, endIsExistingPipe);
    }

    private static ConnectionPlan buildConnectionPlan(Level level, List<BlockPos> path, boolean startIsExistingPipe, boolean endIsExistingPipe) {
        List<BlockPos> placementPositions = new ArrayList<>();
        for (int index = 0; index < path.size(); index++) {
            if ((index == 0 && startIsExistingPipe) || (index == path.size() - 1 && endIsExistingPipe)) {
                continue;
            }

            BlockPos position = path.get(index);
            BlockState currentState = level.getBlockState(position);
            if (isConnectablePipe(currentState)) {
                continue;
            }
            if (!isTraversableBlock(level, position)) {
                return null;
            }
            placementPositions.add(position);
        }

        return new ConnectionPlan(path, placementPositions);
    }

    private static void appendSegmentPath(List<BlockPos> mergedPath, List<BlockPos> segmentPath) {
        if (mergedPath.isEmpty()) {
            mergedPath.addAll(segmentPath);
            return;
        }

        for (int index = 0; index < segmentPath.size(); index++) {
            BlockPos position = segmentPath.get(index);
            if (index == 0 && position.equals(mergedPath.get(mergedPath.size() - 1))) {
                continue;
            }
            mergedPath.add(position);
        }
    }

    private static List<BlockPos> findPlacementPath(
            Level level,
            BlockPos startPos,
            Direction startFace,
            boolean startIsExistingPipe,
            BlockPos endPos,
            Direction endFace,
            boolean endIsExistingPipe
    ) {
        if (startIsExistingPipe && endIsExistingPipe) {
            return findFacedPath(level, startPos, startFace, endPos, endFace);
        }

        BlockPos routeStart = startIsExistingPipe ? startPos.relative(startFace) : startPos;
        BlockPos routeEnd = endIsExistingPipe ? endPos.relative(endFace) : endPos;
        if (!isTraversable(level, routeStart, startPos, endPos) || !isTraversable(level, routeEnd, startPos, endPos)) {
            return null;
        }

        List<BlockPos> route = findPath(level, routeStart, routeEnd);
        if (route == null) {
            return null;
        }

        List<BlockPos> path = new ArrayList<>(route.size() + 2);
        if (startIsExistingPipe) {
            path.add(startPos);
        }
        path.addAll(route);
        if (endIsExistingPipe) {
            path.add(endPos);
        }
        return path;
    }

    private static Direction resolveStraightLineFace(BlockPos endpointPos, Direction clickedFace, BlockPos targetPos) {
        Direction directFace = directFaceBetween(endpointPos, targetPos);
        if (directFace == null) {
            return clickedFace;
        }

        return directFace;
    }

    public static List<BlockPos> findPath(Level level, BlockPos startPos, BlockPos endPos) {
        return findPath(level, startPos, null, endPos, null);
    }

    public static List<BlockPos> findPath(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace) {
        if (startFace != null && endFace != null) {
            return findFacedPath(level, startPos, startFace, endPos, endFace);
        }

        List<BlockPos> directPath = tryDirectAxisPaths(level, startPos, endPos);
        if (directPath != null) {
            return directPath;
        }
        return findAStarPath(level, startPos, endPos);
    }

    private static List<BlockPos> findFacedPath(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace) {
        BlockPos startExitPos = startPos.relative(startFace);
        BlockPos endEntryPos = endPos.relative(endFace);
        boolean directlyConnected = startExitPos.equals(endPos) && endEntryPos.equals(startPos);
        if (directlyConnected) {
            return List.of(startPos, endPos);
        }
        if (startExitPos.equals(endPos) || endEntryPos.equals(startPos)) {
            return null;
        }
        if (!isTraversable(level, startExitPos, startPos, endPos) || !isTraversable(level, endEntryPos, startPos, endPos)) {
            return null;
        }

        List<BlockPos> middlePath = findPath(level, startExitPos, endEntryPos);
        if (middlePath == null) {
            return null;
        }

        List<BlockPos> path = new ArrayList<>(middlePath.size() + 2);
        path.add(startPos);
        path.addAll(middlePath);
        path.add(endPos);
        return path;
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

    private static Map<BlockPos, Direction> preferredDirectionsForPath(List<BlockPos> path) {
        Map<BlockPos, Direction> preferredDirections = new HashMap<>(path.size());
        for (int index = 0; index < path.size(); index++) {
            BlockPos position = path.get(index);
            if (preferredDirections.containsKey(position)) {
                continue;
            }

            if (index + 1 < path.size()) {
                preferredDirections.put(position, directionBetween(position, path.get(index + 1)));
            } else if (index > 0) {
                preferredDirections.put(position, directionBetween(path.get(index - 1), position));
            } else {
                preferredDirections.put(position, Direction.NORTH);
            }
        }
        return preferredDirections;
    }

    private static Direction directionBetween(BlockPos from, BlockPos to) {
        Direction directFace = directFaceBetween(from, to);
        if (directFace != null) {
            return directFace;
        }

        return Direction.NORTH;
    }

    private static Direction directFaceBetween(BlockPos from, BlockPos to) {
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

    private static boolean isTraversable(Level level, BlockPos position, BlockPos startPos, BlockPos endPos) {
        if (position.equals(startPos) || position.equals(endPos)) {
            return true;
        }

        return isTraversableBlock(level, position);
    }

    private static boolean isTraversableBlock(Level level, BlockPos position) {
        BlockState state = level.getBlockState(position);
        return state.isAir() || state.canBeReplaced() || isConnectablePipe(state);
    }

    private static int countMatchingStacks(List<ItemStack> stacks, Item item) {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static int consumeMatchingStacks(List<ItemStack> stacks, Item item, int remaining) {
        for (ItemStack stack : stacks) {
            if (remaining <= 0) {
                return 0;
            }
            if (!stack.is(item)) {
                continue;
            }
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
        }
        return remaining;
    }

    public record Selection(BlockPos position, Block pipeBlock, Direction face, boolean existingPipe) {
        public Selection {
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(pipeBlock, "pipeBlock");
            Objects.requireNonNull(face, "face");
        }
    }

    public record PlacementTarget(BlockPos position, Direction face, boolean existingPipe) {
        public PlacementTarget {
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(face, "face");
        }
    }

    public record PreviewPipe(BlockPos position, BlockState state) {
        public PreviewPipe {
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(state, "state");
        }
    }

    public record ConnectionPlan(List<BlockPos> path, List<BlockPos> placementPositions) {
        public ConnectionPlan {
            path = List.copyOf(path);
            placementPositions = List.copyOf(placementPositions);
        }

        public int requiredPipes() {
            return placementPositions.size();
        }
    }

    private record PathNode(BlockPos position, Direction direction, int steps, int turns, int priority) {
    }

    private record SegmentEndpoint(BlockPos position, Direction face, boolean existingPipe) {
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
