package com.javiluli.createpipeconnector.client.network;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.client.state.ClientRemotePreviewState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.network.CreatePipeConnectorNetwork;
import com.javiluli.createpipeconnector.network.payload.PreviewSnapshotPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * Publishes changed local previews at a bounded rate and keeps static previews
 * alive while another client is observing them.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ClientPreviewSyncManager {
    private static int lastPreviewVersion = -1;
    private static List<PlacementTarget> lastAnchors = List.of();
    private static long lastSendTime = Long.MIN_VALUE;
    private static boolean lastSnapshotEmpty = true;
    private static Level currentLevel;

    private ClientPreviewSyncManager() {
    }

    /**
     * Publishes changed previews after the configured rate limit and refreshes
     * unchanged previews periodically so remote clients can expire stale data.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            reset();
            ClientRemotePreviewState.clear();
            return;
        }
        if (currentLevel != minecraft.level) {
            reset();
            currentLevel = minecraft.level;
        }

        List<PreviewPipe> previewPipes = ClientPipeConnectorState.getSelection() == null
                ? List.of()
                : ClientPipeConnectorState.getPreviewPipes();
        List<PlacementTarget> anchors = ClientPipeConnectorState.getSelection() == null
                ? List.of()
                : ClientPipeConnectorState.getAnchors();
        if (previewPipes.size() > Constants.MAX_SHARED_PREVIEW_BLOCKS
                || anchors.size() > Constants.MAX_SHARED_PREVIEW_ANCHORS) {
            previewPipes = List.of();
            anchors = List.of();
        }
        boolean snapshotEmpty = previewPipes.isEmpty() && anchors.isEmpty();
        long gameTime = minecraft.level.getGameTime();
        boolean changed = ClientPipeConnectorState.getPreviewVersion() != lastPreviewVersion
                || !anchors.equals(lastAnchors)
                || snapshotEmpty != lastSnapshotEmpty;
        boolean neverSent = lastSendTime == Long.MIN_VALUE;
        boolean heartbeatDue = !snapshotEmpty
                && (neverSent || gameTime - lastSendTime >= Constants.SHARED_PREVIEW_HEARTBEAT_TICKS);
        boolean intervalElapsed = neverSent
                || gameTime - lastSendTime >= Constants.SHARED_PREVIEW_SYNC_INTERVAL_TICKS;

        if ((changed && (snapshotEmpty || intervalElapsed)) || heartbeatDue) {
            sendSnapshot(previewPipes, anchors, gameTime);
        }
    }

    private static void sendSnapshot(List<PreviewPipe> previewPipes, List<PlacementTarget> anchors, long gameTime) {
        CreatePipeConnectorNetwork.sendToServer(new PreviewSnapshotPayload(previewPipes, anchors));
        lastPreviewVersion = ClientPipeConnectorState.getPreviewVersion();
        lastAnchors = List.copyOf(anchors);
        lastSnapshotEmpty = previewPipes.isEmpty() && anchors.isEmpty();
        lastSendTime = gameTime;
    }

    private static void reset() {
        lastPreviewVersion = -1;
        lastAnchors = List.of();
        lastSendTime = Long.MIN_VALUE;
        lastSnapshotEmpty = true;
        currentLevel = null;
    }
}
