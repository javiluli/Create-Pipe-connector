package com.javiluli.createpipeconnector.feature.placement.client;

import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.javiluli.createpipeconnector.feature.routing.PipePathfinder;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Conserva la siguiente pieza de una construccion progresiva como preview.
 *
 * <p>El avance se basa en los bloques reales recibidos del servidor. De este
 * modo el fantasma siempre precede a la pieza que se va a colocar y no depende
 * de una animacion calculada de forma independiente en el cliente.</p>
 */
public final class ClientPlacementLeadPreview {
    private static final long NO_PROGRESS_TIMEOUT_TICKS = 200L;
    private static final Deque<PendingPreview> PENDING_PREVIEWS = new ArrayDeque<>();
    private static Level activeLevel;
    private static int nextVersion;
    private static long cachedGameTime = Long.MIN_VALUE;
    private static List<ActivePreview> cachedActivePreviews = List.of();

    /** Impide crear instancias del estado global de cliente. */
    private ClientPlacementLeadPreview() {
    }

    /**
     * Encola la ruta confirmada si la construccion progresiva esta activa.
     *
     * @param level mundo donde se colocara la ruta
     * @param pieces piezas finales calculadas por el preview normal
     */
    public static void enqueue(Level level, List<PreviewPipe> pieces) {
        if (!PlacementAnimationClientConfig.get().enabled() || pieces.isEmpty()) {
            return;
        }
        if (activeLevel != level) {
            clear();
            activeLevel = level;
        }
        PENDING_PREVIEWS.addLast(new PendingPreview(
                List.copyOf(pieces),
                ++nextVersion,
                level.getGameTime()
        ));
        invalidateSnapshot();
    }

    /**
     * Devuelve todas las rutas activas y descarta las que ya terminaron.
     *
     * @return copia inmutable de los previews que avanzan en paralelo
     */
    public static List<ActivePreview> getActivePreviews(Level level) {
        if (level != activeLevel) {
            clear();
            return List.of();
        }

        long gameTime = level.getGameTime();
        if (cachedGameTime == gameTime) {
            return cachedActivePreviews;
        }

        List<ActivePreview> activePreviews = new ArrayList<>(PENDING_PREVIEWS.size());
        Iterator<PendingPreview> iterator = PENDING_PREVIEWS.iterator();
        while (iterator.hasNext()) {
            PendingPreview pending = iterator.next();
            pending.advancePlacedPieces(level, gameTime);

            if (pending.isComplete()) {
                iterator.remove();
                continue;
            }
            if (pending.hasTimedOut(gameTime)
                    || !PipePathfinder.isTraversableBlock(level, pending.activePiece().position())) {
                iterator.remove();
                continue;
            }
            activePreviews.add(new ActivePreview(pending.pieces, pending.nextPieceIndex, pending.version));
        }

        if (PENDING_PREVIEWS.isEmpty()) {
            activeLevel = null;
        }
        cachedGameTime = gameTime;
        cachedActivePreviews = List.copyOf(activePreviews);
        return cachedActivePreviews;
    }

    /** Elimina cualquier preview de construccion pendiente. */
    public static void clear() {
        PENDING_PREVIEWS.clear();
        activeLevel = null;
        invalidateSnapshot();
    }

    /** Invalida la instantanea compartida por las dos fases de render del frame. */
    private static void invalidateSnapshot() {
        cachedGameTime = Long.MIN_VALUE;
        cachedActivePreviews = List.of();
    }

    /** Datos inmutables que necesita el renderizador para dibujar una ruta activa. */
    public record ActivePreview(List<PreviewPipe> pieces, int pieceIndex, int version) {
    }

    /** Estado mutable de una ruta confirmada que espera actualizaciones del mundo. */
    private static final class PendingPreview {
        private final List<PreviewPipe> pieces;
        private final int version;
        private int nextPieceIndex;
        private long lastProgressTick;

        private PendingPreview(List<PreviewPipe> pieces, int version, long gameTime) {
            this.pieces = pieces;
            this.version = version;
            lastProgressTick = gameTime;
        }

        /** Avanza mientras el servidor ya haya colocado las piezas esperadas. */
        private void advancePlacedPieces(Level level, long gameTime) {
            int previousIndex = nextPieceIndex;
            while (!isComplete()) {
                PreviewPipe piece = activePiece();
                if (level.getBlockState(piece.position()).getBlock() != piece.state().getBlock()) {
                    break;
                }
                nextPieceIndex++;
            }
            if (nextPieceIndex != previousIndex) {
                lastProgressTick = gameTime;
            }
        }

        /** Indica si el servidor ya materializo toda la ruta. */
        private boolean isComplete() {
            return nextPieceIndex >= pieces.size();
        }

        /** Evita que un rechazo silencioso del servidor deje un fantasma permanente. */
        private boolean hasTimedOut(long gameTime) {
            return gameTime - lastProgressTick > NO_PROGRESS_TIMEOUT_TICKS;
        }

        /** Devuelve la pieza que aun no existe en el mundo. */
        private PreviewPipe activePiece() {
            return pieces.get(nextPieceIndex);
        }
    }
}
