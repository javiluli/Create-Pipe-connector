package com.javiluli.createpipeconnector.client.state;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.CopperCasingMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeStyleMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PumpMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.RoutePriority;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores the local connector session and invalidates render caches on change.
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

    private ClientPipeConnectorState() {
    }

    public static boolean isConnectorModeEnabled() {
        return connectorModeEnabled;
    }

    public static void setConnectorModeEnabled(boolean enabled) {
        connectorModeEnabled = enabled;
        if (!enabled) {
            clearSelection();
        }
    }

    public static boolean isAutoPumpsEnabled() {
        return pumpMode.isAutomatic();
    }

    public static void setAutoPumpsEnabled(boolean enabled) {
        setPumpMode(enabled ? PumpMode.EFFICIENT : PumpMode.OFF);
    }

    public static PumpMode getPumpMode() {
        return pumpMode;
    }

    public static void setPumpMode(PumpMode mode) {
        pumpMode = mode == null ? PumpMode.OFF : mode;
        setPreviewPipes(List.of());
    }

    public static CopperCasingMode getCopperCasingMode() {
        return copperCasingMode;
    }

    public static void setCopperCasingMode(CopperCasingMode mode) {
        copperCasingMode = mode == null ? CopperCasingMode.MANUAL : mode;
        setPreviewPipes(List.of());
    }

    public static PipeStyleMode getPipeStyleMode() {
        return pipeStyleMode;
    }

    public static void setPipeStyleMode(PipeStyleMode mode) {
        pipeStyleMode = mode == null ? PipeStyleMode.DEFAULT : mode;
        setPreviewPipes(List.of());
    }

    public static boolean isAutoPumpDirectionReversed() {
        return autoPumpDirectionReversed;
    }

    public static void setAutoPumpDirectionReversed(boolean reversed) {
        autoPumpDirectionReversed = reversed;
        setPreviewPipes(List.of());
    }

    public static RoutePriority getRoutePriority() {
        return routePriority;
    }

    public static void setRoutePriority(RoutePriority priority) {
        routePriority = priority == null ? RoutePriority.AUTO : priority;
    }

    public static Selection getSelection() {
        return selection;
    }

    public static void setSelection(Selection newSelection) {
        selection = newSelection;
        anchors = List.of();
        manualPumps = List.of();
        copperCasings = List.of();
        materialStatus = null;
        setPreviewPipes(List.of());
    }

    public static void clearSelection() {
        selection = null;
        anchors = List.of();
        manualPumps = List.of();
        copperCasings = List.of();
        materialStatus = null;
        setPreviewPipes(List.of());
    }

    public static List<PlacementTarget> getAnchors() {
        return anchors;
    }

    public static void addAnchor(PlacementTarget anchor) {
        List<PlacementTarget> updatedAnchors = new ArrayList<>(anchors);
        if (!updatedAnchors.isEmpty() && updatedAnchors.get(updatedAnchors.size() - 1).position().equals(anchor.position())) {
            updatedAnchors.set(updatedAnchors.size() - 1, anchor);
        } else {
            updatedAnchors.add(anchor);
        }
        anchors = List.copyOf(updatedAnchors);
    }

    public static boolean removeLastAnchor() {
        if (anchors.isEmpty()) {
            return false;
        }

        List<PlacementTarget> updatedAnchors = new ArrayList<>(anchors);
        updatedAnchors.remove(updatedAnchors.size() - 1);
        anchors = updatedAnchors.isEmpty() ? List.of() : List.copyOf(updatedAnchors);
        return true;
    }

    public static List<BlockPos> getManualPumps() {
        return manualPumps;
    }

    public static void toggleManualPump(BlockPos position) {
        List<BlockPos> updatedManualPumps = new ArrayList<>(manualPumps);
        if (updatedManualPumps.remove(position)) {
            manualPumps = updatedManualPumps.isEmpty() ? List.of() : List.copyOf(updatedManualPumps);
            return;
        }

        updatedManualPumps.add(position);
        manualPumps = List.copyOf(updatedManualPumps);
    }

    public static boolean removeLastManualPump() {
        if (manualPumps.isEmpty()) {
            return false;
        }

        List<BlockPos> updatedManualPumps = new ArrayList<>(manualPumps);
        updatedManualPumps.remove(updatedManualPumps.size() - 1);
        manualPumps = updatedManualPumps.isEmpty() ? List.of() : List.copyOf(updatedManualPumps);
        return true;
    }

    public static List<BlockPos> getCopperCasings() {
        return copperCasings;
    }

    public static void toggleCopperCasing(BlockPos position) {
        List<BlockPos> updatedCopperCasings = new ArrayList<>(copperCasings);
        if (updatedCopperCasings.remove(position)) {
            copperCasings = updatedCopperCasings.isEmpty() ? List.of() : List.copyOf(updatedCopperCasings);
            return;
        }

        updatedCopperCasings.add(position);
        copperCasings = List.copyOf(updatedCopperCasings);
    }

    public static boolean removeLastCopperCasing() {
        if (copperCasings.isEmpty()) {
            return false;
        }

        List<BlockPos> updatedCopperCasings = new ArrayList<>(copperCasings);
        updatedCopperCasings.remove(updatedCopperCasings.size() - 1);
        copperCasings = updatedCopperCasings.isEmpty() ? List.of() : List.copyOf(updatedCopperCasings);
        return true;
    }

    public static List<PreviewPipe> getPreviewPipes() {
        return previewPipes;
    }

    public static int getPreviewVersion() {
        return previewVersion;
    }

    public static void setPreviewPipes(List<PreviewPipe> newPreviewPipes) {
        List<PreviewPipe> copiedPreviewPipes = newPreviewPipes == null ? List.of() : List.copyOf(newPreviewPipes);
        if (previewPipes.equals(copiedPreviewPipes)) {
            return;
        }

        previewPipes = copiedPreviewPipes;
        previewVersion++;
    }

    public static MaterialStatus getMaterialStatus() {
        return materialStatus;
    }

    public static void setMaterialStatus(MaterialStatus newMaterialStatus) {
        materialStatus = newMaterialStatus;
    }

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
