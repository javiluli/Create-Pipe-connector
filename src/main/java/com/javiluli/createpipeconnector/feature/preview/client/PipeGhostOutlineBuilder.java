package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.List;

/**
 * Precalcula las cajas y colores de los contornos del preview.
 *
 * <p>Replica una sola vez el desplazamiento cromatico usado por Minecraft en
 * formas compuestas. El hilo de render recibe datos finales y evita calculos
 * de forma y color en cada frame.</p>
 */
final class PipeGhostOutlineBuilder {
    private static final OutlineColor DEFAULT_OUTLINE = new OutlineColor(0.15F, 0.85F, 1.00F);
    private static final OutlineColor MISSING_OUTLINE = new OutlineColor(1.00F, 0.25F, 0.20F);
    private static final OutlineColor PUMP_OUTLINE = new OutlineColor(1.00F, 0.82F, 0.18F);

    /** Impide crear instancias del constructor de contornos. */
    private PipeGhostOutlineBuilder() {
    }

    /** Construye los contornos inmutables de una seccion. */
    static List<PipeGhostGeometryCache.OutlinePiece> build(Level level, List<PreviewPipe> previewPipes) {
        List<PipeGhostGeometryCache.OutlinePiece> outlines = new ArrayList<>(previewPipes.size());
        for (PreviewPipe previewPipe : previewPipes) {
            PipeGhostGeometryCache.OutlinePiece outline = buildPiece(level, previewPipe);
            if (outline != null) {
                outlines.add(outline);
            }
        }
        return List.copyOf(outlines);
    }

    /** Convierte la forma de una pieza en cajas absolutas ya coloreadas. */
    private static PipeGhostGeometryCache.OutlinePiece buildPiece(Level level, PreviewPipe previewPipe) {
        BlockPos position = previewPipe.position();
        VoxelShape shape = previewPipe.state().getShape(level, position, CollisionContext.empty());
        List<AABB> localBoxes = shape.toAabbs();
        if (localBoxes.isEmpty()) {
            return null;
        }

        OutlineColor baseColor = outlineColor(previewPipe);
        List<PipeGhostGeometryCache.OutlineBox> boxes = new ArrayList<>(localBoxes.size());
        int boxCount = localBoxes.size();
        for (int index = 0; index < boxCount; index++) {
            OutlineColor color = index == 0
                    ? baseColor
                    : shiftHue(baseColor, (float) index / (float) boxCount);
            boxes.add(new PipeGhostGeometryCache.OutlineBox(
                    localBoxes.get(index).move(position),
                    color.red(),
                    color.green(),
                    color.blue()
            ));
        }
        return new PipeGhostGeometryCache.OutlinePiece(position, List.copyOf(boxes));
    }

    /** Selecciona el color segun tipo de pieza y disponibilidad. */
    private static OutlineColor outlineColor(PreviewPipe previewPipe) {
        if (previewPipe.missingMaterial()) {
            return MISSING_OUTLINE;
        }
        return previewPipe.isMechanicalPump() ? PUMP_OUTLINE : DEFAULT_OUTLINE;
    }

    /** Reproduce el desplazamiento cromatico de los contornos compuestos. */
    private static OutlineColor shiftHue(OutlineColor color, float hueShift) {
        float secondHue = (hueShift + 1.0F / 3.0F) % 1.0F;
        float thirdHue = (hueShift + 2.0F / 3.0F) % 1.0F;
        float red = color.red() * mixRed(hueShift)
                + color.green() * mixRed(secondHue)
                + color.blue() * mixRed(thirdHue);
        float green = color.red() * mixGreen(hueShift)
                + color.green() * mixGreen(secondHue)
                + color.blue() * mixGreen(thirdHue);
        float blue = color.red() * mixBlue(hueShift)
                + color.green() * mixBlue(secondHue)
                + color.blue() * mixBlue(thirdHue);
        float divisor = Math.max(1.0F, Math.max(red, Math.max(green, blue)));
        return new OutlineColor(red / divisor, green / divisor, blue / divisor);
    }

    /** Canal rojo del color ciclico usado por Minecraft. */
    private static float mixRed(float hue) {
        float sectorValue = hue * 5.99999F;
        int sector = Math.min(5, Math.max(0, (int) sectorValue));
        float fraction = sectorValue - sector;
        return switch (sector) {
            case 0 -> 1.0F;
            case 1 -> 1.0F - fraction;
            case 4 -> fraction;
            case 5 -> 1.0F;
            default -> 0.0F;
        };
    }

    /** Canal verde del color ciclico usado por Minecraft. */
    private static float mixGreen(float hue) {
        float sectorValue = hue * 5.99999F;
        int sector = Math.min(5, Math.max(0, (int) sectorValue));
        float fraction = sectorValue - sector;
        return switch (sector) {
            case 0 -> fraction;
            case 1, 2 -> 1.0F;
            case 3 -> 1.0F - fraction;
            default -> 0.0F;
        };
    }

    /** Canal azul del color ciclico usado por Minecraft. */
    private static float mixBlue(float hue) {
        float sectorValue = hue * 5.99999F;
        int sector = Math.min(5, Math.max(0, (int) sectorValue));
        float fraction = sectorValue - sector;
        return switch (sector) {
            case 2 -> fraction;
            case 3, 4 -> 1.0F;
            case 5 -> 1.0F - fraction;
            default -> 0.0F;
        };
    }

    /** Color lineal normalizado de un contorno. */
    private record OutlineColor(float red, float green, float blue) {
    }
}
