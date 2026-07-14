package com.javiluli.createpipeconnector.client.state;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;

import java.util.List;

public final class ClientPipeConnectorState {
    private static Selection selection;
    private static List<PreviewPipe> previewPipes = List.of();

    private ClientPipeConnectorState() {
    }

    public static Selection getSelection() {
        return selection;
    }

    public static void setSelection(Selection newSelection) {
        selection = newSelection;
        previewPipes = List.of();
    }

    public static void clearSelection() {
        selection = null;
        previewPipes = List.of();
    }

    public static List<PreviewPipe> getPreviewPipes() {
        return previewPipes;
    }

    public static void setPreviewPipes(List<PreviewPipe> newPreviewPipes) {
        previewPipes = newPreviewPipes == null ? List.of() : List.copyOf(newPreviewPipes);
    }
}
