package com.javiluli.createpipeconnector.client.render.overlay;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PlacementTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;

import java.util.List;

public final class AnchorOverlayRenderer {
    private static final float FILL_RED = 1.0F;
    private static final float FILL_GREEN = 0.85F;
    private static final float FILL_BLUE = 0.05F;
    private static final float FILL_ALPHA = 0.18F;
    private static final float LINE_RED = 1.0F;
    private static final float LINE_GREEN = 0.95F;
    private static final float LINE_BLUE = 0.10F;
    private static final float LINE_ALPHA = 0.95F;
    private static final double BOX_INFLATE = 0.003D;

    private AnchorOverlayRenderer() {
    }

    public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, List<PlacementTarget> anchors) {
        if (anchors.isEmpty()) {
            return;
        }

        RenderSystem.disableCull();
        try {
            renderAnchorFaces(poseStack, bufferSource.getBuffer(RenderType.debugFilledBox()), anchors);
            bufferSource.endBatch(RenderType.debugFilledBox());
        } finally {
            RenderSystem.enableCull();
        }

        renderAnchorOutlines(poseStack, bufferSource.getBuffer(RenderType.lines()), anchors);
        bufferSource.endBatch(RenderType.lines());
    }

    private static void renderAnchorFaces(PoseStack poseStack, VertexConsumer vertexConsumer, List<PlacementTarget> anchors) {
        for (PlacementTarget anchor : anchors) {
            BlockPos position = anchor.position();
            LevelRenderer.addChainedFilledBoxVertices(
                    poseStack,
                    vertexConsumer,
                    position.getX() - BOX_INFLATE,
                    position.getY() - BOX_INFLATE,
                    position.getZ() - BOX_INFLATE,
                    position.getX() + 1.0D + BOX_INFLATE,
                    position.getY() + 1.0D + BOX_INFLATE,
                    position.getZ() + 1.0D + BOX_INFLATE,
                    FILL_RED,
                    FILL_GREEN,
                    FILL_BLUE,
                    FILL_ALPHA
            );
        }
    }

    private static void renderAnchorOutlines(PoseStack poseStack, VertexConsumer vertexConsumer, List<PlacementTarget> anchors) {
        for (PlacementTarget anchor : anchors) {
            BlockPos position = anchor.position();
            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexConsumer,
                    position.getX() - BOX_INFLATE,
                    position.getY() - BOX_INFLATE,
                    position.getZ() - BOX_INFLATE,
                    position.getX() + 1.0D + BOX_INFLATE,
                    position.getY() + 1.0D + BOX_INFLATE,
                    position.getZ() + 1.0D + BOX_INFLATE,
                    LINE_RED,
                    LINE_GREEN,
                    LINE_BLUE,
                    LINE_ALPHA
            );
        }
    }
}
