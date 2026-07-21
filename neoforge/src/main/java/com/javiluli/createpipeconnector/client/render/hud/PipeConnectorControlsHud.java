package com.javiluli.createpipeconnector.client.render.hud;

import com.javiluli.createpipeconnector.Constants;
import com.javiluli.createpipeconnector.client.input.ClientPipeConnectorKeyMappings;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PipeConnectorControlsHud {
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pipe_connector_controls");
    private static final int BACKGROUND_COLOR = 0xAA101010;
    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int TITLE_COLOR = 0xFF66D9EF;
    private static final int MAX_WIDTH_PADDING = 24;
    private static final float TEXT_SCALE = 0.75F;
    private static final int LINE_HEIGHT = 9;
    private static final int HOTBAR_OFFSET = 90;

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
        int maxWidth = Math.round((guiGraphics.guiWidth() - MAX_WIDTH_PADDING) / TEXT_SCALE);
        List<String> lines = buildControlLines(minecraft, font, maxWidth);
        if (lines.isEmpty()) {
            return;
        }

        int width = Math.round(lines.stream().mapToInt(font::width).max().orElse(0) * TEXT_SCALE);
        int x = (guiGraphics.guiWidth() - width) / 2;
        int y = Math.max(8, guiGraphics.guiHeight() - HOTBAR_OFFSET - (lines.size() - 1) * LINE_HEIGHT);

        guiGraphics.fill(x - 5, y - 4, x + width + 5, y + lines.size() * LINE_HEIGHT + 2, BACKGROUND_COLOR);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0F);
        try {
            for (int index = 0; index < lines.size(); index++) {
                int color = index == 0 ? TITLE_COLOR : TEXT_COLOR;
                int lineWidth = Math.round(font.width(lines.get(index)) * TEXT_SCALE);
                int lineX = Math.round(((guiGraphics.guiWidth() - lineWidth) / 2.0F) / TEXT_SCALE);
                int lineY = Math.round((y + index * LINE_HEIGHT) / TEXT_SCALE);
                guiGraphics.drawString(font, lines.get(index), lineX, lineY, color, true);
            }
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    private static boolean shouldRender(Minecraft minecraft) {
        return minecraft.level != null
                && minecraft.player != null
                && minecraft.screen == null
                && !minecraft.options.hideGui
                && ClientPipeConnectorState.isConnectorModeEnabled();
    }

    private static List<String> buildControlLines(Minecraft minecraft, Font font, int maxWidth) {
        List<String> lines = new ArrayList<>();
        lines.add(Component.translatable("hud.createpipeconnector.connector_mode").getString());

        List<String> hints = List.of(
                hint(keyName(minecraft.options.keyUse), "hud.createpipeconnector.control.start_confirm"),
                hint(keyName(minecraft.options.keyAttack), "hud.createpipeconnector.control.cancel"),
                hint(keyName(ClientPipeConnectorKeyMappings.addAnchorKey()), "hud.createpipeconnector.control.add_anchor"),
                hint(keyName(ClientPipeConnectorKeyMappings.removeLastAnchorKey()), "hud.createpipeconnector.control.remove_anchor"),
                hint(keyName(ClientPipeConnectorKeyMappings.togglePreviewLockKey()), "hud.createpipeconnector.control.lock_preview"),
                hint(keyName(ClientPipeConnectorKeyMappings.toggleConnectorModeKey()), "hud.createpipeconnector.control.exit_mode")
        );

        String separator = "  |  ";
        String currentLine = "";
        for (String hint : hints) {
            String candidate = currentLine.isEmpty() ? hint : currentLine + separator + hint;
            if (!currentLine.isEmpty() && font.width(candidate) > maxWidth) {
                lines.add(currentLine);
                currentLine = hint;
            } else {
                currentLine = candidate;
            }
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine);
        }

        return lines;
    }

    private static String hint(String keyName, String actionTranslationKey) {
        return Component.translatable(actionTranslationKey).getString() + ": \"" + keyName + "\"";
    }

    private static String keyName(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage().getString();
    }
}
