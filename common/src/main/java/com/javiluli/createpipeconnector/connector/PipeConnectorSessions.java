package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class PipeConnectorSessions {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();
    private static final Map<UUID, List<PlacementTarget>> ANCHORS = new HashMap<>();
    private static final Set<UUID> CONNECTOR_MODE_PLAYERS = new HashSet<>();
    private static final Set<UUID> AUTO_PUMP_PLAYERS = new HashSet<>();
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
        return AUTO_PUMP_PLAYERS.contains(playerId);
    }

    static void setAutoPumpsEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            AUTO_PUMP_PLAYERS.add(playerId);
            return;
        }

        AUTO_PUMP_PLAYERS.remove(playerId);
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

    static Selection getSelection(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    static void setSelection(UUID playerId, Selection selection) {
        SELECTIONS.put(playerId, selection);
        ANCHORS.remove(playerId);
    }

    static void clearSelection(UUID playerId) {
        SELECTIONS.remove(playerId);
        ANCHORS.remove(playerId);
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
}
