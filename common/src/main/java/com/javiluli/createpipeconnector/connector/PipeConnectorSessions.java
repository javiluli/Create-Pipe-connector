package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Stores transient server-side connector state keyed by player UUID.
 *
 * <p>Selections and manual route modifiers are intentionally discarded when
 * connector mode is disabled or the player session ends.</p>
 */
final class PipeConnectorSessions {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();
    private static final Map<UUID, List<PlacementTarget>> ANCHORS = new HashMap<>();
    private static final Map<UUID, List<BlockPos>> MANUAL_PUMPS = new HashMap<>();
    private static final Map<UUID, List<BlockPos>> COPPER_CASINGS = new HashMap<>();
    private static final Map<UUID, PipeConnectorLogic.RoutePriority> ROUTE_PRIORITIES = new HashMap<>();
    private static final Map<UUID, PipeConnectorLogic.PumpMode> PUMP_MODES = new HashMap<>();
    private static final Map<UUID, PipeConnectorLogic.CopperCasingMode> COPPER_CASING_MODES = new HashMap<>();
    private static final Map<UUID, PipeConnectorLogic.PipeStyleMode> PIPE_STYLE_MODES = new HashMap<>();
    private static final Set<UUID> CONNECTOR_MODE_PLAYERS = new HashSet<>();
    private static final Set<UUID> REVERSED_AUTO_PUMP_PLAYERS = new HashSet<>();

    private PipeConnectorSessions() {
    }

    static boolean isConnectorModeEnabled(UUID playerId) {
        return CONNECTOR_MODE_PLAYERS.contains(playerId);
    }

    static void setConnectorModeEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            CONNECTOR_MODE_PLAYERS.add(playerId);
            return;
        }

        CONNECTOR_MODE_PLAYERS.remove(playerId);
        clearSelection(playerId);
    }

    static boolean isAutoPumpsEnabled(UUID playerId) {
        return getPumpMode(playerId).isAutomatic();
    }

    static void setAutoPumpsEnabled(UUID playerId, boolean enabled) {
        setPumpMode(playerId, enabled ? PipeConnectorLogic.PumpMode.EFFICIENT : PipeConnectorLogic.PumpMode.OFF);
    }

    static PipeConnectorLogic.PumpMode getPumpMode(UUID playerId) {
        return PUMP_MODES.getOrDefault(playerId, PipeConnectorLogic.PumpMode.OFF);
    }

    static void setPumpMode(UUID playerId, PipeConnectorLogic.PumpMode mode) {
        if (mode == null || mode == PipeConnectorLogic.PumpMode.OFF) {
            PUMP_MODES.remove(playerId);
            return;
        }

        PUMP_MODES.put(playerId, mode);
    }

    static PipeConnectorLogic.CopperCasingMode getCopperCasingMode(UUID playerId) {
        return COPPER_CASING_MODES.getOrDefault(playerId, PipeConnectorLogic.CopperCasingMode.MANUAL);
    }

    static void setCopperCasingMode(UUID playerId, PipeConnectorLogic.CopperCasingMode mode) {
        if (mode == null || mode == PipeConnectorLogic.CopperCasingMode.MANUAL) {
            COPPER_CASING_MODES.remove(playerId);
            return;
        }

        COPPER_CASING_MODES.put(playerId, mode);
    }

    static PipeConnectorLogic.PipeStyleMode getPipeStyleMode(UUID playerId) {
        return PIPE_STYLE_MODES.getOrDefault(playerId, PipeConnectorLogic.PipeStyleMode.DEFAULT);
    }

    static void setPipeStyleMode(UUID playerId, PipeConnectorLogic.PipeStyleMode mode) {
        if (mode == null || mode == PipeConnectorLogic.PipeStyleMode.DEFAULT) {
            PIPE_STYLE_MODES.remove(playerId);
            return;
        }

        PIPE_STYLE_MODES.put(playerId, mode);
    }

    static boolean isAutoPumpDirectionReversed(UUID playerId) {
        return REVERSED_AUTO_PUMP_PLAYERS.contains(playerId);
    }

    static void setAutoPumpDirectionReversed(UUID playerId, boolean reversed) {
        if (reversed) {
            REVERSED_AUTO_PUMP_PLAYERS.add(playerId);
            return;
        }

        REVERSED_AUTO_PUMP_PLAYERS.remove(playerId);
    }

    static PipeConnectorLogic.RoutePriority getRoutePriority(UUID playerId) {
        return ROUTE_PRIORITIES.getOrDefault(playerId, PipeConnectorLogic.RoutePriority.AUTO);
    }

    static void setRoutePriority(UUID playerId, PipeConnectorLogic.RoutePriority priority) {
        if (priority == null || priority == PipeConnectorLogic.RoutePriority.AUTO) {
            ROUTE_PRIORITIES.remove(playerId);
            return;
        }

        ROUTE_PRIORITIES.put(playerId, priority);
    }

    static Selection getSelection(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    static void setSelection(UUID playerId, Selection selection) {
        SELECTIONS.put(playerId, selection);
        ANCHORS.remove(playerId);
        MANUAL_PUMPS.remove(playerId);
        COPPER_CASINGS.remove(playerId);
    }

    static void clearSelection(UUID playerId) {
        SELECTIONS.remove(playerId);
        ANCHORS.remove(playerId);
        MANUAL_PUMPS.remove(playerId);
        COPPER_CASINGS.remove(playerId);
    }

    static List<PlacementTarget> getAnchors(UUID playerId) {
        return List.copyOf(ANCHORS.getOrDefault(playerId, List.of()));
    }

    static void addAnchor(UUID playerId, PlacementTarget anchor) {
        List<PlacementTarget> anchors = new ArrayList<>(ANCHORS.getOrDefault(playerId, List.of()));
        if (!anchors.isEmpty() && anchors.get(anchors.size() - 1).position().equals(anchor.position())) {
            anchors.set(anchors.size() - 1, anchor);
        } else {
            anchors.add(anchor);
        }
        ANCHORS.put(playerId, List.copyOf(anchors));
    }

    static void removeLastAnchor(UUID playerId) {
        List<PlacementTarget> anchors = new ArrayList<>(ANCHORS.getOrDefault(playerId, List.of()));
        if (anchors.isEmpty()) {
            return;
        }

        anchors.remove(anchors.size() - 1);
        if (anchors.isEmpty()) {
            ANCHORS.remove(playerId);
        } else {
            ANCHORS.put(playerId, List.copyOf(anchors));
        }
    }

    static void clearAnchors(UUID playerId) {
        ANCHORS.remove(playerId);
    }

    static List<BlockPos> getManualPumps(UUID playerId) {
        return List.copyOf(MANUAL_PUMPS.getOrDefault(playerId, List.of()));
    }

    static void toggleManualPump(UUID playerId, BlockPos position) {
        List<BlockPos> manualPumps = new ArrayList<>(MANUAL_PUMPS.getOrDefault(playerId, List.of()));
        if (manualPumps.remove(position)) {
            updateManualPumps(playerId, manualPumps);
            return;
        }

        manualPumps.add(position);
        MANUAL_PUMPS.put(playerId, List.copyOf(manualPumps));
    }

    static void removeLastManualPump(UUID playerId) {
        List<BlockPos> manualPumps = new ArrayList<>(MANUAL_PUMPS.getOrDefault(playerId, List.of()));
        if (manualPumps.isEmpty()) {
            return;
        }

        manualPumps.remove(manualPumps.size() - 1);
        updateManualPumps(playerId, manualPumps);
    }

    private static void updateManualPumps(UUID playerId, List<BlockPos> manualPumps) {
        if (manualPumps.isEmpty()) {
            MANUAL_PUMPS.remove(playerId);
        } else {
            MANUAL_PUMPS.put(playerId, List.copyOf(manualPumps));
        }
    }

    static List<BlockPos> getCopperCasings(UUID playerId) {
        return List.copyOf(COPPER_CASINGS.getOrDefault(playerId, List.of()));
    }

    static void toggleCopperCasing(UUID playerId, BlockPos position) {
        List<BlockPos> copperCasings = new ArrayList<>(COPPER_CASINGS.getOrDefault(playerId, List.of()));
        if (copperCasings.remove(position)) {
            updateCopperCasings(playerId, copperCasings);
            return;
        }

        copperCasings.add(position);
        COPPER_CASINGS.put(playerId, List.copyOf(copperCasings));
    }

    static void removeLastCopperCasing(UUID playerId) {
        List<BlockPos> copperCasings = new ArrayList<>(COPPER_CASINGS.getOrDefault(playerId, List.of()));
        if (copperCasings.isEmpty()) {
            return;
        }

        copperCasings.remove(copperCasings.size() - 1);
        updateCopperCasings(playerId, copperCasings);
    }

    private static void updateCopperCasings(UUID playerId, List<BlockPos> copperCasings) {
        if (copperCasings.isEmpty()) {
            COPPER_CASINGS.remove(playerId);
        } else {
            COPPER_CASINGS.put(playerId, List.copyOf(copperCasings));
        }
    }
}
