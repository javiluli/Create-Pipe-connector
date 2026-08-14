package com.javiluli.createpipeconnector.feature.ui.client;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Muestra el cambio de estado del modo mediante un texto breve y animado. */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class PipeConnectorModeStatusHud {
    private static final String OVERLAY_ID = "pipe_connector_mode_status";
    private static final String MODE_STATUS = "hud.createpipeconnector.mode.status";
    private static final long DISPLAY_DURATION_MILLISECONDS = 5_000L;
    private static final long FULL_OPACITY_DURATION_MILLISECONDS = 3_000L;
    private static final long FADE_OUT_DURATION_MILLISECONDS = 2_000L;
    private static final int HOTBAR_OFFSET = 68;
    private static final Component ENABLED_MESSAGE = modeStatusMessage(true);
    private static final Component DISABLED_MESSAGE = modeStatusMessage(false);
    private static Boolean enabled;
    private static long startedAtMilliseconds;

    /** Impide crear instancias del HUD global. */
    private PipeConnectorModeStatusHud() {
    }

    /** Registra el texto sobre la interfaz vanilla. */
    @SubscribeEvent
    public static void register(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                VanillaGuiOverlay.HOTBAR.id(),
                OVERLAY_ID,
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> render(guiGraphics)
        );
    }

    /** Reinicia la notificacion con el estado recibido. */
    public static void show(boolean modeEnabled) {
        enabled = modeEnabled;
        startedAtMilliseconds = Util.getMillis();
    }

    /** Oculta el mensaje antes de iniciar una ruta para no solapar informacion. */
    public static void dismiss() {
        clear();
    }

    /** Dibuja el texto a tamano fijo y aplica una salida progresiva. */
    private static void render(GuiGraphics guiGraphics) {
        if (enabled == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            clear();
            return;
        }
        if (minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }

        long elapsedMilliseconds = Util.getMillis() - startedAtMilliseconds;
        if (elapsedMilliseconds < 0L || elapsedMilliseconds >= DISPLAY_DURATION_MILLISECONDS) {
            clear();
            return;
        }

        float opacity = calculateOpacity(elapsedMilliseconds);
        if (opacity <= 1.0F / 255.0F) {
            clear();
            return;
        }
        Component message = enabled ? ENABLED_MESSAGE : DISABLED_MESSAGE;
        drawCentered(guiGraphics, minecraft.font, message, opacity);
    }

    /** Centra el mensaje sobre la barra rapida. */
    private static void drawCentered(
            GuiGraphics guiGraphics,
            Font font,
            Component message,
            float opacity
    ) {
        int centerX = guiGraphics.guiWidth() / 2;
        int y = Math.max(4, guiGraphics.guiHeight() - HOTBAR_OFFSET);
        int color = Math.round(255.0F * opacity) << 24 | 0x00FFFFFF;
        guiGraphics.drawString(font, message, centerX - font.width(message) / 2, y, color, true);
    }

    /** Construye una vez el mensaje localizado para evitar asignaciones durante el render. */
    private static Component modeStatusMessage(boolean modeEnabled) {
        ChatFormatting statusColor = modeEnabled ? ChatFormatting.GREEN : ChatFormatting.RED;
        Component status = Component.literal(modeEnabled ? "ON" : "OFF").withStyle(statusColor);
        return Component.translatable(MODE_STATUS, status).withStyle(ChatFormatting.WHITE);
    }

    /** Mantiene el mensaje visible y reduce su opacidad durante los dos segundos finales. */
    private static float calculateOpacity(long elapsedMilliseconds) {
        if (elapsedMilliseconds < FULL_OPACITY_DURATION_MILLISECONDS) {
            return 1.0F;
        }

        return Mth.clamp(
                (DISPLAY_DURATION_MILLISECONDS - elapsedMilliseconds) / (float) FADE_OUT_DURATION_MILLISECONDS,
                0.0F,
                1.0F
        );
    }

    /** Elimina el estado una vez finalizada la notificacion. */
    private static void clear() {
        enabled = null;
        startedAtMilliseconds = 0L;
    }
}
