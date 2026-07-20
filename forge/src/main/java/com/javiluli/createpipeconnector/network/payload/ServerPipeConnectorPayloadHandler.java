package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ServerPipeConnectorPayloadHandler {
    private ServerPipeConnectorPayloadHandler() {
    }

    public static void handleAddAnchor(AddAnchorPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            Selection selection = PipeConnectorLogic.getSelection(player.getUUID());
            if (selection == null || !PipeConnectorLogic.isPlayerInPipeMode(player, selection)) {
                return;
            }

            PlacementTarget anchor = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
            if (!isAnchorValid(serverLevel, selection, anchor)) {
                return;
            }

            ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(serverLevel, selection, PipeConnectorLogic.getAnchors(player.getUUID()), anchor);
            if (plan != null) {
                PipeConnectorLogic.addAnchor(player.getUUID(), anchor);
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleRemoveLastAnchor(RemoveLastAnchorPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            Selection selection = PipeConnectorLogic.getSelection(player.getUUID());
            if (selection == null || !PipeConnectorLogic.isPlayerInPipeMode(player, selection)) {
                return;
            }

            PipeConnectorLogic.removeLastAnchor(player.getUUID());
        });
        context.setPacketHandled(true);
    }

    private static boolean isAnchorValid(ServerLevel level, Selection selection, PlacementTarget anchor) {
        if (selection.position().equals(anchor.position())) {
            return false;
        }

        if (!anchor.existingPipe()) {
            return PipeConnectorLogic.canPlacePipeAt(level, anchor.position());
        }

        BlockState anchorState = level.getBlockState(anchor.position());
        return PipeConnectorLogic.isConnectablePipe(anchorState) && anchorState.getBlock() == selection.pipeBlock();
    }
}
