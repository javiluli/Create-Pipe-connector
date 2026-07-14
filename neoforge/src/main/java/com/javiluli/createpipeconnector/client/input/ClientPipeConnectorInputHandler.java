package com.javiluli.createpipeconnector.client.input;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientPipeConnectorInputHandler {
    private ClientPipeConnectorInputHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) {
            return;
        }

        BlockPos clickedPos = event.getPos();
        BlockState clickedState = event.getLevel().getBlockState(clickedPos);
        if (!PipeConnectorLogic.isConnectablePipe(clickedState)) {
            return;
        }

        Selection currentSelection = ClientPipeConnectorState.getSelection();
        if (currentSelection == null) {
            ClientPipeConnectorState.setSelection(new Selection(clickedPos, clickedState.getBlock()));
            return;
        }

        if (currentSelection.position().equals(clickedPos) || currentSelection.pipeBlock() != clickedState.getBlock()) {
            ClientPipeConnectorState.clearSelection();
            return;
        }

        ClientPipeConnectorState.clearSelection();
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            ClientPipeConnectorState.clearSelection();
            return;
        }

        Selection selection = ClientPipeConnectorState.getSelection();
        if (selection == null) {
            ClientPipeConnectorState.setPreviewPipes(List.of());
            return;
        }

        BlockState selectionState = minecraft.level.getBlockState(selection.position());
        if (!PipeConnectorLogic.isConnectablePipe(selectionState) || selection.pipeBlock() != selectionState.getBlock()) {
            ClientPipeConnectorState.clearSelection();
            return;
        }

        BlockPos targetPos = getHoveredPipePos(minecraft);
        if (targetPos == null || targetPos.equals(selection.position())) {
            ClientPipeConnectorState.setPreviewPipes(List.of(new PreviewPipe(
                    selection.position(),
                    PipeConnectorLogic.createPipeState(selection.pipeBlock(), selectionState)
            )));
            return;
        }

        BlockState targetState = minecraft.level.getBlockState(targetPos);
        if (!PipeConnectorLogic.isConnectablePipe(targetState) || selection.pipeBlock() != targetState.getBlock()) {
            ClientPipeConnectorState.setPreviewPipes(List.of(new PreviewPipe(
                    selection.position(),
                    PipeConnectorLogic.createPipeState(selection.pipeBlock(), selectionState)
            )));
            return;
        }

        ClientPipeConnectorState.setPreviewPipes(PipeConnectorLogic.buildPreview(
                minecraft.level,
                selection.position(),
                targetPos,
                selection.pipeBlock()
        ));
    }

    private static BlockPos getHoveredPipePos(Minecraft minecraft) {
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return null;
        }

        BlockPos targetPos = blockHitResult.getBlockPos();
        BlockState targetState = minecraft.level.getBlockState(targetPos);
        return PipeConnectorLogic.isConnectablePipe(targetState) ? targetPos : null;
    }
}
