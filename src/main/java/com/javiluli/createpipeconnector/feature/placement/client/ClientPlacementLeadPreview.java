package com.javiluli.createpipeconnector.feature.placement.client;

import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.javiluli.createpipeconnector.feature.routing.PipePathfinder;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.Deque;
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
        PENDING_PREVIEWS.addLast(new PendingPreview(List.copyOf(pieces), ++nextVersion));
    }

    /**
     * Devuelve la siguiente pieza pendiente y descarta las rutas completadas.
     *
     * @return preview activo o {@code null} si no queda ninguna pieza
     */
    public static ActivePreview getActive(Level level) {
        if (level != activeLevel) {
            clear();
            return null;
        }

        while (!PENDING_PREVIEWS.isEmpty()) {
            PendingPreview pending = PENDING_PREVIEWS.peekFirst();
            long gameTime = level.getGameTime();
            pending.startIfNeeded(gameTime);
            pending.advancePlacedPieces(level, gameTime);

            if (pending.isComplete()) {
                PENDING_PREVIEWS.removeFirst();
                continue;
            }
            if (pending.hasTimedOut(gameTime)
                    || !PipePathfinder.isTraversableBlock(level, pending.activePiece().position())) {
                PENDING_PREVIEWS.removeFirst();
                continue;
            }
            return new ActivePreview(pending.pieces, pending.nextPieceIndex, pending.version);
        }

        activeLevel = null;
        return null;
    }

    /** Elimina cualquier preview de construccion pendiente. */
    public static void clear() {
        PENDING_PREVIEWS.clear();
        activeLevel = null;
    }

    /** Datos inmutables que necesita el renderizador para dibujar una pieza. */
    public record ActivePreview(List<PreviewPipe> pieces, int pieceIndex, int version) {
        /** Devuelve la pieza que debe aparecer antes del siguiente bloque real. */
        public PreviewPipe activePiece() {
            return pieces.get(pieceIndex);
        }
    }

    /** Estado mutable de una ruta confirmada que espera actualizaciones del mundo. */
    private static final class PendingPreview {
        private final List<PreviewPipe> pieces;
        private final int version;
        private int nextPieceIndex;
        private long lastProgressTick;
        private boolean started;

        private PendingPreview(List<PreviewPipe> pieces, int version) {
            this.pieces = pieces;
            this.version = version;
        }

        /** Inicia el control de espera solamente cuando la ruta llega al frente. */
        private void startIfNeeded(long gameTime) {
            if (!started) {
                started = true;
                lastProgressTick = gameTime;
            }
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
