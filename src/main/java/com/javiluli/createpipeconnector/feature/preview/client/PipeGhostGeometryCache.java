package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Map;

/**
 * Conserva la geometria inmutable de un preview dividida en secciones visibles.
 *
 * <p>La misma estructura sirve para el preview editable y para las rutas que
 * ya se estan construyendo. El renderer solo decide el tinte, la profundidad
 * y las secciones que deben enviarse a la GPU.</p>
 */
record PipeGhostGeometryCache(List<Section> sections, int fluidMask, int[] suffixFluidMasks) {
    /** Precalcula los fluidos restantes para consultas constantes durante el render. */
    PipeGhostGeometryCache(List<Section> sections, int fluidMask) {
        this(sections, fluidMask, buildSuffixFluidMasks(sections));
    }

    /** Crea una cache vacia reutilizable. */
    static PipeGhostGeometryCache empty() {
        return new PipeGhostGeometryCache(List.of(), 0);
    }

    /** Indica si la cache no contiene secciones renderizables. */
    boolean isEmpty() {
        return sections.isEmpty();
    }

    /**
     * Combina solo los fluidos de las secciones pendientes de una ruta
     * progresiva. En esas caches cada seccion representa una pieza ordenada.
     */
    int fluidMaskFrom(int firstSection) {
        int index = Math.max(0, firstSection);
        if (index >= suffixFluidMasks.length) {
            return 0;
        }
        return suffixFluidMasks[index];
    }

    /** Devuelve el fluido de una pieza concreta del preview progresivo. */
    int fluidMaskAt(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= sections.size()) {
            return 0;
        }
        return sections.get(sectionIndex).fluidMask();
    }

    /** Construye una tabla acumulada desde el final de la ruta. */
    private static int[] buildSuffixFluidMasks(List<Section> sections) {
        int[] masks = new int[sections.size()];
        int accumulatedMask = 0;
        for (int index = sections.size() - 1; index >= 0; index--) {
            accumulatedMask |= sections.get(index).fluidMask();
            masks[index] = accumulatedMask;
        }
        return masks;
    }

    /** Agrupa modelos y piezas dentro de un volumen descartable por frustum. */
    record Section(
            AABB bounds,
            List<PreviewPipe> pieces,
            Map<RenderType, SuperByteBuffer> base,
            Map<RenderType, SuperByteBuffer> missing,
            int fluidMask
    ) {
    }
}
