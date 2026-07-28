package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import com.javiluli.createpipeconnector.connector.ServerPipeConnectorEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ServerPipeConnectorPayloadHandler {
    private ServerPipeConnectorPayloadHandler() {
    }

    public static void handleToggleConnectorMode(ToggleConnectorModePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            PipeConnectorLogic.setConnectorModeEnabled(player.getUUID(), payload.enabled());
            if (!payload.enabled()) {
                ServerPipeConnectorEvents.cancelPipeConnection(player);
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleToggleAutoPumps(ToggleAutoPumpsPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PipeConnectorLogic.setAutoPumpsEnabled(player.getUUID(), payload.enabled());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handlePumpMode(PumpModePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PipeConnectorLogic.setPumpMode(player.getUUID(), payload.mode());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleCopperCasingMode(CopperCasingModePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PipeConnectorLogic.setCopperCasingMode(player.getUUID(), payload.mode());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handlePipeStyleMode(PipeStyleModePayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PipeConnectorLogic.setPipeStyleMode(player.getUUID(), payload.mode());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleReverseAutoPumpDirection(ReverseAutoPumpDirectionPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PipeConnectorLogic.setAutoPumpDirectionReversed(player.getUUID(), payload.reversed());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleRoutePriority(RoutePriorityPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                PipeConnectorLogic.setRoutePriority(player.getUUID(), payload.priority());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleSelectPipeTarget(SelectPipeTargetPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            PlacementTarget target = new PlacementTarget(payload.position(), payload.face(), payload.existingPipe());
            ServerPipeConnectorEvents.handlePipeTarget(player, serverLevel, target);
        });
        context.setPacketHandled(true);
    }

    public static void handleCancelPipeConnection(CancelPipeConnectionPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                ServerPipeConnectorEvents.cancelPipeConnection(player);
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleAddAnchor(AddAnchorPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
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
        });
        context.setPacketHandled(true);
    }

    public static void handleRemoveLastAnchor(RemoveLastAnchorPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && validatedSelection(player) != null) {
                PipeConnectorLogic.removeLastAnchor(player.getUUID());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleRemoveLastManualPump(RemoveLastManualPumpPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && validatedSelection(player) != null) {
                PipeConnectorLogic.removeLastManualPump(player.getUUID());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleRemoveLastCopperCasing(RemoveLastCopperCasingPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && validatedSelection(player) != null) {
                PipeConnectorLogic.removeLastCopperCasing(player.getUUID());
            }
        });
        context.setPacketHandled(true);
    }

    public static void handleToggleCopperCasing(ToggleCopperCasingPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || validatedSelection(player) == null) {
                return;
            }
            if (!PipeConnectorLogic.isWithinInteractionRange(player, payload.position())) {
                return;
            }

            PipeConnectorLogic.toggleCopperCasing(player.getUUID(), payload.position());
        });
        context.setPacketHandled(true);
    }

    public static void handleToggleManualPump(ToggleManualPumpPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || validatedSelection(player) == null) {
                return;
            }
            if (!PipeConnectorLogic.isWithinInteractionRange(player, payload.position())) {
                return;
            }

            PipeConnectorLogic.toggleManualPump(player.getUUID(), payload.position());
        });
        context.setPacketHandled(true);
    }

    public static void handleWrenchPipeDisplay(WrenchPipeDisplayPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            ServerPipeConnectorEvents.handleWrenchPipeDisplayClick(player, serverLevel, payload.position());
        });
        context.setPacketHandled(true);
    }

    private static Selection validatedSelection(ServerPlayer player) {
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

    private static boolean isAnchorValid(ServerPlayer player, ServerLevel level, Selection selection, PlacementTarget anchor) {
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
