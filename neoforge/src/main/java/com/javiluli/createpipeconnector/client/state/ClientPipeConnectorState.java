package com.javiluli.createpipeconnector.client.state;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;

import java.util.ArrayList;
import java.util.List;

public final class ClientPipeConnectorState {
    private static Selection selection;
    private static List<PlacementTarget> anchors = List.of();
    private static List<PreviewPipe> previewPipes = List.of();
    private static int previewVersion;

    private ClientPipeConnectorState() {
    }

    public static Selection getSelection() {
        return selection;
    }

    public static void setSelection(Selection newSelection) {
        selection = newSelection;
        anchors = List.of();
        setPreviewPipes(List.of());
    }

    public static void clearSelection() {
        selection = null;
        anchors = List.of();
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
}
