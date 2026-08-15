package com.javiluli.createpipeconnector.feature.connector.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.connector.interaction.RouteInteractionResolver;
import com.javiluli.createpipeconnector.feature.material.client.ClientMaterialPreview;
import com.javiluli.createpipeconnector.feature.material.client.MissingMaterialsAlertHud;
import com.javiluli.createpipeconnector.feature.manual.ManualAction;
import com.javiluli.createpipeconnector.feature.manual.ManualMarkerChange;
import com.javiluli.createpipeconnector.feature.manual.ManualRouteChange;
import com.javiluli.createpipeconnector.feature.manual.config.ManualAnchorClientConfig;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementAnimationSynchronizer;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import com.javiluli.createpipeconnector.feature.ui.client.ConnectorOptionsRadialScreen;
import com.javiluli.createpipeconnector.feature.ui.client.PipeConnectorModeStatusHud;
import com.javiluli.createpipeconnector.feature.anchor.network.AddAnchorPayload;
import com.javiluli.createpipeconnector.feature.anchor.network.RemoveAnchorPayload;
import com.javiluli.createpipeconnector.feature.casing.network.CopperCasingModePayload;
import com.javiluli.createpipeconnector.feature.casing.network.RemoveLastCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.casing.network.ToggleCopperCasingPayload;
import com.javiluli.createpipeconnector.feature.connector.network.ToggleConnectorModePayload;
import com.javiluli.createpipeconnector.feature.pump.network.PumpModePayload;
import com.javiluli.createpipeconnector.feature.pump.network.RemoveLastManualPumpPayload;
import com.javiluli.createpipeconnector.feature.pump.network.PumpDirectionPayload;
import com.javiluli.createpipeconnector.feature.pump.network.ToggleManualPumpPayload;
import com.javiluli.createpipeconnector.feature.routing.network.CancelPipeConnectionPayload;
import com.javiluli.createpipeconnector.feature.routing.network.SelectPipeTargetPayload;
import com.javiluli.createpipeconnector.feature.style.network.PipeStyleModePayload;
import com.javiluli.createpipeconnector.feature.style.network.WrenchPipeDisplayPayload;
import com.javiluli.createpipeconnector.platform.network.CreatePipeConnectorNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.List;

/**
 * Traduce la entrada del cliente en estado, preview y paquetes validados.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientPipeConnectorInputHandler {
    private static final String NO_ROUTE_MESSAGE = "hud.createpipeconnector.no_route";
    private static final int MANUAL_MARKER_SNAP_DISTANCE = 3;
    private static boolean showingPipeStatus;
    private static boolean previewTargetLocked;
    private static PlacementTarget lockedPreviewTarget;
    private static RoutePlanKey cachedRoutePlanKey;
    private static ConnectionPlan cachedRoutePlan;
    private static boolean hasCachedRoutePlan;

    /** Impide crear instancias del manejador de entrada. */
    private ClientPipeConnectorInputHandler() {
    }

    /**
     * Impide la interaccion vanilla cuando el modo Pipe Connector controla el objetivo.
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
        Selection currentSelection = ClientPipeConnectorState.getSelection();
        boolean connectorHandlesInteraction = RouteInteractionResolver.shouldConnectorHandle(
                player,
                currentSelection,
                event.getLevel(),
                event.getPos()
        );
        if (!connectorHandlesInteraction) {
            return;
        }

        Block routePipeBlock = currentSelection == null
                ? PipeConnectorLogic.getHeldPipeBlock(player)
                : currentSelection.pipeBlock();
        if (routePipeBlock == null) {
            return;
        }

        if (PipeConnectorLogic.resolvePlacementTarget(event.getLevel(), event.getPos(), event.getFace(), routePipeBlock) == null) {
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
                CreatePipeConnectorNetwork.sendToServer(new CancelPipeConnectionPayload());
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
            CreatePipeConnectorNetwork.sendToServer(new WrenchPipeDisplayPayload(wrenchPipeDisplayTarget));
            return;
        }
        BlockPos interactionPosition = minecraft.hitResult instanceof BlockHitResult blockHitResult
                ? blockHitResult.getBlockPos()
                : null;
        boolean connectorHandlesInteraction = RouteInteractionResolver.shouldConnectorHandle(
                player,
                currentSelection,
                minecraft.level,
                interactionPosition
        );
        if (!connectorHandlesInteraction) {
            return;
        }

        Block routePipeBlock = currentSelection == null
                ? PipeConnectorLogic.getHeldPipeBlock(player)
                : currentSelection.pipeBlock();
        if (routePipeBlock == null) {
            return;
        }

        PlacementTarget target = currentSelection == null
                ? getBlockPreviewTarget(minecraft, routePipeBlock)
                : ClientPipeConnectorState.getPreviewTarget();
        if (target == null) {
            if (currentSelection != null) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
            return;
        }

        event.setCanceled(true);
        event.setSwingHand(false);
        if (currentSelection != null && ClientPipeConnectorState.getPreviewPipes().isEmpty()) {
            clearCurrentConnection(player);
            CreatePipeConnectorNetwork.sendToServer(new CancelPipeConnectionPayload());
            return;
        }
        if (currentSelection != null) {
            Component missingMaterialsMessage = ClientMaterialPreview.missingMaterialsMessage(ClientPipeConnectorState.getMaterialStatus());
            if (missingMaterialsMessage != null) {
                player.level().playLocalSound(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.NOTE_BLOCK_BASS.value(),
                        SoundSource.PLAYERS,
                        0.65F,
                        0.5F,
                        false
                );
                MissingMaterialsAlertHud.show(missingMaterialsMessage.copy().withStyle(ChatFormatting.RED));
                return;
            }
        }
        handleClientTarget(player, routePipeBlock, target);
    }

    /** Inicia o confirma localmente una ruta y envia el objetivo al servidor. */
    private static void handleClientTarget(LocalPlayer player, Block routePipeBlock, PlacementTarget target) {
        ClientPlacementAnimationSynchronizer.syncIfConnected();
        CreatePipeConnectorNetwork.sendToServer(new SelectPipeTargetPayload(target.position(), target.face(), target.existingPipe()));

        Selection currentSelection = ClientPipeConnectorState.getSelection();
        if (currentSelection == null) {
            PipeConnectorModeStatusHud.dismiss();
            ClientPipeConnectorState.setSelection(new Selection(target.position(), routePipeBlock, target.face(), target.existingPipe()));
            clearPreviewTargetLock();
            clearPipeStatus(player);
            return;
        }

        if (currentSelection.position().equals(target.position())) {
            clearCurrentConnection(player);
            return;
        }

        ClientPlacementLeadPreview.enqueue(
                player.level(),
                routePipeBlock,
                ClientPipeConnectorState.getPreviewPipes()
        );
        clearCurrentConnection(player);
    }

    /**
     * Consume controles configurables y actualiza el preview una vez por tick.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            ClientPipeConnectorState.setConnectorModeEnabled(false);
            clearCurrentConnection(minecraft.player);
            return;
        }

        if (minecraft.screen != null) {
            ClientPipeConnectorKeyMappings.drainPlacementClicks();
            return;
        }

        if (ClientPipeConnectorKeyMappings.consumePipeConnectorModeToggle()) {
            boolean enabled = !ClientPipeConnectorState.isConnectorModeEnabled();
            ClientPipeConnectorState.setConnectorModeEnabled(enabled);
            CreatePipeConnectorNetwork.sendToServer(new ToggleConnectorModePayload(enabled));
            clearPreviewTargetLock();
            clearPipeStatus(minecraft.player);
            PipeConnectorModeStatusHud.show(enabled);
        }

        if (!ClientPipeConnectorState.isConnectorModeEnabled()) {
            ClientPipeConnectorKeyMappings.drainPlacementClicks();
            clearCurrentConnection(minecraft.player);
            return;
        }

        if (ClientPipeConnectorKeyMappings.consumePumpModeCycle()) {
            PumpMode pumpMode = ClientPipeConnectorState.getPumpMode().next();
            ClientPipeConnectorState.setPumpMode(pumpMode);
            CreatePipeConnectorNetwork.sendToServer(new PumpModePayload(pumpMode));
            clearPipeStatus(minecraft.player);
        }

        if (ClientPipeConnectorKeyMappings.consumePumpDirectionReverse()) {
            boolean reversed = !ClientPipeConnectorState.isPumpDirectionReversed();
            ClientPipeConnectorState.setPumpDirectionReversed(reversed);
            CreatePipeConnectorNetwork.sendToServer(new PumpDirectionPayload(reversed));
            clearPipeStatus(minecraft.player);
        }

        if (ClientPipeConnectorKeyMappings.consumeCopperCasingModeCycle()) {
            CopperCasingMode mode = ClientPipeConnectorState.getCopperCasingMode().next();
            ClientPipeConnectorState.setCopperCasingMode(mode);
            CreatePipeConnectorNetwork.sendToServer(new CopperCasingModePayload(mode));
            clearPipeStatus(minecraft.player);
        }

        if (ClientPipeConnectorKeyMappings.consumePipeStyleModeCycle()) {
            PipeStyleMode mode = ClientPipeConnectorState.getPipeStyleMode().next();
            ClientPipeConnectorState.setPipeStyleMode(mode);
            CreatePipeConnectorNetwork.sendToServer(new PipeStyleModePayload(mode));
            clearPipeStatus(minecraft.player);
        }

        if (ClientPipeConnectorKeyMappings.consumeOpenPipeConnectorOptions()) {
            minecraft.setScreen(new ConnectorOptionsRadialScreen());
            clearPipeStatus(minecraft.player);
            return;
        }

        if (ClientPipeConnectorKeyMappings.consumeManualActionCycle()) {
            ClientPipeConnectorState.setManualAction(ClientPipeConnectorState.getManualAction().next());
            clearPipeStatus(minecraft.player);
        }

        Selection selection = ClientPipeConnectorState.getSelection();
        if (selection == null) {
            ClientPipeConnectorKeyMappings.drainRouteClicks();
            ClientPipeConnectorState.setPreviewPipes(List.of());
            ClientPipeConnectorState.setMaterialStatus(null);
            clearPreviewTargetLock();
            clearPipeStatus(minecraft.player);
            return;
        }

        if (!PipeConnectorLogic.isSelectionStillValid(minecraft.level, selection)) {
            clearCurrentConnection(minecraft.player);
            return;
        }

        Block routePipeBlock = selection.pipeBlock();
        boolean applyManualActionPressed = ClientPipeConnectorKeyMappings.consumeApplyManualAction();
        boolean undoLastRouteActionPressed = ClientPipeConnectorKeyMappings.consumeUndoLastRouteAction();
        ManualAction manualAction = ClientPipeConnectorState.getManualAction();
        boolean anchorPressed = applyManualActionPressed && manualAction == ManualAction.ANCHOR;
        boolean dedicatedCopperCasingPressed = ClientPipeConnectorKeyMappings.consumeCopperCasingToggle();
        boolean dedicatedManualPumpPressed = ClientPipeConnectorKeyMappings.consumeManualPumpToggle();
        boolean copperCasingPressed = dedicatedCopperCasingPressed
                || applyManualActionPressed && manualAction == ManualAction.COPPER_CASING;
        boolean manualPumpPressed = dedicatedManualPumpPressed
                || applyManualActionPressed && manualAction == ManualAction.MECHANICAL_PUMP;
        boolean removeManualPumpPressed = ClientPipeConnectorKeyMappings.consumeRemoveLastManualPump()
                || dedicatedManualPumpPressed && Screen.hasShiftDown();
        boolean removeCopperCasingPressed = ClientPipeConnectorKeyMappings.consumeRemoveLastCopperCasing()
                || dedicatedCopperCasingPressed && Screen.hasShiftDown();
        if (undoLastRouteActionPressed) {
            syncManualRouteUndo(ClientPipeConnectorState.undoLastManualRouteAction());
        }
        if (removeManualPumpPressed) {
            manualPumpPressed = false;
            ManualMarkerChange change = ClientPipeConnectorState.removeLastManualPump();
            if (change != null) {
                CreatePipeConnectorNetwork.sendToServer(new RemoveLastManualPumpPayload());
                syncManualSupportAnchor(change, null);
            }
        }
        if (removeCopperCasingPressed) {
            copperCasingPressed = false;
            ManualMarkerChange change = ClientPipeConnectorState.removeLastCopperCasing();
            if (change != null) {
                CreatePipeConnectorNetwork.sendToServer(new RemoveLastCopperCasingPayload());
                syncManualSupportAnchor(change, null);
            }
        }

        PlacementTarget target = getTrackingPreviewTarget(minecraft, routePipeBlock);
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
            showPipeStatus(
                    minecraft.player,
                    Component.translatable(NO_ROUTE_MESSAGE).withStyle(ChatFormatting.RED)
            );
            return;
        }
        plan = applyAutoPumps(plan);

        if (anchorPressed && canAddAnchor(selection, target)) {
            ClientPipeConnectorState.addAnchor(target);
            CreatePipeConnectorNetwork.sendToServer(new AddAnchorPayload(target.position(), target.face(), target.existingPipe()));
            plan = getBasePlacementPlan(minecraft, selection, target);
            if (plan == null) {
                ClientPipeConnectorState.setPreviewPipes(List.of());
                ClientPipeConnectorState.setMaterialStatus(null);
                return;
            }
            plan = applyAutoPumps(plan);
        }

        if (manualPumpPressed) {
            BlockPos manualPumpPosition = closestManualPumpPosition(plan, target.position());
            if (manualPumpPosition != null) {
                PlacementTarget supportAnchor = manualSupportAnchorIfEnabled(
                        plan,
                        manualPumpPosition,
                        target.face()
                );
                ManualMarkerChange change = ClientPipeConnectorState.toggleManualPump(manualPumpPosition, supportAnchor);
                CreatePipeConnectorNetwork.sendToServer(new ToggleManualPumpPayload(manualPumpPosition));
                syncManualSupportAnchor(change, supportAnchor);
            }
        }
        plan = PipeConnectorLogic.withManualPumps(
                plan,
                ClientPipeConnectorState.getManualPumps(),
                ClientPipeConnectorState.isPumpDirectionReversed()
        );

        if (copperCasingPressed && PipeConnectorLogic.supportsCopperCasing(selection.pipeBlock())) {
            BlockPos copperCasingPosition = closestCopperCasingPosition(plan, target.position());
            if (copperCasingPosition != null) {
                PlacementTarget supportAnchor = manualSupportAnchorIfEnabled(
                        plan,
                        copperCasingPosition,
                        target.face()
                );
                ManualMarkerChange change = ClientPipeConnectorState.toggleCopperCasing(copperCasingPosition, supportAnchor);
                CreatePipeConnectorNetwork.sendToServer(new ToggleCopperCasingPayload(copperCasingPosition));
                syncManualSupportAnchor(change, supportAnchor);
            }
        }
        plan = applyPreviewAppearance(plan, selection);

        updatePreview(minecraft, selection, target, plan);
        clearPipeStatus(minecraft.player);
    }

    /** Construye el preview minimo visible desde el primer bloque seleccionado. */
    private static boolean showInitialPipePreview(Minecraft minecraft, Selection selection) {
        if (selection.existingPipe() || !ClientPipeConnectorState.getAnchors().isEmpty()) {
            return false;
        }

        ConnectionPlan plan = new ConnectionPlan(
                List.of(selection.position()),
                List.of(selection.position())
        );
        plan = applyPreviewAppearance(plan, selection);

        updatePreview(minecraft, selection, null, plan);
        clearPipeStatus(minecraft.player);
        return true;
    }

    /** Aplica bombas, revestimientos y estilo al plan base del preview. */
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

    /** Calcula materiales y publica las piezas definitivas del preview. */
    private static void updatePreview(
            Minecraft minecraft,
            Selection selection,
            PlacementTarget target,
            ConnectionPlan plan
    ) {
        ClientPipeConnectorState.MaterialStatus materialStatus = ClientMaterialPreview.createStatus(
                minecraft.player,
                selection,
                plan
        );
        List<PreviewPipe> previewPipes = ClientMaterialPreview.markMissingMaterials(
                plan,
                PipeConnectorLogic.buildPreview(minecraft.level, plan, selection.pipeBlock()),
                materialStatus
        );
        ClientPipeConnectorState.setPreviewPipes(previewPipes);
        ClientPipeConnectorState.setPreviewTarget(previewPipes.isEmpty() ? null : target);
        ClientPipeConnectorState.setMaterialStatus(materialStatus);
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
        if (ClientPipeConnectorKeyMappings.consumePreviewLockToggle()) {
            if (previewTargetLocked) {
                clearPreviewTargetLock();
            } else if (hoveredTarget != null) {
                lockedPreviewTarget = hoveredTarget;
                previewTargetLocked = true;
            }
        }

        return previewTargetLocked ? lockedPreviewTarget : hoveredTarget;
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
        return PipeConnectorLogic.withPumpMode(plan, ClientPipeConnectorState.getPumpMode(), ClientPipeConnectorState.isPumpDirectionReversed());
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

    /** Crea un ancla colocable que conserva la posicion elegida por la accion manual. */
    private static PlacementTarget manualSupportAnchor(
            ConnectionPlan plan,
            BlockPos position,
            Direction fallbackFace
    ) {
        List<BlockPos> path = plan.path();
        int index = path.indexOf(position);
        Direction face = fallbackFace;
        if (index >= 0 && index + 1 < path.size()) {
            face = PipeConnectorLogic.directionBetween(position, path.get(index + 1));
        } else if (index > 0) {
            face = PipeConnectorLogic.directionBetween(position, path.get(index - 1));
        }
        return new PlacementTarget(position, face == null ? Direction.UP : face, false);
    }

    /** Crea el ancla auxiliar solo cuando la preferencia individual esta activa. */
    private static PlacementTarget manualSupportAnchorIfEnabled(
            ConnectionPlan plan,
            BlockPos position,
            Direction fallbackFace
    ) {
        return ManualAnchorClientConfig.isEnabled()
                ? manualSupportAnchor(plan, position, fallbackFace)
                : null;
    }

    /** Sincroniza el alta o retirada del ancla creada por una marca manual. */
    private static void syncManualSupportAnchor(ManualMarkerChange change, PlacementTarget supportAnchor) {
        if (change == null || !change.anchorChanged()) {
            return;
        }
        if (change.added() && supportAnchor != null) {
            CreatePipeConnectorNetwork.sendToServer(new AddAnchorPayload(
                    supportAnchor.position(),
                    supportAnchor.face(),
                    supportAnchor.existingPipe()
            ));
            return;
        }
        CreatePipeConnectorNetwork.sendToServer(new RemoveAnchorPayload(change.position()));
    }

    /** Sincroniza el deshacer global respetando el tipo real de la ultima accion. */
    private static void syncManualRouteUndo(ManualRouteChange change) {
        if (change == null) {
            return;
        }

        if (change.action() == ManualAction.MECHANICAL_PUMP) {
            CreatePipeConnectorNetwork.sendToServer(new RemoveLastManualPumpPayload());
        } else if (change.action() == ManualAction.COPPER_CASING) {
            CreatePipeConnectorNetwork.sendToServer(new RemoveLastCopperCasingPayload());
        }

        if (change.anchorRemoved()) {
            CreatePipeConnectorNetwork.sendToServer(new RemoveAnchorPayload(change.position()));
        }
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

        double reach = PipeConnectorLogic.getInteractionRange(player);
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
    }

    /** Clave inmutable utilizada para reutilizar calculos de ruta identicos. */
    private record RoutePlanKey(Selection selection, List<PlacementTarget> anchors, PlacementTarget target, RoutePriority routePriority) {
    }
}
