package com.javiluli.createpipeconnector.feature.connector.server;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.session.ConnectorSessionStore;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.style.PipeDisplayToggleResult;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gestiona en Forge el ciclo de ruta, la colocacion y el doble clic con la llave.
 */
public final class ServerPipeConnectorEvents {
    private static final Map<UUID, WrenchPipeClick> WRENCH_PIPE_CLICKS = new HashMap<>();

    /** Impide crear instancias del manejador de eventos. */
    private ServerPipeConnectorEvents() {
    }

    /**
     * Limpia selecciones obsoletas cuando el jugador deja un estado compatible.
     */
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        clearExpiredWrenchClicks(player.level().getGameTime());

        Selection selection = ConnectorSessionStore.getSelection(player.getUUID());
        if (selection == null) {
            return;
        }

        if (!ConnectorSessionStore.isConnectorModeEnabled(player.getUUID())) {
            ConnectorSessionStore.clearSelection(player.getUUID());
            clearActionBar(player);
            return;
        }

        if (PipeConnectorLogic.isPlayerInPipeMode(player, selection)) {
            return;
        }

        ConnectorSessionStore.clearSelection(player.getUUID());
        clearActionBar(player);
    }

    /**
     * Detecta un doble clic breve antes de convertir un tramo conectado.
     */
    public static void handleWrenchPipeDisplayClick(Player player, ServerLevel serverLevel, BlockPos position) {
        if (!ConnectorSessionStore.isConnectorModeEnabled(player.getUUID())) {
            return;
        }
        if (!PipeConnectorLogic.isCreateWrench(player.getMainHandItem())) {
            return;
        }
        if (!PipeConnectorLogic.isWithinInteractionRange(player, position)) {
            return;
        }
        if (!PipeConnectorLogic.isPipeDisplayToggleTarget(serverLevel.getBlockState(position))) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        UUID playerId = player.getUUID();
        WrenchPipeClick previousClick = WRENCH_PIPE_CLICKS.get(playerId);
        if (previousClick == null
                || !previousClick.position().equals(position)
                || gameTime - previousClick.gameTime() > Constants.WRENCH_DOUBLE_CLICK_TICKS) {
            WRENCH_PIPE_CLICKS.put(playerId, new WrenchPipeClick(position, gameTime));
            player.displayClientMessage(Component.translatable(Constants.HUD_PIPE_STYLE_CLICK_AGAIN), true);
            return;
        }

        WRENCH_PIPE_CLICKS.remove(playerId);
        PipeDisplayToggleResult result = PipeConnectorLogic.togglePipeDisplaySegment(serverLevel, position);
        if (result.changed() <= 0) {
            player.displayClientMessage(Component.translatable(Constants.HUD_PIPE_STYLE_NO_CHANGES), true);
            return;
        }

        String translationKey = result.glassMode()
                ? Constants.HUD_PIPE_STYLE_TO_GLASS
                : Constants.HUD_PIPE_STYLE_TO_DEFAULT;
        player.displayClientMessage(Component.translatable(translationKey, result.changed()), true);
    }

    /**
     * Gestiona inicios y confirmaciones dirigidos a bloques en el servidor.
     */
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (!ConnectorSessionStore.isConnectorModeEnabled(player.getUUID())) {
            return;
        }

        Block heldPipeBlock = PipeConnectorLogic.getHeldPipeBlock(player);
        if (heldPipeBlock == null) {
            return;
        }

        PlacementTarget clickedTarget = PipeConnectorLogic.resolvePlacementTarget(event.getLevel(), event.getPos(), event.getFace(), heldPipeBlock);
        if (clickedTarget == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            handlePipeTarget(player, serverLevel, clickedTarget);
        }
    }

    /**
     * Inicia una ruta o valida, consume y coloca el plan confirmado.
     *
     * @return {@code true} si se acepto la interaccion
     */
    public static boolean handlePipeTarget(Player player, ServerLevel serverLevel, PlacementTarget target) {
        if (!ConnectorSessionStore.isConnectorModeEnabled(player.getUUID())) {
            return false;
        }

        Block heldPipeBlock = PipeConnectorLogic.getHeldPipeBlock(player);
        Selection currentSelection = ConnectorSessionStore.getSelection(player.getUUID());
        if (heldPipeBlock == null || !isTargetValid(player, serverLevel, heldPipeBlock, target, currentSelection == null)) {
            if (currentSelection != null) {
                ConnectorSessionStore.clearSelection(player.getUUID());
                clearActionBar(player);
            }
            return false;
        }

        if (currentSelection == null) {
            ConnectorSessionStore.setSelection(player.getUUID(), new Selection(target.position(), heldPipeBlock, target.face(), target.existingPipe()));
            player.displayClientMessage(Component.translatable(Constants.HUD_FIRST_POINT_SELECTED), true);
            return true;
        }

        if (currentSelection.position().equals(target.position())) {
            ConnectorSessionStore.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        if (currentSelection.pipeBlock() != heldPipeBlock) {
            ConnectorSessionStore.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(
                serverLevel,
                currentSelection,
                ConnectorSessionStore.getAnchors(player.getUUID()),
                target,
                ConnectorSessionStore.getRoutePriority(player.getUUID())
        );
        if (plan == null) {
            ConnectorSessionStore.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }
        PumpMode pumpMode = ConnectorSessionStore.getPumpMode(player.getUUID());
        plan = PipeConnectorLogic.withPumpMode(
                plan,
                pumpMode,
                ConnectorSessionStore.isAutoPumpDirectionReversed(player.getUUID())
        );
        plan = PipeConnectorLogic.withManualPumps(plan, ConnectorSessionStore.getManualPumps(player.getUUID()));
        plan = PipeConnectorLogic.withCopperCasingMode(
                plan,
                ConnectorSessionStore.getCopperCasingMode(player.getUUID()),
                ConnectorSessionStore.getCopperCasings(player.getUUID()),
                currentSelection.pipeBlock()
        );
        plan = PipeConnectorLogic.withPipeStyleMode(
                plan,
                ConnectorSessionStore.getPipeStyleMode(player.getUUID()),
                currentSelection.pipeBlock()
        );

        if (!PipeConnectorLogic.hasEnoughItems(player, currentSelection.pipeBlock(), plan)) {
            player.displayClientMessage(missingMaterialsMessage(player, currentSelection.pipeBlock(), plan).copy().withStyle(ChatFormatting.RED), true);
            return true;
        }

        boolean connected = PipeConnectorLogic.connect(serverLevel, plan, currentSelection.pipeBlock());
        if (connected) {
            PipeConnectorLogic.consumeItems(player, currentSelection.pipeBlock(), plan);
        }

        ConnectorSessionStore.clearSelection(player.getUUID());
        clearActionBar(player);
        return true;
    }

    /**
     * Limpia los datos temporales y el mensaje de la barra de accion.
     */
    public static void cancelPipeConnection(Player player) {
        ConnectorSessionStore.clearSelection(player.getUUID());
        clearActionBar(player);
    }

    /** Comprueba alcance, ocupacion y tipo de tuberia del objetivo. */
    private static boolean isTargetValid(Player player, ServerLevel level, Block pipeBlock, PlacementTarget target, boolean requireReach) {
        if (requireReach && !PipeConnectorLogic.isWithinInteractionRange(player, target.position())) {
            return false;
        }

        BlockState targetState = level.getBlockState(target.position());
        if (!target.existingPipe()) {
            return !PipeConnectorLogic.isConnectablePipe(targetState)
                    && PipeConnectorLogic.canPlacePipeAt(level, target.position());
        }

        return PipeConnectorLogic.isConnectablePipe(targetState)
                && targetState.getBlock() == pipeBlock;
    }

    /** Borra el mensaje temporal mostrado sobre la barra rapida. */
    private static void clearActionBar(Player player) {
        player.displayClientMessage(Component.empty(), true);
    }

    /** Construye un mensaje localizado con todos los materiales que faltan. */
    private static Component missingMaterialsMessage(Player player, Block pipeBlock, ConnectionPlan plan) {
        List<Component> missingMaterials = new ArrayList<>();
        addMissingMaterial(missingMaterials, plan.requiredPipes(), PipeConnectorLogic.countAvailablePipes(player, pipeBlock), Constants.HUD_MISSING_PIPES);
        addMissingMaterial(missingMaterials, plan.requiredPumps(), PipeConnectorLogic.countAvailablePumps(player), Constants.HUD_MISSING_PUMPS);
        addMissingMaterial(missingMaterials, plan.requiredCopperCasings(), PipeConnectorLogic.countAvailableCopperCasings(player), Constants.HUD_MISSING_CASINGS);
        if (missingMaterials.isEmpty()) {
            return Component.translatable(Constants.HUD_MISSING_MATERIALS, Component.literal("?"));
        }
        return Component.translatable(Constants.HUD_MISSING_MATERIALS, joinComponents(missingMaterials));
    }

    /** Anade una entrada al mensaje cuando el material es insuficiente. */
    private static void addMissingMaterial(List<Component> missingMaterials, int required, int available, String translationKey) {
        int missing = required - available;
        if (missing > 0) {
            missingMaterials.add(Component.translatable(translationKey, missing));
        }
    }

    /** Une componentes localizados mediante separadores legibles. */
    private static MutableComponent joinComponents(List<Component> components) {
        MutableComponent joined = Component.empty();
        for (int index = 0; index < components.size(); index++) {
            if (index > 0) {
                joined.append(", ");
            }
            joined.append(components.get(index));
        }
        return joined;
    }

    /** Elimina registros de primer clic cuya ventana temporal ya expiro. */
    private static void clearExpiredWrenchClicks(long gameTime) {
        Iterator<Map.Entry<UUID, WrenchPipeClick>> iterator = WRENCH_PIPE_CLICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, WrenchPipeClick> entry = iterator.next();
            if (gameTime - entry.getValue().gameTime() > Constants.WRENCH_DOUBLE_CLICK_TICKS) {
                iterator.remove();
            }
        }
    }

    /** Conserva la posicion y el tick del primer clic con la llave. */
    private record WrenchPipeClick(BlockPos position, long gameTime) {
    }
}
