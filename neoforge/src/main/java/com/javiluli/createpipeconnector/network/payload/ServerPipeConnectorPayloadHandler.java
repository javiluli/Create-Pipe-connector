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

/**
 * Applies decoded connector commands on the authoritative server thread.
 */
public final class ServerPipeConnectorPayloadHandler {
    private ServerPipeConnectorPayloadHandler() {
    }

    public static void handleToggleConnectorMode(ToggleConnectorModePayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setConnectorModeEnabled(player.getUUID(), payload.enabled());
    }

    public static void handleToggleAutoPumps(ToggleAutoPumpsPayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setAutoPumpsEnabled(player.getUUID(), payload.enabled());
    }

    public static void handlePumpMode(PumpModePayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setPumpMode(player.getUUID(), payload.mode());
    }

    public static void handleCopperCasingMode(CopperCasingModePayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setCopperCasingMode(player.getUUID(), payload.mode());
    }

    public static void handlePipeStyleMode(PipeStyleModePayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setPipeStyleMode(player.getUUID(), payload.mode());
    }

    public static void handleReverseAutoPumpDirection(ReverseAutoPumpDirectionPayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setAutoPumpDirectionReversed(player.getUUID(), payload.reversed());
    }

    public static void handleRoutePriority(RoutePriorityPayload payload, IPayloadContext context) {
        Player player = context.player();
        PipeConnectorLogic.setRoutePriority(player.getUUID(), payload.priority());
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

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(serverLevel, selection, PipeConnectorLogic.getAnchors(player.getUUID()), anchor, PipeConnectorLogic.getRoutePriority(player.getUUID()));
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

    public static void handleRemoveLastManualPump(RemoveLastManualPumpPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (validatedSelection(player) != null) {
            PipeConnectorLogic.removeLastManualPump(player.getUUID());
        }
    }

    public static void handleRemoveLastCopperCasing(RemoveLastCopperCasingPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (validatedSelection(player) != null) {
            PipeConnectorLogic.removeLastCopperCasing(player.getUUID());
        }
    }

    public static void handleToggleCopperCasing(ToggleCopperCasingPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (validatedSelection(player) == null) {
            return;
        }
        if (!PipeConnectorLogic.isWithinInteractionRange(player, payload.position())) {
            return;
        }

        PipeConnectorLogic.toggleCopperCasing(player.getUUID(), payload.position());
    }

    public static void handleToggleManualPump(ToggleManualPumpPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (validatedSelection(player) == null) {
            return;
        }
        if (!PipeConnectorLogic.isWithinInteractionRange(player, payload.position())) {
            return;
        }

        PipeConnectorLogic.toggleManualPump(player.getUUID(), payload.position());
    }

    public static void handleWrenchPipeDisplay(WrenchPipeDisplayPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ServerPipeConnectorEvents.handleWrenchPipeDisplayClick(player, serverLevel, payload.position());
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
