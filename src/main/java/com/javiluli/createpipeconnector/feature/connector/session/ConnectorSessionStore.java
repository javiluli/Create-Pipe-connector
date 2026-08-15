package com.javiluli.createpipeconnector.feature.connector.session;

import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Almacena el estado temporal del conector asociado al UUID de cada jugador.
 *
 * <p>Las selecciones y los modificadores manuales se descartan al desactivar el
 * modo Pipe Connector o al terminar la sesion del jugador.</p>
 */
public final class ConnectorSessionStore {
    private static final Map<UUID, Selection> SELECTIONS = new HashMap<>();
    private static final Map<UUID, List<PlacementTarget>> ANCHORS = new HashMap<>();
    private static final Map<UUID, List<BlockPos>> MANUAL_PUMPS = new HashMap<>();
    private static final Map<UUID, List<BlockPos>> COPPER_CASINGS = new HashMap<>();
    private static final Map<UUID, RoutePriority> ROUTE_PRIORITIES = new HashMap<>();
    private static final Map<UUID, PumpMode> PUMP_MODES = new HashMap<>();
    private static final Map<UUID, CopperCasingMode> COPPER_CASING_MODES = new HashMap<>();
    private static final Map<UUID, PipeStyleMode> PIPE_STYLE_MODES = new HashMap<>();
    private static final Set<UUID> CONNECTOR_MODE_PLAYERS = new HashSet<>();
    private static final Set<UUID> REVERSED_PUMP_PLAYERS = new HashSet<>();

    /** Impide crear instancias del almacen global de sesiones. */
    private ConnectorSessionStore() {
    }

    /** Indica si el jugador tiene activo el modo Pipe Connector. */
    public static boolean isConnectorModeEnabled(UUID playerId) {
        return CONNECTOR_MODE_PLAYERS.contains(playerId);
    }

    /** Actualiza el modo Pipe Connector y limpia la ruta al desactivarlo. */
    public static void setConnectorModeEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            CONNECTOR_MODE_PLAYERS.add(playerId);
            return;
        }

        CONNECTOR_MODE_PLAYERS.remove(playerId);
        clearSelection(playerId);
    }

    /** Devuelve el modo de bombas guardado o su valor predeterminado. */
    public static PumpMode getPumpMode(UUID playerId) {
        return PUMP_MODES.getOrDefault(playerId, PumpMode.OFF);
    }

    /** Guarda el modo de bombas omitiendo el valor predeterminado. */
    public static void setPumpMode(UUID playerId, PumpMode mode) {
        if (mode == null || mode == PumpMode.OFF) {
            PUMP_MODES.remove(playerId);
            return;
        }

        PUMP_MODES.put(playerId, mode);
    }

    /** Devuelve el modo de revestimiento guardado para el jugador. */
    public static CopperCasingMode getCopperCasingMode(UUID playerId) {
        return COPPER_CASING_MODES.getOrDefault(playerId, CopperCasingMode.NONE);
    }

    /** Guarda el modo de revestimiento omitiendo el valor predeterminado. */
    public static void setCopperCasingMode(UUID playerId, CopperCasingMode mode) {
        if (mode == null || mode == CopperCasingMode.NONE) {
            COPPER_CASING_MODES.remove(playerId);
            return;
        }

        COPPER_CASING_MODES.put(playerId, mode);
    }

    /** Devuelve el estilo de tuberia guardado para el jugador. */
    public static PipeStyleMode getPipeStyleMode(UUID playerId) {
        return PIPE_STYLE_MODES.getOrDefault(playerId, PipeStyleMode.DEFAULT);
    }

    /** Guarda el estilo de tuberia omitiendo el valor predeterminado. */
    public static void setPipeStyleMode(UUID playerId, PipeStyleMode mode) {
        if (mode == null || mode == PipeStyleMode.DEFAULT) {
            PIPE_STYLE_MODES.remove(playerId);
            return;
        }

        PIPE_STYLE_MODES.put(playerId, mode);
    }

    /** Indica si el sentido de las bombas de la ruta esta invertido. */
    public static boolean isPumpDirectionReversed(UUID playerId) {
        return REVERSED_PUMP_PLAYERS.contains(playerId);
    }

    /** Guarda o elimina la inversion de las bombas de la ruta. */
    public static void setPumpDirectionReversed(UUID playerId, boolean reversed) {
        if (reversed) {
            REVERSED_PUMP_PLAYERS.add(playerId);
            return;
        }

        REVERSED_PUMP_PLAYERS.remove(playerId);
    }

    /** Devuelve la prioridad de ejes activa para el jugador. */
    public static RoutePriority getRoutePriority(UUID playerId) {
        return ROUTE_PRIORITIES.getOrDefault(playerId, RoutePriority.AUTO);
    }

    /** Guarda la prioridad de ejes omitiendo el modo automatico. */
    public static void setRoutePriority(UUID playerId, RoutePriority priority) {
        if (priority == null || priority == RoutePriority.AUTO) {
            ROUTE_PRIORITIES.remove(playerId);
            return;
        }

        ROUTE_PRIORITIES.put(playerId, priority);
    }

    /** Devuelve el punto inicial y el tipo de tuberia seleccionados. */
    public static Selection getSelection(UUID playerId) {
        return SELECTIONS.get(playerId);
    }

    /** Inicia una seleccion y reinicia sus modificadores de ruta. */
    public static void setSelection(UUID playerId, Selection selection) {
        SELECTIONS.put(playerId, selection);
        ANCHORS.remove(playerId);
        MANUAL_PUMPS.remove(playerId);
        COPPER_CASINGS.remove(playerId);
    }

    /** Elimina la seleccion y todos los modificadores vinculados. */
    public static void clearSelection(UUID playerId) {
        SELECTIONS.remove(playerId);
        ANCHORS.remove(playerId);
        MANUAL_PUMPS.remove(playerId);
        COPPER_CASINGS.remove(playerId);
    }

    /** Devuelve una copia inmutable de las anclas del jugador. */
    public static List<PlacementTarget> getAnchors(UUID playerId) {
        return getValues(ANCHORS, playerId);
    }

    /** Anade un ancla o reemplaza la ultima si ocupa el mismo bloque. */
    public static void addAnchor(UUID playerId, PlacementTarget anchor) {
        List<PlacementTarget> anchors = new ArrayList<>(getValues(ANCHORS, playerId));
        if (!anchors.isEmpty() && anchors.get(anchors.size() - 1).position().equals(anchor.position())) {
            anchors.set(anchors.size() - 1, anchor);
        } else {
            anchors.add(anchor);
        }
        storeValues(ANCHORS, playerId, anchors);
    }

    /** Elimina el ancla y las marcas manuales situadas en el bloque indicado. */
    public static void removeAnchor(UUID playerId, BlockPos position) {
        List<PlacementTarget> anchors = new ArrayList<>(getValues(ANCHORS, playerId));
        anchors.removeIf(anchor -> anchor.position().equals(position));
        storeValues(ANCHORS, playerId, anchors);
        removeValue(MANUAL_PUMPS, playerId, position);
        removeValue(COPPER_CASINGS, playerId, position);
    }

    /** Devuelve una copia inmutable de las bombas manuales. */
    public static List<BlockPos> getManualPumps(UUID playerId) {
        return getValues(MANUAL_PUMPS, playerId);
    }

    /** Anade o retira una marca manual de bomba. */
    public static void toggleManualPump(UUID playerId, BlockPos position) {
        toggleValue(MANUAL_PUMPS, playerId, position);
    }

    /** Elimina la ultima marca manual de bomba. */
    public static void removeLastManualPump(UUID playerId) {
        removeLastValue(MANUAL_PUMPS, playerId);
    }

    /** Devuelve una copia inmutable de los revestimientos manuales. */
    public static List<BlockPos> getCopperCasings(UUID playerId) {
        return getValues(COPPER_CASINGS, playerId);
    }

    /** Anade o retira una marca manual de revestimiento. */
    public static void toggleCopperCasing(UUID playerId, BlockPos position) {
        toggleValue(COPPER_CASINGS, playerId, position);
    }

    /** Elimina la ultima marca manual de revestimiento. */
    public static void removeLastCopperCasing(UUID playerId) {
        removeLastValue(COPPER_CASINGS, playerId);
    }

    /** Elimina todo el estado temporal conservado para un jugador desconectado. */
    public static void clearPlayer(UUID playerId) {
        clearSelection(playerId);
        ROUTE_PRIORITIES.remove(playerId);
        PUMP_MODES.remove(playerId);
        COPPER_CASING_MODES.remove(playerId);
        PIPE_STYLE_MODES.remove(playerId);
        CONNECTOR_MODE_PLAYERS.remove(playerId);
        REVERSED_PUMP_PLAYERS.remove(playerId);
    }

    /** Devuelve una copia inmutable de los valores asociados al jugador. */
    private static <T> List<T> getValues(Map<UUID, List<T>> valuesByPlayer, UUID playerId) {
        return List.copyOf(valuesByPlayer.getOrDefault(playerId, List.of()));
    }

    /** Anade o retira un valor y normaliza su almacenamiento. */
    private static <T> void toggleValue(Map<UUID, List<T>> valuesByPlayer, UUID playerId, T value) {
        List<T> values = new ArrayList<>(getValues(valuesByPlayer, playerId));
        if (!values.remove(value)) {
            values.add(value);
        }
        storeValues(valuesByPlayer, playerId, values);
    }

    /** Retira un valor concreto y normaliza su almacenamiento. */
    private static <T> void removeValue(Map<UUID, List<T>> valuesByPlayer, UUID playerId, T value) {
        List<T> values = new ArrayList<>(getValues(valuesByPlayer, playerId));
        values.remove(value);
        storeValues(valuesByPlayer, playerId, values);
    }

    /** Retira el ultimo valor cuando existe. */
    private static <T> void removeLastValue(Map<UUID, List<T>> valuesByPlayer, UUID playerId) {
        List<T> values = new ArrayList<>(getValues(valuesByPlayer, playerId));
        if (values.isEmpty()) {
            return;
        }
        values.remove(values.size() - 1);
        storeValues(valuesByPlayer, playerId, values);
    }

    /** Guarda una lista inmutable o elimina su entrada cuando queda vacia. */
    private static <T> void storeValues(Map<UUID, List<T>> valuesByPlayer, UUID playerId, List<T> values) {
        if (values.isEmpty()) {
            valuesByPlayer.remove(playerId);
        } else {
            valuesByPlayer.put(playerId, List.copyOf(values));
        }
    }
}
