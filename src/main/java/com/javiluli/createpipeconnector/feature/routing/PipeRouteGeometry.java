package com.javiluli.createpipeconnector.feature.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ofrece calculos geometricos reutilizables sobre recorridos ortogonales.
 */
public final class PipeRouteGeometry {
    private static final Direction[] DIRECTIONS = Direction.values();

    /** Impide crear instancias de la utilidad geometrica. */
    private PipeRouteGeometry() {
    }

    /**
     * Devuelve la direccion cardinal entre dos posiciones contiguas y alineadas.
     * Las posiciones no alineadas usan norte porque Create necesita una direccion
     * estable para actualizar el estado de la tuberia.
     */
    public static Direction directionBetween(BlockPos from, BlockPos to) {
        Direction directDirection = directDirectionBetween(from, to);
        return directDirection == null ? Direction.NORTH : directDirection;
    }

    /**
     * Devuelve la orientacion valida de una bomba en un tramo recto.
     *
     * @return direccion de la ruta o {@code null} si la posicion no pertenece al
     * recorrido o forma parte de un codo
     */
    public static Direction straightPumpFacing(List<BlockPos> path, BlockPos position) {
        return straightPumpFacingAt(path, path.indexOf(position));
    }

    /** Resuelve la orientacion recta de la posicion situada en el indice indicado. */
    public static Direction straightPumpFacingAt(List<BlockPos> path, int index) {
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

    /** Crea un indice rapido de posicion a orden dentro de la ruta. */
    public static Map<BlockPos, Integer> indexByPosition(List<BlockPos> path) {
        Map<BlockPos, Integer> indices = new HashMap<>(path.size());
        for (int index = 0; index < path.size(); index++) {
            indices.put(path.get(index), index);
        }
        return indices;
    }

    /** Obtiene una direccion vecina util para actualizar una tuberia. */
    public static Direction preferredDirection(List<BlockPos> path, BlockPos position) {
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

    /** Devuelve la direccion exacta solo si ambas posiciones son adyacentes. */
    public static Direction directDirectionBetween(BlockPos from, BlockPos to) {
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
