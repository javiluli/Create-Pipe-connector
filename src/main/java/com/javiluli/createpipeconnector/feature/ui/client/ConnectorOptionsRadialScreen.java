package com.javiluli.createpipeconnector.feature.ui.client;

import com.javiluli.createpipeconnector.feature.casing.CopperCasingMode;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorKeyMappings;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.feature.manual.ManualAction;
import com.javiluli.createpipeconnector.feature.manual.config.ManualAnchorClientConfig;
import com.javiluli.createpipeconnector.feature.pump.PumpMode;
import com.javiluli.createpipeconnector.feature.routing.RoutePriority;
import com.javiluli.createpipeconnector.feature.style.PipeStyleMode;
import com.javiluli.createpipeconnector.feature.casing.network.CopperCasingModePayload;
import com.javiluli.createpipeconnector.feature.pump.network.PumpModePayload;
import com.javiluli.createpipeconnector.feature.routing.network.RoutePriorityPayload;
import com.javiluli.createpipeconnector.feature.style.network.PipeStyleModePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Menu radial sin pausa para rutas, bombas, revestimientos y estilos.
 *
 * <p>El anillo interior selecciona una mecanica y el exterior aplica una de sus
 * implementaciones polimorficas de {@link RadialOption}.</p>
 */
public final class ConnectorOptionsRadialScreen extends Screen {
    private static final String TITLE_KEY = "screen.createpipeconnector.options.title";
    private static final String HINT_KEY = "screen.createpipeconnector.options.hint";
    private static final String MECHANIC_KEY_PREFIX = "screen.createpipeconnector.options.mechanic.";
    private static final String OPTION_KEY_PREFIX = "screen.createpipeconnector.options.option.";
    private static final String DESCRIPTION_KEY_PREFIX = "screen.createpipeconnector.options.description.";
    private static final String MANUAL_INDICATOR_KEY = "screen.createpipeconnector.options.manual_indicator";
    private static final int BACKGROUND_COLOR = 0x4D000000;
    private static final int MECHANIC_COLOR = 0x73000000;
    private static final int ACTIVE_COLOR = 0xA64A3518;
    private static final int HOVERED_COLOR = 0x59000000;
    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int TITLE_TEXT_COLOR = 0xFFFFFFFF;
    private static final int HINT_COLOR = 0xFFE2C783;
    private static final int ACCENT_COLOR = 0xFFC69C5D;
    private static final int ANCHOR_INDICATOR_COLOR = 0xFFFFD84A;
    private static final int DIVIDER_COLOR = 0xFF000000;
    private static final int HOVER_BORDER_COLOR = 0xFFFFFFFF;
    private static final int SELECTED_BORDER_COLOR = 0xFFC69C5D;
    private static final int OPTION_HOVER_INNER_COLOR = 0x80000000;
    private static final int OPTION_HOVER_OUTER_COLOR = 0x00000000;
    private static final int OPTION_SELECTED_COLOR = 0xA64A3518;
    private static final int RADIAL_GRID_UNIT = 2;
    private static final int MECHANIC_INNER_RADIUS = RADIAL_GRID_UNIT * 20;
    private static final int CENTER_RADIUS = RADIAL_GRID_UNIT * 16;
    private static final int MECHANIC_OUTER_RADIUS = RADIAL_GRID_UNIT * 40;
    private static final int RING_GAP = RADIAL_GRID_UNIT * 4;
    private static final int OPTION_INNER_RADIUS = MECHANIC_OUTER_RADIUS + RING_GAP;
    private static final int OPTION_OUTER_RADIUS = OPTION_INNER_RADIUS + RADIAL_GRID_UNIT * 19;
    private static final float SECTOR_GAP_PIXELS = 0.0F;
    private static final int PIXEL_RING_CELL_SIZE = RADIAL_GRID_UNIT;
    private static final float PIXEL_RING_BORDER_WIDTH = 2.0F;
    private static final int MECHANIC_LABEL_RADIUS = 61;
    private static final int OPTION_LABEL_RADIUS = 107;
    private static final int MECHANIC_LABEL_MAX_WIDTH = 64;
    private static final int OPTION_LABEL_MAX_WIDTH = 74;
    private static final float MECHANIC_LABEL_SCALE = 0.68F;
    private static final float OPTION_LABEL_SCALE = 0.62F;
    private static final float TITLE_TEXT_SCALE = 1.15F;
    private static final float BODY_TEXT_SCALE = 1.0F;
    private static final float HINT_TEXT_SCALE = 0.68F;
    private static final int RADIAL_SCREEN_MARGIN = 8;
    private static final int RADIAL_HORIZONTAL_EXTENSION = 8;
    private static final int RADIAL_VERTICAL_EXTENSION = 54;
    private static final Mechanic[] MECHANICS = Mechanic.values();
    private static final RingCells MECHANIC_RING = RingCells.create(MECHANIC_INNER_RADIUS, MECHANIC_OUTER_RADIUS, MECHANICS.length);
    private static final CircleCells MANUAL_INDICATOR_CIRCLE = CircleCells.create(CENTER_RADIUS);
    private static final Map<Integer, RingCells> OPTION_RINGS = Map.of(
            2, RingCells.create(OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, 2),
            3, RingCells.create(OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, 3),
            6, RingCells.create(OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, 6)
    );

    private Mechanic selectedMechanic = Mechanic.ROUTE_STYLE;
    private Mechanic hoveredMechanic;
    private RadialOption hoveredOption;

    /** Crea el menu con su titulo localizado. */
    public ConnectorOptionsRadialScreen() {
        super(Component.translatable(TITLE_KEY));
    }

    /** Dibuja el fondo, los anillos, las etiquetas y la ayuda contextual. */
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, BACKGROUND_COLOR);
        float radialScale = radialScale();
        updateHover(mouseX, mouseY, radialScale);

        int centerX = wheelCenterX();
        int centerY = wheelCenterY();
        Font font = Minecraft.getInstance().font;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(centerX, centerY, 0.0F);
        guiGraphics.pose().scale(radialScale, radialScale, 1.0F);
        guiGraphics.pose().translate(-centerX, -centerY, 0.0F);
        try {
            int titleY = centerY - OPTION_OUTER_RADIUS - 48;
            drawScaledCenteredString(guiGraphics, font, title, centerX, titleY, TITLE_TEXT_COLOR, TITLE_TEXT_SCALE);
            renderTooltip(guiGraphics, font, centerX, titleY + 19);
            renderWheel(guiGraphics, centerX, centerY);
            renderManualIndicator(guiGraphics, font, centerX, centerY);
            renderLabels(guiGraphics, font, centerX, centerY);

            Component hint = Component.translatable(HINT_KEY);
            drawScaledCenteredString(guiGraphics, font, hint, centerX, centerY + OPTION_OUTER_RADIUS + 44, HINT_COLOR, HINT_TEXT_SCALE);
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    /** Actualiza la mecanica u opcion situada bajo el cursor. */
    private void updateHover(int mouseX, int mouseY, float radialScale) {
        int centerX = wheelCenterX();
        int centerY = wheelCenterY();
        int deltaX = Math.round((mouseX - centerX) / radialScale);
        int deltaY = Math.round((mouseY - centerY) / radialScale);

        hoveredMechanic = null;
        hoveredOption = null;

        if (isInsideOptionsRing(deltaX, deltaY)) {
            hoveredOption = optionAtOffset(deltaX, deltaY);
            return;
        }

        if (isInsideMechanicRing(deltaX, deltaY)) {
            hoveredMechanic = mechanicAtOffset(deltaX, deltaY);
            selectedMechanic = hoveredMechanic;
        }
    }

    /** Dibuja ambos anillos y sus bordes de seleccion. */
    private void renderWheel(GuiGraphics guiGraphics, int centerX, int centerY) {
        try (GuiPixelBatch pixelBatch = new GuiPixelBatch(guiGraphics)) {
            renderPixelatedMechanicRing(pixelBatch, centerX, centerY);
            renderPixelatedOptionRing(pixelBatch, centerX, centerY);
            renderManualIndicatorCircle(pixelBatch, centerX, centerY);
        }
    }

    /** Dibuja en un lote las celdas del anillo de mecanicas. */
    private void renderPixelatedMechanicRing(GuiPixelBatch pixelBatch, int centerX, int centerY) {
        for (RingCell cell : MECHANIC_RING.cells()) {
            int color = pixelatedMechanicCellColor(MECHANICS[cell.sectorIndex()], cell);
            pixelBatch.fill(centerX + cell.x(), centerY + cell.y(), centerX + cell.x() + PIXEL_RING_CELL_SIZE, centerY + cell.y() + PIXEL_RING_CELL_SIZE, color);
        }
    }

    /** Resuelve el color de una celda del anillo interior. */
    private int pixelatedMechanicCellColor(Mechanic mechanic, RingCell cell) {
        if (cell.border()) {
            if (mechanic == hoveredMechanic) {
                return HOVER_BORDER_COLOR;
            }
            return mechanic == selectedMechanic ? SELECTED_BORDER_COLOR : DIVIDER_COLOR;
        }
        return mechanicColorFor(mechanic);
    }

    /** Dibuja en un lote las celdas del anillo de opciones. */
    private void renderPixelatedOptionRing(GuiPixelBatch pixelBatch, int centerX, int centerY) {
        RadialOption[] options = selectedMechanic.options();
        RingCells ring = optionRing(options.length);
        for (RingCell cell : ring.cells()) {
            int color = pixelatedOptionCellColor(options[cell.sectorIndex()], cell);
            pixelBatch.fill(centerX + cell.x(), centerY + cell.y(), centerX + cell.x() + PIXEL_RING_CELL_SIZE, centerY + cell.y() + PIXEL_RING_CELL_SIZE, color);
        }
    }

    /** Resuelve el color de una celda del anillo exterior. */
    private int pixelatedOptionCellColor(RadialOption option, RingCell cell) {
        if (cell.border()) {
            if (option.isActive()) {
                return SELECTED_BORDER_COLOR;
            }
            return option == hoveredOption ? HOVER_BORDER_COLOR : DIVIDER_COLOR;
        }
        if (option.isActive()) {
            return OPTION_SELECTED_COLOR;
        }
        if (option == hoveredOption) {
            return colorBetween(OPTION_HOVER_INNER_COLOR, OPTION_HOVER_OUTER_COLOR, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, cell.distance());
        }
        return MECHANIC_COLOR;
    }

    /** Detecta si una celda forma parte del borde circular o radial del sector. */
    private static boolean isPixelatedSectorBorder(int sectorIndex, int sectorCount, int innerRadius, int outerRadius, double angle, double distance) {
        if (Math.abs(distance - innerRadius) <= PIXEL_RING_BORDER_WIDTH || Math.abs(distance - outerRadius) <= PIXEL_RING_BORDER_WIDTH) {
            return true;
        }

        double startAngle = sectorStartAngle(sectorIndex, sectorCount, innerRadius, outerRadius);
        double endAngle = sectorEndAngle(sectorIndex, sectorCount, innerRadius, outerRadius);
        return angularDistance(angle, startAngle) * distance <= PIXEL_RING_BORDER_WIDTH
                || angularDistance(angle, endAngle) * distance <= PIXEL_RING_BORDER_WIDTH;
    }

    /** Resuelve el color base, activo o senalado de una mecanica. */
    private int mechanicColorFor(Mechanic mechanic) {
        if (mechanic == hoveredMechanic) {
            return HOVERED_COLOR;
        }
        if (mechanic == selectedMechanic) {
            return ACTIVE_COLOR;
        }
        return MECHANIC_COLOR;
    }

    /** Dibuja las etiquetas de ambos anillos ajustadas a su espacio. */
    private void renderLabels(GuiGraphics guiGraphics, Font font, int centerX, int centerY) {
        for (Mechanic mechanic : MECHANICS) {
            double angle = sectorCenterAngle(mechanic.ordinal(), MECHANICS.length);
            int labelX = Math.round(centerX + (float) Math.sin(angle) * MECHANIC_LABEL_RADIUS);
            int labelY = Math.round(centerY - (float) Math.cos(angle) * MECHANIC_LABEL_RADIUS);
            int color = mechanic == selectedMechanic ? ACCENT_COLOR : TEXT_COLOR;
            drawFittedCenteredString(guiGraphics, font, Component.translatable(mechanic.translationKey()), labelX, labelY, color, MECHANIC_LABEL_SCALE, MECHANIC_LABEL_MAX_WIDTH);
        }

        RadialOption[] options = selectedMechanic.options();
        for (int index = 0; index < options.length; index++) {
            RadialOption option = options[index];
            double angle = sectorCenterAngle(index, options.length);
            int labelX = Math.round(centerX + (float) Math.sin(angle) * OPTION_LABEL_RADIUS);
            int labelY = Math.round(centerY - (float) Math.cos(angle) * OPTION_LABEL_RADIUS);
            ItemStack icon = option.icon();
            if (!icon.isEmpty()) {
                guiGraphics.renderItem(icon, labelX - 8, labelY - 8);
                continue;
            }
            drawFittedCenteredString(guiGraphics, font, Component.translatable(option.translationKey()), labelX, labelY, TEXT_COLOR, OPTION_LABEL_SCALE, OPTION_LABEL_MAX_WIDTH);
        }
    }

    /** Dibuja bajo el encabezado el nombre y la descripcion de la opcion relevante. */
    private void renderTooltip(GuiGraphics guiGraphics, Font font, int centerX, int optionTitleY) {
        RadialOption option = relevantOption();
        Component optionTitle = Component.translatable(option.translationKey());
        Component description = Component.translatable(option.descriptionTranslationKey());

        drawScaledCenteredString(guiGraphics, font, optionTitle, centerX, optionTitleY, TEXT_COLOR, BODY_TEXT_SCALE);
        drawScaledCenteredString(guiGraphics, font, description, centerX, optionTitleY + 12, HINT_COLOR, BODY_TEXT_SCALE);
    }

    /** Dibuja el fondo cacheado del indicador manual dentro del lote radial. */
    private static void renderManualIndicatorCircle(GuiPixelBatch pixelBatch, int centerX, int centerY) {
        for (CircleCell cell : MANUAL_INDICATOR_CIRCLE.cells()) {
            pixelBatch.fill(
                    centerX + cell.x(),
                    centerY + cell.y(),
                    centerX + cell.x() + PIXEL_RING_CELL_SIZE,
                    centerY + cell.y() + PIXEL_RING_CELL_SIZE,
                    cell.border() ? DIVIDER_COLOR : MECHANIC_COLOR
            );
        }
    }

    /** Dibuja los textos del tercer circulo no interactivo. */
    private static void renderManualIndicator(GuiGraphics guiGraphics, Font font, int centerX, int centerY) {
        String actionKey = OPTION_KEY_PREFIX + "manual_action_" + ClientPipeConnectorState.getManualAction().id();
        drawFittedCenteredString(
                guiGraphics,
                font,
                Component.translatable(MANUAL_INDICATOR_KEY),
                centerX,
                centerY - 7,
                TEXT_COLOR,
                0.68F,
                CENTER_RADIUS * 2 - 8
        );
        Component actionLabel = Component.translatable(actionKey);
        boolean createsSupportAnchor = ManualAnchorClientConfig.willCreateSupportAnchor(
                ClientPipeConnectorState.getManualAction()
        );
        drawFittedCenteredSegments(
                guiGraphics,
                font,
                actionLabel,
                createsSupportAnchor ? Component.literal(" (+A)") : Component.empty(),
                centerX,
                centerY + 7,
                TEXT_COLOR,
                ANCHOR_INDICATOR_COLOR,
                0.68F,
                CENTER_RADIUS * 2 - 8
        );
    }

    /** Devuelve la opcion activa o la opcion exterior situada bajo el cursor. */
    private RadialOption relevantOption() {
        return hoveredOption == null ? selectedMechanic.activeOption() : hoveredOption;
    }

    /** Reduce una etiqueta si supera el ancho disponible y la centra. */
    private static void drawFittedCenteredString(GuiGraphics guiGraphics, Font font, Component label, int centerX, int centerY, int color, float baseScale, int maxWidth) {
        float scale = baseScale;
        int labelWidth = font.width(label);
        if (labelWidth > 0) {
            scale = Math.min(baseScale, Math.max(0.34F, maxWidth / (float) labelWidth));
        }
        drawScaledCenteredString(guiGraphics, font, label, centerX, centerY, color, scale);
    }

    /** Dibuja dos segmentos centrados y conserva un color distinto en el segundo. */
    private static void drawFittedCenteredSegments(
            GuiGraphics guiGraphics,
            Font font,
            Component primary,
            Component accent,
            int centerX,
            int centerY,
            int primaryColor,
            int accentColor,
            float baseScale,
            int maxWidth
    ) {
        int primaryWidth = font.width(primary);
        int accentWidth = font.width(accent);
        int totalWidth = primaryWidth + accentWidth;
        float scale = totalWidth <= 0
                ? baseScale
                : Math.min(baseScale, Math.max(0.34F, maxWidth / (float) totalWidth));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        try {
            int scaledCenterX = Math.round(centerX / scale);
            int scaledY = Math.round(centerY / scale) - font.lineHeight / 2;
            int startX = scaledCenterX - totalWidth / 2;
            guiGraphics.drawString(font, primary, startX, scaledY, primaryColor, true);
            if (!accent.getString().isEmpty()) {
                guiGraphics.drawString(font, accent, startX + primaryWidth, scaledY, accentColor, true);
            }
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    /** Dibuja una etiqueta centrada con una escala explicita. */
    private static void drawScaledCenteredString(GuiGraphics guiGraphics, Font font, Component label, int centerX, int centerY, int color, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        try {
            int scaledX = Math.round(centerX / scale);
            int scaledY = Math.round(centerY / scale);
            guiGraphics.drawString(font, label, scaledX - font.width(label) / 2, scaledY - font.lineHeight / 2, color, true);
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    /** Convierte un desplazamiento del cursor en una mecanica. */
    private static Mechanic mechanicAtOffset(int x, int y) {
        return MECHANICS[sectorIndexAtOffset(x, y, MECHANICS.length)];
    }

    /** Convierte un desplazamiento del cursor en una opcion de la mecanica activa. */
    private RadialOption optionAtOffset(int x, int y) {
        RadialOption[] options = selectedMechanic.options();
        return options[sectorIndexAtOffset(x, y, options.length)];
    }

    /** Indica si el desplazamiento esta dentro del anillo de mecanicas. */
    private static boolean isInsideMechanicRing(int x, int y) {
        return isInsideRing(x, y, MECHANIC_INNER_RADIUS, MECHANIC_OUTER_RADIUS);
    }

    /** Indica si el desplazamiento esta dentro del anillo de opciones. */
    private static boolean isInsideOptionsRing(int x, int y) {
        return isInsideRing(x, y, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS);
    }

    /** Comprueba un desplazamiento contra los radios de un anillo. */
    private static boolean isInsideRing(int x, int y, int innerRadius, int outerRadius) {
        int distanceSquared = x * x + y * y;
        return distanceSquared >= innerRadius * innerRadius && distanceSquared <= outerRadius * outerRadius;
    }

    /** Interpola dos colores segun la distancia radial. */
    private static int colorBetween(int innerColor, int outerColor, int innerRadius, int outerRadius, double distance) {
        float progress = (float) ((distance - innerRadius) / (outerRadius - innerRadius));
        progress = Math.max(0.0F, Math.min(1.0F, progress));
        int alpha = Math.round(alpha(innerColor) + (alpha(outerColor) - alpha(innerColor)) * progress);
        int red = Math.round(red(innerColor) + (red(outerColor) - red(innerColor)) * progress);
        int green = Math.round(green(innerColor) + (green(outerColor) - green(innerColor)) * progress);
        int blue = Math.round(blue(innerColor) + (blue(outerColor) - blue(innerColor)) * progress);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    /** Extrae el canal alfa de un color ARGB. */
    private static int alpha(int color) {
        return color >>> 24 & 255;
    }

    /** Extrae el canal rojo de un color ARGB. */
    private static int red(int color) {
        return color >>> 16 & 255;
    }

    /** Extrae el canal verde de un color ARGB. */
    private static int green(int color) {
        return color >>> 8 & 255;
    }

    /** Extrae el canal azul de un color ARGB. */
    private static int blue(int color) {
        return color & 255;
    }

    /** Devuelve la coordenada horizontal del centro del menu. */
    private int wheelCenterX() {
        return width / 2;
    }

    /** Devuelve la coordenada vertical del centro del menu. */
    private int wheelCenterY() {
        return height / 2;
    }

    /** Reduce uniformemente el radial cuando la resolucion de GUI no dispone de espacio suficiente. */
    private float radialScale() {
        int availableWidth = Math.max(1, width - RADIAL_SCREEN_MARGIN * 2);
        int availableHeight = Math.max(1, height - RADIAL_SCREEN_MARGIN * 2);
        int requiredWidth = (OPTION_OUTER_RADIUS + RADIAL_HORIZONTAL_EXTENSION) * 2;
        int requiredHeight = (OPTION_OUTER_RADIUS + RADIAL_VERTICAL_EXTENSION) * 2;
        return Math.min(1.0F, Math.min(
                availableWidth / (float) requiredWidth,
                availableHeight / (float) requiredHeight
        ));
    }

    /** Convierte un desplazamiento cartesiano en indice de sector. */
    private static int sectorIndexAtOffset(int x, int y, int sectorCount) {
        double angle = angleAtOffset(x, y);
        return (int) Math.floor((angle + sectorSize(sectorCount) / 2.0D) / sectorSize(sectorCount)) % sectorCount;
    }

    /** Localiza el sector que contiene un angulo concreto. */
    private static int sectorIndexAtAngle(double angle, int sectorCount, int innerRadius, int outerRadius) {
        for (int index = 0; index < sectorCount; index++) {
            if (isAngleBetween(angle, sectorStartAngle(index, sectorCount, innerRadius, outerRadius), sectorEndAngle(index, sectorCount, innerRadius, outerRadius))) {
                return index;
            }
        }
        return -1;
    }

    /** Convierte un desplazamiento en un angulo normalizado con origen superior. */
    private static double angleAtOffset(double x, double y) {
        return normalizeAngle(Math.atan2(x, -y));
    }

    /** Comprueba un angulo incluso cuando el intervalo cruza cero. */
    private static boolean isAngleBetween(double angle, double startAngle, double endAngle) {
        double normalizedAngle = normalizeAngle(angle);
        double normalizedStart = normalizeAngle(startAngle);
        double normalizedEnd = normalizeAngle(endAngle);
        if (normalizedStart <= normalizedEnd) {
            return normalizedAngle >= normalizedStart && normalizedAngle <= normalizedEnd;
        }
        return normalizedAngle >= normalizedStart || normalizedAngle <= normalizedEnd;
    }

    /** Calcula la distancia minima entre dos angulos. */
    private static double angularDistance(double firstAngle, double secondAngle) {
        double distance = Math.abs(normalizeAngle(firstAngle) - normalizeAngle(secondAngle));
        return Math.min(distance, Math.PI * 2.0D - distance);
    }

    /** Normaliza un angulo al intervalo de cero a dos pi. */
    private static double normalizeAngle(double angle) {
        double normalizedAngle = angle % (Math.PI * 2.0D);
        return normalizedAngle < 0.0D ? normalizedAngle + Math.PI * 2.0D : normalizedAngle;
    }

    /** Devuelve el angulo central de un sector. */
    private static double sectorCenterAngle(int index, int sectorCount) {
        return index * sectorSize(sectorCount);
    }

    /** Devuelve el angulo inicial de un sector considerando su separacion. */
    private static double sectorStartAngle(int index, int sectorCount, int innerRadius, int outerRadius) {
        double halfSector = sectorSize(sectorCount) / 2.0D;
        return sectorCenterAngle(index, sectorCount) - halfSector + sectorGapAngle(innerRadius, outerRadius) / 2.0D;
    }

    /** Devuelve el angulo final de un sector considerando su separacion. */
    private static double sectorEndAngle(int index, int sectorCount, int innerRadius, int outerRadius) {
        double halfSector = sectorSize(sectorCount) / 2.0D;
        return sectorCenterAngle(index, sectorCount) + halfSector - sectorGapAngle(innerRadius, outerRadius) / 2.0D;
    }

    /** Convierte la separacion lineal configurada en una separacion angular. */
    private static double sectorGapAngle(int innerRadius, int outerRadius) {
        return SECTOR_GAP_PIXELS / ((innerRadius + outerRadius) / 2.0D);
    }

    /** Devuelve el tamano angular uniforme de cada sector. */
    private static double sectorSize(int sectorCount) {
        return (Math.PI * 2.0D) / sectorCount;
    }

    /** Devuelve la geometria cacheada del anillo con el numero de sectores indicado. */
    private static RingCells optionRing(int sectorCount) {
        RingCells cachedRing = OPTION_RINGS.get(sectorCount);
        return cachedRing == null ? RingCells.create(OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, sectorCount) : cachedRing;
    }

    /**
     * Celda de interfaz precalculada dentro de un sector pixelado.
     */
    private record RingCell(int x, int y, int sectorIndex, double distance, boolean border) {
    }

    /**
     * Geometria inmutable compartida entre frames con igual numero de sectores.
     */
    private record RingCells(List<RingCell> cells) {
        /** Genera las celdas visibles y marca cuales pertenecen al borde. */
        private static RingCells create(int innerRadius, int outerRadius, int sectorCount) {
            int radius = outerRadius + PIXEL_RING_CELL_SIZE;
            List<RingCell> cells = new ArrayList<>();
            for (int x = -radius; x <= radius; x += PIXEL_RING_CELL_SIZE) {
                for (int y = -radius; y <= radius; y += PIXEL_RING_CELL_SIZE) {
                    float cellCenterX = x + PIXEL_RING_CELL_SIZE / 2.0F;
                    float cellCenterY = y + PIXEL_RING_CELL_SIZE / 2.0F;
                    double distance = Math.sqrt(cellCenterX * cellCenterX + cellCenterY * cellCenterY);
                    if (distance < innerRadius || distance > outerRadius) {
                        continue;
                    }

                    double angle = angleAtOffset(cellCenterX, cellCenterY);
                    int sectorIndex = sectorIndexAtAngle(angle, sectorCount, innerRadius, outerRadius);
                    if (sectorIndex < 0) {
                        continue;
                    }

                    cells.add(new RingCell(
                            x,
                            y,
                            sectorIndex,
                            distance,
                            isPixelatedSectorBorder(sectorIndex, sectorCount, innerRadius, outerRadius, angle, distance)
                    ));
                }
            }
            return new RingCells(List.copyOf(cells));
        }
    }

    /** Celda precalculada del indicador circular central. */
    private record CircleCell(int x, int y, boolean border) {
    }

    /** Geometria inmutable del indicador manual central. */
    private record CircleCells(List<CircleCell> cells) {
        /** Genera una vez las celdas visibles del circulo. */
        private static CircleCells create(int circleRadius) {
            int radius = circleRadius + PIXEL_RING_CELL_SIZE;
            List<CircleCell> cells = new ArrayList<>();
            for (int x = -radius; x <= radius; x += PIXEL_RING_CELL_SIZE) {
                for (int y = -radius; y <= radius; y += PIXEL_RING_CELL_SIZE) {
                    float cellCenterX = x + PIXEL_RING_CELL_SIZE / 2.0F;
                    float cellCenterY = y + PIXEL_RING_CELL_SIZE / 2.0F;
                    double distance = Math.sqrt(cellCenterX * cellCenterX + cellCenterY * cellCenterY);
                    if (distance <= circleRadius) {
                        cells.add(new CircleCell(
                                x,
                                y,
                                Math.abs(distance - circleRadius) <= PIXEL_RING_BORDER_WIDTH
                        ));
                    }
                }
            }
            return new CircleCells(List.copyOf(cells));
        }
    }

    /** Aplica opciones o recorre la herramienta manual con ambos botones. */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && isManualShortcut(mouseX, mouseY)) {
            cycleManualAction(button == 1);
            return true;
        }
        if (button == 0 && hoveredOption != null) {
            applyOption(hoveredOption);
            return true;
        }
        if (button == 1) {
            onClose();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Comprueba el circulo central y el sector interior de herramienta manual. */
    private boolean isManualShortcut(double mouseX, double mouseY) {
        float radialScale = radialScale();
        double deltaX = (mouseX - wheelCenterX()) / radialScale;
        double deltaY = (mouseY - wheelCenterY()) / radialScale;
        boolean insideCenter = deltaX * deltaX + deltaY * deltaY <= CENTER_RADIUS * CENTER_RADIUS;
        return insideCenter || hoveredMechanic == Mechanic.MANUAL_ACTION;
    }

    /**
     * Recorre las acciones manuales sin exigir apuntar a una opcion exterior.
     * El boton derecho avanza en sentido horario y el izquierdo retrocede.
     */
    private void cycleManualAction(boolean clockwise) {
        RadialOption option = clockwise
                ? Mechanic.MANUAL_ACTION.nextOption()
                : Mechanic.MANUAL_ACTION.previousOption();
        applyOption(option);
    }

    /** Cierra el menu al soltar el boton de raton asignado. */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (ClientPipeConnectorKeyMappings.openPipeConnectorOptionsKey().matchesMouse(button)) {
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    /** Cierra el menu al soltar la tecla asignada. */
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ClientPipeConnectorKeyMappings.openPipeConnectorOptionsKey().matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    /** Recorre mecanicas u opciones mediante la rueda del raton. */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0.0D) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        if (hoveredMechanic != null || hoveredOption == null) {
            selectedMechanic = scrollY > 0.0D ? selectedMechanic.previous() : selectedMechanic.next();
            return true;
        }

        RadialOption option = scrollY > 0.0D ? selectedMechanic.previousOption() : selectedMechanic.nextOption();
        applyOption(option);
        hoveredOption = option;
        return true;
    }

    /** Aplica una opcion nueva, la sincroniza y reproduce respuesta sonora. */
    private static void applyOption(RadialOption option) {
        if (!option.isActive()) {
            if (option.apply()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }
    }

    /** Mantiene el juego activo mientras el menu esta abierto. */
    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /**
     * Categorias del anillo interior. Cada constante proporciona sus opciones
     * para evitar condicionales especificos en el renderizador.
     */
    private enum Mechanic {
        ROUTE_STYLE("route_style") {
            /** Devuelve las prioridades de ruta disponibles. */
            @Override
            RadialOption[] options() {
                return RouteStyleOption.VALUES;
            }

            /** Devuelve la prioridad de ruta aplicada actualmente. */
            @Override
            RadialOption activeOption() {
                for (RouteStyleOption option : RouteStyleOption.VALUES) {
                    if (option.priority == ClientPipeConnectorState.getRoutePriority()) {
                        return option;
                    }
                }
                return RouteStyleOption.AUTO;
            }
        },
        PUMPS("pumps") {
            /** Devuelve los modos automaticos disponibles. */
            @Override
            RadialOption[] options() {
                return PumpConfigurationOption.VALUES;
            }

            /** Devuelve el modo de bombas aplicado actualmente. */
            @Override
            RadialOption activeOption() {
                for (PumpConfigurationOption option : PumpConfigurationOption.VALUES) {
                    if (option.matchesCurrentState()) {
                        return option;
                    }
                }
                return PumpConfigurationOption.OFF;
            }
        },
        PIPE_STYLE("pipe_style") {
            /** Devuelve los estilos de tuberia disponibles. */
            @Override
            RadialOption[] options() {
                return PipeStyleModeOption.VALUES;
            }

            /** Devuelve el estilo de tuberia aplicado actualmente. */
            @Override
            RadialOption activeOption() {
                for (PipeStyleModeOption option : PipeStyleModeOption.VALUES) {
                    if (option.mode == ClientPipeConnectorState.getPipeStyleMode()) {
                        return option;
                    }
                }
                return PipeStyleModeOption.DEFAULT;
            }
        },
        COPPER_CASING("copper_casing") {
            /** Devuelve los modos de revestimiento disponibles. */
            @Override
            RadialOption[] options() {
                return CopperCasingModeOption.RADIAL_VALUES;
            }

            /** Devuelve el modo de revestimiento aplicado actualmente. */
            @Override
            RadialOption activeOption() {
                for (CopperCasingModeOption option : CopperCasingModeOption.VALUES) {
                    if (option.mode == ClientPipeConnectorState.getCopperCasingMode()) {
                        return option;
                    }
                }
                return CopperCasingModeOption.NONE;
            }
        },
        MANUAL_ACTION("manual_action") {
            /** Devuelve las acciones puntuales disponibles. */
            @Override
            RadialOption[] options() {
                return ManualActionOption.VALUES;
            }

            /** Devuelve la accion puntual seleccionada actualmente. */
            @Override
            RadialOption activeOption() {
                for (ManualActionOption option : ManualActionOption.VALUES) {
                    if (option.action == ClientPipeConnectorState.getManualAction()) {
                        return option;
                    }
                }
                return ManualActionOption.ANCHOR;
            }
        };

        private final String id;

        /** Crea una categoria con su identificador de traduccion. */
        Mechanic(String id) {
            this.id = id;
        }

        /** Devuelve las opciones polimorficas de la categoria. */
        abstract RadialOption[] options();

        /** Devuelve la opcion aplicada actualmente en la categoria. */
        abstract RadialOption activeOption();

        /** Devuelve la siguiente categoria de forma circular. */
        Mechanic next() {
            return MECHANICS[(ordinal() + 1) % MECHANICS.length];
        }

        /** Devuelve la categoria anterior de forma circular. */
        Mechanic previous() {
            return MECHANICS[(ordinal() - 1 + MECHANICS.length) % MECHANICS.length];
        }

        /** Devuelve la siguiente opcion de forma circular. */
        RadialOption nextOption() {
            RadialOption[] options = options();
            int currentIndex = optionIndex(activeOption(), options);
            if (currentIndex < 0) {
                return options[0];
            }
            return options[(currentIndex + 1) % options.length];
        }

        /** Devuelve la opcion anterior de forma circular. */
        RadialOption previousOption() {
            RadialOption[] options = options();
            int currentIndex = optionIndex(activeOption(), options);
            if (currentIndex < 0) {
                return options[options.length - 1];
            }
            return options[(currentIndex - 1 + options.length) % options.length];
        }

        /** Construye la clave de traduccion de la categoria. */
        String translationKey() {
            return MECHANIC_KEY_PREFIX + id;
        }

        /** Localiza una opcion dentro del conjunto de la categoria. */
        private static int optionIndex(RadialOption option, RadialOption[] options) {
            for (int index = 0; index < options.length; index++) {
                if (options[index] == option) {
                    return index;
                }
            }
            return -1;
        }
    }

    /**
     * Accion polimorfica mostrada en un sector del anillo exterior.
     */
    private interface RadialOption {
        /** Devuelve el identificador estable de la opcion. */
        String id();

        /** Indica si la opcion coincide con el estado actual. */
        boolean isActive();

        /** Aplica y sincroniza la opcion. */
        boolean apply();

        /** Devuelve el icono mostrado en el centro del radial. */
        default ItemStack icon() {
            return ItemStack.EMPTY;
        }

        /** Construye la clave de traduccion del nombre de la opcion. */
        default String translationKey() {
            return OPTION_KEY_PREFIX + id();
        }

        /** Construye la clave de traduccion de la descripcion de la opcion. */
        default String descriptionTranslationKey() {
            return DESCRIPTION_KEY_PREFIX + id();
        }
    }

    /** Opciones visuales que representan las prioridades del pathfinder. */
    private enum RouteStyleOption implements RadialOption {
        AUTO(RoutePriority.AUTO),
        HORIZONTAL_FIRST(RoutePriority.HORIZONTAL_FIRST),
        VERTICAL_FIRST(RoutePriority.VERTICAL_FIRST),
        X_FIRST(RoutePriority.X_FIRST),
        Z_FIRST(RoutePriority.Z_FIRST),
        AVOID_VERTICAL(RoutePriority.AVOID_VERTICAL);

        private static final RouteStyleOption[] VALUES = values();
        private final RoutePriority priority;

        /** Crea una opcion asociada a una prioridad de ruta. */
        RouteStyleOption(RoutePriority priority) {
            this.priority = priority;
        }

        /** Devuelve el identificador derivado de la prioridad. */
        @Override
        public String id() {
            return "route_" + priority.name().toLowerCase(Locale.ROOT);
        }

        /** Indica si esta prioridad esta activa. */
        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getRoutePriority() == priority;
        }

        /** Aplica y sincroniza esta prioridad. */
        @Override
        public boolean apply() {
            ClientPipeConnectorState.setRoutePriority(priority);
            PacketDistributor.sendToServer(new RoutePriorityPayload(priority));
            return true;
        }

    }

    /** Opciones de bombas automaticas; el sentido se controla mediante su tecla dedicada. */
    private enum PumpConfigurationOption implements RadialOption {
        OFF(PumpMode.OFF),
        EFFICIENT(PumpMode.EFFICIENT),
        SAFE(PumpMode.SAFE);

        private static final PumpConfigurationOption[] VALUES = values();
        private final PumpMode mode;

        /** Crea una opcion asociada a un modo de bombas. */
        PumpConfigurationOption(PumpMode mode) {
            this.mode = mode;
        }

        /** Devuelve el identificador estable del modo. */
        @Override
        public String id() {
            return "pump_configuration_" + mode.name().toLowerCase(Locale.ROOT);
        }

        /** Indica si el modo coincide con el estado local. */
        @Override
        public boolean isActive() {
            return matchesCurrentState();
        }

        /** Aplica y sincroniza el modo sin modificar el sentido seleccionado con R. */
        @Override
        public boolean apply() {
            if (ClientPipeConnectorState.getPumpMode() != mode) {
                ClientPipeConnectorState.setPumpMode(mode);
                PacketDistributor.sendToServer(new PumpModePayload(mode));
            }
            return true;
        }

        /** Comprueba el modo automatico actual. */
        private boolean matchesCurrentState() {
            return ClientPipeConnectorState.getPumpMode() == mode;
        }

        /** Sustituye el texto del modo apagado por el icono vanilla de prohibido. */
        @Override
        public ItemStack icon() {
            return mode == PumpMode.OFF ? new ItemStack(Items.BARRIER) : ItemStack.EMPTY;
        }
    }

    /** Opciones de revestimiento automatico; las marcas manuales son independientes. */
    private enum CopperCasingModeOption implements RadialOption {
        NONE(CopperCasingMode.NONE),
        ALL(CopperCasingMode.ALL);

        private static final CopperCasingModeOption[] VALUES = values();
        private static final CopperCasingModeOption[] RADIAL_VALUES = VALUES;
        private final CopperCasingMode mode;

        /** Crea una opcion asociada a un modo de revestimiento. */
        CopperCasingModeOption(CopperCasingMode mode) {
            this.mode = mode;
        }

        /** Devuelve el identificador derivado del modo. */
        @Override
        public String id() {
            return "casing_mode_" + mode.name().toLowerCase(Locale.ROOT);
        }

        /** Indica si este modo de revestimiento esta activo. */
        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getCopperCasingMode() == mode;
        }

        /** Aplica y sincroniza este modo de revestimiento. */
        @Override
        public boolean apply() {
            ClientPipeConnectorState.setCopperCasingMode(mode);
            PacketDistributor.sendToServer(new CopperCasingModePayload(mode));
            return true;
        }

        /** Sustituye el texto del modo apagado por el icono vanilla de prohibido. */
        @Override
        public ItemStack icon() {
            return mode == CopperCasingMode.NONE ? new ItemStack(Items.BARRIER) : ItemStack.EMPTY;
        }
    }

    /** Opciones que comparten los controles contextuales de anadir y deshacer. */
    private enum ManualActionOption implements RadialOption {
        ANCHOR(ManualAction.ANCHOR),
        MECHANICAL_PUMP(ManualAction.MECHANICAL_PUMP),
        COPPER_CASING(ManualAction.COPPER_CASING);

        private static final ManualActionOption[] VALUES = values();
        private final ManualAction action;

        /** Crea una opcion asociada a una accion puntual. */
        ManualActionOption(ManualAction action) {
            this.action = action;
        }

        /** Devuelve el identificador estable mostrado por el radial. */
        @Override
        public String id() {
            return "manual_action_" + action.id();
        }

        /** Indica si esta accion controla actualmente las teclas contextuales. */
        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getManualAction() == action;
        }

        /** Selecciona la accion sin modificar la ruta en curso. */
        @Override
        public boolean apply() {
            ClientPipeConnectorState.setManualAction(action);
            return true;
        }

    }

    /** Opciones visuales del estilo normal o de cristal. */
    private enum PipeStyleModeOption implements RadialOption {
        DEFAULT(PipeStyleMode.DEFAULT),
        GLASS(PipeStyleMode.GLASS);

        private static final PipeStyleModeOption[] VALUES = values();
        private final PipeStyleMode mode;

        /** Crea una opcion asociada a un estilo de tuberia. */
        PipeStyleModeOption(PipeStyleMode mode) {
            this.mode = mode;
        }

        /** Devuelve el identificador derivado del estilo. */
        @Override
        public String id() {
            return "pipe_style_" + mode.name().toLowerCase(Locale.ROOT);
        }

        /** Indica si este estilo esta activo. */
        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getPipeStyleMode() == mode;
        }

        /** Aplica y sincroniza este estilo de tuberia. */
        @Override
        public boolean apply() {
            ClientPipeConnectorState.setPipeStyleMode(mode);
            PacketDistributor.sendToServer(new PipeStyleModePayload(mode));
            return true;
        }

    }
}
