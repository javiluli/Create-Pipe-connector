package com.javiluli.createpipeconnector.client.screen;

import com.javiluli.createpipeconnector.client.input.ClientPipeConnectorKeyMappings;
import com.javiluli.createpipeconnector.client.state.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.RoutePriority;
import com.javiluli.createpipeconnector.network.payload.RoutePriorityPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

public final class RouteStyleRadialScreen extends Screen {
    private static final int BACKGROUND_COLOR = 0xA8101010;
    private static final int OPTION_COLOR = 0xB81A1A1A;
    private static final int OPTION_HOVERED_COLOR = 0xD4423112;
    private static final int OPTION_ACTIVE_COLOR = 0xD4383418;
    private static final int ACTIVE_BORDER_COLOR = 0xFFFFD166;
    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int HINT_COLOR = 0xFFB8B8B8;
    private static final int DIVIDER_COLOR = 0xAA0F0F0F;
    private static final int WHEEL_RADIUS = 78;
    private static final int INNER_RADIUS = 18;
    private static final int LABEL_RADIUS = 50;
    private static final float LABEL_SCALE = 0.66F;

    private RoutePriority hoveredPriority;

    public RouteStyleRadialScreen() {
        super(Component.translatable("screen.createpipeconnector.route_style.title"));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDimBackground(guiGraphics);
        hoveredPriority = hoveredPriority(mouseX, mouseY);

        int centerX = width / 2;
        int centerY = height / 2;
        Font font = Minecraft.getInstance().font;

        Component title = Component.translatable("screen.createpipeconnector.route_style.title");
        guiGraphics.drawCenteredString(font, title, centerX, centerY - WHEEL_RADIUS - 42, ACTIVE_BORDER_COLOR);
        renderTooltip(guiGraphics, font, centerX, centerY);

        renderWheel(guiGraphics, centerX, centerY);
        renderLabels(guiGraphics, font, centerX, centerY);

        Component hint = Component.translatable("screen.createpipeconnector.route_style.hint");
        guiGraphics.drawCenteredString(font, hint, centerX, centerY + WHEEL_RADIUS + 18, HINT_COLOR);
    }

    private void renderDimBackground(GuiGraphics guiGraphics) {
        guiGraphics.fill(0, 0, width, height, BACKGROUND_COLOR);
    }

    private void renderWheel(GuiGraphics guiGraphics, int centerX, int centerY) {
        for (int y = -WHEEL_RADIUS; y <= WHEEL_RADIUS; y++) {
            int runStart = Integer.MIN_VALUE;
            int runColor = 0;
            for (int x = -WHEEL_RADIUS; x <= WHEEL_RADIUS; x++) {
                int color = wheelColorAt(x, y);
                if (color != runColor) {
                    if (runStart != Integer.MIN_VALUE) {
                        guiGraphics.fill(centerX + runStart, centerY + y, centerX + x, centerY + y + 1, runColor);
                    }
                    runStart = color == 0 ? Integer.MIN_VALUE : x;
                    runColor = color;
                }
            }
            if (runStart != Integer.MIN_VALUE) {
                guiGraphics.fill(centerX + runStart, centerY + y, centerX + WHEEL_RADIUS + 1, centerY + y + 1, runColor);
            }
        }

        for (int index = 0; index < RoutePriority.values().length; index++) {
            double boundaryAngle = sectorCenterAngle(index) - sectorSize() / 2.0D;
            drawRadialLine(guiGraphics, centerX, centerY, boundaryAngle, INNER_RADIUS + 1, WHEEL_RADIUS - 1, DIVIDER_COLOR);
        }
    }

    private int wheelColorAt(int x, int y) {
        int distanceSquared = x * x + y * y;
        if (distanceSquared >= WHEEL_RADIUS * WHEEL_RADIUS || distanceSquared <= INNER_RADIUS * INNER_RADIUS) {
            return 0;
        }

        RoutePriority priority = priorityAtOffset(x, y);
        if (priority == hoveredPriority) {
            return OPTION_HOVERED_COLOR;
        }
        if (priority == ClientPipeConnectorState.getRoutePriority()) {
            return OPTION_ACTIVE_COLOR;
        }
        return OPTION_COLOR;
    }

    private void renderLabels(GuiGraphics guiGraphics, Font font, int centerX, int centerY) {
        for (RoutePriority priority : RoutePriority.values()) {
            double angle = sectorCenterAngle(priority.ordinal());
            int labelX = Math.round(centerX + (float) Math.sin(angle) * LABEL_RADIUS);
            int labelY = Math.round(centerY - (float) Math.cos(angle) * LABEL_RADIUS);
            int color = priority == ClientPipeConnectorState.getRoutePriority() ? ACTIVE_BORDER_COLOR : TEXT_COLOR;
            if (priority == hoveredPriority) {
                color = ACTIVE_BORDER_COLOR;
            }
            drawScaledCenteredString(guiGraphics, font, Component.translatable(routePriorityTranslationKey(priority)), labelX, labelY, color);
        }
    }

    private void renderTooltip(GuiGraphics guiGraphics, Font font, int centerX, int centerY) {
        RoutePriority priority = hoveredPriority == null ? ClientPipeConnectorState.getRoutePriority() : hoveredPriority;
        Component title = Component.translatable(routePriorityTranslationKey(priority));
        Component description = Component.translatable(routePriorityDescriptionTranslationKey(priority));
        guiGraphics.drawCenteredString(font, title, centerX, centerY - WHEEL_RADIUS - 26, ACTIVE_BORDER_COLOR);
        guiGraphics.drawCenteredString(font, description, centerX, centerY - WHEEL_RADIUS - 14, HINT_COLOR);
    }

    private static void drawScaledCenteredString(GuiGraphics guiGraphics, Font font, Component label, int centerX, int centerY, int color) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(LABEL_SCALE, LABEL_SCALE, 1.0F);
        try {
            int scaledX = Math.round(centerX / LABEL_SCALE);
            int scaledY = Math.round(centerY / LABEL_SCALE);
            guiGraphics.drawString(font, label, scaledX - font.width(label) / 2, scaledY - font.lineHeight / 2, color, true);
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    private static void drawRadialLine(GuiGraphics guiGraphics, int centerX, int centerY, double angle, int innerRadius, int outerRadius, int color) {
        for (int radius = innerRadius; radius <= outerRadius; radius++) {
            int x = centerX + Math.round((float) Math.sin(angle) * radius);
            int y = centerY - Math.round((float) Math.cos(angle) * radius);
            guiGraphics.fill(x, y, x + 1, y + 1, color);
        }
    }

    private RoutePriority hoveredPriority(int mouseX, int mouseY) {
        int centerX = width / 2;
        int centerY = height / 2;
        int deltaX = mouseX - centerX;
        int deltaY = mouseY - centerY;
        int distanceSquared = deltaX * deltaX + deltaY * deltaY;
        if (distanceSquared <= INNER_RADIUS * INNER_RADIUS || distanceSquared >= WHEEL_RADIUS * WHEEL_RADIUS) {
            return null;
        }

        return priorityAtOffset(deltaX, deltaY);
    }

    private static RoutePriority priorityAtOffset(int x, int y) {
        double angle = Math.atan2(x, -y);
        if (angle < 0.0D) {
            angle += Math.PI * 2.0D;
        }

        int index = (int) Math.floor((angle + sectorSize() / 2.0D) / sectorSize()) % RoutePriority.values().length;
        return RoutePriority.values()[index];
    }

    private static double sectorCenterAngle(int index) {
        return index * sectorSize();
    }

    private static double sectorSize() {
        return (Math.PI * 2.0D) / RoutePriority.values().length;
    }

    private static String routePriorityTranslationKey(RoutePriority priority) {
        return "hud.createpipeconnector.control.route_priority." + priority.name().toLowerCase(java.util.Locale.ROOT);
    }

    private static String routePriorityDescriptionTranslationKey(RoutePriority priority) {
        return "screen.createpipeconnector.route_style.description." + priority.name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredPriority != null) {
            applyRoutePriority(hoveredPriority);
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
            if (hoveredPriority != null) {
                applyRoutePriority(hoveredPriority);
            }
            onClose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (ClientPipeConnectorKeyMappings.cycleRoutePriorityKey().matches(keyCode, scanCode)) {
            if (hoveredPriority != null) {
                applyRoutePriority(hoveredPriority);
            }
            onClose();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0D) {
            RoutePriority priority = scrollY > 0.0D
                    ? ClientPipeConnectorState.getRoutePriority().previous()
                    : ClientPipeConnectorState.getRoutePriority().next();
            applyRoutePriority(priority);
            hoveredPriority = priority;
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private static void applyRoutePriority(RoutePriority priority) {
        if (priority == null || priority == ClientPipeConnectorState.getRoutePriority()) {
            return;
        }

        ClientPipeConnectorState.setRoutePriority(priority);
        PacketDistributor.sendToServer(new RoutePriorityPayload(priority));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
