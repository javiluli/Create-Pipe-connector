package com.javiluli.createpipeconnector.feature.connector.client;

import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.material.PipeInventory.MaterialAvailability;
import com.javiluli.createpipeconnector.feature.manual.ManualAction;
import com.javiluli.createpipeconnector.feature.manual.ManualMarkerChange;
import com.javiluli.createpipeconnector.feature.manual.ManualRouteAction;
import com.javiluli.createpipeconnector.feature.manual.ManualRouteChange;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Refleja en el cliente la sesion activa y su vista previa mas reciente.
 *
 * <p>El servidor conserva la autoridad sobre la colocacion; este estado mantiene
 * la entrada y el renderizado inmediatos para el jugador local.</p>
 */
public final class ClientPipeConnectorState {
    private static boolean connectorModeEnabled;
    private static boolean pumpDirectionReversed;
    private static PumpMode pumpMode = PumpMode.OFF;
    private static CopperCasingMode copperCasingMode = CopperCasingMode.NONE;
    private static PipeStyleMode pipeStyleMode = PipeStyleMode.DEFAULT;
    private static RoutePriority routePriority = RoutePriority.AUTO;
    private static ManualAction manualAction = ManualAction.ANCHOR;
    private static Selection selection;
    private static List<PlacementTarget> anchors = List.of();
    private static List<BlockPos> manualPumps = List.of();
    private static List<BlockPos> copperCasings = List.of();
    private static List<ManualRouteAction> manualRouteHistory = List.of();
    private static List<PreviewPipe> previewPipes = List.of();
    private static PlacementTarget previewTarget;
    private static MaterialStatus materialStatus;
    private static int previewVersion;

    /** Impide crear instancias del estado global del cliente. */
    private ClientPipeConnectorState() {
    }

    /** Indica si el modo Pipe Connector esta activo localmente. */
    public static boolean isConnectorModeEnabled() {
        return connectorModeEnabled;
    }

    /** Actualiza el modo local y limpia la seleccion al desactivarlo. */
    public static void setConnectorModeEnabled(boolean enabled) {
        connectorModeEnabled = enabled;
        if (!enabled) {
            clearSelection();
        }
    }

    /** Devuelve el modo local de bombas. */
    public static PumpMode getPumpMode() {
        return pumpMode;
    }

    /** Cambia el modo local de bombas e invalida el preview. */
    public static void setPumpMode(PumpMode mode) {
        pumpMode = mode == null ? PumpMode.OFF : mode;
        setPreviewPipes(List.of());
    }

    /** Devuelve el modo local de revestimiento. */
    public static CopperCasingMode getCopperCasingMode() {
        return copperCasingMode;
    }

    /** Cambia el modo local de revestimiento e invalida el preview. */
    public static void setCopperCasingMode(CopperCasingMode mode) {
        copperCasingMode = mode == null ? CopperCasingMode.NONE : mode;
        setPreviewPipes(List.of());
    }

    /** Devuelve el estilo local de tuberias. */
    public static PipeStyleMode getPipeStyleMode() {
        return pipeStyleMode;
    }

    /** Cambia el estilo local de tuberias e invalida el preview. */
    public static void setPipeStyleMode(PipeStyleMode mode) {
        pipeStyleMode = mode == null ? PipeStyleMode.DEFAULT : mode;
        setPreviewPipes(List.of());
    }

    /** Indica si las bombas de la ruta estan invertidas localmente. */
    public static boolean isPumpDirectionReversed() {
        return pumpDirectionReversed;
    }

    /** Cambia la inversion de las bombas e invalida el preview. */
    public static void setPumpDirectionReversed(boolean reversed) {
        pumpDirectionReversed = reversed;
        setPreviewPipes(List.of());
    }

    /** Devuelve la prioridad local de ruta. */
    public static RoutePriority getRoutePriority() {
        return routePriority;
    }

    /** Cambia la prioridad local de ruta. */
    public static void setRoutePriority(RoutePriority priority) {
        routePriority = priority == null ? RoutePriority.AUTO : priority;
    }

    /** Devuelve la accion puntual controlada por las teclas contextuales. */
    public static ManualAction getManualAction() {
        return manualAction;
    }

    /** Cambia la accion puntual sin alterar la ruta en curso. */
    public static void setManualAction(ManualAction action) {
        manualAction = action == null ? ManualAction.ANCHOR : action;
    }

    /** Devuelve la seleccion inicial local. */
    public static Selection getSelection() {
        return selection;
    }

    /** Guarda una seleccion y reinicia sus modificadores locales. */
    public static void setSelection(Selection newSelection) {
        selection = newSelection;
        anchors = List.of();
        manualPumps = List.of();
        copperCasings = List.of();
        manualRouteHistory = List.of();
        materialStatus = null;
        setPreviewPipes(List.of());
    }

    /** Elimina la seleccion y todo el estado temporal de la ruta. */
    public static void clearSelection() {
        selection = null;
        anchors = List.of();
        manualPumps = List.of();
        copperCasings = List.of();
        manualRouteHistory = List.of();
        materialStatus = null;
        setPreviewPipes(List.of());
    }

    /** Devuelve las anclas locales de la ruta activa. */
    public static List<PlacementTarget> getAnchors() {
        return anchors;
    }

    /** Anade o reemplaza la ultima ancla local. */
    public static void addAnchor(PlacementTarget anchor) {
        addAnchorGeometry(anchor);
        manualRouteHistory = appendValue(
                manualRouteHistory,
                new ManualRouteAction(ManualAction.ANCHOR, anchor.position(), true)
        );
    }

    /** Anade o reemplaza una ancla sin registrar una segunda accion manual. */
    private static void addAnchorGeometry(PlacementTarget anchor) {
        List<PlacementTarget> updatedAnchors = new ArrayList<>(anchors);
        if (!updatedAnchors.isEmpty() && updatedAnchors.get(updatedAnchors.size() - 1).position().equals(anchor.position())) {
            updatedAnchors.set(updatedAnchors.size() - 1, anchor);
        } else {
            updatedAnchors.add(anchor);
        }
        anchors = List.copyOf(updatedAnchors);
    }

    /** Deshace la ultima accion que haya modificado la ruta, sin importar su tipo. */
    public static ManualRouteChange undoLastManualRouteAction() {
        if (manualRouteHistory.isEmpty()) {
            return null;
        }

        ManualRouteAction routeAction = manualRouteHistory.get(manualRouteHistory.size() - 1);
        manualRouteHistory = removeLastValue(manualRouteHistory);
        removeMarker(routeAction);
        boolean anchorRemoved = releaseAnchor(routeAction);
        return new ManualRouteChange(routeAction.action(), routeAction.position(), anchorRemoved);
    }

    /** Indica si existe alguna accion manual que pueda deshacerse. */
    public static boolean hasManualRouteActions() {
        return !manualRouteHistory.isEmpty();
    }

    /** Devuelve las bombas manuales locales. */
    public static List<BlockPos> getManualPumps() {
        return manualPumps;
    }

    /** Anade o retira una bomba manual junto con su ancla de soporte. */
    public static ManualMarkerChange toggleManualPump(BlockPos position, PlacementTarget supportAnchor) {
        if (manualPumps.contains(position)) {
            manualPumps = removeValue(manualPumps, position);
            return removeManualRouteAction(ManualAction.MECHANICAL_PUMP, position);
        }

        manualPumps = addValue(manualPumps, position);
        boolean anchorChanged = addManualSupport(ManualAction.MECHANICAL_PUMP, position, supportAnchor);
        return new ManualMarkerChange(position, true, anchorChanged);
    }

    /** Elimina la ultima bomba manual y su ancla de soporte si fue creada por ella. */
    public static ManualMarkerChange removeLastManualPump() {
        if (manualPumps.isEmpty()) {
            return null;
        }

        BlockPos position = manualPumps.get(manualPumps.size() - 1);
        manualPumps = removeValue(manualPumps, position);
        return removeManualRouteAction(ManualAction.MECHANICAL_PUMP, position);
    }

    /** Devuelve los revestimientos manuales locales. */
    public static List<BlockPos> getCopperCasings() {
        return copperCasings;
    }

    /** Anade o retira un revestimiento manual junto con su ancla de soporte. */
    public static ManualMarkerChange toggleCopperCasing(BlockPos position, PlacementTarget supportAnchor) {
        if (copperCasings.contains(position)) {
            copperCasings = removeValue(copperCasings, position);
            return removeManualRouteAction(ManualAction.COPPER_CASING, position);
        }

        copperCasings = addValue(copperCasings, position);
        boolean anchorChanged = addManualSupport(ManualAction.COPPER_CASING, position, supportAnchor);
        return new ManualMarkerChange(position, true, anchorChanged);
    }

    /** Elimina el ultimo revestimiento manual y su ancla de soporte vinculada. */
    public static ManualMarkerChange removeLastCopperCasing() {
        if (copperCasings.isEmpty()) {
            return null;
        }

        BlockPos position = copperCasings.get(copperCasings.size() - 1);
        copperCasings = removeValue(copperCasings, position);
        return removeManualRouteAction(ManualAction.COPPER_CASING, position);
    }

    /** Registra una marca y crea su ancla solo cuando el bloque aun no tiene una. */
    private static boolean addManualSupport(
            ManualAction action,
            BlockPos position,
            PlacementTarget supportAnchor
    ) {
        boolean ownsAnchor = supportAnchor != null && !hasAnchorAt(position);
        if (ownsAnchor) {
            addAnchorGeometry(supportAnchor);
        }

        manualRouteHistory = appendValue(
                manualRouteHistory,
                new ManualRouteAction(action, position, ownsAnchor)
        );
        return ownsAnchor;
    }

    /** Retira del historial la ultima accion concreta y libera su ancla si procede. */
    private static ManualMarkerChange removeManualRouteAction(ManualAction action, BlockPos position) {
        int historyIndex = findLastManualAction(action, position);
        if (historyIndex < 0) {
            return new ManualMarkerChange(position, false, false);
        }

        ManualRouteAction routeAction = manualRouteHistory.get(historyIndex);
        manualRouteHistory = removeValueAt(manualRouteHistory, historyIndex);
        return new ManualMarkerChange(position, false, releaseAnchor(routeAction));
    }

    /** Busca desde el final una accion del tipo y bloque indicados. */
    private static int findLastManualAction(ManualAction action, BlockPos position) {
        for (int index = manualRouteHistory.size() - 1; index >= 0; index--) {
            ManualRouteAction routeAction = manualRouteHistory.get(index);
            if (routeAction.action() == action && routeAction.position().equals(position)) {
                return index;
            }
        }
        return -1;
    }

    /** Elimina el marcador asociado a una accion retirada del historial global. */
    private static void removeMarker(ManualRouteAction routeAction) {
        if (routeAction.action() == ManualAction.MECHANICAL_PUMP) {
            manualPumps = removeValue(manualPumps, routeAction.position());
        } else if (routeAction.action() == ManualAction.COPPER_CASING) {
            copperCasings = removeValue(copperCasings, routeAction.position());
        }
    }

    /** Libera el ancla propia o transfiere su propiedad a otra marca del bloque. */
    private static boolean releaseAnchor(ManualRouteAction routeAction) {
        if (!routeAction.ownsAnchor()) {
            return false;
        }

        int replacementIndex = findLastMarkerAt(routeAction.position());
        if (replacementIndex >= 0) {
            List<ManualRouteAction> updatedHistory = new ArrayList<>(manualRouteHistory);
            updatedHistory.set(replacementIndex, updatedHistory.get(replacementIndex).withAnchorOwnership());
            manualRouteHistory = List.copyOf(updatedHistory);
            return false;
        }

        removeAnchorGeometry(routeAction.position());
        return true;
    }

    /** Busca otra marca manual capaz de conservar el ancla compartida. */
    private static int findLastMarkerAt(BlockPos position) {
        for (int index = manualRouteHistory.size() - 1; index >= 0; index--) {
            ManualRouteAction routeAction = manualRouteHistory.get(index);
            if (routeAction.action() != ManualAction.ANCHOR && routeAction.position().equals(position)) {
                return index;
            }
        }
        return -1;
    }

    /** Comprueba si alguna ancla ocupa el bloque indicado. */
    private static boolean hasAnchorAt(BlockPos position) {
        return anchors.stream().anyMatch(anchor -> anchor.position().equals(position));
    }

    /** Elimina todas las anclas situadas en el bloque indicado. */
    private static void removeAnchorGeometry(BlockPos position) {
        List<PlacementTarget> updatedAnchors = new ArrayList<>(anchors);
        updatedAnchors.removeIf(anchor -> anchor.position().equals(position));
        anchors = immutableList(updatedAnchors);
    }

    /** Devuelve una copia inmutable con el valor anadido al final. */
    private static <T> List<T> addValue(List<T> values, T value) {
        List<T> updatedValues = new ArrayList<>(values);
        if (!updatedValues.contains(value)) {
            updatedValues.add(value);
        }
        return immutableList(updatedValues);
    }

    /** Devuelve una copia inmutable conservando cada entrada del historial. */
    private static <T> List<T> appendValue(List<T> values, T value) {
        List<T> updatedValues = new ArrayList<>(values);
        updatedValues.add(value);
        return List.copyOf(updatedValues);
    }

    /** Devuelve una copia inmutable sin el valor indicado. */
    private static <T> List<T> removeValue(List<T> values, T value) {
        List<T> updatedValues = new ArrayList<>(values);
        updatedValues.remove(value);
        return immutableList(updatedValues);
    }

    /** Devuelve una copia inmutable sin el ultimo valor. */
    private static <T> List<T> removeLastValue(List<T> values) {
        List<T> updatedValues = new ArrayList<>(values);
        updatedValues.remove(updatedValues.size() - 1);
        return immutableList(updatedValues);
    }

    /** Devuelve una copia inmutable sin el elemento del indice indicado. */
    private static <T> List<T> removeValueAt(List<T> values, int index) {
        List<T> updatedValues = new ArrayList<>(values);
        updatedValues.remove(index);
        return immutableList(updatedValues);
    }

    /** Normaliza una lista mutable antes de guardarla en el estado global. */
    private static <T> List<T> immutableList(List<T> values) {
        return values.isEmpty() ? List.of() : List.copyOf(values);
    }

    /** Devuelve las piezas de la vista previa actual. */
    public static List<PreviewPipe> getPreviewPipes() {
        return previewPipes;
    }

    /** Devuelve la version empleada para invalidar caches de renderizado. */
    public static int getPreviewVersion() {
        return previewVersion;
    }

    /** Sustituye el preview e incrementa su version solo si cambio. */
    public static void setPreviewPipes(List<PreviewPipe> newPreviewPipes) {
        List<PreviewPipe> copiedPreviewPipes = newPreviewPipes == null ? List.of() : List.copyOf(newPreviewPipes);
        if (copiedPreviewPipes.isEmpty()) {
            previewTarget = null;
        }
        if (previewPipes.equals(copiedPreviewPipes)) {
            return;
        }

        previewPipes = copiedPreviewPipes;
        // Las caches usan esta version para no comparar cada modelo en cada frame.
        previewVersion++;
    }

    /** Devuelve el objetivo exacto empleado para construir el preview visible. */
    public static PlacementTarget getPreviewTarget() {
        return previewTarget;
    }

    /** Asocia el preview visible con el objetivo que produjo su ruta. */
    public static void setPreviewTarget(PlacementTarget target) {
        previewTarget = target;
    }

    /** Devuelve el ultimo resumen de materiales calculado. */
    public static MaterialStatus getMaterialStatus() {
        return materialStatus;
    }

    /** Sustituye el resumen de materiales mostrado por el HUD. */
    public static void setMaterialStatus(MaterialStatus newMaterialStatus) {
        materialStatus = newMaterialStatus;
    }

    /**
     * Instantanea de inventario mostrada por el HUD para el plan actual.
     */
    public record MaterialStatus(
            Block pipeBlock,
            int requiredPipes,
            MaterialAvailability pipes,
            int requiredPumps,
            MaterialAvailability pumps,
            int requiredCopperCasings,
            MaterialAvailability copperCasings,
            boolean creative
    ) {
        /** Devuelve todas las tuberias accesibles desde ambas fuentes. */
        public int availablePipes() {
            return pipes.totalCount();
        }

        /** Devuelve todas las bombas accesibles desde ambas fuentes. */
        public int availablePumps() {
            return pumps.totalCount();
        }

        /** Devuelve todos los revestimientos accesibles desde ambas fuentes. */
        public int availableCopperCasings() {
            return copperCasings.totalCount();
        }
    }
}
