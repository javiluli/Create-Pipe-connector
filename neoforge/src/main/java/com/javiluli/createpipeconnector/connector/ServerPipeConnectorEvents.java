package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ServerPipeConnectorEvents {
    private ServerPipeConnectorEvents() {
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }

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
        if (heldPipeBlock == null || !isTargetValid(player, serverLevel, heldPipeBlock, target)) {
            return false;
        }

        Selection currentSelection = PipeConnectorLogic.getSelection(player.getUUID());
        if (currentSelection == null) {
            PipeConnectorLogic.setSelection(player.getUUID(), new Selection(target.position(), heldPipeBlock, target.face(), target.existingPipe()));
            player.displayClientMessage(Component.literal("Primer punto seleccionado. Ahora marca el segundo."), true);
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

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(serverLevel, currentSelection, PipeConnectorLogic.getAnchors(player.getUUID()), target);
        if (plan == null) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        int requiredPipes = plan.requiredPipes();
        if (!PipeConnectorLogic.hasEnoughPipes(player, currentSelection.pipeBlock(), requiredPipes)) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        boolean connected = PipeConnectorLogic.connect(serverLevel, plan, currentSelection.pipeBlock());
        if (connected) {
            PipeConnectorLogic.consumePipes(player, currentSelection.pipeBlock(), requiredPipes);
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
}
