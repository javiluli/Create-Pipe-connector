package com.javiluli.createpipeconnector.client.state;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.network.payload.RemotePreviewPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client-only collection of previews currently shared by other players.
 */
public final class ClientRemotePreviewState {
    private static final Map<UUID, RemotePreview> PREVIEWS = new LinkedHashMap<>();
    private static Level currentLevel;

    private ClientRemotePreviewState() {
    }

    /** Applies one validated server relay to the current client world. */
    public static void apply(RemotePreviewPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = minecraft.level;
        if (level == null || minecraft.player == null || payload.ownerId().equals(minecraft.player.getUUID())) {
            return;
        }

        ensureLevel(level);
        if (payload.previewPipes().isEmpty() && payload.anchors().isEmpty()) {
            PREVIEWS.remove(payload.ownerId());
            return;
        }

        RemotePreview previous = PREVIEWS.get(payload.ownerId());
        int visualVersion = previous == null
                ? 1
                : previous.sameContent(payload.previewPipes(), payload.anchors())
                        ? previous.visualVersion()
                        : previous.visualVersion() + 1;
        PREVIEWS.put(payload.ownerId(), new RemotePreview(
                payload.ownerId(),
                visualVersion,
                payload.previewPipes(),
                payload.anchors(),
                level.getGameTime()
        ));
    }

    /** Returns active remote previews after removing timed-out snapshots. */
    public static Collection<RemotePreview> getActive(Level level) {
        ensureLevel(level);
        long minimumUpdateTime = level.getGameTime() - Constants.SHARED_PREVIEW_TIMEOUT_TICKS;
        PREVIEWS.values().removeIf(preview -> preview.lastUpdateTime() < minimumUpdateTime);
        return List.copyOf(PREVIEWS.values());
    }

    /** Clears all remote state when the client leaves or changes worlds. */
    public static void clear() {
        PREVIEWS.clear();
        currentLevel = null;
    }

    private static void ensureLevel(Level level) {
        if (currentLevel == level) {
            return;
        }
        PREVIEWS.clear();
        currentLevel = level;
    }

    /**
     * Immutable render scene received from one remote player.
     */
    public record RemotePreview(
            UUID ownerId,
            int visualVersion,
            List<PreviewPipe> previewPipes,
            List<PlacementTarget> anchors,
            long lastUpdateTime
    ) {
        public RemotePreview {
            previewPipes = List.copyOf(previewPipes);
            anchors = List.copyOf(anchors);
        }

        private boolean sameContent(List<PreviewPipe> newPreviewPipes, List<PlacementTarget> newAnchors) {
            return previewPipes.equals(newPreviewPipes) && anchors.equals(newAnchors);
        }
    }
}
