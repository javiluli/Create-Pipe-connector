package com.javiluli.createpipeconnector.feature.material.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.connector.model.Selection;
import com.javiluli.createpipeconnector.feature.preview.PreviewPipe;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Calcula en cliente la disponibilidad de materiales para el HUD y el preview.
 *
 * <p>El servidor valida de forma autoritativa antes de colocar. Esta utilidad
 * solo proporciona respuesta visual inmediata al jugador local.</p>
 */
public final class ClientMaterialPreview {
    /** Impide crear instancias del calculador de materiales. */
    private ClientMaterialPreview() {
    }

    /**
     * Marca las posiciones que no pueden cubrirse con el inventario de supervivencia.
     */
    public static List<PreviewPipe> markMissingMaterials(
            LocalPlayer player,
            Selection selection,
            ConnectionPlan plan,
            List<PreviewPipe> previewPipes
    ) {
        if (player.getAbilities().instabuild || previewPipes.isEmpty()) {
            return previewPipes;
        }

        int availablePipes = PipeConnectorLogic.countAvailablePipes(player, selection.pipeBlock());
        int availablePumps = PipeConnectorLogic.countAvailablePumps(player);
        int availableCopperCasings = PipeConnectorLogic.countAvailableCopperCasings(player);
        if (availablePipes >= plan.requiredPipes()
                && availablePumps >= plan.requiredPumps()
                && availableCopperCasings >= plan.requiredCopperCasings()) {
            return previewPipes;
        }

        Set<BlockPos> missingPositions = missingMaterialPositions(
                plan,
                availablePipes,
                availablePumps,
                availableCopperCasings
        );
        if (missingPositions.isEmpty()) {
            return previewPipes;
        }

        List<PreviewPipe> markedPreviewPipes = new ArrayList<>(previewPipes.size());
        for (PreviewPipe previewPipe : previewPipes) {
            markedPreviewPipes.add(previewPipe.withMissingMaterial(missingPositions.contains(previewPipe.position())));
        }
        return markedPreviewPipes;
    }

    /** Actualiza los contadores consumidos por el HUD del conector. */
    public static void updateStatus(LocalPlayer player, Selection selection, ConnectionPlan plan) {
        int requiredPipes = plan.requiredPipes();
        int requiredPumps = plan.requiredPumps();
        int requiredCopperCasings = plan.requiredCopperCasings();
        if (player.getAbilities().instabuild) {
            ClientPipeConnectorState.setMaterialStatus(new ClientPipeConnectorState.MaterialStatus(
                    selection.pipeBlock(),
                    requiredPipes,
                    Integer.MAX_VALUE,
                    requiredPumps,
                    Integer.MAX_VALUE,
                    requiredCopperCasings,
                    Integer.MAX_VALUE,
                    true
            ));
            return;
        }

        ClientPipeConnectorState.setMaterialStatus(new ClientPipeConnectorState.MaterialStatus(
                selection.pipeBlock(),
                requiredPipes,
                PipeConnectorLogic.countAvailablePipes(player, selection.pipeBlock()),
                requiredPumps,
                PipeConnectorLogic.countAvailablePumps(player),
                requiredCopperCasings,
                PipeConnectorLogic.countAvailableCopperCasings(player),
                false
        ));
    }

    /**
     * Construye el error localizado mostrado cuando se bloquea la confirmacion.
     *
     * @return mensaje o {@code null} cuando estan disponibles todos los materiales
     */
    public static Component missingMaterialsMessage(ClientPipeConnectorState.MaterialStatus materialStatus) {
        if (materialStatus == null || materialStatus.creative()) {
            return null;
        }

        List<Component> missingMaterials = new ArrayList<>();
        addMissingMaterial(
                missingMaterials,
                materialStatus.requiredPipes(),
                materialStatus.availablePipes(),
                Constants.HUD_MISSING_PIPES
        );
        addMissingMaterial(
                missingMaterials,
                materialStatus.requiredPumps(),
                materialStatus.availablePumps(),
                Constants.HUD_MISSING_PUMPS
        );
        addMissingMaterial(
                missingMaterials,
                materialStatus.requiredCopperCasings(),
                materialStatus.availableCopperCasings(),
                Constants.HUD_MISSING_CASINGS
        );
        if (missingMaterials.isEmpty()) {
            return null;
        }
        return Component.translatable(Constants.HUD_MISSING_MATERIALS, joinComponents(missingMaterials));
    }

    /** Determina que posiciones deben marcarse como material insuficiente. */
    private static Set<BlockPos> missingMaterialPositions(
            ConnectionPlan plan,
            int availablePipes,
            int availablePumps,
            int availableCopperCasings
    ) {
        Set<BlockPos> missingPositions = new HashSet<>();
        int pipeIndex = 0;
        int pumpIndex = 0;
        boolean missingCopperCasing = plan.requiredCopperCasings() > availableCopperCasings;

        for (BlockPos position : plan.placementPositions()) {
            if (plan.pumpPlacements().containsKey(position)) {
                pumpIndex++;
                if (pumpIndex > availablePumps) {
                    missingPositions.add(position);
                }
                continue;
            }

            pipeIndex++;
            if (pipeIndex > availablePipes
                    || missingCopperCasing && plan.copperCasingPlacements().contains(position)) {
                missingPositions.add(position);
            }
        }
        return missingPositions;
    }

    /** Anade un material al mensaje unicamente cuando falta alguna unidad. */
    private static void addMissingMaterial(
            List<Component> missingMaterials,
            int required,
            int available,
            String translationKey
    ) {
        int missing = required - available;
        if (missing > 0) {
            missingMaterials.add(Component.translatable(translationKey, missing));
        }
    }

    /** Une componentes localizados mediante separadores legibles. */
    private static MutableComponent joinComponents(List<Component> components) {
        MutableComponent joined = Component.empty();
        for (int index = 0; index < components.size(); index++) {
            if (index > 0) {
                joined.append(", ");
            }
            joined.append(components.get(index));
        }
        return joined;
    }
}
