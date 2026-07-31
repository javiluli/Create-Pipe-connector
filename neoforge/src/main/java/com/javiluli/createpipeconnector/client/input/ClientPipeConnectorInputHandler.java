package com.javiluli.createpipeconnector.client.input;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.client.screen.ConnectorOptionsRadialScreen;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.CopperCasingMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeStyleMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PumpMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import com.javiluli.createpipeconnector.network.payload.AddAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.network.payload.CopperCasingModePayload;
import com.javiluli.createpipeconnector.network.payload.PipeStyleModePayload;
import com.javiluli.createpipeconnector.network.payload.PumpModePayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastCopperCasingPayload;
import com.javiluli.createpipeconnector.network.payload.RemoveLastManualPumpPayload;
import com.javiluli.createpipeconnector.network.payload.ReverseAutoPumpDirectionPayload;
import com.javiluli.createpipeconnector.network.payload.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.network.payload.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.network.payload.ToggleCopperCasingPayload;
import com.javiluli.createpipeconnector.network.payload.ToggleManualPumpPayload;
import com.javiluli.createpipeconnector.network.payload.WrenchPipeDisplayPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
/**
 * Coordinates connector-mode input, live route planning and client feedback.
 */
public final class ClientPipeConnectorInputHandler {
    private static boolean showingPipeStatus;
    private static boolean previewTargetLocked;
    private static PlacementTarget lockedPreviewTarget;
    private static RoutePlanKey cachedRoutePlanKey;
    private static ConnectionPlan cachedRoutePlan;
    private static boolean hasCachedRoutePlan;

    private ClientPipeConnectorInputHandler() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!ClientPipeConnectorState.isConnectorModeEnabled()) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (PipeConnectorLogic.isCreateWrench(player.getMainHandItem())
                && PipeConnectorLogic.isPipeDisplayToggleTarget(event.getLevel().getBlockState(event.getPos()))) {
            event.setCanceled(true);
            return;
        }

        Block heldPipeBlock = PipeConnectorLogic.getHeldPipeBlock(player);
        if (heldPipeBlock == null) {
            return;
        }

        if (PipeConnectorLogic.resolvePlacementTarget(event.getLevel(), event.getPos(), event.getFace(), heldPipeBlock) == null) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onInteractionKeyMappingTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || event.getHand() != InteractionHand.MAIN_HAND || !ClientPipeConnectorState.isConnectorModeEnabled()) {
            return;
        }

        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        Selection currentSelection = ClientPipeConnectorState.getSelection();
        if (event.isAttack()) {
            if (currentSelection != null) {
                event.setCanceled(true);
                event.setSwingHand(false);
                clearCurrentConnection(player);
                PacketDistributor.sendToServer(new CancelPipeConnectionPayload());
            }
            return;
        }

        if (!event.isUseItem()) {
            return;
        }

        BlockPos wrenchPipeDisplayTarget = getWrenchPipeDisplayTarget(minecraft);
        if (wrenchPipeDisplayTarget != null) {
            event.setCanceled(true);
            event.setSwingHand(false);
            PacketDistributor.sendToServer(new WrenchPipeDisplayPayload(wrenchPipeDisplayTarget));
            return;
        }

        Block heldPipeBlock = PipeConnectorLogic.getHeldPipeBlock(player);
        if (heldPipeBlock == null) {
            return;
        }

        PlacementTarget target = currentSelection == null
                ? getBlockPreviewTarget(minecraft, heldPipeBlock)
                : getActivePreviewTarget(minecraft, heldPipeBlock);
        if (target == null) {
            if (currentSelection != null) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(false);
        if (currentSelection != null && !previewTargetLocked && !PipeConnectorLogic.isWithinInteractionRange(player, target.position())) {
            clearCurrentConnection(player);
            PacketDistributor.sendToServer(new CancelPipeConnectionPayload());
            return;
        }
        if (currentSelection != null && ClientPipeConnectorState.getPreviewPipes().isEmpty()) {
            clearCurrentConnection(player);
            PacketDistributor.sendToServer(new CancelPipeConnectionPayload());
            return;
        }
        if (currentSelection != null) {
            Component missingMaterialsMessage = ClientMaterialPreview.missingMaterialsMessage(ClientPipeConnectorState.getMaterialStatus());
            if (missingMaterialsMessage != null) {
                player.displayClientMessage(missingMaterialsMessage.copy().withStyle(ChatFormatting.RED), true);
                return;
            }
        }
        handleClientTarget(player, heldPipeBlock, target);
    }

    private static void handleClientTarget(LocalPlayer player, Block heldPipeBlock, PlacementTarget target) {
        PacketDistributor.sendToServer(new SelectPipeTargetPayload(target.position(), target.face(), target.existingPipe()));

        Selection currentSelection = ClientPipeConnectorState.getSelection();
        if (currentSelection == null) {
            ClientPipeConnectorState.setSelection(new Selection(target.position(), heldPipeBlock, target.face(), target.existingPipe()));
            clearPreviewTargetLock();
            clearPipeStatus(player);
            return;
        }

        if (currentSelection.position().equals(target.position()) || currentSelection.pipeBlock() != heldPipeBlock) {
            clearCurrentConnection(player);
            return;
        }

        clearCurrentConnection(player);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            ClientPipeConnectorState.setConnectorModeEnabled(false);
            clearCurrentConnection(minecraft.player);
            return;
        }

        if (consumeConnectorModeToggle(minecraft)) {
            boolean enabled = !ClientPipeConnectorState.isConnectorModeEnabled();
            ClientPipeConnectorState.setConnectorModeEnabled(enabled);
            PacketDistributor.sendToServer(new ToggleConnectorModePayload(enabled));
            clearPreviewTargetLock();
            clearPipeStatus(minecraft.player);
        }

        if (!ClientPipeConnectorState.isConnectorModeEnabled()) {
            ClientPipeConnectorKeyMappings.drainPlacementClicks();
            clearCurrentConnection(minecraft.player);
            return;
        }

        if (consumeAutoPumpsToggle(minecraft)) {
            PumpMode pumpMode = ClientPipeConnectorState.getPumpMode().next();
            ClientPipeConnectorState.setPumpMode(pumpMode);
            PacketDistributor.sendToServer(new PumpModePayload(pumpMode));
            clearPipeStatus(minecraft.player);
        }

        if (consumeAutoPumpDirectionReverse(minecraft) && ClientPipeConnectorState.getPumpMode().isAutomatic()) {
            boolean reversed = !ClientPipeConnectorState.isAutoPumpDirectionReversed();
            ClientPipeConnectorState.setAutoPumpDirectionReversed(reversed);
            PacketDistributor.sendToServer(new ReverseAutoPumpDirectionPayload(reversed));
            clearPipeStatus(minecraft.player);
        }

        if (consumeCopperCasingModeCycle(minecraft)) {
            CopperCasingMode mode = ClientPipeConnectorState.getCopperCasingMode().next();
            ClientPipeConnectorState.setCopperCasingMode(mode);
            PacketDistributor.sendToServer(new CopperCasingModePayload(mode));
            clearPipeStatus(minecraft.player);
        }

        if (consumePipeStyleModeCycle(minecraft)) {
            PipeStyleMode mode = ClientPipeConnectorState.getPipeStyleMode().next();
            ClientPipeConnectorState.setPipeStyleMode(mode);
            PacketDistributor.sendToServer(new PipeStyleModePayload(mode));
            clearPipeStatus(minecraft.player);
        }

        if (consumeRoutePriorityCycle(minecraft)) {
            minecraft.setScreen(new ConnectorOptionsRadialScreen());
            clearPipeStatus(minecraft.player);
        }

        Selection selection = ClientPipeConnectorState.getSelection();
        if (selection == null) {
            drainRoutingKeys();
            ClientPipeConnectorState.setPreviewPipes(List.of());
            ClientPipeConnectorState.setMaterialStatus(null);
            clearPreviewTargetLock();
            clearPipeStatus(minecraft.player);
            return;
        }

        if (!PipeConnectorLogic.isPlayerInPipeMode(minecraft.player, selection)) {
            clearCurrentConnection(minecraft.player);
            return;
        }

        Block heldPipeBlock = selection.pipeBlock();
        boolean anchorPressed = consumeAddAnchor(minecraft);
        boolean removeAnchorPressed = consumeRemoveLastAnchor(minecraft);
        boolean copperCasingPressed = consumeCopperCasing(minecraft);
        boolean manualPumpPressed = consumeManualPump(minecraft);
        boolean removeManualPumpPressed = consumeRemoveLastManualPump(minecraft) || manualPumpPressed && Screen.hasShiftDown();
        boolean removeCopperCasingPressed = consumeRemoveLastCopperCasing(minecraft) || copperCasingPressed && Screen.hasShiftDown();
        if (removeAnchorPressed && ClientPipeConnectorState.removeLastAnchor()) {
            PacketDistributor.sendToServer(new RemoveLastAnchorPayload());
            clearPreviewTargetLock();
        }

        PlacementTarget target = getTrackingPreviewTarget(minecraft, heldPipeBlock);
        if (target == null || target.position().equals(selection.position())) {
            if (!showInitialPipePreview(minecraft, selection)) {
                ClientPipeConnectorState.setPreviewPipes(List.of());
                ClientPipeConnectorState.setMaterialStatus(null);
                clearPipeStatus(minecraft.player);
            }
            return;
        }

        ConnectionPlan plan = getBasePlacementPlan(minecraft, selection, target);
        if (plan == null) {
            ClientPipeConnectorState.setPreviewPipes(List.of());
            ClientPipeConnectorState.setMaterialStatus(null);
            showPipeStatus(minecraft.player, Component.translatable(Constants.HUD_NO_ROUTE).withStyle(ChatFormatting.RED));
            return;
        }
        plan = applyAutoPumps(plan);

        if (anchorPressed && canAddAnchor(selection, target)) {
            ClientPipeConnectorState.addAnchor(target);
            PacketDistributor.sendToServer(new AddAnchorPayload(target.position(), target.face(), target.existingPipe()));
            clearPreviewTargetLock();
            plan = getBasePlacementPlan(minecraft, selection, target);
            if (plan == null) {
                ClientPipeConnectorState.setPreviewPipes(List.of());
                ClientPipeConnectorState.setMaterialStatus(null);
                return;
            }
            plan = applyAutoPumps(plan);
        }

        if (manualPumpPressed || removeManualPumpPressed) {
            if (removeManualPumpPressed) {
                if (ClientPipeConnectorState.removeLastManualPump()) {
                    PacketDistributor.sendToServer(new RemoveLastManualPumpPayload());
                }
            } else {
                BlockPos manualPumpPosition = closestManualPumpPosition(plan, target.position());
                if (manualPumpPosition != null) {
                    ClientPipeConnectorState.toggleManualPump(manualPumpPosition);
                    PacketDistributor.sendToServer(new ToggleManualPumpPayload(manualPumpPosition));
                }
            }
        }
        plan = PipeConnectorLogic.withManualPumps(plan, ClientPipeConnectorState.getManualPumps());

        if ((copperCasingPressed || removeCopperCasingPressed) && PipeConnectorLogic.supportsCopperCasing(selection.pipeBlock())) {
            if (removeCopperCasingPressed) {
                if (ClientPipeConnectorState.removeLastCopperCasing()) {
                    PacketDistributor.sendToServer(new RemoveLastCopperCasingPayload());
                }
            } else {
                if (ClientPipeConnectorState.getCopperCasingMode() != CopperCasingMode.MANUAL) {
                    ClientPipeConnectorState.setCopperCasingMode(CopperCasingMode.MANUAL);
                    PacketDistributor.sendToServer(new CopperCasingModePayload(CopperCasingMode.MANUAL));
                }
                BlockPos copperCasingPosition = closestCopperCasingPosition(plan, target.position());
                if (copperCasingPosition != null) {
                    ClientPipeConnectorState.toggleCopperCasing(copperCasingPosition);
                    PacketDistributor.sendToServer(new ToggleCopperCasingPayload(copperCasingPosition));
                }
            }
        }
        plan = applyPreviewAppearance(plan, selection);

        updatePreview(minecraft, selection, plan);
        clearPipeStatus(minecraft.player);
    }

    private static boolean showInitialPipePreview(Minecraft minecraft, Selection selection) {
        if (selection.existingPipe() || !ClientPipeConnectorState.getAnchors().isEmpty()) {
            return false;
        }

        ConnectionPlan plan = new ConnectionPlan(
                List.of(selection.position()),
                List.of(selection.position())
        );
        plan = applyPreviewAppearance(plan, selection);

        updatePreview(minecraft, selection, plan);
        clearPipeStatus(minecraft.player);
        return true;
    }

    private static ConnectionPlan applyPreviewAppearance(ConnectionPlan plan, Selection selection) {
        ConnectionPlan styledPlan = PipeConnectorLogic.withCopperCasingMode(
                plan,
                ClientPipeConnectorState.getCopperCasingMode(),
                ClientPipeConnectorState.getCopperCasings(),
                selection.pipeBlock()
        );
        return PipeConnectorLogic.withPipeStyleMode(
                styledPlan,
                ClientPipeConnectorState.getPipeStyleMode(),
                selection.pipeBlock()
        );
    }

    private static void updatePreview(Minecraft minecraft, Selection selection, ConnectionPlan plan) {
        ClientPipeConnectorState.setPreviewPipes(ClientMaterialPreview.markMissingMaterials(
                minecraft.player,
                selection,
                plan,
                PipeConnectorLogic.buildPreview(minecraft.level, plan, selection.pipeBlock())
        ));
        ClientMaterialPreview.updateStatus(minecraft.player, selection, plan);
    }

    private static ConnectionPlan getBasePlacementPlan(Minecraft minecraft, Selection selection, PlacementTarget target) {
        RoutePlanKey routePlanKey = new RoutePlanKey(selection, List.copyOf(ClientPipeConnectorState.getAnchors()), target, ClientPipeConnectorState.getRoutePriority());
        if (hasCachedRoutePlan && routePlanKey.equals(cachedRoutePlanKey)) {
            return cachedRoutePlan;
        }

        cachedRoutePlanKey = routePlanKey;
        cachedRoutePlan = PipeConnectorLogic.buildPlacementPlan(minecraft.level, selection, routePlanKey.anchors(), target, routePlanKey.routePriority());
        hasCachedRoutePlan = true;
        return cachedRoutePlan;
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

    private static PlacementTarget getActivePreviewTarget(Minecraft minecraft, Block pipeBlock) {
        return previewTargetLocked ? lockedPreviewTarget : getPreviewTarget(minecraft, pipeBlock);
    }

    private static boolean consumePreviewLockToggle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumePreviewLockToggle();
    }

    private static boolean consumeConnectorModeToggle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeConnectorModeToggle();
    }

    private static boolean consumeAutoPumpsToggle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeAutoPumpsToggle();
    }

    private static boolean consumeAutoPumpDirectionReverse(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeAutoPumpDirectionReverse();
    }

    private static boolean consumeCopperCasingModeCycle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeCopperCasingModeCycle();
    }

    private static boolean consumePipeStyleModeCycle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumePipeStyleModeCycle();
    }

    private static boolean consumeRoutePriorityCycle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRoutePriorityCycle();
    }

    private static boolean consumeAddAnchor(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeAddAnchor();
    }

    private static boolean consumeRemoveLastAnchor(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRemoveLastAnchor();
    }

    private static boolean consumeCopperCasing(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeCopperCasingToggle();
    }

    private static boolean consumeManualPump(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeManualPumpToggle();
    }

    private static boolean consumeRemoveLastManualPump(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRemoveLastManualPump();
    }

    private static boolean consumeRemoveLastCopperCasing(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRemoveLastCopperCasing();
    }

    private static boolean canAddAnchor(Selection selection, PlacementTarget target) {
        if (selection.position().equals(target.position())) {
            return false;
        }

        List<PlacementTarget> anchors = ClientPipeConnectorState.getAnchors();
        return anchors.isEmpty() || !anchors.get(anchors.size() - 1).position().equals(target.position());
    }

    private static ConnectionPlan applyAutoPumps(ConnectionPlan plan) {
        return PipeConnectorLogic.withPumpMode(plan, ClientPipeConnectorState.getPumpMode(), ClientPipeConnectorState.isAutoPumpDirectionReversed());
    }

    private static BlockPos closestCopperCasingPosition(ConnectionPlan plan, BlockPos targetPosition) {
        BlockPos closestPosition = null;
        int closestDistance = Integer.MAX_VALUE;
        for (BlockPos position : plan.placementPositions()) {
            if (plan.pumpPlacements().containsKey(position)) {
                continue;
            }

            int distance = position.distManhattan(targetPosition);
            if (distance < closestDistance) {
                closestPosition = position;
                closestDistance = distance;
            }
        }
        return closestDistance <= Constants.MANUAL_MARKER_SNAP_DISTANCE ? closestPosition : null;
    }

    private static BlockPos closestManualPumpPosition(ConnectionPlan plan, BlockPos targetPosition) {
        if (PipeConnectorLogic.getMechanicalPumpBlock() == null) {
            return null;
        }

        BlockPos closestPosition = null;
        int closestDistance = Integer.MAX_VALUE;
        for (BlockPos position : plan.placementPositions()) {
            if (plan.pumpPlacements().containsKey(position)) {
                continue;
            }
            if (PipeConnectorLogic.straightPumpFacing(plan.path(), position) == null) {
                continue;
            }

            int distance = position.distManhattan(targetPosition);
            if (distance < closestDistance) {
                closestPosition = position;
                closestDistance = distance;
            }
        }
        return closestDistance <= Constants.MANUAL_MARKER_SNAP_DISTANCE ? closestPosition : null;
    }

    private static PlacementTarget getPreviewTarget(Minecraft minecraft, Block pipeBlock) {
        if (minecraft.level == null || minecraft.player == null) {
            return null;
        }

        PlacementTarget blockTarget = getBlockPreviewTarget(minecraft, pipeBlock);
        if (blockTarget != null) {
            return blockTarget;
        }

        return getAirPreviewTarget(minecraft);
    }

    private static PlacementTarget getBlockPreviewTarget(Minecraft minecraft, Block pipeBlock) {
        if (minecraft.level == null) {
            return null;
        }

        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        return PipeConnectorLogic.resolvePlacementTarget(minecraft.level, blockHitResult.getBlockPos(), blockHitResult.getDirection(), pipeBlock);
    }

    private static BlockPos getWrenchPipeDisplayTarget(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || !PipeConnectorLogic.isCreateWrench(minecraft.player.getMainHandItem())) {
            return null;
        }

        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult) || hitResult.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos position = blockHitResult.getBlockPos();
        return PipeConnectorLogic.isPipeDisplayToggleTarget(minecraft.level.getBlockState(position)) ? position : null;
    }

    private static PlacementTarget getAirPreviewTarget(Minecraft minecraft) {
        LocalPlayer player = minecraft.player;
        if (minecraft.level == null || player == null) {
            return null;
        }

        double reach = player.blockInteractionRange();
        Vec3 lookVector = player.getViewVector(1.0F);
        BlockPos targetPosition = BlockPos.containing(player.getEyePosition().add(lookVector.scale(reach)));
        if (!PipeConnectorLogic.canPlacePipeAt(minecraft.level, targetPosition)) {
            return null;
        }

        Direction face = Direction.getNearest(lookVector.x(), lookVector.y(), lookVector.z());
        return new PlacementTarget(targetPosition, face, false);
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

    private static void clearCurrentConnection(LocalPlayer player) {
        ClientPipeConnectorState.clearSelection();
        clearRoutePlanCache();
        clearPreviewTargetLock();
        clearPipeStatus(player);
    }

    private static void clearRoutePlanCache() {
        cachedRoutePlanKey = null;
        cachedRoutePlan = null;
        hasCachedRoutePlan = false;
    }

    private static void drainRoutingKeys() {
        while (ClientPipeConnectorKeyMappings.consumePreviewLockToggle()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeAddAnchor()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeRemoveLastAnchor()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeCopperCasingToggle()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeRemoveLastCopperCasing()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeManualPumpToggle()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeRemoveLastManualPump()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeAutoPumpsToggle()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeCopperCasingModeCycle()) {
        }
        while (ClientPipeConnectorKeyMappings.consumePipeStyleModeCycle()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeAutoPumpDirectionReverse()) {
        }
        while (ClientPipeConnectorKeyMappings.consumeRoutePriorityCycle()) {
        }
    }

    private record RoutePlanKey(Selection selection, List<PlacementTarget> anchors, PlacementTarget target, PipeConnectorLogic.RoutePriority routePriority) {
    }
}
