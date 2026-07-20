package com.javiluli.createpipeconnector.client.input;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class ClientPipeConnectorInputHandler {
    private static boolean showingPipeStatus;
    private static boolean previewTargetLocked;
    private static PlacementTarget lockedPreviewTarget;

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

        Block heldPipeBlock = PipeConnectorLogic.getPipeBlock(player.getOffhandItem());
        if (heldPipeBlock == null) {
            return;
        }

        PlacementTarget clickedTarget = PipeConnectorLogic.resolvePlacementTarget(event.getLevel(), event.getPos(), event.getFace(), heldPipeBlock);
        if (clickedTarget == null) {
            return;
        }

        event.setCanceled(true);
        Selection currentSelection = ClientPipeConnectorState.getSelection();
        if (currentSelection == null) {
            ClientPipeConnectorState.setSelection(new Selection(clickedTarget.position(), heldPipeBlock, clickedTarget.face(), clickedTarget.existingPipe()));
            clearPreviewTargetLock();
            clearPipeStatus(player);
            return;
        }

        if (currentSelection.position().equals(clickedTarget.position()) || currentSelection.pipeBlock() != heldPipeBlock) {
            ClientPipeConnectorState.clearSelection();
            clearPreviewTargetLock();
            clearPipeStatus(player);
            return;
        }

        ClientPipeConnectorState.clearSelection();
        clearPreviewTargetLock();
        clearPipeStatus(player);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            ClientPipeConnectorState.clearSelection();
            clearPreviewTargetLock();
            clearPipeStatus(minecraft.player);
            return;
        }

        Selection selection = ClientPipeConnectorState.getSelection();
        if (selection == null) {
            ClientPipeConnectorKeyMappings.drainPlacementClicks();
            ClientPipeConnectorState.setPreviewPipes(List.of());
            clearPreviewTargetLock();
            clearPipeStatus(minecraft.player);
            return;
        }

        if (!PipeConnectorLogic.isPlayerInPipeMode(minecraft.player, selection)) {
            ClientPipeConnectorState.clearSelection();
            clearPreviewTargetLock();
            clearPipeStatus(minecraft.player);
            return;
        }

        Block heldPipeBlock = selection.pipeBlock();
        boolean anchorPressed = consumeAddAnchor(minecraft);
        boolean removeAnchorPressed = consumeRemoveLastAnchor(minecraft);
        if (removeAnchorPressed && ClientPipeConnectorState.removeLastAnchor()) {
            PacketDistributor.sendToServer(new RemoveLastAnchorPayload());
            clearPreviewTargetLock();
        }

        PlacementTarget target = getTrackingPreviewTarget(minecraft, heldPipeBlock);
        if (target == null || target.position().equals(selection.position())) {
            ClientPipeConnectorState.setPreviewPipes(List.of());
            clearPipeStatus(minecraft.player);
            return;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(minecraft.level, selection, ClientPipeConnectorState.getAnchors(), target);
        if (plan == null) {
            ClientPipeConnectorState.setPreviewPipes(List.of());
            showPipeStatus(minecraft.player, Component.literal("No hay recorrido libre.").withStyle(ChatFormatting.RED));
            return;
        }

        if (anchorPressed && canAddAnchor(selection, target)) {
            ClientPipeConnectorState.addAnchor(target);
            PacketDistributor.sendToServer(new AddAnchorPayload(target.position(), target.face(), target.existingPipe()));
            clearPreviewTargetLock();
            plan = PipeConnectorLogic.buildPlacementPlan(minecraft.level, selection, ClientPipeConnectorState.getAnchors(), target);
            if (plan == null) {
                ClientPipeConnectorState.setPreviewPipes(List.of());
                return;
            }
        }

        ClientPipeConnectorState.setPreviewPipes(PipeConnectorLogic.buildPreview(minecraft.level, plan, selection.pipeBlock()));
        showPipeRequirement(minecraft.player, selection, plan);
    }

    private static PlacementTarget getTrackingPreviewTarget(Minecraft minecraft, Block pipeBlock) {
        PlacementTarget hoveredTarget = getPreviewTarget(minecraft, pipeBlock);
        if (consumePreviewLockToggle(minecraft)) {
            if (previewTargetLocked) {
                clearPreviewTargetLock();
            } else if (hoveredTarget != null) {
                lockedPreviewTarget = hoveredTarget;
                previewTargetLocked = true;
            }
        }

        return previewTargetLocked ? lockedPreviewTarget : hoveredTarget;
    }

    private static boolean consumePreviewLockToggle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumePreviewLockToggle();
    }

    private static boolean consumeAddAnchor(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeAddAnchor();
    }

    private static boolean consumeRemoveLastAnchor(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRemoveLastAnchor();
    }

    private static boolean canAddAnchor(Selection selection, PlacementTarget target) {
        if (selection.position().equals(target.position())) {
            return false;
        }

        List<PlacementTarget> anchors = ClientPipeConnectorState.getAnchors();
        return anchors.isEmpty() || !anchors.get(anchors.size() - 1).position().equals(target.position());
    }

    private static PlacementTarget getPreviewTarget(Minecraft minecraft, Block pipeBlock) {
        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return null;
        }

        return PipeConnectorLogic.resolvePlacementTarget(minecraft.level, blockHitResult.getBlockPos(), blockHitResult.getDirection(), pipeBlock);
    }

    private static void showPipeRequirement(LocalPlayer player, Selection selection, ConnectionPlan plan) {
        int requiredPipes = plan.requiredPipes();
        if (player.getAbilities().instabuild) {
            showPipeStatus(player, Component.literal(requiredPipes + "/\u221e").withStyle(ChatFormatting.WHITE));
            return;
        }

        int availablePipes = PipeConnectorLogic.countAvailablePipes(player, selection.pipeBlock());
        boolean hasEnough = availablePipes >= requiredPipes;
        MutableComponent message = Component.literal(String.valueOf(requiredPipes))
                .withStyle(hasEnough ? ChatFormatting.WHITE : ChatFormatting.RED)
                .append(Component.literal("/" + availablePipes).withStyle(ChatFormatting.WHITE));
        showPipeStatus(player, message);
    }

    private static void showPipeStatus(LocalPlayer player, Component message) {
        showingPipeStatus = true;
        player.displayClientMessage(message, true);
    }

    private static void clearPipeStatus(LocalPlayer player) {
        if (!showingPipeStatus || player == null) {
            return;
        }

        showingPipeStatus = false;
        player.displayClientMessage(Component.empty(), true);
    }

    private static void clearPreviewTargetLock() {
        previewTargetLocked = false;
        lockedPreviewTarget = null;
    }
}
