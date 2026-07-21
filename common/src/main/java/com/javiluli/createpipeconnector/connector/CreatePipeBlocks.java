package com.javiluli.createpipeconnector.connector;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

final class CreatePipeBlocks {
    static final ResourceLocation FLUID_PIPE = ResourceLocation.fromNamespaceAndPath("create", "fluid_pipe");
    static final ResourceLocation SMART_FLUID_PIPE = ResourceLocation.fromNamespaceAndPath("create", "smart_fluid_pipe");
    static final ResourceLocation GLASS_FLUID_PIPE = ResourceLocation.fromNamespaceAndPath("create", "glass_fluid_pipe");
    static final ResourceLocation MECHANICAL_PUMP = ResourceLocation.fromNamespaceAndPath("create", "mechanical_pump");
    private static final ResourceLocation WRENCH = ResourceLocation.fromNamespaceAndPath("create", "wrench");
    private static final Set<ResourceLocation> CONNECTABLE_PIPES = Set.of(FLUID_PIPE, SMART_FLUID_PIPE);
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

    static BlockState createPipeState(Block pipeBlock, BlockState sourceState) {
        return copyWaterlogged(sourceState, pipeBlock.defaultBlockState());
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

        Block glassPipeBlock = BuiltInRegistries.BLOCK.get(GLASS_FLUID_PIPE);
        if (glassPipeBlock == Blocks.AIR) {
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

    static BlockState updatePipeState(BlockState state, Direction preferredDirection, BlockAndTintGetter world, BlockPos position) {
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

    private static BlockState copyWaterlogged(BlockState sourceState, BlockState targetState) {
        if (targetState.hasProperty(BlockStateProperties.WATERLOGGED) && sourceState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return targetState.setValue(BlockStateProperties.WATERLOGGED, sourceState.getValue(BlockStateProperties.WATERLOGGED));
        }
        return targetState;
    }
}
