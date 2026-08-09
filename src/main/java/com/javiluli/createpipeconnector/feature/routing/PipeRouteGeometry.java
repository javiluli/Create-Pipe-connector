package com.javiluli.createpipeconnector.feature.routing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /** Precalcula direcciones preferidas para una coleccion completa de posiciones. */
    public static Map<BlockPos, Direction> preferredDirections(List<BlockPos> path, Collection<BlockPos> positions) {
        Map<BlockPos, Direction> pathDirections = new HashMap<>(path.size());
        for (int index = 0; index < path.size(); index++) {
            BlockPos position = path.get(index);
            Direction direction;
            if (index + 1 < path.size()) {
                direction = directionBetween(position, path.get(index + 1));
            } else if (index > 0) {
                direction = directionBetween(path.get(index - 1), position);
            } else {
                direction = Direction.NORTH;
            }
            pathDirections.put(position, direction);
        }

        Map<BlockPos, Direction> preferredDirections = new HashMap<>(positions.size());
        BlockPos.MutableBlockPos neighbourPosition = new BlockPos.MutableBlockPos();
        for (BlockPos position : positions) {
            Direction pathDirection = pathDirections.get(position);
            if (pathDirection != null) {
                preferredDirections.put(position, pathDirection);
                continue;
            }
            for (Direction direction : DIRECTIONS) {
                neighbourPosition.set(
                        position.getX() + direction.getStepX(),
                        position.getY() + direction.getStepY(),
                        position.getZ() + direction.getStepZ()
                );
                if (pathDirections.containsKey(neighbourPosition)) {
                    preferredDirections.put(position, direction);
                    break;
                }
            }
        }
        return preferredDirections;
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
