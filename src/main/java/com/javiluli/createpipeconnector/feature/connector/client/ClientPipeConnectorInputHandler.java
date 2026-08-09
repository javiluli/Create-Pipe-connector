package com.javiluli.createpipeconnector.feature.connector.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.ui.client.ConnectorOptionsRadialScreen;
import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import com.javiluli.createpipeconnector.feature.anchor.network.AddAnchorPayload;
import com.javiluli.createpipeconnector.feature.routing.network.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.feature.casing.network.CopperCasingModePayload;
import com.javiluli.createpipeconnector.feature.style.network.PipeStyleModePayload;
import com.javiluli.createpipeconnector.feature.pump.network.PumpModePayload;
import com.javiluli.createpipeconnector.feature.anchor.network.RemoveLastAnchorPayload;
import com.javiluli.createpipeconnector.feature.casing.network.RemoveLastCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.pump.network.RemoveLastManualPumpPayload;
import com.javiluli.createpipeconnector.feature.pump.network.ReverseAutoPumpDirectionPayload;
import com.javiluli.createpipeconnector.feature.routing.network.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.feature.connector.network.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.feature.casing.network.ToggleCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.pump.network.ToggleManualPumpPayload;
import com.javiluli.createpipeconnector.feature.style.network.WrenchPipeDisplayPayload;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementAnimationSynchronizer;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview;
import com.javiluli.createpipeconnector.feature.material.client.ClientMaterialPreview;
import com.javiluli.createpipeconnector.feature.preview.client.ClientPipePreviewCache;
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

/**
 * Coordina la entrada, el plan de ruta y la respuesta visual del modo conector.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientPipeConnectorInputHandler {
    private static final String NO_ROUTE_MESSAGE = "hud.createpipeconnector.no_route";
    private static final int MANUAL_MARKER_SNAP_DISTANCE = 3;
    private static boolean showingPipeStatus;
    private static boolean previewTargetLocked;
    private static PlacementTarget lockedPreviewTarget;
    private static RoutePlanKey cachedRoutePlanKey;
    private static ConnectionPlan cachedRoutePlan;
    private static boolean hasCachedRoutePlan;
    private static ConnectionPlan cachedAutoPumpBasePlan;
    private static ConnectionPlan cachedAutoPumpPlan;
    private static PumpMode cachedAutoPumpMode;
    private static boolean cachedAutoPumpDirectionReversed;
    private static ConnectionPlan cachedModifiedBasePlan;
    private static ConnectionPlan cachedModifiedPlan;
    private static List<BlockPos> cachedManualPumps;
    private static CopperCasingMode cachedCopperCasingMode;
    private static List<BlockPos> cachedCopperCasings;
    private static PipeStyleMode cachedPipeStyleMode;
    private static Block cachedModifiedPipeBlock;
    private static Selection cachedInitialPreviewSelection;
    private static ConnectionPlan cachedInitialPreviewPlan;

    /** Impide crear instancias del manejador de entrada. */
    private ClientPipeConnectorInputHandler() {
    }

    /**
     * Impide la interaccion vanilla cuando el modo conector controla el objetivo.
     */
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

    /**
     * Gestiona la confirmacion en el aire y la cancelacion con clic izquierdo.
     */
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

    /** Inicia o confirma localmente una ruta y envia el objetivo al servidor. */
    private static void handleClientTarget(LocalPlayer player, Block heldPipeBlock, PlacementTarget target) {
        ClientPlacementAnimationSynchronizer.syncIfConnected();
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

        ClientPlacementLeadPreview.enqueue(player.level(), ClientPipeConnectorState.getPreviewPipes());
        clearCurrentConnection(player);
    }

    /**
     * Consume controles configurables y actualiza el preview una vez por tick.
     */
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
            showPipeStatus(minecraft.player, Component.translatable(NO_ROUTE_MESSAGE).withStyle(ChatFormatting.RED));
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
        plan = applyPreviewModifiers(plan, selection);

        updatePreview(minecraft, selection, plan);
        clearPipeStatus(minecraft.player);
    }

    /** Construye el preview minimo visible desde el primer bloque seleccionado. */
    private static boolean showInitialPipePreview(Minecraft minecraft, Selection selection) {
        if (selection.existingPipe() || !ClientPipeConnectorState.getAnchors().isEmpty()) {
            return false;
        }

        if (!selection.equals(cachedInitialPreviewSelection)) {
            cachedInitialPreviewSelection = selection;
            cachedInitialPreviewPlan = new ConnectionPlan(
                    List.of(selection.position()),
                    List.of(selection.position())
            );
        }
        ConnectionPlan plan = applyPreviewModifiers(cachedInitialPreviewPlan, selection);

        updatePreview(minecraft, selection, plan);
        clearPipeStatus(minecraft.player);
        return true;
    }

    /** Calcula materiales y publica las piezas definitivas del preview. */
    private static void updatePreview(Minecraft minecraft, Selection selection, ConnectionPlan plan) {
        ClientPipePreviewCache.update(minecraft, selection, plan);
    }

    /** Obtiene o reutiliza el plan base para una seleccion y objetivo. */
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

    /** Obtiene el objetivo que sigue a la mirada mientras el preview esta libre. */
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

    /** Devuelve el objetivo fijado o el objetivo actual de la mirada. */
    private static PlacementTarget getActivePreviewTarget(Minecraft minecraft, Block pipeBlock) {
        return previewTargetLocked ? lockedPreviewTarget : getPreviewTarget(minecraft, pipeBlock);
    }

    /** Consume el control de bloqueo si la pantalla permite entrada de juego. */
    private static boolean consumePreviewLockToggle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumePreviewLockToggle();
    }

    /** Consume el control del modo conector si la pantalla lo permite. */
    private static boolean consumeConnectorModeToggle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeConnectorModeToggle();
    }

    /** Consume el control heredado de bombas automaticas si procede. */
    private static boolean consumeAutoPumpsToggle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeAutoPumpsToggle();
    }

    /** Consume el control de inversion automatica si procede. */
    private static boolean consumeAutoPumpDirectionReverse(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeAutoPumpDirectionReverse();
    }

    /** Consume el control de ciclo de revestimiento si procede. */
    private static boolean consumeCopperCasingModeCycle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeCopperCasingModeCycle();
    }

    /** Consume el control de ciclo de estilo si procede. */
    private static boolean consumePipeStyleModeCycle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumePipeStyleModeCycle();
    }

    /** Consume el control de prioridad o abre el menu radial. */
    private static boolean consumeRoutePriorityCycle(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRoutePriorityCycle();
    }

    /** Consume el control para anadir anclas. */
    private static boolean consumeAddAnchor(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeAddAnchor();
    }

    /** Consume el control para retirar la ultima ancla. */
    private static boolean consumeRemoveLastAnchor(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRemoveLastAnchor();
    }

    /** Consume el control para alternar revestimiento manual. */
    private static boolean consumeCopperCasing(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeCopperCasingToggle();
    }

    /** Consume el control para alternar una bomba manual. */
    private static boolean consumeManualPump(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeManualPumpToggle();
    }

    /** Consume el control para retirar la ultima bomba manual. */
    private static boolean consumeRemoveLastManualPump(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRemoveLastManualPump();
    }

    /** Consume el control para retirar el ultimo revestimiento manual. */
    private static boolean consumeRemoveLastCopperCasing(Minecraft minecraft) {
        return minecraft.screen == null && ClientPipeConnectorKeyMappings.consumeRemoveLastCopperCasing();
    }

    /** Comprueba que el objetivo puede anadirse como nueva ancla. */
    private static boolean canAddAnchor(Selection selection, PlacementTarget target) {
        if (selection.position().equals(target.position())) {
            return false;
        }

        List<PlacementTarget> anchors = ClientPipeConnectorState.getAnchors();
        return anchors.isEmpty() || !anchors.get(anchors.size() - 1).position().equals(target.position());
    }

    /** Aplica al plan la configuracion local de bombas automaticas. */
    private static ConnectionPlan applyAutoPumps(ConnectionPlan plan) {
        PumpMode pumpMode = ClientPipeConnectorState.getPumpMode();
        boolean reversed = ClientPipeConnectorState.isAutoPumpDirectionReversed();
        if (cachedAutoPumpBasePlan == plan
                && cachedAutoPumpMode == pumpMode
                && cachedAutoPumpDirectionReversed == reversed) {
            return cachedAutoPumpPlan;
        }

        cachedAutoPumpBasePlan = plan;
        cachedAutoPumpMode = pumpMode;
        cachedAutoPumpDirectionReversed = reversed;
        cachedAutoPumpPlan = PipeConnectorLogic.withPumpMode(plan, pumpMode, reversed);
        return cachedAutoPumpPlan;
    }

    /** Reutiliza los modificadores finales mientras sus entradas no cambien. */
    private static ConnectionPlan applyPreviewModifiers(ConnectionPlan plan, Selection selection) {
        List<BlockPos> manualPumps = ClientPipeConnectorState.getManualPumps();
        CopperCasingMode copperCasingMode = ClientPipeConnectorState.getCopperCasingMode();
        List<BlockPos> copperCasings = ClientPipeConnectorState.getCopperCasings();
        PipeStyleMode pipeStyleMode = ClientPipeConnectorState.getPipeStyleMode();
        Block pipeBlock = selection.pipeBlock();
        if (cachedModifiedBasePlan == plan
                && cachedManualPumps == manualPumps
                && cachedCopperCasingMode == copperCasingMode
                && cachedCopperCasings == copperCasings
                && cachedPipeStyleMode == pipeStyleMode
                && cachedModifiedPipeBlock == pipeBlock) {
            return cachedModifiedPlan;
        }

        ConnectionPlan modifiedPlan = PipeConnectorLogic.withManualPumps(plan, manualPumps);
        modifiedPlan = PipeConnectorLogic.withCopperCasingMode(
                modifiedPlan,
                copperCasingMode,
                copperCasings,
                pipeBlock
        );
        modifiedPlan = PipeConnectorLogic.withPipeStyleMode(modifiedPlan, pipeStyleMode, pipeBlock);

        cachedModifiedBasePlan = plan;
        cachedManualPumps = manualPumps;
        cachedCopperCasingMode = copperCasingMode;
        cachedCopperCasings = copperCasings;
        cachedPipeStyleMode = pipeStyleMode;
        cachedModifiedPipeBlock = pipeBlock;
        cachedModifiedPlan = modifiedPlan;
        return modifiedPlan;
    }

    /** Busca la posicion colocable mas cercana para marcar revestimiento. */
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
        return closestDistance <= MANUAL_MARKER_SNAP_DISTANCE ? closestPosition : null;
    }

    /** Busca la posicion recta mas cercana para marcar una bomba. */
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
        return closestDistance <= MANUAL_MARKER_SNAP_DISTANCE ? closestPosition : null;
    }

    /** Resuelve un objetivo de bloque o aire para el preview activo. */
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

    /** Convierte el bloque senalado en un objetivo de ruta valido. */
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

    /** Obtiene la tuberia senalada para una accion con la llave. */
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

    /** Proyecta un objetivo de ruta en el aire dentro del alcance del jugador. */
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

    /** Muestra un mensaje temporal del conector en la barra de accion. */
    private static void showPipeStatus(LocalPlayer player, Component message) {
        showingPipeStatus = true;
        player.displayClientMessage(message, true);
    }

    /** Limpia el mensaje temporal del conector. */
    private static void clearPipeStatus(LocalPlayer player) {
        if (!showingPipeStatus || player == null) {
            return;
        }

        showingPipeStatus = false;
        player.displayClientMessage(Component.empty(), true);
    }

    /** Libera el objetivo fijado por el modo de camara libre. */
    private static void clearPreviewTargetLock() {
        previewTargetLocked = false;
        lockedPreviewTarget = null;
    }

    /** Cancela el estado local de la conexion y limpia su interfaz. */
    private static void clearCurrentConnection(LocalPlayer player) {
        ClientPipeConnectorState.clearSelection();
        clearRoutePlanCache();
        clearPreviewTargetLock();
        clearPipeStatus(player);
    }

    /** Invalida la cache del ultimo plan calculado. */
    private static void clearRoutePlanCache() {
        cachedRoutePlanKey = null;
        cachedRoutePlan = null;
        hasCachedRoutePlan = false;
        cachedAutoPumpBasePlan = null;
        cachedAutoPumpPlan = null;
        cachedAutoPumpMode = null;
        cachedModifiedBasePlan = null;
        cachedModifiedPlan = null;
        cachedManualPumps = null;
        cachedCopperCasingMode = null;
        cachedCopperCasings = null;
        cachedPipeStyleMode = null;
        cachedModifiedPipeBlock = null;
        cachedInitialPreviewSelection = null;
        cachedInitialPreviewPlan = null;
        ClientPipePreviewCache.clear();
    }

    /** Descarta pulsaciones pendientes de controles asociados a una ruta. */
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

    /** Clave inmutable utilizada para reutilizar calculos de ruta identicos. */
    private record RoutePlanKey(Selection selection, List<PlacementTarget> anchors, PlacementTarget target, RoutePriority routePriority) {
    }
}
