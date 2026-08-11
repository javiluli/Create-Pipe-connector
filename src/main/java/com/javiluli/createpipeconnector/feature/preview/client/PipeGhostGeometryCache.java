package com.javiluli.createpipeconnector.feature.preview.client;

import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Conserva la geometria inmutable de un preview dividida en secciones visibles.
 *
 * <p>La misma estructura sirve para el preview editable y para las rutas que
 * ya se estan construyendo. El renderer solo decide el tinte, la profundidad
 * y las secciones que deben enviarse a la GPU.</p>
 */
record PipeGhostGeometryCache(
        List<Section> sections,
        int fluidMask,
        int[] suffixFluidMasks,
        Map<BlockPos, OutlinePiece> outlinesByPosition
) {
    private static final int[] NO_SUFFIX_FLUID_MASKS = new int[0];
    private static final PipeGhostGeometryCache EMPTY = new PipeGhostGeometryCache(
            List.of(),
            0,
            NO_SUFFIX_FLUID_MASKS,
            Map.of()
    );

    /** Crea una cache vacia reutilizable. */
    static PipeGhostGeometryCache empty() {
        return EMPTY;
    }

    /** Crea la cache espacial usada mientras el jugador edita la ruta. */
    static PipeGhostGeometryCache editable(List<Section> sections, int fluidMask) {
        return new PipeGhostGeometryCache(
                sections,
                fluidMask,
                NO_SUFFIX_FLUID_MASKS,
                buildOutlineIndex(sections)
        );
    }

    /** Crea la cache ordenada usada durante la construccion progresiva. */
    static PipeGhostGeometryCache progressive(List<Section> sections) {
        int[] suffixFluidMasks = buildSuffixFluidMasks(sections);
        return new PipeGhostGeometryCache(
                sections,
                suffixFluidMasks.length == 0 ? 0 : suffixFluidMasks[0],
                suffixFluidMasks,
                Map.of()
        );
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

    /** Combina los fluidos de un rango pequeno de piezas animadas. */
    int fluidMaskRange(int firstSection, int lastSection) {
        int startIndex = Math.max(0, firstSection);
        int endIndex = Math.min(lastSection, sections.size());
        int mask = 0;
        for (int index = startIndex; index < endIndex; index++) {
            mask |= sections.get(index).fluidMask();
        }
        return mask;
    }

    /** Localiza un contorno sin recorrer toda la ruta. */
    OutlinePiece outlineAt(BlockPos position) {
        return outlinesByPosition.get(position);
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

    /** Indexa los contornos para el redibujado puntual alrededor de anclas. */
    private static Map<BlockPos, OutlinePiece> buildOutlineIndex(List<Section> sections) {
        if (sections.isEmpty()) {
            return Map.of();
        }

        Map<BlockPos, OutlinePiece> outlines = new HashMap<>();
        for (Section section : sections) {
            for (OutlinePiece outline : section.outlines()) {
                outlines.put(outline.position(), outline);
            }
        }
        return Map.copyOf(outlines);
    }

    /** Agrupa modelos y piezas dentro de un volumen descartable por frustum. */
    record Section(
            AABB bounds,
            List<OutlinePiece> outlines,
            List<SuperByteBuffer> base,
            List<SuperByteBuffer> missing,
            int fluidMask
    ) {
    }

    /** Conserva las cajas de contorno calculadas al reconstruir el preview. */
    record OutlinePiece(BlockPos position, List<OutlineBox> boxes) {
    }

    /** Caja absoluta con el color final calculado al construir la cache. */
    record OutlineBox(AABB bounds, float red, float green, float blue) {
    }
}
