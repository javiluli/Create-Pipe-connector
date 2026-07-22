package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeDisplayToggleResult;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public final class ServerPipeConnectorEvents {
    private static final int WRENCH_DOUBLE_CLICK_TICKS = 10;
    private static final Map<UUID, WrenchPipeClick> WRENCH_PIPE_CLICKS = new HashMap<>();

    private ServerPipeConnectorEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

        clearExpiredWrenchClicks(player.level().getGameTime());

        Selection selection = PipeConnectorLogic.getSelection(player.getUUID());
        if (selection == null) {
            return;
        }

        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return;
        }

        if (PipeConnectorLogic.isPlayerInPipeMode(player, selection)) {
            return;
        }

        PipeConnectorLogic.clearSelection(player.getUUID());
        clearActionBar(player);
    }

    public static void handleWrenchPipeDisplayClick(Player player, ServerLevel serverLevel, BlockPos position) {
        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
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
                || gameTime - previousClick.gameTime() > WRENCH_DOUBLE_CLICK_TICKS) {
            WRENCH_PIPE_CLICKS.put(playerId, new WrenchPipeClick(position, gameTime));
            player.displayClientMessage(Component.translatable("hud.createpipeconnector.pipe_style_click_again"), true);
            return;
        }

        WRENCH_PIPE_CLICKS.remove(playerId);
        PipeDisplayToggleResult result = PipeConnectorLogic.togglePipeDisplaySegment(serverLevel, position);
        if (result.changed() <= 0) {
            player.displayClientMessage(Component.translatable("hud.createpipeconnector.pipe_style_no_changes"), true);
            return;
        }

        String translationKey = result.glassMode()
                ? "hud.createpipeconnector.pipe_style_to_glass"
                : "hud.createpipeconnector.pipe_style_to_default";
        player.displayClientMessage(Component.translatable(translationKey, result.changed()), true);
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
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

    public static boolean handlePipeTarget(Player player, ServerLevel serverLevel, PlacementTarget target) {
        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
            return false;
        }

        Block heldPipeBlock = PipeConnectorLogic.getHeldPipeBlock(player);
        Selection currentSelection = PipeConnectorLogic.getSelection(player.getUUID());
        if (heldPipeBlock == null || !isTargetValid(player, serverLevel, heldPipeBlock, target)) {
            if (currentSelection != null) {
                PipeConnectorLogic.clearSelection(player.getUUID());
                clearActionBar(player);
            }
            return false;
        }

        if (currentSelection == null) {
            PipeConnectorLogic.setSelection(player.getUUID(), new Selection(target.position(), heldPipeBlock, target.face(), target.existingPipe()));
            player.displayClientMessage(Component.translatable("hud.createpipeconnector.first_point_selected"), true);
            return true;
        }

        if (currentSelection.position().equals(target.position())) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        if (currentSelection.pipeBlock() != heldPipeBlock) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(serverLevel, currentSelection, PipeConnectorLogic.getAnchors(player.getUUID()), target, PipeConnectorLogic.getRoutePriority(player.getUUID()));
        if (plan == null) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }
        if (PipeConnectorLogic.isAutoPumpsEnabled(player.getUUID())) {
            plan = PipeConnectorLogic.withAutoPumps(plan, PipeConnectorLogic.isAutoPumpDirectionReversed(player.getUUID()));
        }

        if (!PipeConnectorLogic.hasEnoughItems(player, currentSelection.pipeBlock(), plan)) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        boolean connected = PipeConnectorLogic.connect(serverLevel, plan, currentSelection.pipeBlock());
        if (connected) {
            PipeConnectorLogic.consumeItems(player, currentSelection.pipeBlock(), plan);
        }

        PipeConnectorLogic.clearSelection(player.getUUID());
        clearActionBar(player);
        return true;
    }

    public static void cancelPipeConnection(Player player) {
        PipeConnectorLogic.clearSelection(player.getUUID());
        clearActionBar(player);
    }

    private static boolean isTargetValid(Player player, ServerLevel level, Block pipeBlock, PlacementTarget target) {
        if (!PipeConnectorLogic.isWithinInteractionRange(player, target.position())) {
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

    private static void clearActionBar(Player player) {
        player.displayClientMessage(Component.empty(), true);
    }

    private static void clearExpiredWrenchClicks(long gameTime) {
        Iterator<Map.Entry<UUID, WrenchPipeClick>> iterator = WRENCH_PIPE_CLICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, WrenchPipeClick> entry = iterator.next();
            if (gameTime - entry.getValue().gameTime() > WRENCH_DOUBLE_CLICK_TICKS) {
                iterator.remove();
            }
        }
    }

    private record WrenchPipeClick(BlockPos position, long gameTime) {
    }
}
