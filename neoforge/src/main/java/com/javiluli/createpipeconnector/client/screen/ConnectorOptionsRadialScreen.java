package com.javiluli.createpipeconnector.client.screen;

import com.javiluli.createpipeconnector.client.input.ClientPipeConnectorKeyMappings;
import com.javiluli.createpipeconnector.client.render.gui.GuiPixelBatch;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.CopperCasingMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PipeStyleMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.PumpMode;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.RoutePriority;
import com.javiluli.createpipeconnector.network.payload.CopperCasingModePayload;
import com.javiluli.createpipeconnector.network.payload.PipeStyleModePayload;
import com.javiluli.createpipeconnector.network.payload.PumpModePayload;
import com.javiluli.createpipeconnector.network.payload.ReverseAutoPumpDirectionPayload;
import com.javiluli.createpipeconnector.network.payload.RoutePriorityPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Locale;

public final class ConnectorOptionsRadialScreen extends Screen {
    private static final int BACKGROUND_COLOR = 0x33000000;
    private static final int MECHANIC_COLOR = 0x73000000;
    private static final int ACTIVE_COLOR = 0xA64A3518;
    private static final int HOVERED_COLOR = 0x59000000;
    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int TITLE_TEXT_COLOR = 0xFFFFFFFF;
    private static final int HINT_COLOR = 0xFFE2C783;
    private static final int ACCENT_COLOR = 0xFFC69C5D;
    private static final int DIVIDER_COLOR = 0xFF000000;
    private static final int HOVER_BORDER_COLOR = 0xFFFFFFFF;
    private static final int SELECTED_BORDER_COLOR = 0xFFC69C5D;
    private static final int OPTION_HOVER_INNER_COLOR = 0x80000000;
    private static final int OPTION_HOVER_OUTER_COLOR = 0x00000000;
    private static final int OPTION_SELECTED_COLOR = 0xA64A3518;
    private static final int RADIAL_GRID_UNIT = 2;
    private static final int MECHANIC_INNER_RADIUS = RADIAL_GRID_UNIT * 20;
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
    private static final float SMALL_TEXT_SCALE = 0.68F;
    private static final Mechanic[] MECHANICS = Mechanic.values();

    private Mechanic selectedMechanic = Mechanic.ROUTE_STYLE;
    private Mechanic hoveredMechanic;
    private RadialOption hoveredOption;

    public ConnectorOptionsRadialScreen() {
        super(Component.translatable("screen.createpipeconnector.options.title"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, BACKGROUND_COLOR);
        updateHover(mouseX, mouseY);

        int centerX = wheelCenterX();
        int centerY = wheelCenterY();
        Font font = Minecraft.getInstance().font;

        guiGraphics.drawCenteredString(font, title, centerX, centerY - OPTION_OUTER_RADIUS - 30, TITLE_TEXT_COLOR);
        renderWheel(guiGraphics, centerX, centerY);
        renderLabels(guiGraphics, font, centerX, centerY);
        renderTooltip(guiGraphics, font, centerX, centerY);

        Component hint = Component.translatable("screen.createpipeconnector.options.hint");
        drawScaledCenteredString(guiGraphics, font, hint, centerX, centerY + OPTION_OUTER_RADIUS + 44, HINT_COLOR, SMALL_TEXT_SCALE);
    }

    private void updateHover(int mouseX, int mouseY) {
        int centerX = wheelCenterX();
        int centerY = wheelCenterY();
        int deltaX = mouseX - centerX;
        int deltaY = mouseY - centerY;

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

    private void renderWheel(GuiGraphics guiGraphics, int centerX, int centerY) {
        renderPixelatedMechanicRing(guiGraphics, centerX, centerY);
        renderPixelatedOptionRing(guiGraphics, centerX, centerY);
        renderSelectedBorder(guiGraphics, centerX, centerY);
        renderHoveredBorder(guiGraphics, centerX, centerY);
    }

    private void renderPixelatedMechanicRing(GuiGraphics guiGraphics, int centerX, int centerY) {
        int radius = MECHANIC_OUTER_RADIUS + PIXEL_RING_CELL_SIZE;
        try (GuiPixelBatch pixelBatch = new GuiPixelBatch(guiGraphics)) {
            for (int x = -radius; x <= radius; x += PIXEL_RING_CELL_SIZE) {
                for (int y = -radius; y <= radius; y += PIXEL_RING_CELL_SIZE) {
                    float cellCenterX = x + PIXEL_RING_CELL_SIZE / 2.0F;
                    float cellCenterY = y + PIXEL_RING_CELL_SIZE / 2.0F;
                    double distance = Math.sqrt(cellCenterX * cellCenterX + cellCenterY * cellCenterY);
                    if (distance < MECHANIC_INNER_RADIUS || distance > MECHANIC_OUTER_RADIUS) {
                        continue;
                    }

                    double angle = angleAtOffset(cellCenterX, cellCenterY);
                    int sectorIndex = sectorIndexAtAngle(angle, MECHANICS.length, MECHANIC_INNER_RADIUS, MECHANIC_OUTER_RADIUS);
                    if (sectorIndex < 0) {
                        continue;
                    }

                    int color = pixelatedMechanicCellColor(MECHANICS[sectorIndex], sectorIndex, angle, distance);
                    pixelBatch.fill(centerX + x, centerY + y, centerX + x + PIXEL_RING_CELL_SIZE, centerY + y + PIXEL_RING_CELL_SIZE, color);
                }
            }
        }
    }

    private int pixelatedMechanicCellColor(Mechanic mechanic, int sectorIndex, double angle, double distance) {
        if (isPixelatedMechanicBorder(sectorIndex, angle, distance)) {
            return mechanic == selectedMechanic ? SELECTED_BORDER_COLOR : DIVIDER_COLOR;
        }
        return mechanicColorFor(mechanic);
    }

    private static boolean isPixelatedMechanicBorder(int sectorIndex, double angle, double distance) {
        return isPixelatedSectorBorder(sectorIndex, MECHANICS.length, MECHANIC_INNER_RADIUS, MECHANIC_OUTER_RADIUS, angle, distance);
    }

    private void renderPixelatedOptionRing(GuiGraphics guiGraphics, int centerX, int centerY) {
        RadialOption[] options = selectedMechanic.options();
        int radius = OPTION_OUTER_RADIUS + PIXEL_RING_CELL_SIZE;
        try (GuiPixelBatch pixelBatch = new GuiPixelBatch(guiGraphics)) {
            for (int x = -radius; x <= radius; x += PIXEL_RING_CELL_SIZE) {
                for (int y = -radius; y <= radius; y += PIXEL_RING_CELL_SIZE) {
                    float cellCenterX = x + PIXEL_RING_CELL_SIZE / 2.0F;
                    float cellCenterY = y + PIXEL_RING_CELL_SIZE / 2.0F;
                    double distance = Math.sqrt(cellCenterX * cellCenterX + cellCenterY * cellCenterY);
                    if (distance < OPTION_INNER_RADIUS || distance > OPTION_OUTER_RADIUS) {
                        continue;
                    }

                    double angle = angleAtOffset(cellCenterX, cellCenterY);
                    int sectorIndex = sectorIndexAtAngle(angle, options.length, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS);
                    if (sectorIndex < 0) {
                        continue;
                    }

                    int color = pixelatedOptionCellColor(options[sectorIndex], sectorIndex, options.length, angle, distance);
                    pixelBatch.fill(centerX + x, centerY + y, centerX + x + PIXEL_RING_CELL_SIZE, centerY + y + PIXEL_RING_CELL_SIZE, color);
                }
            }
        }
    }

    private int pixelatedOptionCellColor(RadialOption option, int sectorIndex, int sectorCount, double angle, double distance) {
        if (isPixelatedSectorBorder(sectorIndex, sectorCount, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, angle, distance)) {
            if (option.isActive()) {
                return SELECTED_BORDER_COLOR;
            }
            return option == hoveredOption ? HOVER_BORDER_COLOR : DIVIDER_COLOR;
        }
        if (option.isActive()) {
            return OPTION_SELECTED_COLOR;
        }
        if (option == hoveredOption) {
            return colorBetween(OPTION_HOVER_INNER_COLOR, OPTION_HOVER_OUTER_COLOR, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, distance);
        }
        return MECHANIC_COLOR;
    }

    private static boolean isPixelatedSectorBorder(int sectorIndex, int sectorCount, int innerRadius, int outerRadius, double angle, double distance) {
        if (Math.abs(distance - innerRadius) <= PIXEL_RING_BORDER_WIDTH || Math.abs(distance - outerRadius) <= PIXEL_RING_BORDER_WIDTH) {
            return true;
        }

        double startAngle = sectorStartAngle(sectorIndex, sectorCount, innerRadius, outerRadius);
        double endAngle = sectorEndAngle(sectorIndex, sectorCount, innerRadius, outerRadius);
        return angularDistance(angle, startAngle) * distance <= PIXEL_RING_BORDER_WIDTH
                || angularDistance(angle, endAngle) * distance <= PIXEL_RING_BORDER_WIDTH;
    }

    private int mechanicColorFor(Mechanic mechanic) {
        if (mechanic == hoveredMechanic) {
            return HOVERED_COLOR;
        }
        if (mechanic == selectedMechanic) {
            return ACTIVE_COLOR;
        }
        return MECHANIC_COLOR;
    }

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
            drawFittedCenteredString(guiGraphics, font, Component.translatable(option.translationKey()), labelX, labelY, TEXT_COLOR, OPTION_LABEL_SCALE, OPTION_LABEL_MAX_WIDTH);
        }
    }

    private void renderTooltip(GuiGraphics guiGraphics, Font font, int centerX, int centerY) {
        RadialOption option = hoveredOption == null ? selectedMechanic.activeOption() : hoveredOption;
        Component optionTitle = Component.translatable(option.translationKey());
        Component description = Component.translatable(option.descriptionTranslationKey());

        int tooltipY = centerY + OPTION_OUTER_RADIUS + 15;
        drawScaledCenteredString(guiGraphics, font, optionTitle, centerX, tooltipY, TEXT_COLOR, SMALL_TEXT_SCALE);
        drawScaledCenteredString(guiGraphics, font, description, centerX, tooltipY + 11, HINT_COLOR, SMALL_TEXT_SCALE);
    }

    private void renderHoveredBorder(GuiGraphics guiGraphics, int centerX, int centerY) {
        if (hoveredOption != null) {
            return;
        }

        if (hoveredMechanic != null) {
            renderPixelatedMechanicOutline(guiGraphics, centerX, centerY, hoveredMechanic, HOVER_BORDER_COLOR);
        }
    }

    private static void renderPixelatedMechanicOutline(GuiGraphics guiGraphics, int centerX, int centerY, Mechanic mechanic, int color) {
        int radius = MECHANIC_OUTER_RADIUS + PIXEL_RING_CELL_SIZE;
        int sectorIndex = mechanic.ordinal();
        try (GuiPixelBatch pixelBatch = new GuiPixelBatch(guiGraphics)) {
            for (int x = -radius; x <= radius; x += PIXEL_RING_CELL_SIZE) {
                for (int y = -radius; y <= radius; y += PIXEL_RING_CELL_SIZE) {
                    float cellCenterX = x + PIXEL_RING_CELL_SIZE / 2.0F;
                    float cellCenterY = y + PIXEL_RING_CELL_SIZE / 2.0F;
                    double distance = Math.sqrt(cellCenterX * cellCenterX + cellCenterY * cellCenterY);
                    if (distance < MECHANIC_INNER_RADIUS || distance > MECHANIC_OUTER_RADIUS) {
                        continue;
                    }

                    double angle = angleAtOffset(cellCenterX, cellCenterY);
                    if (sectorIndexAtAngle(angle, MECHANICS.length, MECHANIC_INNER_RADIUS, MECHANIC_OUTER_RADIUS) == sectorIndex
                            && isPixelatedMechanicBorder(sectorIndex, angle, distance)) {
                        pixelBatch.fill(centerX + x, centerY + y, centerX + x + PIXEL_RING_CELL_SIZE, centerY + y + PIXEL_RING_CELL_SIZE, color);
                    }
                }
            }
        }
    }

    private void renderSelectedBorder(GuiGraphics guiGraphics, int centerX, int centerY) {
        RadialOption[] options = selectedMechanic.options();
        renderPixelatedOptionOutline(guiGraphics, centerX, centerY, optionIndex(selectedMechanic.activeOption(), options), SELECTED_BORDER_COLOR);
    }

    private void renderPixelatedOptionOutline(GuiGraphics guiGraphics, int centerX, int centerY, int sectorIndex, int color) {
        RadialOption[] options = selectedMechanic.options();
        int radius = OPTION_OUTER_RADIUS + PIXEL_RING_CELL_SIZE;
        try (GuiPixelBatch pixelBatch = new GuiPixelBatch(guiGraphics)) {
            for (int x = -radius; x <= radius; x += PIXEL_RING_CELL_SIZE) {
                for (int y = -radius; y <= radius; y += PIXEL_RING_CELL_SIZE) {
                    float cellCenterX = x + PIXEL_RING_CELL_SIZE / 2.0F;
                    float cellCenterY = y + PIXEL_RING_CELL_SIZE / 2.0F;
                    double distance = Math.sqrt(cellCenterX * cellCenterX + cellCenterY * cellCenterY);
                    if (distance < OPTION_INNER_RADIUS || distance > OPTION_OUTER_RADIUS) {
                        continue;
                    }

                    double angle = angleAtOffset(cellCenterX, cellCenterY);
                    if (sectorIndexAtAngle(angle, options.length, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS) == sectorIndex
                            && isPixelatedSectorBorder(sectorIndex, options.length, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS, angle, distance)) {
                        pixelBatch.fill(centerX + x, centerY + y, centerX + x + PIXEL_RING_CELL_SIZE, centerY + y + PIXEL_RING_CELL_SIZE, color);
                    }
                }
            }
        }
    }

    private static void drawFittedCenteredString(GuiGraphics guiGraphics, Font font, Component label, int centerX, int centerY, int color, float baseScale, int maxWidth) {
        float scale = baseScale;
        int labelWidth = font.width(label);
        if (labelWidth > 0) {
            scale = Math.min(baseScale, Math.max(0.34F, maxWidth / (float) labelWidth));
        }
        drawScaledCenteredString(guiGraphics, font, label, centerX, centerY, color, scale);
    }

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

    private static Mechanic mechanicAtOffset(int x, int y) {
        return MECHANICS[sectorIndexAtOffset(x, y, MECHANICS.length)];
    }

    private RadialOption optionAtOffset(int x, int y) {
        RadialOption[] options = selectedMechanic.options();
        return options[sectorIndexAtOffset(x, y, options.length)];
    }

    private static boolean isInsideMechanicRing(int x, int y) {
        return isInsideRing(x, y, MECHANIC_INNER_RADIUS, MECHANIC_OUTER_RADIUS);
    }

    private static boolean isInsideOptionsRing(int x, int y) {
        return isInsideRing(x, y, OPTION_INNER_RADIUS, OPTION_OUTER_RADIUS);
    }

    private static boolean isInsideRing(int x, int y, int innerRadius, int outerRadius) {
        int distanceSquared = x * x + y * y;
        return distanceSquared >= innerRadius * innerRadius && distanceSquared <= outerRadius * outerRadius;
    }

    private static int colorBetween(int innerColor, int outerColor, int innerRadius, int outerRadius, double distance) {
        float progress = (float) ((distance - innerRadius) / (outerRadius - innerRadius));
        progress = Math.max(0.0F, Math.min(1.0F, progress));
        int alpha = Math.round(alpha(innerColor) + (alpha(outerColor) - alpha(innerColor)) * progress);
        int red = Math.round(red(innerColor) + (red(outerColor) - red(innerColor)) * progress);
        int green = Math.round(green(innerColor) + (green(outerColor) - green(innerColor)) * progress);
        int blue = Math.round(blue(innerColor) + (blue(outerColor) - blue(innerColor)) * progress);
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int alpha(int color) {
        return color >>> 24 & 255;
    }

    private static int red(int color) {
        return color >>> 16 & 255;
    }

    private static int green(int color) {
        return color >>> 8 & 255;
    }

    private static int blue(int color) {
        return color & 255;
    }

    private int wheelCenterX() {
        return width / 2;
    }

    private int wheelCenterY() {
        return height / 2;
    }

    private static int sectorIndexAtOffset(int x, int y, int sectorCount) {
        double angle = angleAtOffset(x, y);
        return (int) Math.floor((angle + sectorSize(sectorCount) / 2.0D) / sectorSize(sectorCount)) % sectorCount;
    }

    private static int sectorIndexAtAngle(double angle, int sectorCount, int innerRadius, int outerRadius) {
        for (int index = 0; index < sectorCount; index++) {
            if (isAngleBetween(angle, sectorStartAngle(index, sectorCount, innerRadius, outerRadius), sectorEndAngle(index, sectorCount, innerRadius, outerRadius))) {
                return index;
            }
        }
        return -1;
    }

    private static double angleAtOffset(double x, double y) {
        return normalizeAngle(Math.atan2(x, -y));
    }

    private static boolean isAngleBetween(double angle, double startAngle, double endAngle) {
        double normalizedAngle = normalizeAngle(angle);
        double normalizedStart = normalizeAngle(startAngle);
        double normalizedEnd = normalizeAngle(endAngle);
        if (normalizedStart <= normalizedEnd) {
            return normalizedAngle >= normalizedStart && normalizedAngle <= normalizedEnd;
        }
        return normalizedAngle >= normalizedStart || normalizedAngle <= normalizedEnd;
    }

    private static double angularDistance(double firstAngle, double secondAngle) {
        double distance = Math.abs(normalizeAngle(firstAngle) - normalizeAngle(secondAngle));
        return Math.min(distance, Math.PI * 2.0D - distance);
    }

    private static double normalizeAngle(double angle) {
        double normalizedAngle = angle % (Math.PI * 2.0D);
        return normalizedAngle < 0.0D ? normalizedAngle + Math.PI * 2.0D : normalizedAngle;
    }

    private static double sectorCenterAngle(int index, int sectorCount) {
        return index * sectorSize(sectorCount);
    }

    private static double sectorStartAngle(int index, int sectorCount, int innerRadius, int outerRadius) {
        double halfSector = sectorSize(sectorCount) / 2.0D;
        return sectorCenterAngle(index, sectorCount) - halfSector + sectorGapAngle(innerRadius, outerRadius) / 2.0D;
    }

    private static double sectorEndAngle(int index, int sectorCount, int innerRadius, int outerRadius) {
        double halfSector = sectorSize(sectorCount) / 2.0D;
        return sectorCenterAngle(index, sectorCount) + halfSector - sectorGapAngle(innerRadius, outerRadius) / 2.0D;
    }

    private static double sectorGapAngle(int innerRadius, int outerRadius) {
        return SECTOR_GAP_PIXELS / ((innerRadius + outerRadius) / 2.0D);
    }

    private static double sectorSize(int sectorCount) {
        return (Math.PI * 2.0D) / sectorCount;
    }

    private static int optionIndex(RadialOption option, RadialOption[] options) {
        for (int index = 0; index < options.length; index++) {
            if (options[index] == option) {
                return index;
            }
        }
        return 0;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (ClientPipeConnectorKeyMappings.cycleRoutePriorityKey().matchesMouse(button)) {
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ClientPipeConnectorKeyMappings.cycleRoutePriorityKey().matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

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

    private static void applyOption(RadialOption option) {
        if (!option.isActive()) {
            if (option.apply()) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private enum Mechanic {
        ROUTE_STYLE("route_style") {
            @Override
            RadialOption[] options() {
                return RouteStyleOption.VALUES;
            }

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
        AUTO_PUMPS("auto_pumps") {
            @Override
            RadialOption[] options() {
                return PumpModeOption.VALUES;
            }

            @Override
            RadialOption activeOption() {
                for (PumpModeOption option : PumpModeOption.VALUES) {
                    if (option.mode == ClientPipeConnectorState.getPumpMode()) {
                        return option;
                    }
                }
                return PumpModeOption.OFF;
            }
        },
        PUMP_DIRECTION("pump_direction") {
            @Override
            RadialOption[] options() {
                return PumpDirectionOption.VALUES;
            }

            @Override
            RadialOption activeOption() {
                return ClientPipeConnectorState.isAutoPumpDirectionReversed() ? PumpDirectionOption.REVERSED : PumpDirectionOption.NORMAL;
            }
        },
        COPPER_CASING("copper_casing") {
            @Override
            RadialOption[] options() {
                return CopperCasingModeOption.VALUES;
            }

            @Override
            RadialOption activeOption() {
                for (CopperCasingModeOption option : CopperCasingModeOption.VALUES) {
                    if (option.mode == ClientPipeConnectorState.getCopperCasingMode()) {
                        return option;
                    }
                }
                return CopperCasingModeOption.MANUAL;
            }
        },
        PIPE_STYLE("pipe_style") {
            @Override
            RadialOption[] options() {
                return PipeStyleModeOption.VALUES;
            }

            @Override
            RadialOption activeOption() {
                for (PipeStyleModeOption option : PipeStyleModeOption.VALUES) {
                    if (option.mode == ClientPipeConnectorState.getPipeStyleMode()) {
                        return option;
                    }
                }
                return PipeStyleModeOption.DEFAULT;
            }
        };

        private final String id;

        Mechanic(String id) {
            this.id = id;
        }

        abstract RadialOption[] options();

        abstract RadialOption activeOption();

        Mechanic next() {
            return MECHANICS[(ordinal() + 1) % MECHANICS.length];
        }

        Mechanic previous() {
            return MECHANICS[(ordinal() - 1 + MECHANICS.length) % MECHANICS.length];
        }

        RadialOption nextOption() {
            RadialOption[] options = options();
            int currentIndex = optionIndex(activeOption(), options);
            return options[(currentIndex + 1) % options.length];
        }

        RadialOption previousOption() {
            RadialOption[] options = options();
            int currentIndex = optionIndex(activeOption(), options);
            return options[(currentIndex - 1 + options.length) % options.length];
        }

        String translationKey() {
            return "screen.createpipeconnector.options.mechanic." + id;
        }

        private static int optionIndex(RadialOption option, RadialOption[] options) {
            for (int index = 0; index < options.length; index++) {
                if (options[index] == option) {
                    return index;
                }
            }
            return 0;
        }
    }

    private interface RadialOption {
        String id();

        boolean isActive();

        boolean apply();

        default String translationKey() {
            return "screen.createpipeconnector.options.option." + id();
        }

        default String descriptionTranslationKey() {
            return "screen.createpipeconnector.options.description." + id();
        }
    }

    private enum RouteStyleOption implements RadialOption {
        AUTO(RoutePriority.AUTO),
        HORIZONTAL_FIRST(RoutePriority.HORIZONTAL_FIRST),
        VERTICAL_FIRST(RoutePriority.VERTICAL_FIRST),
        X_FIRST(RoutePriority.X_FIRST),
        Z_FIRST(RoutePriority.Z_FIRST),
        AVOID_VERTICAL(RoutePriority.AVOID_VERTICAL);

        private static final RouteStyleOption[] VALUES = values();
        private final RoutePriority priority;

        RouteStyleOption(RoutePriority priority) {
            this.priority = priority;
        }

        @Override
        public String id() {
            return "route_" + priority.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getRoutePriority() == priority;
        }

        @Override
        public boolean apply() {
            ClientPipeConnectorState.setRoutePriority(priority);
            PacketDistributor.sendToServer(new RoutePriorityPayload(priority));
            return true;
        }
    }

    private enum PumpModeOption implements RadialOption {
        OFF(PumpMode.OFF),
        EFFICIENT(PumpMode.EFFICIENT),
        SAFE(PumpMode.SAFE);

        private static final PumpModeOption[] VALUES = values();
        private final PumpMode mode;

        PumpModeOption(PumpMode mode) {
            this.mode = mode;
        }

        @Override
        public String id() {
            return "pump_mode_" + mode.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getPumpMode() == mode;
        }

        @Override
        public boolean apply() {
            ClientPipeConnectorState.setPumpMode(mode);
            PacketDistributor.sendToServer(new PumpModePayload(mode));
            return true;
        }
    }

    private enum PumpDirectionOption implements RadialOption {
        NORMAL(false),
        REVERSED(true);

        private static final PumpDirectionOption[] VALUES = values();
        private final boolean reversed;

        PumpDirectionOption(boolean reversed) {
            this.reversed = reversed;
        }

        @Override
        public String id() {
            return reversed ? "pump_direction_reversed" : "pump_direction_normal";
        }

        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.isAutoPumpDirectionReversed() == reversed;
        }

        @Override
        public boolean apply() {
            ClientPipeConnectorState.setAutoPumpDirectionReversed(reversed);
            PacketDistributor.sendToServer(new ReverseAutoPumpDirectionPayload(reversed));
            return true;
        }
    }

    private enum CopperCasingModeOption implements RadialOption {
        NONE(CopperCasingMode.NONE),
        MANUAL(CopperCasingMode.MANUAL),
        ALL(CopperCasingMode.ALL);

        private static final CopperCasingModeOption[] VALUES = values();
        private final CopperCasingMode mode;

        CopperCasingModeOption(CopperCasingMode mode) {
            this.mode = mode;
        }

        @Override
        public String id() {
            return "casing_mode_" + mode.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getCopperCasingMode() == mode;
        }

        @Override
        public boolean apply() {
            ClientPipeConnectorState.setCopperCasingMode(mode);
            PacketDistributor.sendToServer(new CopperCasingModePayload(mode));
            return true;
        }
    }

    private enum PipeStyleModeOption implements RadialOption {
        DEFAULT(PipeStyleMode.DEFAULT),
        GLASS(PipeStyleMode.GLASS);

        private static final PipeStyleModeOption[] VALUES = values();
        private final PipeStyleMode mode;

        PipeStyleModeOption(PipeStyleMode mode) {
            this.mode = mode;
        }

        @Override
        public String id() {
            return "pipe_style_" + mode.name().toLowerCase(Locale.ROOT);
        }

        @Override
        public boolean isActive() {
            return ClientPipeConnectorState.getPipeStyleMode() == mode;
        }

        @Override
        public boolean apply() {
            ClientPipeConnectorState.setPipeStyleMode(mode);
            PacketDistributor.sendToServer(new PipeStyleModePayload(mode));
            return true;
        }
    }
}
