package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class ServerPipeConnectorEvents {
    private ServerPipeConnectorEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return;
        }

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = event.getLevel().getBlockState(clickedPos);
        if (!PipeConnectorLogic.isConnectablePipe(clickedState)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);

        Selection currentSelection = PipeConnectorLogic.getSelection(player.getUUID());
        if (currentSelection == null) {
            PipeConnectorLogic.setSelection(player.getUUID(), new Selection(clickedPos, clickedState.getBlock()));
            player.displayClientMessage(Component.literal("Primer tubo seleccionado. Ahora marca el segundo."), true);
            return;
        }

        if (currentSelection.position().equals(clickedPos)) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            player.displayClientMessage(Component.literal("Selección cancelada."), true);
            return;
        }

        if (currentSelection.pipeBlock() != clickedState.getBlock()) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            player.displayClientMessage(Component.literal("Las dos tuberías deben ser del mismo tipo."), true);
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean connected = PipeConnectorLogic.connect(serverLevel, currentSelection.position(), clickedPos, currentSelection.pipeBlock());
        PipeConnectorLogic.clearSelection(player.getUUID());
        player.displayClientMessage(Component.literal(
                connected ? "Tuberías conectadas." : "No se encontró un recorrido libre para conectar esas tuberías."
        ), true);
    }
}
