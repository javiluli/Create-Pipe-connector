package com.javiluli.createpipeconnector.feature.connector.planning;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.routing.PipePathfinder;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Construye planes de colocacion a partir de extremos, caras y anclas. */
public final class ConnectionPlanBuilder {
    /** Impide crear instancias del constructor estatico. */
    private ConnectionPlanBuilder() {
    }

    /** Calcula un plan basico entre dos posiciones con prioridad automatica. */
    public static ConnectionPlan build(Level level, BlockPos startPos, BlockPos endPos) {
        return build(level, startPos, null, endPos, null);
    }

    /** Calcula un plan basico respetando las caras de ambos extremos. */
    public static ConnectionPlan build(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace) {
        return buildSegment(level, startPos, startFace, true, endPos, endFace, true, RoutePriority.AUTO);
    }

    /** Calcula un plan entre una seleccion y un objetivo. */
    public static ConnectionPlan build(Level level, Selection selection, PlacementTarget target, RoutePriority priority) {
        return buildSegment(
                level,
                selection.position(),
                selection.face(),
                selection.existingPipe(),
                target.position(),
                target.face(),
                target.existingPipe(),
                priority
        );
    }

    /** Calcula y fusiona los tramos definidos por seleccion, anclas y objetivo. */
    public static ConnectionPlan build(
            Level level,
            Selection selection,
            List<PlacementTarget> anchors,
            PlacementTarget target,
            RoutePriority priority
    ) {
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
            ConnectionPlan segment = buildSegment(
                    level,
                    start.position(),
                    start.face(),
                    start.existingPipe(),
                    waypoint.position(),
                    waypoint.face(),
                    waypoint.existingPipe(),
                    priority
            );
            if (segment == null) {
                return null;
            }
            appendSegmentPath(mergedPath, segment.path());
            placementPositions.addAll(segment.placementPositions());
            if (!waypoint.existingPipe() && !CreatePipeBlocks.isConnectablePipe(level.getBlockState(waypoint.position()))) {
                placementPositions.add(waypoint.position());
            }
            start = new SegmentEndpoint(waypoint.position(), waypoint.face(), waypoint.existingPipe());
        }

        return mergedPath.size() < 2 ? null : new ConnectionPlan(mergedPath, new ArrayList<>(placementPositions));
    }

    /** Calcula y valida un tramo completo con una prioridad de ejes concreta. */
    public static ConnectionPlan buildSegment(
            Level level,
            BlockPos startPos,
            Direction startFace,
            boolean startIsExistingPipe,
            BlockPos endPos,
            Direction endFace,
            boolean endIsExistingPipe,
            RoutePriority priority
    ) {
        Objects.requireNonNull(startFace, "startFace");
        if (endIsExistingPipe) {
            Objects.requireNonNull(endFace, "endFace");
        }
        if (startPos.equals(endPos)
                || startIsExistingPipe && !CreatePipeBlocks.isConnectablePipe(level.getBlockState(startPos))
                || endIsExistingPipe && !CreatePipeBlocks.isConnectablePipe(level.getBlockState(endPos))
                || !startIsExistingPipe && !canPlacePipeAt(level, startPos)
                || !endIsExistingPipe && !canPlacePipeAt(level, endPos)) {
            return null;
        }

        Direction resolvedStartFace = startIsExistingPipe
                ? resolveStraightLineFace(startPos, startFace, endPos)
                : startFace;
        Direction resolvedEndFace = endIsExistingPipe
                ? resolveStraightLineFace(endPos, endFace, startPos)
                : endFace;
        List<BlockPos> path = findPlacementPath(
                level,
                startPos,
                resolvedStartFace,
                startIsExistingPipe,
                endPos,
                resolvedEndFace,
                endIsExistingPipe,
                PipePathfinder.normalizePriority(priority)
        );
        return path == null || path.size() < 2 ? null : createPlan(level, path, startIsExistingPipe, endIsExistingPipe);
    }

    /** Comprueba si una posicion admite una tuberia nueva. */
    private static boolean canPlacePipeAt(Level level, BlockPos position) {
        return PipePathfinder.isTraversableBlock(level, position);
    }

    /** Obtiene las posiciones realmente colocables de un recorrido calculado. */
    private static ConnectionPlan createPlan(Level level, List<BlockPos> path, boolean startIsExistingPipe, boolean endIsExistingPipe) {
        List<BlockPos> placementPositions = new ArrayList<>();
        for (int index = 0; index < path.size(); index++) {
            if ((index == 0 && startIsExistingPipe) || (index == path.size() - 1 && endIsExistingPipe)) {
                continue;
            }
            BlockPos position = path.get(index);
            BlockState currentState = level.getBlockState(position);
            if (CreatePipeBlocks.isConnectablePipe(currentState)) {
                continue;
            }
            if (!PipePathfinder.isTraversableBlock(level, position)) {
                return null;
            }
            placementPositions.add(position);
        }
        return new ConnectionPlan(path, placementPositions);
    }

    /** Une un tramo a la ruta acumulada evitando duplicar su punto compartido. */
    private static void appendSegmentPath(List<BlockPos> mergedPath, List<BlockPos> segmentPath) {
        if (mergedPath.isEmpty()) {
            mergedPath.addAll(segmentPath);
            return;
        }
        for (int index = 0; index < segmentPath.size(); index++) {
            BlockPos position = segmentPath.get(index);
            if (index != 0 || !position.equals(mergedPath.get(mergedPath.size() - 1))) {
                mergedPath.add(position);
            }
        }
    }

    /** Adapta extremos nuevos o existentes antes de delegar en el pathfinder. */
    private static List<BlockPos> findPlacementPath(
            Level level,
            BlockPos startPos,
            Direction startFace,
            boolean startIsExistingPipe,
            BlockPos endPos,
            Direction endFace,
            boolean endIsExistingPipe,
            RoutePriority priority
    ) {
        if (startIsExistingPipe && endIsExistingPipe) {
            return PipePathfinder.findPath(level, startPos, startFace, endPos, endFace, priority);
        }
        BlockPos routeStart = startIsExistingPipe ? startPos.relative(startFace) : startPos;
        BlockPos routeEnd = endIsExistingPipe ? endPos.relative(endFace) : endPos;
        if (!PipePathfinder.isTraversable(level, routeStart, startPos, endPos)
                || !PipePathfinder.isTraversable(level, routeEnd, startPos, endPos)) {
            return null;
        }
        List<BlockPos> route = PipePathfinder.findPath(level, routeStart, null, routeEnd, null, priority);
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

    /** Orienta una conexion recta hacia su destino aunque se pulsara otra cara. */
    private static Direction resolveStraightLineFace(BlockPos endpointPos, Direction clickedFace, BlockPos targetPos) {
        Direction directFace = PipeRouteGeometry.directDirectionBetween(endpointPos, targetPos);
        return directFace == null ? clickedFace : directFace;
    }

    /** Describe un extremo entre dos tramos consecutivos. */
    private record SegmentEndpoint(BlockPos position, Direction face, boolean existingPipe) {
    }
}

