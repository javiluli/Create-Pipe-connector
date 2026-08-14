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
        Map<BlockPos, OutlinePiece> outlinesByPosition
) {
    private static final PipeGhostGeometryCache EMPTY = new PipeGhostGeometryCache(
            List.of(),
            Map.of()
    );

    /** Crea una cache vacia reutilizable. */
    static PipeGhostGeometryCache empty() {
        return EMPTY;
    }

    /** Crea la cache espacial usada mientras el jugador edita la ruta. */
    static PipeGhostGeometryCache editable(List<Section> sections) {
        return new PipeGhostGeometryCache(
                sections,
                buildOutlineIndex(sections)
        );
    }

    /** Crea la cache ordenada usada durante la construccion progresiva. */
    static PipeGhostGeometryCache progressive(List<Section> sections) {
        return new PipeGhostGeometryCache(
                sections,
                Map.of()
        );
    }

    /** Indica si la cache no contiene secciones renderizables. */
    boolean isEmpty() {
        return sections.isEmpty();
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
