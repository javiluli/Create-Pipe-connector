package com.javiluli.createpipeconnector.feature.material.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Muestra una alerta breve cuando la ruta no dispone de materiales suficientes. */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MissingMaterialsAlertHud {
    private static final String OVERLAY_ID = "missing_materials_alert";
    private static final int HOTBAR_OFFSET = 84;
    private static final long DISPLAY_DURATION_MILLISECONDS = 1_800L;
    private static final long MATERIAL_PULSE_DURATION_MILLISECONDS = 520L;
    private static final long SHAKE_FRAME_MILLISECONDS = 45L;
    private static final int[] SHAKE_X = {-2, 2, -1, 1, -1, 1, 0, 0};
    private static final int[] SHAKE_Y = {0, -1, 1, 0, 0, 0, 0, 0};
    private static Component message;
    private static long startedAtMilliseconds;

    /** Impide crear instancias del HUD global. */
    private MissingMaterialsAlertHud() {
    }

    /** Registra la alerta sobre la barra rapida. */
    @SubscribeEvent
    public static void register(RegisterGuiOverlaysEvent event) {
        event.registerAbove(
                VanillaGuiOverlay.HOTBAR.id(),
                OVERLAY_ID,
                (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> render(guiGraphics)
        );
    }

    /** Reinicia la alerta con el mensaje localizado recibido. */
    public static void show(Component alertMessage) {
        message = alertMessage;
        startedAtMilliseconds = Util.getMillis();
    }

    /**
     * Devuelve un rojo que pulsa brevemente tras rechazar la construccion.
     *
     * <p>Solo cambia el color del dato faltante para no desplazar el resto del HUD.</p>
     */
    public static int missingMaterialColor(int baseColor) {
        if (message == null) {
            return baseColor;
        }
        long elapsedMilliseconds = Util.getMillis() - startedAtMilliseconds;
        if (elapsedMilliseconds < 0L || elapsedMilliseconds >= MATERIAL_PULSE_DURATION_MILLISECONDS) {
            return baseColor;
        }

        double progress = elapsedMilliseconds / (double) MATERIAL_PULSE_DURATION_MILLISECONDS;
        float intensity = (float) Math.sin(progress * Math.PI);
        return blendWithWhite(baseColor, intensity * 0.55F);
    }

    /** Aclara un color ARGB conservando el alfa del color base. */
    private static int blendWithWhite(int baseColor, float amount) {
        float clampedAmount = Math.max(0.0F, Math.min(1.0F, amount));
        int alpha = baseColor >>> 24;
        int red = blendChannel(baseColor >>> 16, clampedAmount);
        int green = blendChannel(baseColor >>> 8, clampedAmount);
        int blue = blendChannel(baseColor, clampedAmount);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    /** Interpola un canal de color de ocho bits hacia blanco. */
    private static int blendChannel(int base, float amount) {
        int baseChannel = base & 0xFF;
        return Math.round(baseChannel + (255 - baseChannel) * amount);
    }

    /** Dibuja el texto y aplica una sacudida corta al inicio. */
    private static void render(GuiGraphics guiGraphics) {
        if (message == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null
                || minecraft.player == null
                || !ClientPipeConnectorState.isConnectorModeEnabled()) {
            clear();
            return;
        }
        if (minecraft.screen != null || minecraft.options.hideGui) {
            return;
        }

        long elapsedMilliseconds = Util.getMillis() - startedAtMilliseconds;
        if (elapsedMilliseconds >= DISPLAY_DURATION_MILLISECONDS) {
            clear();
            return;
        }

        ShakeOffset shakeOffset = shakeOffset(elapsedMilliseconds);
        Font font = minecraft.font;
        int x = (guiGraphics.guiWidth() - font.width(message)) / 2 + shakeOffset.x();
        int y = guiGraphics.guiHeight() - HOTBAR_OFFSET + shakeOffset.y();
        guiGraphics.drawString(font, message, x, y, 0xFFFFFFFF, true);
    }

    /** Devuelve un desplazamiento discreto para conservar el estilo de Minecraft. */
    private static ShakeOffset shakeOffset(long elapsedMilliseconds) {
        int frame = (int) (elapsedMilliseconds / SHAKE_FRAME_MILLISECONDS);
        if (frame < 0 || frame >= SHAKE_X.length) {
            return ShakeOffset.NONE;
        }
        return new ShakeOffset(SHAKE_X[frame], SHAKE_Y[frame]);
    }

    /** Elimina el mensaje activo y su temporizacion. */
    private static void clear() {
        message = null;
        startedAtMilliseconds = 0L;
    }

    /** Desplazamiento de un frame de la sacudida. */
    private record ShakeOffset(int x, int y) {
        private static final ShakeOffset NONE = new ShakeOffset(0, 0);
    }
}
