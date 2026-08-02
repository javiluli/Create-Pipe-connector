package com.javiluli.createpipeconnector.feature.style;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.feature.pipe.PipeNetworkUpdater;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Convierte un tramo conectado entre los estilos normal y de cristal de Create.
 *
 * <p>El recorrido se detiene en bombas mecanicas para no alterar otros tramos.</p>
 */
public final class PipeDisplayToggler {
    private static final String FLUID_TRANSPORT_CLASS = "com.simibubi.create.content.fluids.FluidTransportBehaviour";
    private static final String CACHE_FLOWS_METHOD = "cacheFlows";
    private static final String LOAD_FLOWS_METHOD = "loadFlows";
    private static final int MAX_TOGGLE_BLOCKS = 512;
    private static final Direction[] DIRECTIONS = Direction.values();

    /** Impide crear instancias del conversor de tramos. */
    private PipeDisplayToggler() {
    }

    /** Alterna el aspecto del tramo recto conectado al bloque de origen. */
    public static PipeDisplayToggleResult toggleSegment(ServerLevel level, BlockPos origin) {
        BlockState originState = level.getBlockState(origin);
        if (!CreatePipeBlocks.isPipeDisplayToggleTarget(originState)) {
            return PipeDisplayToggleResult.empty(false);
        }

        List<BlockPos> segment = collectSegment(level, origin);
        if (segment.isEmpty()) {
            return PipeDisplayToggleResult.empty(false);
        }

        boolean convertToGlass = CreatePipeBlocks.isFluidPipe(originState);
        int changed = 0;
        int skipped = 0;

        for (BlockPos position : segment) {
            BlockState currentState = level.getBlockState(position);
            BlockState newState = convertToGlass
                    ? CreatePipeBlocks.createGlassPipeState(currentState)
                    : CreatePipeBlocks.createRegularPipeState(level, position, currentState);
            if (newState == null || newState.equals(currentState)) {
                if (convertToGlass && CreatePipeBlocks.isFluidPipe(currentState)) {
                    skipped++;
                }
                continue;
            }

            cacheFluidFlows(level, position);
            level.setBlockAndUpdate(position, newState);
            loadFluidFlows(level, position);
            changed++;
        }

        PipeNetworkUpdater.refresh(level, segment);
        return new PipeDisplayToggleResult(convertToGlass, changed, skipped, segment.size());
    }

    /** Recorre el tramo hasta encontrar bifurcaciones, bombas o esquinas. */
    private static List<BlockPos> collectSegment(Level level, BlockPos origin) {
        List<BlockPos> segment = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> openSet = new ArrayDeque<>();
        openSet.add(origin);

        // Evita que redes malformadas o enormes bloqueen el tick del servidor.
        while (!openSet.isEmpty() && segment.size() < MAX_TOGGLE_BLOCKS) {
            BlockPos position = openSet.removeFirst();
            if (!visited.add(position)) {
                continue;
            }

            BlockState state = level.getBlockState(position);
            if (!CreatePipeBlocks.isPipeDisplayToggleTarget(state)) {
                continue;
            }

            segment.add(position);
            for (Direction direction : DIRECTIONS) {
                if (!CreatePipeBlocks.isPipeOpenAt(state, direction)) {
                    continue;
                }

                BlockPos neighbourPos = position.relative(direction);
                BlockState neighbourState = level.getBlockState(neighbourPos);
                if (CreatePipeBlocks.isMechanicalPump(neighbourState)) {
                    continue;
                }
                if (CreatePipeBlocks.isPipeDisplayToggleTarget(neighbourState)
                        && CreatePipeBlocks.isPipeOpenAt(neighbourState, direction.getOpposite())) {
                    openSet.add(neighbourPos);
                }
            }
        }

        return segment;
    }

    /** Conserva los flujos internos antes de sustituir el bloque. */
    private static void cacheFluidFlows(LevelAccessor level, BlockPos position) {
        invokeFluidTransportMethod(CACHE_FLOWS_METHOD, level, position);
    }

    /** Restaura los flujos internos despues de sustituir el bloque. */
    private static void loadFluidFlows(LevelAccessor level, BlockPos position) {
        invokeFluidTransportMethod(LOAD_FLOWS_METHOD, level, position);
    }

    /** Invoca de forma compatible una operacion interna de transporte de Create. */
    private static void invokeFluidTransportMethod(String methodName, LevelAccessor level, BlockPos position) {
        try {
            Class<?> fluidTransport = Class.forName(FLUID_TRANSPORT_CLASS);
            Method method = fluidTransport.getMethod(methodName, LevelAccessor.class, BlockPos.class);
            method.invoke(null, level, position);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }
}
