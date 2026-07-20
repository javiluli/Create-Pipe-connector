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
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return;
        }

        Block heldPipeBlock = PipeConnectorLogic.getPipeBlock(player.getOffhandItem());
        if (heldPipeBlock == null) {
            return;
        }

        PlacementTarget clickedTarget = PipeConnectorLogic.resolvePlacementTarget(event.getLevel(), event.getPos(), event.getFace(), heldPipeBlock);
        if (clickedTarget == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        Selection currentSelection = PipeConnectorLogic.getSelection(player.getUUID());
        if (currentSelection == null) {
            PipeConnectorLogic.setSelection(player.getUUID(), new Selection(clickedTarget.position(), heldPipeBlock, clickedTarget.face(), clickedTarget.existingPipe()));
            player.displayClientMessage(Component.literal("Primer punto seleccionado. Ahora marca el segundo."), true);
            return;
        }

        if (currentSelection.position().equals(clickedTarget.position())) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return;
        }

        if (currentSelection.pipeBlock() != heldPipeBlock) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(serverLevel, currentSelection, PipeConnectorLogic.getAnchors(player.getUUID()), clickedTarget);
        if (plan == null) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return;
        }

        int requiredPipes = plan.requiredPipes();
        if (!PipeConnectorLogic.hasEnoughPipes(player, currentSelection.pipeBlock(), requiredPipes)) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return;
        }

        boolean connected = PipeConnectorLogic.connect(serverLevel, plan, currentSelection.pipeBlock());
        if (connected) {
            PipeConnectorLogic.consumePipes(player, currentSelection.pipeBlock(), requiredPipes);
        }

        PipeConnectorLogic.clearSelection(player.getUUID());
        clearActionBar(player);
    }

    private static void clearActionBar(Player player) {
        player.displayClientMessage(Component.empty(), true);
    }
}
