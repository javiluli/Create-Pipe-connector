package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import com.javiluli.createpipeconnector.connector.ServerPipeConnectorEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class ServerPipeConnectorPayloadHandler {
    private ServerPipeConnectorPayloadHandler() {
    }

    public static void handleToggleConnectorMode(ToggleConnectorModePayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setConnectorModeEnabled(player.getUUID(), payload.enabled());
    }

    public static void handleSelectPipeTarget(SelectPipeTargetPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        PlacementTarget target = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
        ServerPipeConnectorEvents.handlePipeTarget(player, serverLevel, target);
    }

    public static void handleCancelPipeConnection(CancelPipeConnectionPayload payload, IPayloadContext context) {
        ServerPipeConnectorEvents.cancelPipeConnection(context.player());
    }

    public static void handleAddAnchor(AddAnchorPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Selection selection = validatedSelection(player);
        if (selection == null) {
            return;
        }

        PlacementTarget anchor = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
        if (!isAnchorValid(player, serverLevel, selection, anchor)) {
            return;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(serverLevel, selection, PipeConnectorLogic.getAnchors(player.getUUID()), anchor);
        if (plan != null) {
            PipeConnectorLogic.addAnchor(player.getUUID(), anchor);
        }
    }

    public static void handleRemoveLastAnchor(RemoveLastAnchorPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (validatedSelection(player) != null) {
            PipeConnectorLogic.removeLastAnchor(player.getUUID());
        }
    }

    private static Selection validatedSelection(Player player) {
        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            return null;
        }

        Selection selection = PipeConnectorLogic.getSelection(player.getUUID());
        if (selection != null && PipeConnectorLogic.isPlayerInPipeMode(player, selection)) {
            return selection;
        }

        PipeConnectorLogic.clearSelection(player.getUUID());
        return null;
    }

    private static boolean isAnchorValid(Player player, ServerLevel level, Selection selection, PlacementTarget anchor) {
        if (selection.position().equals(anchor.position())) {
            return false;
        }
        if (!PipeConnectorLogic.isWithinInteractionRange(player, anchor.position())) {
            return false;
        }

        if (!anchor.existingPipe()) {
            return PipeConnectorLogic.canPlacePipeAt(level, anchor.position());
        }

        BlockState anchorState = level.getBlockState(anchor.position());
        return PipeConnectorLogic.isConnectablePipe(anchorState) && anchorState.getBlock() == selection.pipeBlock();
    }
}
