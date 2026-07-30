package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.Constants;
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
import java.util.Set;

/**
 * Centralizes registry lookups and block-state interoperability with Create.
 *
 * <p>Keeping Create-specific reflection here prevents routing and placement
 * code from depending on implementation details that may move between Create
 * versions.</p>
 */
final class CreatePipeBlocks {
    static final ResourceLocation FLUID_PIPE = createId(Constants.FLUID_PIPE);
    static final ResourceLocation GLASS_FLUID_PIPE = createId(Constants.GLASS_FLUID_PIPE);
    static final ResourceLocation ENCASED_FLUID_PIPE = createId(Constants.ENCASED_FLUID_PIPE);
    static final ResourceLocation MECHANICAL_PUMP = createId(Constants.MECHANICAL_PUMP);
    static final ResourceLocation COPPER_CASING = createId(Constants.COPPER_CASING);
    private static final ResourceLocation WRENCH = createId(Constants.WRENCH);
    private static final Set<ResourceLocation> CONNECTABLE_PIPES = Set.of(FLUID_PIPE);
    private static final Direction[] DIRECTIONS = Direction.values();

    private CreatePipeBlocks() {
    }

    static boolean isConnectablePipe(BlockState state) {
        return CONNECTABLE_PIPES.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    static boolean isCreateWrench(ItemStack stack) {
        return WRENCH.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    static boolean isPipeDisplayToggleTarget(BlockState state) {
        return isFluidPipe(state) || isGlassFluidPipe(state);
    }

    static Block getPipeBlock(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem && isConnectablePipe(blockItem.getBlock().defaultBlockState())) {
            return blockItem.getBlock();
        }
        return null;
    }

    static Block getHeldPipeBlock(Player player) {
        Block mainHandPipe = getPipeBlock(player.getMainHandItem());
        if (mainHandPipe != null) {
            return mainHandPipe;
        }

        return getPipeBlock(player.getOffhandItem());
    }

    static Block getMechanicalPumpBlock() {
        Block pumpBlock = BuiltInRegistries.BLOCK.get(MECHANICAL_PUMP);
        return pumpBlock == Blocks.AIR ? null : pumpBlock;
    }

    static Block getCopperCasingBlock() {
        Block casingBlock = BuiltInRegistries.BLOCK.get(COPPER_CASING);
        return casingBlock == Blocks.AIR ? null : casingBlock;
    }

    static Block getGlassFluidPipeBlock() {
        Block glassPipeBlock = BuiltInRegistries.BLOCK.get(GLASS_FLUID_PIPE);
        return glassPipeBlock == Blocks.AIR ? null : glassPipeBlock;
    }

    static Block getEncasedFluidPipeBlock() {
        Block encasedPipeBlock = BuiltInRegistries.BLOCK.get(ENCASED_FLUID_PIPE);
        return encasedPipeBlock == Blocks.AIR ? null : encasedPipeBlock;
    }

    static BlockState createPipeState(Block pipeBlock, BlockState sourceState) {
        return copyWaterlogged(sourceState, pipeBlock.defaultBlockState());
    }

    static BlockState createEncasedPipeState(BlockState pipeState, BlockState sourceState) {
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

    static BlockState createPumpState(Block pumpBlock, BlockState sourceState, Direction facing) {
        BlockState pumpState = pumpBlock.defaultBlockState();
        if (pumpState.hasProperty(BlockStateProperties.FACING)) {
            pumpState = pumpState.setValue(BlockStateProperties.FACING, facing);
        }
        return copyWaterlogged(sourceState, pumpState);
    }

    static BlockState createGlassPipeState(BlockState state) {
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

    static BlockState createRegularPipeState(BlockAndTintGetter level, BlockPos position, BlockState state) {
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

    static Direction.Axis straightPipeAxis(BlockState state) {
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

    static boolean isPipeOpenAt(BlockState state, Direction direction) {
        if (isGlassFluidPipe(state)) {
            return state.hasProperty(BlockStateProperties.AXIS) && state.getValue(BlockStateProperties.AXIS) == direction.getAxis();
        }
        if (isMechanicalPump(state)) {
            return state.hasProperty(BlockStateProperties.FACING) && state.getValue(BlockStateProperties.FACING).getAxis() == direction.getAxis();
        }

        BooleanProperty property = pipeConnectionProperty(state, direction);
        return property != null && state.getValue(property);
    }

    static BooleanProperty pipeConnectionProperty(BlockState state, Direction direction) {
        String propertyName = direction.getSerializedName();
        for (var property : state.getProperties()) {
            if (property instanceof BooleanProperty booleanProperty && property.getName().equals(propertyName)) {
                return booleanProperty;
            }
        }
        return null;
    }

    static boolean isFluidPipe(BlockState state) {
        return FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    static boolean isGlassFluidPipe(BlockState state) {
        return GLASS_FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    static boolean isMechanicalPump(BlockState state) {
        return MECHANICAL_PUMP.equals(BuiltInRegistries.BLOCK.getKey(state.getBlock()));
    }

    static boolean supportsCopperCasing(Block pipeBlock) {
        return FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(pipeBlock))
                && getCopperCasingBlock() != null
                && getEncasedFluidPipeBlock() != null;
    }

    static boolean supportsGlassPipeStyle(Block pipeBlock) {
        return FLUID_PIPE.equals(BuiltInRegistries.BLOCK.getKey(pipeBlock))
                && getGlassFluidPipeBlock() != null;
    }

    static BlockState updatePipeState(BlockState state, Direction preferredDirection, BlockAndTintGetter world, BlockPos position) {
        try {
            Method updateBlockState = state.getBlock().getClass().getMethod(
                    Constants.UPDATE_BLOCK_STATE,
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

    private static BlockState copyWaterlogged(BlockState sourceState, BlockState targetState) {
        if (targetState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return targetState.setValue(BlockStateProperties.WATERLOGGED, sourceState.getFluidState().is(FluidTags.WATER));
        }
        return targetState;
    }

    private static ResourceLocation createId(String path) {
        return new ResourceLocation(Constants.NAMESPACE, path);
    }
}
