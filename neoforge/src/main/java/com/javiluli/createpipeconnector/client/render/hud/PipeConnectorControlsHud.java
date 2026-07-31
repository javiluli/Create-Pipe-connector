package com.javiluli.createpipeconnector.client.render.hud;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.input.ClientPipeConnectorKeyMappings;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState.MaterialStatus;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
/**
 * Draws the compact controls and material availability above the hotbar.
 */
public final class PipeConnectorControlsHud {
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, Constants.CONTROLS_OVERLAY);
    private static final int BACKGROUND_COLOR = 0xAA101010;
    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int MISSING_TEXT_COLOR = 0xFFFF6666;
    private static final float CONTROL_TEXT_SCALE = 0.75F;
    private static final int CONTROL_HOTBAR_OFFSET = 90;
    private static final int CONTROL_PANEL_PADDING = 5;
    private static final int MATERIAL_ICON_TEXT_GAP = 4;
    private static final int MATERIAL_ENTRY_GAP = 14;
    private static final int MATERIAL_HOTBAR_OFFSET = 64;

    private PipeConnectorControlsHud() {
    }

    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, PipeConnectorControlsHud::render);
    }

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRender(minecraft)) {
            return;
        }

        Font font = minecraft.font;
        List<MaterialEntry> materialEntries = buildMaterialEntries();
        renderMinimalControls(guiGraphics, font);
        renderMaterialPanel(guiGraphics, font, materialEntries);
    }

    private static void renderMinimalControls(GuiGraphics guiGraphics, Font font) {
        String controls = String.join("  |  ",
                hint(keyName(ClientPipeConnectorKeyMappings.toggleConnectorModeKey()), Constants.HUD_CONTROL_CONNECTOR_MODE),
                hint(keyName(Minecraft.getInstance().options.keyUse), Constants.HUD_CONTROL_START_CONFIRM),
                hint(keyName(ClientPipeConnectorKeyMappings.cycleRoutePriorityKey()), Constants.HUD_CONTROL_ROUTE_PRIORITY),
                hint(keyName(ClientPipeConnectorKeyMappings.addAnchorKey()), Constants.HUD_CONTROL_ADD_ANCHOR),
                hint(keyName(ClientPipeConnectorKeyMappings.togglePreviewLockKey()), Constants.HUD_CONTROL_LOCK_PREVIEW)
        );

        int width = Math.round(font.width(controls) * CONTROL_TEXT_SCALE);
        int x = (guiGraphics.guiWidth() - width) / 2;
        int y = Math.max(8, guiGraphics.guiHeight() - CONTROL_HOTBAR_OFFSET);
        guiGraphics.fill(x - CONTROL_PANEL_PADDING, y - 4, x + width + CONTROL_PANEL_PADDING, y + Math.round(font.lineHeight * CONTROL_TEXT_SCALE) + 2, BACKGROUND_COLOR);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(CONTROL_TEXT_SCALE, CONTROL_TEXT_SCALE, 1.0F);
        try {
            guiGraphics.drawString(font, controls, Math.round(x / CONTROL_TEXT_SCALE), Math.round(y / CONTROL_TEXT_SCALE), TEXT_COLOR, true);
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    private static void renderMaterialPanel(GuiGraphics guiGraphics, Font font, List<MaterialEntry> materialEntries) {
        if (materialEntries.isEmpty()) {
            return;
        }

        int materialWidth = materialEntriesWidth(font, materialEntries);
        int x = (guiGraphics.guiWidth() - materialWidth) / 2;
        int y = guiGraphics.guiHeight() - MATERIAL_HOTBAR_OFFSET;

        renderMaterialEntries(guiGraphics, font, materialEntries, x, y + 1);
    }

    private static boolean shouldRender(Minecraft minecraft) {
        return minecraft.level != null
                && minecraft.player != null
                && minecraft.screen == null
                && !minecraft.options.hideGui
                && ClientPipeConnectorState.isConnectorModeEnabled();
    }

    private static List<MaterialEntry> buildMaterialEntries() {
        MaterialStatus materialStatus = ClientPipeConnectorState.getMaterialStatus();
        if (materialStatus == null) {
            return List.of();
        }

        List<MaterialEntry> entries = new ArrayList<>();
        entries.add(new MaterialEntry(new ItemStack(materialStatus.pipeBlock().asItem()), materialStatus.requiredPipes(), materialStatus.availablePipes(), materialStatus.creative()));

        Block pumpBlock = PipeConnectorLogic.getMechanicalPumpBlock();
        if (materialStatus.requiredPumps() > 0 && pumpBlock != null) {
            entries.add(new MaterialEntry(new ItemStack(pumpBlock.asItem()), materialStatus.requiredPumps(), materialStatus.availablePumps(), materialStatus.creative()));
        }

        Block casingBlock = PipeConnectorLogic.getCopperCasingBlock();
        if (materialStatus.requiredCopperCasings() > 0 && casingBlock != null) {
            entries.add(new MaterialEntry(new ItemStack(casingBlock.asItem()), materialStatus.requiredCopperCasings(), materialStatus.availableCopperCasings(), materialStatus.creative()));
        }
        return entries;
    }

    private static int materialEntriesWidth(Font font, List<MaterialEntry> entries) {
        if (entries.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (int index = 0; index < entries.size(); index++) {
            MaterialEntry entry = entries.get(index);
            width += 16 + MATERIAL_ICON_TEXT_GAP + font.width(entry.countText());
            if (index + 1 < entries.size()) {
                width += MATERIAL_ENTRY_GAP;
            }
        }
        return width;
    }

    private static void renderMaterialEntries(GuiGraphics guiGraphics, Font font, List<MaterialEntry> entries, int x, int y) {
        int entryX = x;
        for (MaterialEntry entry : entries) {
            if (!entry.stack().isEmpty()) {
                guiGraphics.renderItem(entry.stack(), entryX, y);
            }
            int textX = entryX + 16 + MATERIAL_ICON_TEXT_GAP;
            guiGraphics.drawString(font, entry.countText(), textX, y + 5, entry.hasEnough() ? TEXT_COLOR : MISSING_TEXT_COLOR, true);
            entryX += 16 + MATERIAL_ICON_TEXT_GAP + font.width(entry.countText()) + MATERIAL_ENTRY_GAP;
        }
    }

    private static String hint(String keyName, String actionTranslationKey) {
        return Component.translatable(actionTranslationKey).getString() + ": \"" + keyName + "\"";
    }

    private static String keyName(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage().getString();
    }

    private record MaterialEntry(ItemStack stack, int required, int available, boolean creative) {
        private boolean hasEnough() {
            return creative || available >= required;
        }

        private String countText() {
            return required + "/" + (creative ? "\u221E" : available);
        }
    }
}
