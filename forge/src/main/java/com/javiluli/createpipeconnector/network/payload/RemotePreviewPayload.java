package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.state.ClientRemotePreviewState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Server-to-client relay identifying the player who owns a shared preview.
 */
public record RemotePreviewPayload(
        UUID ownerId,
        List<PreviewPipe> previewPipes,
        List<PlacementTarget> anchors
) {
    public RemotePreviewPayload {
        previewPipes = List.copyOf(previewPipes);
        anchors = List.copyOf(anchors);
        if (previewPipes.size() > Constants.MAX_SHARED_PREVIEW_BLOCKS) {
            throw new IllegalArgumentException("Too many shared preview blocks");
        }
        if (anchors.size() > Constants.MAX_SHARED_PREVIEW_ANCHORS) {
            throw new IllegalArgumentException("Too many shared preview anchors");
        }
    }

    public static void encode(RemotePreviewPayload payload, FriendlyByteBuf buffer) {
        buffer.writeUUID(payload.ownerId());
        SharedPreviewCodec.writePreviewPipes(buffer, payload.previewPipes());
        SharedPreviewCodec.writeAnchors(buffer, payload.anchors());
    }

    public static RemotePreviewPayload decode(FriendlyByteBuf buffer) {
        return new RemotePreviewPayload(
                buffer.readUUID(),
                SharedPreviewCodec.readPreviewPipes(buffer),
                SharedPreviewCodec.readAnchors(buffer)
        );
    }

    public static void handle(RemotePreviewPayload payload, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientRemotePreviewState.apply(payload)
        ));
        context.setPacketHandled(true);
    }
}
