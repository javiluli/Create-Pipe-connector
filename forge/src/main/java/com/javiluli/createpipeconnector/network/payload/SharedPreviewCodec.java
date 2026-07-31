package com.javiluli.createpipeconnector.network.payload;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact codecs shared by the client snapshot and server relay payloads.
 */
final class SharedPreviewCodec {
    private SharedPreviewCodec() {
    }

    static void writePreviewPipes(FriendlyByteBuf buffer, List<PreviewPipe> previewPipes) {
        buffer.writeVarInt(previewPipes.size());
        for (PreviewPipe previewPipe : previewPipes) {
            buffer.writeBlockPos(previewPipe.position());
            buffer.writeVarInt(Block.getId(previewPipe.state()));
            Direction pumpFacing = previewPipe.mechanicalPumpFacing();
            buffer.writeByte(pumpFacing == null ? -1 : pumpFacing.get3DDataValue());
            buffer.writeBoolean(previewPipe.missingMaterial());
        }
    }

    static List<PreviewPipe> readPreviewPipes(FriendlyByteBuf buffer) {
        int size = readBoundedSize(buffer, Constants.MAX_SHARED_PREVIEW_BLOCKS, "preview blocks");
        List<PreviewPipe> previewPipes = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            BlockPos position = buffer.readBlockPos();
            BlockState state = Block.stateById(buffer.readVarInt());
            int pumpFacingId = buffer.readByte();
            Direction pumpFacing = pumpFacingId < 0 ? null : Direction.from3DDataValue(pumpFacingId);
            boolean missingMaterial = buffer.readBoolean();
            previewPipes.add(new PreviewPipe(position, state, pumpFacing, missingMaterial));
        }
        return List.copyOf(previewPipes);
    }

    static void writeAnchors(FriendlyByteBuf buffer, List<PlacementTarget> anchors) {
        buffer.writeVarInt(anchors.size());
        for (PlacementTarget anchor : anchors) {
            buffer.writeBlockPos(anchor.position());
            buffer.writeEnum(anchor.face());
            buffer.writeBoolean(anchor.existingPipe());
        }
    }

    static List<PlacementTarget> readAnchors(FriendlyByteBuf buffer) {
        int size = readBoundedSize(buffer, Constants.MAX_SHARED_PREVIEW_ANCHORS, "preview anchors");
        List<PlacementTarget> anchors = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            anchors.add(new PlacementTarget(
                    buffer.readBlockPos(),
                    buffer.readEnum(Direction.class),
                    buffer.readBoolean()
            ));
        }
        return List.copyOf(anchors);
    }

    private static int readBoundedSize(FriendlyByteBuf buffer, int maximum, String valueName) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximum) {
            throw new DecoderException("Invalid " + valueName + " count: " + size);
        }
        return size;
    }
}
