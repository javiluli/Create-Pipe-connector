package com.javiluli.createpipeconnector.client.render.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

/**
 * Batches pixel-aligned GUI geometry used by the radial selector.
 */
public final class GuiPixelBatch implements AutoCloseable {
    private final Matrix4f matrix;
    private BufferBuilder bufferBuilder;

    public GuiPixelBatch(GuiGraphics guiGraphics) {
        this.matrix = guiGraphics.pose().last().pose();
    }

    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        if (bufferBuilder == null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }

        bufferBuilder.addVertex(matrix, minX, minY, 0.0F).setColor(color);
        bufferBuilder.addVertex(matrix, minX, maxY, 0.0F).setColor(color);
        bufferBuilder.addVertex(matrix, maxX, maxY, 0.0F).setColor(color);
        bufferBuilder.addVertex(matrix, maxX, minY, 0.0F).setColor(color);
    }

    @Override
    public void close() {
        if (bufferBuilder == null) {
            return;
        }

        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());
        bufferBuilder = null;
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
