package com.javiluli.createpipeconnector.feature.connector.client;

import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import com.javiluli.createpipeconnector.feature.connector.model.PlacementTarget;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Conserva la sesion local e invalida caches visuales al cambiar.
 */
public final class ClientPipeConnectorState {
    private static boolean connectorModeEnabled;
    private static boolean autoPumpDirectionReversed;
    private static PumpMode pumpMode = PumpMode.OFF;
    private static CopperCasingMode copperCasingMode = CopperCasingMode.MANUAL;
    private static PipeStyleMode pipeStyleMode = PipeStyleMode.DEFAULT;
    private static RoutePriority routePriority = RoutePriority.AUTO;
    private static Selection selection;
    private static List<PlacementTarget> anchors = List.of();
    private static List<BlockPos> manualPumps = List.of();
    private static List<BlockPos> copperCasings = List.of();
    private static List<PreviewPipe> previewPipes = List.of();
    private static MaterialStatus materialStatus;
    private static int previewVersion;

    /** Impide crear instancias del estado global del cliente. */
    private ClientPipeConnectorState() {
    }

    /** Indica si el modo conector esta activo localmente. */
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

    /** Indica si el modo local coloca bombas automaticamente. */
    public static boolean isAutoPumpsEnabled() {
        return pumpMode.isAutomatic();
    }

    /** Traduce el ajuste booleano heredado al modo de bombas. */
    public static void setAutoPumpsEnabled(boolean enabled) {
        setPumpMode(enabled ? PumpMode.EFFICIENT : PumpMode.OFF);
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
        copperCasingMode = mode == null ? CopperCasingMode.MANUAL : mode;
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

    /** Indica si las bombas automaticas estan invertidas localmente. */
    public static boolean isAutoPumpDirectionReversed() {
        return autoPumpDirectionReversed;
    }

    /** Cambia la inversion local e invalida el preview. */
    public static void setAutoPumpDirectionReversed(boolean reversed) {
        autoPumpDirectionReversed = reversed;
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
        materialStatus = null;
        setPreviewPipes(List.of());
    }

    /** Elimina la seleccion y todo el estado temporal de la ruta. */
    public static void clearSelection() {
        selection = null;
        anchors = List.of();
        manualPumps = List.of();
        copperCasings = List.of();
        materialStatus = null;
        setPreviewPipes(List.of());
    }

    /** Devuelve las anclas locales de la ruta activa. */
    public static List<PlacementTarget> getAnchors() {
        return anchors;
    }

    /** Anade o reemplaza la ultima ancla local. */
    public static void addAnchor(PlacementTarget anchor) {
        List<PlacementTarget> updatedAnchors = new ArrayList<>(anchors);
        if (!updatedAnchors.isEmpty() && updatedAnchors.get(updatedAnchors.size() - 1).position().equals(anchor.position())) {
            updatedAnchors.set(updatedAnchors.size() - 1, anchor);
        } else {
            updatedAnchors.add(anchor);
        }
        anchors = List.copyOf(updatedAnchors);
    }

    /** Elimina la ultima ancla local si existe. */
    public static boolean removeLastAnchor() {
        if (anchors.isEmpty()) {
            return false;
        }

        List<PlacementTarget> updatedAnchors = new ArrayList<>(anchors);
        updatedAnchors.remove(updatedAnchors.size() - 1);
        anchors = updatedAnchors.isEmpty() ? List.of() : List.copyOf(updatedAnchors);
        return true;
    }

    /** Devuelve las bombas manuales locales. */
    public static List<BlockPos> getManualPumps() {
        return manualPumps;
    }

    /** Anade o retira una bomba manual local. */
    public static void toggleManualPump(BlockPos position) {
        manualPumps = toggleValue(manualPumps, position);
    }

    /** Elimina la ultima bomba manual local si existe. */
    public static boolean removeLastManualPump() {
        if (manualPumps.isEmpty()) {
            return false;
        }

        manualPumps = removeLastValue(manualPumps);
        return true;
    }

    /** Devuelve los revestimientos manuales locales. */
    public static List<BlockPos> getCopperCasings() {
        return copperCasings;
    }

    /** Anade o retira un revestimiento manual local. */
    public static void toggleCopperCasing(BlockPos position) {
        copperCasings = toggleValue(copperCasings, position);
    }

    /** Elimina el ultimo revestimiento manual local si existe. */
    public static boolean removeLastCopperCasing() {
        if (copperCasings.isEmpty()) {
            return false;
        }

        copperCasings = removeLastValue(copperCasings);
        return true;
    }

    /** Devuelve una copia inmutable con el valor anadido o retirado. */
    private static <T> List<T> toggleValue(List<T> values, T value) {
        List<T> updatedValues = new ArrayList<>(values);
        if (!updatedValues.remove(value)) {
            updatedValues.add(value);
        }
        return updatedValues.isEmpty() ? List.of() : List.copyOf(updatedValues);
    }

    /** Devuelve una copia inmutable sin el ultimo valor. */
    private static <T> List<T> removeLastValue(List<T> values) {
        List<T> updatedValues = new ArrayList<>(values);
        updatedValues.remove(updatedValues.size() - 1);
        return updatedValues.isEmpty() ? List.of() : List.copyOf(updatedValues);
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
        if (previewPipes.equals(copiedPreviewPipes)) {
            return;
        }

        previewPipes = copiedPreviewPipes;
        previewVersion++;
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
            int availablePipes,
            int requiredPumps,
            int availablePumps,
            int requiredCopperCasings,
            int availableCopperCasings,
            boolean creative
    ) {
    }
}
