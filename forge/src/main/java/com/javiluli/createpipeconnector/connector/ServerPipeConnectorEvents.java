package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeDisplayToggleResult;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ServerPipeConnectorEvents {
    private static final int WRENCH_DOUBLE_CLICK_TICKS = 10;
    private static final Map<UUID, WrenchPipeClick> WRENCH_PIPE_CLICKS = new HashMap<>();

    private ServerPipeConnectorEvents() {
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Player player = event.player;
        if (player.level().isClientSide()) {
            return;
        }

        clearExpiredWrenchClicks(player.level().getGameTime());

        Selection selection = PipeConnectorLogic.getSelection(player.getUUID());
        if (selection == null) {
            return;
        }

        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return;
        }

        if (PipeConnectorLogic.isPlayerInPipeMode(player, selection)) {
            return;
        }

        PipeConnectorLogic.clearSelection(player.getUUID());
        clearActionBar(player);
    }

    public static void handleWrenchPipeDisplayClick(Player player, ServerLevel serverLevel, BlockPos position) {
        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
            return;
        }
        if (!PipeConnectorLogic.isCreateWrench(player.getMainHandItem())) {
            return;
        }
        if (!PipeConnectorLogic.isWithinInteractionRange(player, position)) {
            return;
        }
        if (!PipeConnectorLogic.isPipeDisplayToggleTarget(serverLevel.getBlockState(position))) {
            return;
        }

        long gameTime = serverLevel.getGameTime();
        UUID playerId = player.getUUID();
        WrenchPipeClick previousClick = WRENCH_PIPE_CLICKS.get(playerId);
        if (previousClick == null
                || !previousClick.position().equals(position)
                || gameTime - previousClick.gameTime() > WRENCH_DOUBLE_CLICK_TICKS) {
            WRENCH_PIPE_CLICKS.put(playerId, new WrenchPipeClick(position, gameTime));
            player.displayClientMessage(Component.translatable("hud.createpipeconnector.pipe_style_click_again"), true);
            return;
        }

        WRENCH_PIPE_CLICKS.remove(playerId);
        PipeDisplayToggleResult result = PipeConnectorLogic.togglePipeDisplaySegment(serverLevel, position);
        if (result.changed() <= 0) {
            player.displayClientMessage(Component.translatable("hud.createpipeconnector.pipe_style_no_changes"), true);
            return;
        }

        String translationKey = result.glassMode()
                ? "hud.createpipeconnector.pipe_style_to_glass"
                : "hud.createpipeconnector.pipe_style_to_default";
        player.displayClientMessage(Component.translatable(translationKey, result.changed()), true);
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();
        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
            return;
        }

        Block heldPipeBlock = PipeConnectorLogic.getHeldPipeBlock(player);
        if (heldPipeBlock == null) {
            return;
        }

        PlacementTarget clickedTarget = PipeConnectorLogic.resolvePlacementTarget(event.getLevel(), event.getPos(), event.getFace(), heldPipeBlock);
        if (clickedTarget == null) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            handlePipeTarget(player, serverLevel, clickedTarget);
        }
    }

    public static boolean handlePipeTarget(Player player, ServerLevel serverLevel, PlacementTarget target) {
        if (!PipeConnectorLogic.isConnectorModeEnabled(player.getUUID())) {
            return false;
        }

        Block heldPipeBlock = PipeConnectorLogic.getHeldPipeBlock(player);
        Selection currentSelection = PipeConnectorLogic.getSelection(player.getUUID());
        if (heldPipeBlock == null || !isTargetValid(player, serverLevel, heldPipeBlock, target, currentSelection == null)) {
            if (currentSelection != null) {
                PipeConnectorLogic.clearSelection(player.getUUID());
                clearActionBar(player);
            }
            return false;
        }

        if (currentSelection == null) {
            PipeConnectorLogic.setSelection(player.getUUID(), new Selection(target.position(), heldPipeBlock, target.face(), target.existingPipe()));
            player.displayClientMessage(Component.translatable("hud.createpipeconnector.first_point_selected"), true);
            return true;
        }

        if (currentSelection.position().equals(target.position())) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        if (currentSelection.pipeBlock() != heldPipeBlock) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }

        ConnectionPlan plan = PipeConnectorLogic.buildPlacementPlan(serverLevel, currentSelection, PipeConnectorLogic.getAnchors(player.getUUID()), target, PipeConnectorLogic.getRoutePriority(player.getUUID()));
        if (plan == null) {
            PipeConnectorLogic.clearSelection(player.getUUID());
            clearActionBar(player);
            return true;
        }
        PipeConnectorLogic.PumpMode pumpMode = PipeConnectorLogic.getPumpMode(player.getUUID());
        plan = PipeConnectorLogic.withPumpMode(plan, pumpMode, PipeConnectorLogic.isAutoPumpDirectionReversed(player.getUUID()));
        plan = PipeConnectorLogic.withManualPumps(plan, PipeConnectorLogic.getManualPumps(player.getUUID()));
        plan = PipeConnectorLogic.withCopperCasingMode(plan, PipeConnectorLogic.getCopperCasingMode(player.getUUID()), PipeConnectorLogic.getCopperCasings(player.getUUID()), currentSelection.pipeBlock());
        plan = PipeConnectorLogic.withPipeStyleMode(plan, PipeConnectorLogic.getPipeStyleMode(player.getUUID()), currentSelection.pipeBlock());

        if (!PipeConnectorLogic.hasEnoughItems(player, currentSelection.pipeBlock(), plan)) {
            player.displayClientMessage(missingMaterialsMessage(player, currentSelection.pipeBlock(), plan).copy().withStyle(ChatFormatting.RED), true);
            return true;
        }

        boolean connected = PipeConnectorLogic.connect(serverLevel, plan, currentSelection.pipeBlock());
        if (connected) {
            PipeConnectorLogic.consumeItems(player, currentSelection.pipeBlock(), plan);
        }

        PipeConnectorLogic.clearSelection(player.getUUID());
        clearActionBar(player);
        return true;
    }

    public static void cancelPipeConnection(Player player) {
        PipeConnectorLogic.clearSelection(player.getUUID());
        clearActionBar(player);
    }

    private static boolean isTargetValid(Player player, ServerLevel level, Block pipeBlock, PlacementTarget target, boolean requireReach) {
        if (requireReach && !PipeConnectorLogic.isWithinInteractionRange(player, target.position())) {
            return false;
        }

        BlockState targetState = level.getBlockState(target.position());
        if (!target.existingPipe()) {
            return !PipeConnectorLogic.isConnectablePipe(targetState)
                    && PipeConnectorLogic.canPlacePipeAt(level, target.position());
        }

        return PipeConnectorLogic.isConnectablePipe(targetState)
                && targetState.getBlock() == pipeBlock;
    }

    private static void clearActionBar(Player player) {
        player.displayClientMessage(Component.empty(), true);
    }

    private static Component missingMaterialsMessage(Player player, Block pipeBlock, ConnectionPlan plan) {
        List<Component> missingMaterials = new ArrayList<>();
        addMissingMaterial(missingMaterials, plan.requiredPipes(), PipeConnectorLogic.countAvailablePipes(player, pipeBlock), "hud.createpipeconnector.missing_pipes");
        addMissingMaterial(missingMaterials, plan.requiredPumps(), PipeConnectorLogic.countAvailablePumps(player), "hud.createpipeconnector.missing_pumps");
        addMissingMaterial(missingMaterials, plan.requiredCopperCasings(), PipeConnectorLogic.countAvailableCopperCasings(player), "hud.createpipeconnector.missing_casings");
        if (missingMaterials.isEmpty()) {
            return Component.translatable("hud.createpipeconnector.missing_materials", Component.literal("?"));
        }
        return Component.translatable("hud.createpipeconnector.missing_materials", joinComponents(missingMaterials));
    }

    private static void addMissingMaterial(List<Component> missingMaterials, int required, int available, String translationKey) {
        int missing = required - available;
        if (missing > 0) {
            missingMaterials.add(Component.translatable(translationKey, missing));
        }
    }

    private static MutableComponent joinComponents(List<Component> components) {
        MutableComponent joined = Component.empty();
        for (int index = 0; index < components.size(); index++) {
            if (index > 0) {
                joined.append(", ");
            }
            joined.append(components.get(index));
        }
        return joined;
    }

    private static void clearExpiredWrenchClicks(long gameTime) {
        Iterator<Map.Entry<UUID, WrenchPipeClick>> iterator = WRENCH_PIPE_CLICKS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, WrenchPipeClick> entry = iterator.next();
            if (gameTime - entry.getValue().gameTime() > WRENCH_DOUBLE_CLICK_TICKS) {
                iterator.remove();
            }
        }
    }

    private record WrenchPipeClick(BlockPos position, long gameTime) {
    }
}
