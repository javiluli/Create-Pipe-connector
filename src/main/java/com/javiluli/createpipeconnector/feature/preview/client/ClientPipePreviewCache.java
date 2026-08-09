package com.javiluli.createpipeconnector.feature.preview.client;

import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.material.client.ClientMaterialPreview;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Publica el preview final solo cuando cambia el plan, el inventario o su entorno.
 *
 * <p>El sondeo de estados es lineal y evita repetir cada tick las pasadas de
 * conexiones de Create, la reflexion y la construccion de todas las piezas.</p>
 */
public final class ClientPipePreviewCache {
    private static final Direction[] DIRECTIONS = Direction.values();
    private static Level cachedLevel;
    private static ConnectionPlan cachedPlan;
    private static Block cachedPipeBlock;
    private static ClientPipeConnectorState.MaterialStatus cachedMaterialStatus;
    private static long cachedEnvironmentFingerprint;
    private static int cachedPublishedPreviewVersion = -1;

    /** Impide crear instancias de la cache global del cliente. */
    private ClientPipePreviewCache() {
    }

    /** Calcula y publica el preview cuando alguna entrada observable cambia. */
    public static void update(Minecraft minecraft, Selection selection, ConnectionPlan plan) {
        Level level = minecraft.level;
        if (level == null || minecraft.player == null) {
            clear();
            return;
        }

        ClientPipeConnectorState.MaterialStatus materialStatus = ClientMaterialPreview.createStatus(
                minecraft.player,
                selection,
                plan
        );
        long environmentFingerprint = environmentFingerprint(level, plan.path());
        if (cachedLevel == level
                && cachedPlan == plan
                && cachedPipeBlock == selection.pipeBlock()
                && materialStatus.equals(cachedMaterialStatus)
                && environmentFingerprint == cachedEnvironmentFingerprint
                && cachedPublishedPreviewVersion == ClientPipeConnectorState.getPreviewVersion()) {
            return;
        }

        List<PreviewPipe> previewPipes = PipeConnectorLogic.buildPreview(level, plan, selection.pipeBlock());
        ClientPipeConnectorState.setPreviewPipes(ClientMaterialPreview.markMissingMaterials(
                plan,
                previewPipes,
                materialStatus
        ));
        ClientPipeConnectorState.setMaterialStatus(materialStatus);

        cachedLevel = level;
        cachedPlan = plan;
        cachedPipeBlock = selection.pipeBlock();
        cachedMaterialStatus = materialStatus;
        cachedEnvironmentFingerprint = environmentFingerprint;
        cachedPublishedPreviewVersion = ClientPipeConnectorState.getPreviewVersion();
    }

    /** Libera referencias al mundo y al ultimo plan publicado. */
    public static void clear() {
        cachedLevel = null;
        cachedPlan = null;
        cachedPipeBlock = null;
        cachedMaterialStatus = null;
        cachedEnvironmentFingerprint = 0L;
        cachedPublishedPreviewVersion = -1;
    }

    /** Resume los estados capaces de cambiar conexiones o waterlogging del plan. */
    private static long environmentFingerprint(Level level, List<BlockPos> path) {
        long fingerprint = 0xCBF29CE484222325L;
        BlockPos.MutableBlockPos neighbourPosition = new BlockPos.MutableBlockPos();
        for (BlockPos position : path) {
            fingerprint = mixState(fingerprint, level, position);
            for (Direction direction : DIRECTIONS) {
                neighbourPosition.set(
                        position.getX() + direction.getStepX(),
                        position.getY() + direction.getStepY(),
                        position.getZ() + direction.getStepZ()
                );
                fingerprint = mixState(fingerprint, level, neighbourPosition);
            }
        }
        return fingerprint;
    }

    /** Mezcla posicion y estado canonico sin crear colecciones temporales. */
    private static long mixState(long fingerprint, Level level, BlockPos position) {
        long mixed = fingerprint ^ position.asLong();
        mixed *= 0x100000001B3L;
        mixed ^= System.identityHashCode(level.getBlockState(position));
        return mixed * 0x100000001B3L;
    }
}
