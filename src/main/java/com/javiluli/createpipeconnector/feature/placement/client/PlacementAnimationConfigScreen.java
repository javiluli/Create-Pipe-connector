package com.javiluli.createpipeconnector.feature.placement.client;

import com.javiluli.createpipeconnector.feature.placement.PlacementAnimationSettings;
import com.javiluli.createpipeconnector.feature.placement.config.PlacementAnimationClientConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Pantalla vanilla para configurar la construccion progresiva del conector. */
public final class PlacementAnimationConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 240;
    private static final int WIDGET_HEIGHT = 20;
    private static final String TRANSLATION_PREFIX = "screen.createpipeconnector.config.";

    private final Screen parent;
    private boolean animationEnabled;
    private int piecesPerSecond;
    private SpeedSlider speedSlider;

    /** Crea la pantalla conservando la pantalla de mods como destino de retorno. */
    public PlacementAnimationConfigScreen(Screen parent) {
        super(Component.translatable(TRANSLATION_PREFIX + "title"));
        this.parent = parent;
        PlacementAnimationSettings settings = PlacementAnimationClientConfig.get();
        animationEnabled = settings.enabled();
        piecesPerSecond = settings.piecesPerSecond();
    }

    /** Construye los controles centrados y aplica su estado inicial. */
    @Override
    protected void init() {
        int left = (width - PANEL_WIDTH) / 2;
        int top = height / 2 - 45;

        speedSlider = addRenderableWidget(new SpeedSlider(
                left,
                top + 28,
                PANEL_WIDTH,
                WIDGET_HEIGHT,
                piecesPerSecond,
                value -> {
                    piecesPerSecond = value;
                    applySettings();
                }
        ));
        speedSlider.active = animationEnabled;

        addRenderableWidget(CycleButton.onOffBuilder(animationEnabled).create(
                left,
                top,
                PANEL_WIDTH,
                WIDGET_HEIGHT,
                Component.translatable(TRANSLATION_PREFIX + "animation"),
                (button, enabled) -> {
                    animationEnabled = enabled;
                    speedSlider.active = enabled;
                    applySettings();
                }
        ));

        addRenderableWidget(Button.builder(
                Component.translatable(TRANSLATION_PREFIX + "reset"),
                button -> resetDefaults()
        ).bounds(left, top + 68, 116, WIDGET_HEIGHT).build());
        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> onClose()
        ).bounds(left + 124, top + 68, 116, WIDGET_HEIGHT).build());
    }

    /** Dibuja el fondo vanilla, el titulo y una ayuda breve sobre la velocidad. */
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 82, 0xFFFFFF);
        graphics.drawCenteredString(
                font,
                Component.translatable(TRANSLATION_PREFIX + "description"),
                width / 2,
                height / 2 - 66,
                0xA0A0A0
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    /** Restaura los valores recomendados sin cerrar la pantalla. */
    private void resetDefaults() {
        animationEnabled = PlacementAnimationSettings.DEFAULT_ENABLED;
        piecesPerSecond = PlacementAnimationSettings.DEFAULT_PIECES_PER_SECOND;
        rebuildWidgets();
        applySettings();
    }

    /** Guarda el TOML y sincroniza los valores actuales con el servidor conectado. */
    private void applySettings() {
        PlacementAnimationClientConfig.save(new PlacementAnimationSettings(animationEnabled, piecesPerSecond));
        ClientPlacementAnimationSynchronizer.syncIfConnected();
    }

    /** Guarda tambien al cerrar mediante Escape y regresa a la pantalla anterior. */
    @Override
    public void onClose() {
        applySettings();
        if (minecraft != null) {
            minecraft.setScreen(parent);
        }
    }

    /** Slider discreto que controla la velocidad objetivo de construccion. */
    private static final class SpeedSlider extends AbstractSliderButton {
        private final java.util.function.IntConsumer onValueChanged;
        private int piecesPerSecond;

        private SpeedSlider(
                int x,
                int y,
                int width,
                int height,
                int piecesPerSecond,
                java.util.function.IntConsumer onValueChanged
        ) {
            super(x, y, width, height, Component.empty(), normalize(piecesPerSecond));
            this.onValueChanged = onValueChanged;
            this.piecesPerSecond = piecesPerSecond;
            updateMessage();
        }

        /** Actualiza el texto con la velocidad literal en piezas por segundo. */
        @Override
        protected void updateMessage() {
            setMessage(Component.translatable(TRANSLATION_PREFIX + "speed", piecesPerSecond));
        }

        /** Convierte la posicion del slider en una velocidad entera. */
        @Override
        protected void applyValue() {
            int selectedSpeed = PlacementAnimationSettings.MIN_PIECES_PER_SECOND
                    + (int) Math.round(value * (PlacementAnimationSettings.MAX_PIECES_PER_SECOND
                    - PlacementAnimationSettings.MIN_PIECES_PER_SECOND));
            if (selectedSpeed == piecesPerSecond) {
                return;
            }
            piecesPerSecond = selectedSpeed;
            onValueChanged.accept(piecesPerSecond);
            updateMessage();
        }

        /** Convierte la velocidad al valor normalizado que espera el widget vanilla. */
        private static double normalize(int piecesPerSecond) {
            return (piecesPerSecond - PlacementAnimationSettings.MIN_PIECES_PER_SECOND)
                    / (double) (PlacementAnimationSettings.MAX_PIECES_PER_SECOND
                    - PlacementAnimationSettings.MIN_PIECES_PER_SECOND);
        }
    }
}
