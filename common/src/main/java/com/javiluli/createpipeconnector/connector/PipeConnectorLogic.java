package com.javiluli.createpipeconnector.connector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int MIN_ASTAR_VISITED_NODES = 2_048;
    private static final int MAX_ASTAR_VISITED_NODES = 20_000;
    private static final int ASTAR_VISITED_NODES_PER_BLOCK = 160;

    private PipeConnectorLogic() {
    }

    public static boolean isConnectablePipe(BlockState state) {
        return CreatePipeBlocks.isConnectablePipe(state);
    }

    public static boolean isCreateWrench(ItemStack stack) {
        return CreatePipeBlocks.isCreateWrench(stack);
    }

    public static boolean isPipeDisplayToggleTarget(BlockState state) {
        return CreatePipeBlocks.isPipeDisplayToggleTarget(state);
    }

    public static Block getPipeBlock(ItemStack stack) {
        return CreatePipeBlocks.getPipeBlock(stack);
    }

    public static Block getHeldPipeBlock(Player player) {
        return CreatePipeBlocks.getHeldPipeBlock(player);
    }

    public static Block getMechanicalPumpBlock() {
        return CreatePipeBlocks.getMechanicalPumpBlock();
    }

    public static Block getCopperCasingBlock() {
        return CreatePipeBlocks.getCopperCasingBlock();
    }

    public static Block getGlassFluidPipeBlock() {
        return CreatePipeBlocks.getGlassFluidPipeBlock();
    }

    public static boolean supportsCopperCasing(Block pipeBlock) {
        return CreatePipeBlocks.supportsCopperCasing(pipeBlock);
    }

    public static boolean supportsGlassPipeStyle(Block pipeBlock) {
        return CreatePipeBlocks.supportsGlassPipeStyle(pipeBlock);
    }

    public static PipeDisplayToggleResult togglePipeDisplaySegment(ServerLevel level, BlockPos origin) {
        return PipeDisplayToggler.toggleSegment(level, origin);
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
        Block heldPipeBlock = getHeldPipeBlock(player);
        return heldPipeBlock == selection.pipeBlock()
                && isSelectionStillValid(player.level(), selection);
    }

    public static boolean isConnectorModeEnabled(UUID playerId) {
        return PipeConnectorSessions.isConnectorModeEnabled(playerId);
    }

    public static boolean isWithinInteractionRange(Player player, BlockPos position) {
        double maxDistance = player.blockInteractionRange() + 1.0D;
        return player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) <= maxDistance * maxDistance;
    }

    public static void setConnectorModeEnabled(UUID playerId, boolean enabled) {
        PipeConnectorSessions.setConnectorModeEnabled(playerId, enabled);
    }

    public static boolean isAutoPumpsEnabled(UUID playerId) {
        return getPumpMode(playerId).isAutomatic();
    }

    public static void setAutoPumpsEnabled(UUID playerId, boolean enabled) {
        setPumpMode(playerId, enabled ? PumpMode.EFFICIENT : PumpMode.OFF);
    }

    public static PumpMode getPumpMode(UUID playerId) {
        return PipeConnectorSessions.getPumpMode(playerId);
    }

    public static void setPumpMode(UUID playerId, PumpMode mode) {
        PipeConnectorSessions.setPumpMode(playerId, mode);
    }

    public static CopperCasingMode getCopperCasingMode(UUID playerId) {
        return PipeConnectorSessions.getCopperCasingMode(playerId);
    }

    public static void setCopperCasingMode(UUID playerId, CopperCasingMode mode) {
        PipeConnectorSessions.setCopperCasingMode(playerId, mode);
    }

    public static PipeStyleMode getPipeStyleMode(UUID playerId) {
        return PipeConnectorSessions.getPipeStyleMode(playerId);
    }

    public static void setPipeStyleMode(UUID playerId, PipeStyleMode mode) {
        PipeConnectorSessions.setPipeStyleMode(playerId, mode);
    }

    public static boolean isAutoPumpDirectionReversed(UUID playerId) {
        return PipeConnectorSessions.isAutoPumpDirectionReversed(playerId);
    }

    public static void setAutoPumpDirectionReversed(UUID playerId, boolean reversed) {
        PipeConnectorSessions.setAutoPumpDirectionReversed(playerId, reversed);
    }

    public static RoutePriority getRoutePriority(UUID playerId) {
        return PipeConnectorSessions.getRoutePriority(playerId);
    }

    public static void setRoutePriority(UUID playerId, RoutePriority priority) {
        PipeConnectorSessions.setRoutePriority(playerId, priority);
    }

    public static Selection getSelection(UUID playerId) {
        return PipeConnectorSessions.getSelection(playerId);
    }

    public static void setSelection(UUID playerId, Selection selection) {
        PipeConnectorSessions.setSelection(playerId, selection);
    }

    public static void clearSelection(UUID playerId) {
        PipeConnectorSessions.clearSelection(playerId);
    }

    public static List<PlacementTarget> getAnchors(UUID playerId) {
        return PipeConnectorSessions.getAnchors(playerId);
    }

    public static void addAnchor(UUID playerId, PlacementTarget anchor) {
        PipeConnectorSessions.addAnchor(playerId, anchor);
    }

    public static void removeLastAnchor(UUID playerId) {
        PipeConnectorSessions.removeLastAnchor(playerId);
    }

    public static void clearAnchors(UUID playerId) {
        PipeConnectorSessions.clearAnchors(playerId);
    }

    public static List<BlockPos> getManualPumps(UUID playerId) {
        return PipeConnectorSessions.getManualPumps(playerId);
    }

    public static void toggleManualPump(UUID playerId, BlockPos position) {
        PipeConnectorSessions.toggleManualPump(playerId, position);
    }

    public static void removeLastManualPump(UUID playerId) {
        PipeConnectorSessions.removeLastManualPump(playerId);
    }

    public static List<BlockPos> getCopperCasings(UUID playerId) {
        return PipeConnectorSessions.getCopperCasings(playerId);
    }

    public static void toggleCopperCasing(UUID playerId, BlockPos position) {
        PipeConnectorSessions.toggleCopperCasing(playerId, position);
    }

    public static void removeLastCopperCasing(UUID playerId) {
        PipeConnectorSessions.removeLastCopperCasing(playerId);
    }

    public static boolean connect(ServerLevel level, BlockPos startPos, BlockPos endPos, Block pipeBlock) {
        ConnectionPlan plan = buildConnectionPlan(level, startPos, endPos);
        if (plan == null) {
            return false;
        }

        return connect(level, plan, pipeBlock);
    }

    public static boolean connect(ServerLevel level, ConnectionPlan plan, Block pipeBlock) {
        Block pumpBlock = getMechanicalPumpBlock();

        for (BlockPos position : plan.placementPositions()) {
            if (!isTraversableBlock(level, position)) {
                return false;
            }
        }

        Map<BlockPos, BlockState> connectionStates = PipePreviewBuilder.buildConnectionStates(level, plan, pipeBlock);
        for (BlockPos position : plan.placementPositions()) {
            BlockState sourceState = level.getBlockState(position);
            BlockState connectedPipeState = connectionStates.getOrDefault(position, createPipeState(pipeBlock, sourceState));
            BlockState state = plan.pumpPlacements().containsKey(position) && pumpBlock != null
                    ? createPumpState(pumpBlock, sourceState, plan.pumpPlacements().get(position))
                    : createPlacementPipeState(connectedPipeState, sourceState, plan.copperCasingPlacements().contains(position), plan.glassPipePlacements().contains(position));
            level.setBlockAndUpdate(position, state);
        }

        refreshPipeStates(level, plan.path());
        return true;
    }

    public static int countAvailablePipes(Player player, Block pipeBlock) {
        return PipeInventory.countAvailablePipes(player, pipeBlock);
    }

    public static boolean hasEnoughPipes(Player player, Block pipeBlock, int requiredPipes) {
        return player.getAbilities().instabuild || PipeInventory.countAvailablePipes(player, pipeBlock) >= requiredPipes;
    }

    public static int countAvailablePumps(Player player) {
        return PipeInventory.countAvailablePumps(player);
    }

    public static int countAvailableGlassPipes(Player player) {
        return PipeInventory.countAvailableGlassPipes(player);
    }

    public static boolean hasEnoughItems(Player player, Block pipeBlock, ConnectionPlan plan) {
        return PipeInventory.hasEnoughItems(player, pipeBlock, plan);
    }

    public static boolean consumePipes(Player player, Block pipeBlock, int requiredPipes) {
        return PipeInventory.consumePipes(player, pipeBlock, requiredPipes);
    }

    public static boolean consumeItems(Player player, Block pipeBlock, ConnectionPlan plan) {
        return PipeInventory.consumeItems(player, pipeBlock, plan);
    }

    public static int countAvailableCopperCasings(Player player) {
        return PipeInventory.countAvailableCopperCasings(player);
    }

    public static BlockState createPipeState(Block pipeBlock, BlockState sourceState) {
        return CreatePipeBlocks.createPipeState(pipeBlock, sourceState);
    }

    public static BlockState createPumpState(Block pumpBlock, BlockState sourceState, Direction facing) {
        return CreatePipeBlocks.createPumpState(pumpBlock, sourceState, facing);
    }

    public static List<PreviewPipe> buildPreview(Level level, BlockPos startPos, BlockPos endPos, Block pipeBlock) {
        ConnectionPlan plan = buildConnectionPlan(level, startPos, endPos);
        if (plan == null) {
            return List.of();
        }

        return buildPreview(level, plan, pipeBlock);
    }

    public static List<PreviewPipe> buildPreview(Level level, ConnectionPlan plan, Block pipeBlock) {
        return PipePreviewBuilder.buildPreview(level, plan, pipeBlock);
    }

    public static ConnectionPlan withAutoPumps(ConnectionPlan plan) {
        return AutoPumpPlanner.apply(plan);
    }

    public static ConnectionPlan withAutoPumps(ConnectionPlan plan, boolean reversed) {
        return AutoPumpPlanner.apply(plan, reversed);
    }

    public static ConnectionPlan withPumpMode(ConnectionPlan plan, PumpMode mode, boolean reversed) {
        return AutoPumpPlanner.apply(plan, mode, reversed);
    }

    public static ConnectionPlan withManualPumps(ConnectionPlan plan, List<BlockPos> pumpPositions) {
        if (getMechanicalPumpBlock() == null || pumpPositions == null || pumpPositions.isEmpty()) {
            return plan;
        }

        Set<BlockPos> placementPositions = new HashSet<>(plan.placementPositions());
        Map<BlockPos, Direction> pumpPlacements = new HashMap<>(plan.pumpPlacements());
        for (BlockPos position : pumpPositions) {
            if (!placementPositions.contains(position)) {
                continue;
            }

            Direction pumpFacing = straightPumpFacing(plan.path(), position);
            if (pumpFacing != null) {
                pumpPlacements.put(position, pumpFacing);
            }
        }
        return new ConnectionPlan(plan.path(), plan.placementPositions(), pumpPlacements, plan.copperCasingPlacements(), plan.glassPipePlacements());
    }

    public static ConnectionPlan withCopperCasings(ConnectionPlan plan, List<BlockPos> casingPositions, Block pipeBlock) {
        return withCopperCasingMode(plan, CopperCasingMode.MANUAL, casingPositions, pipeBlock);
    }

    public static ConnectionPlan withCopperCasingMode(ConnectionPlan plan, CopperCasingMode mode, List<BlockPos> casingPositions, Block pipeBlock) {
        if (!CreatePipeBlocks.supportsCopperCasing(pipeBlock)) {
            return new ConnectionPlan(plan.path(), plan.placementPositions(), plan.pumpPlacements(), Set.of(), plan.glassPipePlacements());
        }

        CopperCasingMode normalizedMode = mode == null ? CopperCasingMode.MANUAL : mode;
        if (normalizedMode == CopperCasingMode.NONE) {
            return new ConnectionPlan(plan.path(), plan.placementPositions(), plan.pumpPlacements(), Set.of(), plan.glassPipePlacements());
        }

        Set<BlockPos> placementPositions = new HashSet<>(plan.placementPositions());
        Set<BlockPos> copperCasingPlacements = new LinkedHashSet<>();
        if (normalizedMode == CopperCasingMode.ALL) {
            for (BlockPos position : plan.placementPositions()) {
                if (!plan.pumpPlacements().containsKey(position)) {
                    copperCasingPlacements.add(position);
                }
            }
        } else if (casingPositions != null) {
            for (BlockPos position : casingPositions) {
                if (placementPositions.contains(position) && !plan.pumpPlacements().containsKey(position)) {
                    copperCasingPlacements.add(position);
                }
            }
        }
        return new ConnectionPlan(plan.path(), plan.placementPositions(), plan.pumpPlacements(), copperCasingPlacements, plan.glassPipePlacements());
    }

    public static ConnectionPlan withPipeStyleMode(ConnectionPlan plan, PipeStyleMode mode, Block pipeBlock) {
        if (mode != PipeStyleMode.GLASS || !CreatePipeBlocks.supportsGlassPipeStyle(pipeBlock)) {
            return new ConnectionPlan(plan.path(), plan.placementPositions(), plan.pumpPlacements(), plan.copperCasingPlacements(), Set.of());
        }

        Set<BlockPos> glassPipePlacements = new LinkedHashSet<>();
        Map<BlockPos, Integer> pathIndices = pathIndexMap(plan.path());
        for (BlockPos position : plan.placementPositions()) {
            if (plan.pumpPlacements().containsKey(position) || plan.copperCasingPlacements().contains(position)) {
                continue;
            }
            Integer pathIndex = pathIndices.get(position);
            if (pathIndex != null && straightPumpFacingAt(plan.path(), pathIndex) != null) {
                glassPipePlacements.add(position);
            }
        }
        return new ConnectionPlan(plan.path(), plan.placementPositions(), plan.pumpPlacements(), plan.copperCasingPlacements(), glassPipePlacements);
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
        return buildPlacementPlan(level, selection, target, RoutePriority.AUTO);
    }

    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, PlacementTarget target, RoutePriority routePriority) {
        return buildPlacementPlan(
                level,
                selection.position(),
                selection.face(),
                selection.existingPipe(),
                target.position(),
                target.face(),
                target.existingPipe(),
                routePriority
        );
    }

    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, List<PlacementTarget> anchors, PlacementTarget target) {
        return buildPlacementPlan(level, selection, anchors, target, RoutePriority.AUTO);
    }

    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, List<PlacementTarget> anchors, PlacementTarget target, RoutePriority routePriority) {
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
                    waypoint.existingPipe(),
                    routePriority
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
        return buildPlacementPlan(level, startPos, startFace, startIsExistingPipe, endPos, endFace, endIsExistingPipe, RoutePriority.AUTO);
    }

    public static ConnectionPlan buildPlacementPlan(
            Level level,
            BlockPos startPos,
            Direction startFace,
            boolean startIsExistingPipe,
            BlockPos endPos,
            Direction endFace,
            boolean endIsExistingPipe,
            RoutePriority routePriority
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
        List<BlockPos> path = findPlacementPath(level, startPos, resolvedStartFace, startIsExistingPipe, endPos, resolvedEndFace, endIsExistingPipe, normalizePriority(routePriority));
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
            boolean endIsExistingPipe,
            RoutePriority routePriority
    ) {
        if (startIsExistingPipe && endIsExistingPipe) {
            return findFacedPath(level, startPos, startFace, endPos, endFace, routePriority);
        }

        BlockPos routeStart = startIsExistingPipe ? startPos.relative(startFace) : startPos;
        BlockPos routeEnd = endIsExistingPipe ? endPos.relative(endFace) : endPos;
        if (!isTraversable(level, routeStart, startPos, endPos) || !isTraversable(level, routeEnd, startPos, endPos)) {
            return null;
        }

        List<BlockPos> route = findPath(level, routeStart, routeEnd, routePriority);
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
        return findPath(level, startPos, endPos, RoutePriority.AUTO);
    }

    public static List<BlockPos> findPath(Level level, BlockPos startPos, BlockPos endPos, RoutePriority routePriority) {
        return findPath(level, startPos, null, endPos, null, routePriority);
    }

    public static List<BlockPos> findPath(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace) {
        return findPath(level, startPos, startFace, endPos, endFace, RoutePriority.AUTO);
    }

    public static List<BlockPos> findPath(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace, RoutePriority routePriority) {
        RoutePriority normalizedPriority = normalizePriority(routePriority);
        if (startFace != null && endFace != null) {
            return findFacedPath(level, startPos, startFace, endPos, endFace, normalizedPriority);
        }

        List<BlockPos> directPath = tryDirectAxisPaths(level, startPos, endPos, normalizedPriority);
        if (directPath != null) {
            return directPath;
        }
        return findAStarPath(level, startPos, endPos, normalizedPriority);
    }

    private static List<BlockPos> findFacedPath(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace, RoutePriority routePriority) {
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

        List<BlockPos> middlePath = findPath(level, startExitPos, endEntryPos, routePriority);
        if (middlePath == null) {
            return null;
        }

        List<BlockPos> path = new ArrayList<>(middlePath.size() + 2);
        path.add(startPos);
        path.addAll(middlePath);
        path.add(endPos);
        return path;
    }

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

    private static List<BlockPos> findAStarPath(Level level, BlockPos startPos, BlockPos endPos, RoutePriority routePriority) {
        int manhattanDistance = startPos.distManhattan(endPos);
        int padding = Math.max(8, Math.min(32, manhattanDistance / 2));
        int maxVisitedNodes = Math.max(MIN_ASTAR_VISITED_NODES, Math.min(MAX_ASTAR_VISITED_NODES, (manhattanDistance + 1) * ASTAR_VISITED_NODES_PER_BLOCK));

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
                        || nextPos.getZ() < minZ || nextPos.getZ() > maxZ) {
                    continue;
                }
                if (!isTraversable(level, nextPos, startPos, endPos)) {
                    continue;
                }

                int tentativeScore = gScore.get(current.position()) + movementCost(direction, routePriority);
                int tentativeTurns = current.turns() + (current.direction() != null && current.direction() != direction ? 1 : 0);
                int knownScore = gScore.getOrDefault(nextPos, Integer.MAX_VALUE);
                int knownTurns = turnScore.getOrDefault(nextPos, Integer.MAX_VALUE);
                if (tentativeScore > knownScore || (tentativeScore == knownScore && tentativeTurns >= knownTurns)) {
                    continue;
                }

                cameFrom.put(nextPos, current.position());
                gScore.put(nextPos, tentativeScore);
                turnScore.put(nextPos, tentativeTurns);
                openSet.add(new PathNode(nextPos, direction, tentativeScore, tentativeTurns, tentativeScore + heuristic(nextPos, endPos, routePriority)));
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

    static void refreshPipeStates(ServerLevel level, List<BlockPos> path) {
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

    public static BlockAndTintGetter createPreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
        return PipePreviewBuilder.createPreviewWorld(level, previewStates);
    }

    private static int heuristic(BlockPos firstPos, BlockPos secondPos, RoutePriority routePriority) {
        return Math.abs(firstPos.getX() - secondPos.getX())
                + Math.abs(firstPos.getZ() - secondPos.getZ())
                + Math.abs(firstPos.getY() - secondPos.getY()) * normalizePriority(routePriority).verticalCost();
    }

    private static int movementCost(Direction direction, RoutePriority routePriority) {
        return direction.getAxis() == Direction.Axis.Y ? normalizePriority(routePriority).verticalCost() : 1;
    }

    private static RoutePriority normalizePriority(RoutePriority routePriority) {
        return routePriority == null ? RoutePriority.AUTO : routePriority;
    }

    private static Axis[] preferredAxisOrder(BlockPos startPos, BlockPos endPos, RoutePriority routePriority) {
        RoutePriority normalizedPriority = normalizePriority(routePriority);
        Axis primaryHorizontalAxis = Math.abs(Axis.X.distance(startPos, endPos)) >= Math.abs(Axis.Z.distance(startPos, endPos)) ? Axis.X : Axis.Z;
        Axis secondaryHorizontalAxis = primaryHorizontalAxis == Axis.X ? Axis.Z : Axis.X;

        return switch (normalizedPriority) {
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

    private static List<Direction> orderedDirections(BlockPos currentPos, BlockPos endPos, Axis[] preferredAxes, Direction previousDirection) {
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
        } else {
            directions.add(axis.positiveDirection());
            directions.add(axis.positiveDirection().getOpposite());
        }
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

    static Direction directionBetween(BlockPos from, BlockPos to) {
        Direction directFace = directFaceBetween(from, to);
        if (directFace != null) {
            return directFace;
        }

        return Direction.NORTH;
    }

    public static Direction straightPumpFacing(List<BlockPos> path, BlockPos position) {
        int index = path.indexOf(position);
        return straightPumpFacingAt(path, index);
    }

    private static Direction straightPumpFacingAt(List<BlockPos> path, int index) {
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

    private static Map<BlockPos, Integer> pathIndexMap(List<BlockPos> path) {
        Map<BlockPos, Integer> pathIndices = new HashMap<>(path.size());
        for (int index = 0; index < path.size(); index++) {
            pathIndices.put(path.get(index), index);
        }
        return pathIndices;
    }

    private static BlockState createPlacementPipeState(BlockState pipeState, BlockState sourceState, boolean copperCasing, boolean glassPipe) {
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

    public record PreviewPipe(BlockPos position, BlockState state, Direction mechanicalPumpFacing, boolean missingMaterial) {
        public PreviewPipe(BlockPos position, BlockState state) {
            this(position, state, null, false);
        }

        public PreviewPipe(BlockPos position, BlockState state, Direction mechanicalPumpFacing) {
            this(position, state, mechanicalPumpFacing, false);
        }

        public PreviewPipe withMissingMaterial(boolean missingMaterial) {
            return new PreviewPipe(position, state, mechanicalPumpFacing, missingMaterial);
        }

        public PreviewPipe {
            Objects.requireNonNull(position, "position");
            Objects.requireNonNull(state, "state");
        }
    }

    public record ConnectionPlan(List<BlockPos> path, List<BlockPos> placementPositions, Map<BlockPos, Direction> pumpPlacements, Set<BlockPos> copperCasingPlacements, Set<BlockPos> glassPipePlacements) {
        public ConnectionPlan(List<BlockPos> path, List<BlockPos> placementPositions) {
            this(path, placementPositions, Map.of());
        }

        public ConnectionPlan(List<BlockPos> path, List<BlockPos> placementPositions, Map<BlockPos, Direction> pumpPlacements) {
            this(path, placementPositions, pumpPlacements, Set.of());
        }

        public ConnectionPlan(List<BlockPos> path, List<BlockPos> placementPositions, Map<BlockPos, Direction> pumpPlacements, Set<BlockPos> copperCasingPlacements) {
            this(path, placementPositions, pumpPlacements, copperCasingPlacements, Set.of());
        }

        public ConnectionPlan {
            path = List.copyOf(path);
            placementPositions = List.copyOf(placementPositions);
            pumpPlacements = Map.copyOf(pumpPlacements);
            copperCasingPlacements = Set.copyOf(copperCasingPlacements);
            glassPipePlacements = Set.copyOf(glassPipePlacements);
        }

        public int requiredPipes() {
            return placementPositions.size() - requiredPumps() - requiredGlassPipes();
        }

        public int requiredPumps() {
            return pumpPlacements.size();
        }

        public int requiredCopperCasings() {
            return copperCasingPlacements.isEmpty() ? 0 : 1;
        }

        public int requiredGlassPipes() {
            return glassPipePlacements.size();
        }
    }

    public record PipeDisplayToggleResult(boolean glassMode, int changed, int skipped, int total) {
        public static PipeDisplayToggleResult empty(boolean glassMode) {
            return new PipeDisplayToggleResult(glassMode, 0, 0, 0);
        }
    }

    public enum RoutePriority {
        AUTO(1),
        HORIZONTAL_FIRST(1),
        VERTICAL_FIRST(1),
        X_FIRST(1),
        Z_FIRST(1),
        AVOID_VERTICAL(8);

        private final int verticalCost;

        RoutePriority(int verticalCost) {
            this.verticalCost = verticalCost;
        }

        public RoutePriority next() {
            RoutePriority[] priorities = values();
            return priorities[(ordinal() + 1) % priorities.length];
        }

        public RoutePriority previous() {
            RoutePriority[] priorities = values();
            return priorities[(ordinal() + priorities.length - 1) % priorities.length];
        }

        private int verticalCost() {
            return verticalCost;
        }
    }

    public enum PumpMode {
        OFF,
        EFFICIENT,
        SAFE;

        public boolean isAutomatic() {
            return this == EFFICIENT || this == SAFE;
        }

        public PumpMode next() {
            PumpMode[] modes = values();
            return modes[(ordinal() + 1) % modes.length];
        }

        public PumpMode previous() {
            PumpMode[] modes = values();
            return modes[(ordinal() + modes.length - 1) % modes.length];
        }
    }

    public enum CopperCasingMode {
        NONE,
        MANUAL,
        ALL;

        public CopperCasingMode next() {
            CopperCasingMode[] modes = values();
            return modes[(ordinal() + 1) % modes.length];
        }

        public CopperCasingMode previous() {
            CopperCasingMode[] modes = values();
            return modes[(ordinal() + modes.length - 1) % modes.length];
        }
    }

    public enum PipeStyleMode {
        DEFAULT,
        GLASS;

        public PipeStyleMode next() {
            PipeStyleMode[] modes = values();
            return modes[(ordinal() + 1) % modes.length];
        }

        public PipeStyleMode previous() {
            PipeStyleMode[] modes = values();
            return modes[(ordinal() + modes.length - 1) % modes.length];
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
