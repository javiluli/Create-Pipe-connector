package com.javiluli.createpipeconnector.core.create;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Centraliza los registros y la interoperabilidad de estados con Create.
 *
 * <p>Mantener aqui la reflexion especifica de Create evita que el enrutado y la
 * colocacion dependan de detalles internos que pueden cambiar entre versiones.</p>
 *
 * <p>Usa los registros vanilla para mantener la interoperabilidad desacoplada
 * del cargador y compatible con las versiones de Create soportadas.</p>
 */
public final class CreatePipeBlocks {
    static final ResourceLocation FLUID_PIPE = createId("fluid_pipe");
    static final ResourceLocation GLASS_FLUID_PIPE = createId("glass_fluid_pipe");
    static final ResourceLocation ENCASED_FLUID_PIPE = createId("encased_fluid_pipe");
    static final ResourceLocation MECHANICAL_PUMP = createId(Constants.MECHANICAL_PUMP);
    static final ResourceLocation COPPER_CASING = createId("copper_casing");
    private static final ResourceLocation WRENCH = createId("wrench");
    private static final Set<ResourceLocation> CONNECTABLE_PIPES = Set.of(FLUID_PIPE);
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Map<Class<?>, Optional<Method>> UPDATE_BLOCK_STATE_METHODS = new ConcurrentHashMap<>();

    /** Impide crear instancias del adaptador de bloques de Create. */
    private CreatePipeBlocks() {
    }

    /** Indica si el estado pertenece a una tuberia admitida por el conector. */
    public static boolean isConnectablePipe(BlockState state) {
        return CONNECTABLE_PIPES.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    /** Indica si la pila contiene la llave de Create. */
    public static boolean isCreateWrench(ItemStack stack) {
        return WRENCH.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    /** Indica si una tuberia admite alternar su aspecto normal y de cristal. */
    public static boolean isPipeDisplayToggleTarget(BlockState state) {
        return isFluidPipe(state) || isGlassFluidPipe(state);
    }

    /** Obtiene el bloque de tuberia compatible contenido en una pila. */
    public static Block getPipeBlock(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && isConnectablePipe(blockItem.getBlock().defaultBlockState())) {
            return blockItem.getBlock();
        }
        return null;
    }

    /** Busca una tuberia compatible en cualquiera de las manos del jugador. */
    public static Block getHeldPipeBlock(Player player) {
        Block mainHandPipe = getPipeBlock(player.getMainHandItem());
        if (mainHandPipe != null) {
            return mainHandPipe;
        }

        return getPipeBlock(player.getOffhandItem());
    }

    /** Obtiene la bomba mecanica registrada por Create, si existe. */
    public static Block getMechanicalPumpBlock() {
        Block pumpBlock = BuiltInRegistries.BLOCK.get(MECHANICAL_PUMP);
        return pumpBlock == Blocks.AIR ? null : pumpBlock;
    }

    /** Obtiene el revestimiento de cobre registrado por Create, si existe. */
    public static Block getCopperCasingBlock() {
        Block casingBlock = BuiltInRegistries.BLOCK.get(COPPER_CASING);
        return casingBlock == Blocks.AIR ? null : casingBlock;
    }

    /** Obtiene la tuberia de cristal registrada por Create, si existe. */
    public static Block getGlassFluidPipeBlock() {
        Block glassPipeBlock = BuiltInRegistries.BLOCK.get(GLASS_FLUID_PIPE);
        return glassPipeBlock == Blocks.AIR ? null : glassPipeBlock;
    }

    /** Obtiene la tuberia revestida registrada por Create, si existe. */
    public static Block getEncasedFluidPipeBlock() {
        Block encasedPipeBlock = BuiltInRegistries.BLOCK.get(ENCASED_FLUID_PIPE);
        return encasedPipeBlock == Blocks.AIR ? null : encasedPipeBlock;
    }

    /** Crea el estado base de una tuberia conservando el agua del bloque origen. */
    public static BlockState createPipeState(Block pipeBlock, BlockState sourceState) {
        return copyWaterlogged(sourceState, pipeBlock.defaultBlockState());
    }

    /** Actualiza el agua del estado final usando el fluido presente al colocarlo. */
    public static BlockState applyCurrentWaterlogging(BlockState sourceState, BlockState targetState) {
        return copyWaterlogged(sourceState, targetState);
    }

    /** Convierte una tuberia normal en revestida conservando sus conexiones. */
    public static BlockState createEncasedPipeState(BlockState pipeState, BlockState sourceState) {
        if (!isFluidPipe(pipeState)) {
            return null;
        }

        Block encasedPipeBlock = getEncasedFluidPipeBlock();
        if (encasedPipeBlock == null) {
            return null;
        }

        BlockState encasedState = copyWaterlogged(sourceState, encasedPipeBlock.defaultBlockState());
        for (Direction direction : DIRECTIONS) {
            BooleanProperty sourceProperty = pipeConnectionProperty(pipeState, direction);
            BooleanProperty targetProperty = pipeConnectionProperty(encasedState, direction);
            if (targetProperty != null) {
                encasedState = encasedState.setValue(targetProperty, sourceProperty != null && pipeState.getValue(sourceProperty));
            }
        }
        return encasedState;
    }

    /** Crea una bomba orientada y conserva correctamente su estado de agua. */
    public static BlockState createPumpState(Block pumpBlock, BlockState sourceState, Direction facing) {
        BlockState pumpState = pumpBlock.defaultBlockState();
        if (pumpState.hasProperty(BlockStateProperties.FACING)) {
            pumpState = pumpState.setValue(BlockStateProperties.FACING, facing);
        }
        return copyWaterlogged(sourceState, pumpState);
    }

    /** Convierte un tramo recto normal en una tuberia de cristal. */
    public static BlockState createGlassPipeState(BlockState state) {
        if (!isFluidPipe(state)) {
            return null;
        }

        Direction.Axis axis = straightPipeAxis(state);
        if (axis == null) {
            return null;
        }

        Block glassPipeBlock = getGlassFluidPipeBlock();
        if (glassPipeBlock == null) {
            return null;
        }

        return copyWaterlogged(state, glassPipeBlock.defaultBlockState().setValue(BlockStateProperties.AXIS, axis));
    }

    /** Convierte una tuberia de cristal en normal y reconstruye sus conexiones. */
    public static BlockState createRegularPipeState(BlockAndTintGetter level, BlockPos position, BlockState state) {
        if (!isGlassFluidPipe(state) || !state.hasProperty(BlockStateProperties.AXIS)) {
            return null;
        }

        Block fluidPipeBlock = BuiltInRegistries.BLOCK.get(FLUID_PIPE);
        if (fluidPipeBlock == Blocks.AIR) {
            return null;
        }

        Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
        Direction side = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockState pipeState = copyWaterlogged(state, fluidPipeBlock.defaultBlockState());
        for (Direction direction : DIRECTIONS) {
            BooleanProperty property = pipeConnectionProperty(pipeState, direction);
            if (property != null) {
                pipeState = pipeState.setValue(property, direction.getAxis() == axis);
            }
        }

        return updatePipeState(pipeState, side, level, position);
    }

    /** Devuelve el eje de una tuberia unicamente cuando tiene dos salidas opuestas. */
    public static Direction.Axis straightPipeAxis(BlockState state) {
        Direction.Axis axis = null;
        int openSides = 0;

        for (Direction direction : DIRECTIONS) {
            if (!isPipeOpenAt(state, direction)) {
                continue;
            }

            openSides++;
            if (axis == null) {
                axis = direction.getAxis();
            } else if (axis != direction.getAxis()) {
                return null;
            }
        }

        return openSides == 2 ? axis : null;
    }

    /** Comprueba si el estado permite flujo por una cara concreta. */
    public static boolean isPipeOpenAt(BlockState state, Direction direction) {
        if (isGlassFluidPipe(state)) {
            return state.hasProperty(BlockStateProperties.AXIS) && state.getValue(BlockStateProperties.AXIS) == direction.getAxis();
        }
        if (isMechanicalPump(state)) {
            return state.hasProperty(BlockStateProperties.FACING) && state.getValue(BlockStateProperties.FACING).getAxis() == direction.getAxis();
        }

        BooleanProperty property = pipeConnectionProperty(state, direction);
        return property != null && state.getValue(property);
    }

    /** Localiza la propiedad booleana que representa una cara de conexion. */
    public static BooleanProperty pipeConnectionProperty(BlockState state, Direction direction) {
        String propertyName = direction.getSerializedName();
        for (var property : state.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && property.getName().equals(propertyName)) {
                return booleanProperty;
            }
        }
        return null;
    }

    /** Indica si el estado es una tuberia de fluido normal. */
    public static boolean isFluidPipe(BlockState state) {
        return FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    /** Indica si el estado es una tuberia de fluido de cristal. */
    public static boolean isGlassFluidPipe(BlockState state) {
        return GLASS_FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    /** Indica si el estado corresponde a una bomba mecanica. */
    public static boolean isMechanicalPump(BlockState state) {
        return MECHANICAL_PUMP.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    /** Comprueba que el tipo de tuberia y los bloques requeridos admiten revestimiento. */
    public static boolean supportsCopperCasing(Block pipeBlock) {
        return FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(pipeBlock))
                && getCopperCasingBlock() != null
                && getEncasedFluidPipeBlock() != null;
    }

    /** Comprueba que el tipo de tuberia admite el estilo de cristal. */
    public static boolean supportsGlassPipeStyle(Block pipeBlock) {
        return FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(pipeBlock))
                && getGlassFluidPipeBlock() != null;
    }

    /** Solicita a Create que recalcule las conexiones de un estado de tuberia. */
    public static BlockState updatePipeState(BlockState state, Direction preferredDirection, BlockAndTintGetter world, BlockPos position) {
        Optional<Method> updateBlockState = UPDATE_BLOCK_STATE_METHODS.computeIfAbsent(
                state.getBlock().getClass(),
                CreatePipeBlocks::findUpdateBlockStateMethod
        );
        if (updateBlockState.isEmpty()) {
            return state;
        }

        try {
            return (BlockState) updateBlockState.get().invoke(state.getBlock(), state, preferredDirection, null, world, position);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            return state;
        }
    }

    /** Localiza una sola vez la API de conexiones expuesta por cada clase de Create. */
    private static Optional<Method> findUpdateBlockStateMethod(Class<?> blockClass) {
        try {
            return Optional.of(blockClass.getMethod(
                    Constants.UPDATE_BLOCK_STATE,
                    BlockState.class,
                    Direction.class,
                    Direction.class,
                    BlockAndTintGetter.class,
                    BlockPos.class
            ));
        } catch (NoSuchMethodException exception) {
            return Optional.empty();
        }
    }

    /** Copia al estado destino si debe permanecer anegado. */
    private static BlockState copyWaterlogged(BlockState sourceState, BlockState targetState) {
        if (targetState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return targetState.setValue(BlockStateProperties.WATERLOGGED, sourceState.getFluidState().is(FluidTags.WATER));
        }
        return targetState;
    }

    /** Crea un identificador dentro del espacio de nombres de Create. */
    private static ResourceLocation createId(String path) {
        return ResourceLocation.fromNamespaceAndPath(Constants.NAMESPACE, path);
    }
}
