package com.javiluli.createpipeconnector.client.input;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PreviewPipe;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.Selection;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Computes client-side material availability for the HUD and ghost preview.
 *
 * <p>The server performs the authoritative validation before placement. This
 * helper only provides immediate visual feedback to the local player.</p>
 */
final class ClientMaterialPreview {
    private ClientMaterialPreview() {
    }

    /**
     * Marks the preview positions that cannot be supplied by the current
     * survival inventory.
     */
    static List<PreviewPipe> markMissingMaterials(
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

    /** Updates the material counts consumed by the connector HUD. */
    static void updateStatus(LocalPlayer player, Selection selection, ConnectionPlan plan) {
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
     * Builds the localized placement error shown when confirmation is blocked.
     *
     * @return the message, or {@code null} when every material is available
     */
    static Component missingMaterialsMessage(ClientPipeConnectorState.MaterialStatus materialStatus) {
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
