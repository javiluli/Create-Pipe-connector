package com.javiluli.createpipeconnector.feature.ui.client;

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
 * Agrupa rectangulos de interfaz alineados a pixeles en una sola llamada de dibujo.
 *
 * <p>El menu radial evita asi subir un bufer independiente por cada celda.</p>
 */
public final class GuiPixelBatch implements AutoCloseable {
    private final Matrix4f matrix;
    private BufferBuilder bufferBuilder;

    /** Prepara el lote con la transformacion activa de la interfaz. */
    public GuiPixelBatch(GuiGraphics guiGraphics) {
        this.matrix = guiGraphics.pose().last().pose();
    }

    /** Anade un rectangulo de color al lote actual. */
    public void fill(int minX, int minY, int maxX, int maxY, int color) {
        if (bufferBuilder == null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            bufferBuilder = Tesselator.getInstance().getBuilder();
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        }

        bufferBuilder.vertex(matrix, minX, minY, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix, minX, maxY, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix, maxX, maxY, 0.0F).color(color).endVertex();
        bufferBuilder.vertex(matrix, maxX, minY, 0.0F).color(color).endVertex();
    }

    /** Dibuja el lote pendiente y restaura el estado basico de renderizado. */
    @Override
    public void close() {
        if (bufferBuilder == null) {
            return;
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        bufferBuilder = null;
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
