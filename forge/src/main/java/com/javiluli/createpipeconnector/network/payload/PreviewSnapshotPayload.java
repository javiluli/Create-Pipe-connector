package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;

/**
 * Client-to-server snapshot of the sender's current visual route preview.
 *
 * <p>The payload is visual only. The server still calculates and validates the
 * authoritative placement independently when the route is confirmed.</p>
 */
public record PreviewSnapshotPayload(
        List<PreviewPipe> previewPipes,
        List<PlacementTarget> anchors
) {
    public PreviewSnapshotPayload {
        previewPipes = List.copyOf(previewPipes);
        anchors = List.copyOf(anchors);
        if (previewPipes.size() > Constants.MAX_SHARED_PREVIEW_BLOCKS) {
            throw new IllegalArgumentException("Too many shared preview blocks");
        }
        if (anchors.size() > Constants.MAX_SHARED_PREVIEW_ANCHORS) {
            throw new IllegalArgumentException("Too many shared preview anchors");
        }
    }

    public boolean isEmpty() {
        return previewPipes.isEmpty() && anchors.isEmpty();
    }

    public static void encode(PreviewSnapshotPayload payload, FriendlyByteBuf buffer) {
        SharedPreviewCodec.writePreviewPipes(buffer, payload.previewPipes());
        SharedPreviewCodec.writeAnchors(buffer, payload.anchors());
    }

    public static PreviewSnapshotPayload decode(FriendlyByteBuf buffer) {
        return new PreviewSnapshotPayload(
                SharedPreviewCodec.readPreviewPipes(buffer),
                SharedPreviewCodec.readAnchors(buffer)
        );
    }
}
