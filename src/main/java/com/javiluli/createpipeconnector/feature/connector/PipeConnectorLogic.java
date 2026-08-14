package com.javiluli.createpipeconnector.feature.connector;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.core.player.PlayerInteractionRange;
import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import com.javiluli.createpipeconnector.feature.casing.CopperCasingPlanner;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.planning.ConnectionPlanBuilder;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.javiluli.createpipeconnector.feature.preview.PipePreviewBuilder;
import com.javiluli.createpipeconnector.feature.pump.AutoPumpPlanner;
import com.javiluli.createpipeconnector.feature.pump.ManualPumpPlanner;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.material.PipeInventory;
import com.javiluli.createpipeconnector.feature.routing.PipePathfinder;
import com.javiluli.createpipeconnector.feature.routing.PipeRouteGeometry;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import com.javiluli.createpipeconnector.feature.style.PipeDisplayToggler;
import com.javiluli.createpipeconnector.feature.style.PipeDisplayToggleResult;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import com.javiluli.createpipeconnector.feature.style.PipeStylePlanner;
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

import java.util.List;
import java.util.Map;

/** Fachada publica para rutas, preview, materiales y colocacion de tuberias. */
public final class PipeConnectorLogic {
    /** Impide crear instancias de la fachada estatica. */
    private PipeConnectorLogic() {
    }

    /** Indica si un estado pertenece a una tuberia admitida. */
    public static boolean isConnectablePipe(BlockState state) {
        return CreatePipeBlocks.isConnectablePipe(state);
    }

    /** Indica si una pila contiene la llave de Create. */
    public static boolean isCreateWrench(ItemStack stack) {
        return CreatePipeBlocks.isCreateWrench(stack);
    }

    /** Indica si una tuberia admite alternar su aspecto. */
    public static boolean isPipeDisplayToggleTarget(BlockState state) {
        return CreatePipeBlocks.isPipeDisplayToggleTarget(state);
    }

    /** Obtiene el bloque de tuberia contenido en una pila compatible. */
    public static Block getPipeBlock(ItemStack stack) {
        return CreatePipeBlocks.getPipeBlock(stack);
    }

    /** Busca una tuberia compatible en cualquiera de las manos. */
    public static Block getHeldPipeBlock(Player player) {
        return CreatePipeBlocks.getHeldPipeBlock(player);
    }

    /** Obtiene el bloque registrado de bomba mecanica. */
    public static Block getMechanicalPumpBlock() {
        return CreatePipeBlocks.getMechanicalPumpBlock();
    }

    /** Obtiene el bloque registrado de revestimiento de cobre. */
    public static Block getCopperCasingBlock() {
        return CreatePipeBlocks.getCopperCasingBlock();
    }

    /** Obtiene el bloque registrado de tuberia de cristal. */
    public static Block getGlassFluidPipeBlock() {
        return CreatePipeBlocks.getGlassFluidPipeBlock();
    }

    /** Comprueba si el tipo de tuberia admite revestimiento. */
    public static boolean supportsCopperCasing(Block pipeBlock) {
        return CreatePipeBlocks.supportsCopperCasing(pipeBlock);
    }

    /** Comprueba si el tipo de tuberia admite estilo de cristal. */
    public static boolean supportsGlassPipeStyle(Block pipeBlock) {
        return CreatePipeBlocks.supportsGlassPipeStyle(pipeBlock);
    }

    /** Alterna el aspecto de un tramo conectado. */
    public static PipeDisplayToggleResult togglePipeDisplaySegment(ServerLevel level, BlockPos origin) {
        return PipeDisplayToggler.toggleSegment(level, origin);
    }

    /** Resuelve una cara pulsada como tuberia existente o posicion colocable. */
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

    /** Comprueba si una posicion permite colocar o reutilizar una tuberia. */
    public static boolean canPlacePipeAt(Level level, BlockPos position) {
        return PipePathfinder.isTraversableBlock(level, position);
    }

    /** Comprueba que el punto inicial sigue siendo valido. */
    public static boolean isSelectionStillValid(Level level, Selection selection) {
        BlockState selectionState = level.getBlockState(selection.position());
        if (selection.existingPipe()) {
            return isConnectablePipe(selectionState) && selection.pipeBlock() == selectionState.getBlock();
        }

        return canPlacePipeAt(level, selection.position());
    }

    /**
     * Indica si la interaccion principal utiliza la tuberia guardada en la ruta.
     *
     * <p>La mano secundaria solo se considera cuando la principal esta vacia,
     * evitando que una tuberia secundaria bloquee el uso de otro objeto.</p>
     */
    public static boolean isUsingSelectedPipe(Player player, Selection selection) {
        Block mainHandPipe = getPipeBlock(player.getMainHandItem());
        if (mainHandPipe != null) {
            return mainHandPipe == selection.pipeBlock();
        }
        return player.getMainHandItem().isEmpty()
                && getPipeBlock(player.getOffhandItem()) == selection.pipeBlock();
    }

    /** Aplica el mismo alcance que una interaccion normal con bloques. */
    public static boolean isWithinInteractionRange(Player player, BlockPos position) {
        double maxDistance = getInteractionRange(player) + 1.0D;
        return player.getEyePosition().distanceToSqr(Vec3.atCenterOf(position)) <= maxDistance * maxDistance;
    }

    /** Devuelve el alcance efectivo de interaccion del jugador. */
    public static double getInteractionRange(Player player) {
        return PlayerInteractionRange.resolve(player);
    }

    /** Inspecciona todos los materiales y shulkers con un unico recorrido. */
    public static PipeInventory.MaterialSnapshot inspectMaterials(
            Player player,
            Block pipeBlock,
            boolean includeShulkers
    ) {
        return PipeInventory.inspectMaterials(player, pipeBlock, includeShulkers);
    }

    /** Crea un estado base de tuberia conservando el agua. */
    public static BlockState createPipeState(Block pipeBlock, BlockState sourceState) {
        return CreatePipeBlocks.createPipeState(pipeBlock, sourceState);
    }

    /** Crea una bomba orientada conservando el agua. */
    public static BlockState createPumpState(Block pumpBlock, BlockState sourceState, Direction facing) {
        return CreatePipeBlocks.createPumpState(pumpBlock, sourceState, facing);
    }

    /** Construye un preview basico sin modificar el nivel. */
    public static List<PreviewPipe> buildPreview(Level level, BlockPos startPos, BlockPos endPos, Block pipeBlock) {
        ConnectionPlan plan = buildConnectionPlan(level, startPos, endPos);
        if (plan == null) {
            return List.of();
        }

        return buildPreview(level, plan, pipeBlock);
    }

    /** Construye las piezas de preview de un plan calculado. */
    public static List<PreviewPipe> buildPreview(Level level, ConnectionPlan plan, Block pipeBlock) {
        return PipePreviewBuilder.buildPreview(level, plan, pipeBlock);
    }

    /** Aplica al plan el modo y sentido de bombas seleccionados. */
    public static ConnectionPlan withPumpMode(ConnectionPlan plan, PumpMode mode, boolean reversed) {
        return AutoPumpPlanner.apply(plan, mode, reversed);
    }

    /** Incorpora bombas manuales validas respetando el sentido seleccionado. */
    public static ConnectionPlan withManualPumps(
            ConnectionPlan plan,
            List<BlockPos> pumpPositions,
            boolean reversed
    ) {
        return ManualPumpPlanner.apply(plan, pumpPositions, reversed);
    }

    /** Aplica el modo de revestimiento y sus marcas manuales. */
    public static ConnectionPlan withCopperCasingMode(ConnectionPlan plan, CopperCasingMode mode, List<BlockPos> casingPositions, Block pipeBlock) {
        return CopperCasingPlanner.apply(plan, mode, casingPositions, pipeBlock);
    }

    /** Aplica el estilo visual a las posiciones compatibles. */
    public static ConnectionPlan withPipeStyleMode(ConnectionPlan plan, PipeStyleMode mode, Block pipeBlock) {
        return PipeStylePlanner.apply(plan, mode, pipeBlock);
    }

    /** Calcula un plan basico entre dos posiciones. */
    public static ConnectionPlan buildConnectionPlan(Level level, BlockPos startPos, BlockPos endPos) {
        return ConnectionPlanBuilder.build(level, startPos, endPos);
    }

    /** Calcula un plan basico respetando ambas caras. */
    public static ConnectionPlan buildConnectionPlan(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace) {
        return ConnectionPlanBuilder.build(level, startPos, startFace, endPos, endFace);
    }

    /** Calcula un plan entre una seleccion y un objetivo. */
    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, PlacementTarget target) {
        return buildPlacementPlan(level, selection, target, RoutePriority.AUTO);
    }

    /** Calcula un plan entre seleccion y objetivo con prioridad explicita. */
    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, PlacementTarget target, RoutePriority routePriority) {
        return ConnectionPlanBuilder.build(level, selection, target, routePriority);
    }

    /** Calcula un plan que atraviesa anclas con prioridad automatica. */
    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, List<PlacementTarget> anchors, PlacementTarget target) {
        return buildPlacementPlan(level, selection, anchors, target, RoutePriority.AUTO);
    }

    /** Calcula y fusiona los tramos definidos por anclas. */
    public static ConnectionPlan buildPlacementPlan(Level level, Selection selection, List<PlacementTarget> anchors, PlacementTarget target, RoutePriority routePriority) {
        return ConnectionPlanBuilder.build(level, selection, anchors, target, routePriority);
    }

    /** Calcula un tramo indicando si sus extremos ya contienen tuberias. */
    public static ConnectionPlan buildPlacementPlan(
            Level level,
            BlockPos startPos,
            Direction startFace,
            boolean startIsExistingPipe,
            BlockPos endPos,
            Direction endFace,
            boolean endIsExistingPipe
    ) {
        return ConnectionPlanBuilder.buildSegment(
                level,
                startPos,
                startFace,
                startIsExistingPipe,
                endPos,
                endFace,
                endIsExistingPipe,
                RoutePriority.AUTO
        );
    }

    /** Calcula un tramo completo con prioridad de ejes concreta. */
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
        return ConnectionPlanBuilder.buildSegment(
                level,
                startPos,
                startFace,
                startIsExistingPipe,
                endPos,
                endFace,
                endIsExistingPipe,
                routePriority
        );
    }

    /** Busca una ruta basica con prioridad automatica. */
    public static List<BlockPos> findPath(Level level, BlockPos startPos, BlockPos endPos) {
        return findPath(level, startPos, endPos, RoutePriority.AUTO);
    }

    /** Busca una ruta basica con la prioridad indicada. */
    public static List<BlockPos> findPath(Level level, BlockPos startPos, BlockPos endPos, RoutePriority routePriority) {
        return findPath(level, startPos, null, endPos, null, routePriority);
    }

    /** Busca una ruta orientada por caras. */
    public static List<BlockPos> findPath(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace) {
        return findPath(level, startPos, startFace, endPos, endFace, RoutePriority.AUTO);
    }

    /** Busca una ruta orientada por caras y prioridad. */
    public static List<BlockPos> findPath(Level level, BlockPos startPos, Direction startFace, BlockPos endPos, Direction endFace, RoutePriority routePriority) {
        return PipePathfinder.findPath(level, startPos, startFace, endPos, endFace, routePriority);
    }

    /** Crea una vista del nivel con estados fantasma virtuales. */
    public static BlockAndTintGetter createPreviewWorld(Level level, Map<BlockPos, BlockState> previewStates) {
        return PipePreviewBuilder.createPreviewWorld(level, previewStates);
    }

    /** Devuelve la direccion cardinal entre dos posiciones. */
    public static Direction directionBetween(BlockPos from, BlockPos to) {
        return PipeRouteGeometry.directionBetween(from, to);
    }

    /** Devuelve una orientacion valida de bomba en un tramo recto. */
    public static Direction straightPumpFacing(List<BlockPos> path, BlockPos position) {
        return PipeRouteGeometry.straightPumpFacing(path, position);
    }

}
