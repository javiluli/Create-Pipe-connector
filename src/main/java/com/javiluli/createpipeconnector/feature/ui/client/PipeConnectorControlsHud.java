package com.javiluli.createpipeconnector.feature.ui.client;

import com.javiluli.createpipeconnector.core.Constants;
import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorKeyMappings;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState;
import com.javiluli.createpipeconnector.feature.connector.client.ClientPipeConnectorState.MaterialStatus;
import com.javiluli.createpipeconnector.feature.material.PipeInventory.MaterialAvailability;
import com.javiluli.createpipeconnector.feature.material.PipeInventory.ShulkerMaterialSource;
import com.javiluli.createpipeconnector.feature.material.client.MissingMaterialsAlertHud;
import com.javiluli.createpipeconnector.feature.manual.ManualAction;
import com.javiluli.createpipeconnector.feature.manual.client.ManualActionIconResolver;
import com.javiluli.createpipeconnector.feature.manual.config.ManualAnchorClientConfig;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.ReservedMaterials;
import com.javiluli.createpipeconnector.feature.placement.client.ClientPlacementLeadPreview.ReservedStack;
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
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

/**
 * Muestra controles activos y materiales previstos sobre la barra rapida.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class PipeConnectorControlsHud {
    private static final ResourceLocation LAYER_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "pipe_connector_controls");
    private static final String PIPE_CONNECTOR_HINT = "hud.createpipeconnector.control.pipe_connector";
    private static final String START_ROUTE_HINT = "hud.createpipeconnector.control.start_route";
    private static final String CONFIRM_ROUTE_HINT = "hud.createpipeconnector.control.confirm_route";
    private static final String CANCEL_ROUTE_HINT = "hud.createpipeconnector.control.cancel_route";
    private static final String PIPE_CONNECTOR_OPTIONS_HINT = "hud.createpipeconnector.control.pipe_connector_options";
    private static final String MANUAL_TOOL_HINT = "hud.createpipeconnector.control.manual_tool";
    private static final String UNDO_MANUAL_PLACEMENT_HINT = "hud.createpipeconnector.control.undo_manual_placement";
    private static final String LOCK_PREVIEW_HINT = "hud.createpipeconnector.control.lock_preview";
    private static final String REVERSE_PUMPS_HINT = "hud.createpipeconnector.control.reverse_pumps";
    private static final int BACKGROUND_COLOR = 0xAA101010;
    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int REQUIRED_TEXT_COLOR = 0xFFFFFFFF;
    private static final int COUNT_SEPARATOR_COLOR = 0xFF9A9A9A;
    private static final int MISSING_TEXT_COLOR = 0xFFFF6666;
    private static final int SHULKER_TEXT_COLOR = 0xFF72D9FF;
    private static final int RESERVED_TEXT_COLOR = 0xFFFFC857;
    private static final int ANCHOR_INDICATOR_COLOR = 0xFFFFD84A;
    private static final float CONTROL_TEXT_SCALE = 0.75F;
    private static final int CONTROL_HOTBAR_OFFSET = 102;
    private static final int CONTROL_PANEL_PADDING = 5;
    private static final int CONTROL_LINE_GAP = 4;
    private static final int MATERIAL_ICON_SIZE = 16;
    private static final int BEACON_ICON_RENDER_SIZE = 12;
    private static final ResourceLocation BEACON_CONFIRM_SPRITE = ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "container/beacon/confirm"
    );
    private static final ResourceLocation BEACON_CANCEL_SPRITE = ResourceLocation.fromNamespaceAndPath(
            "minecraft",
            "container/beacon/cancel"
    );
    private static final int SHULKER_FIRST_OFFSET = 8;
    private static final int SHULKER_STACK_OFFSET = 5;
    private static final int MAX_VISIBLE_SHULKERS = 3;
    private static final int SHULKER_OVERFLOW_GAP = 2;
    private static final int SHULKER_LAYER_DEPTH = 20;
    private static final int MATERIAL_LAYER_DEPTH = 100;
    private static final int MATERIAL_ICON_TEXT_GAP = 2;
    private static final int MATERIAL_SECTION_GAP = 8;
    private static final int MATERIAL_HOTBAR_OFFSET = 70;

    /** Impide crear instancias del HUD. */
    private PipeConnectorControlsHud() {
    }

    /**
     * Registra el HUD del conector sobre la barra rapida de Minecraft.
     */
    @SubscribeEvent
    public static void register(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, PipeConnectorControlsHud::render);
    }

    /** Dibuja controles y materiales cuando el modo Pipe Connector esta activo. */
    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!shouldRender(minecraft)) {
            return;
        }

        Font font = minecraft.font;
        ReservedMaterials reservedMaterials = ClientPlacementLeadPreview.getReservedMaterials(minecraft.level);
        List<MaterialEntry> materialEntries = buildMaterialEntries(reservedMaterials);
        renderMinimalControls(guiGraphics, font);
        renderMaterialPanel(guiGraphics, font, materialEntries);
    }

    /** Dibuja los controles en una o dos lineas segun el ancho disponible. */
    private static void renderMinimalControls(GuiGraphics guiGraphics, Font font) {
        ManualAction manualAction = ClientPipeConnectorState.getManualAction();
        boolean routing = ClientPipeConnectorState.getSelection() != null;
        List<ControlPart> controls = new ArrayList<>();
        controls.add(ControlPart.text(hint(keyName(ClientPipeConnectorKeyMappings.togglePipeConnectorModeKey()), PIPE_CONNECTOR_HINT)));
        controls.add(ControlPart.separator());
        controls.add(ControlPart.text(hint(keyName(ClientPipeConnectorKeyMappings.openPipeConnectorOptionsKey()), PIPE_CONNECTOR_OPTIONS_HINT)));

        if (routing) {
            addManualActionControl(controls, manualAction);
            if (ClientPipeConnectorState.hasManualRouteActions()) {
                controls.add(ControlPart.separator());
                controls.add(ControlPart.text(hint(keyName(ClientPipeConnectorKeyMappings.undoLastRouteActionKey()), UNDO_MANUAL_PLACEMENT_HINT)));
            }
            controls.add(ControlPart.separator());
            controls.add(ControlPart.text(hint(keyName(ClientPipeConnectorKeyMappings.togglePreviewLockKey()), LOCK_PREVIEW_HINT)));
            if (ClientPipeConnectorState.getPumpMode().isAutomatic()
                    || !ClientPipeConnectorState.getManualPumps().isEmpty()) {
                controls.add(ControlPart.separator());
                controls.add(ControlPart.text(hint(keyName(ClientPipeConnectorKeyMappings.reversePumpDirectionKey()), REVERSE_PUMPS_HINT)));
            }
        }

        controls.add(ControlPart.separator());
        controls.add(ControlPart.text(hint(
                keyName(Minecraft.getInstance().options.keyUse),
                routing ? CONFIRM_ROUTE_HINT : START_ROUTE_HINT
        )));
        if (routing) {
            controls.add(ControlPart.separator());
            controls.add(ControlPart.text(hint(keyName(Minecraft.getInstance().options.keyAttack), CANCEL_ROUTE_HINT)));
        }

        int availableWidth = Math.max(1, guiGraphics.guiWidth() - (CONTROL_PANEL_PADDING + 4) * 2);
        List<List<ControlPart>> controlLines = splitControlLines(font, controls, availableWidth);
        int maximumLogicalWidth = controlLines.stream().mapToInt(line -> controlLineWidth(font, line)).max().orElse(1);
        float scale = Math.min(CONTROL_TEXT_SCALE, availableWidth / (float) maximumLogicalWidth);
        int width = Math.round(maximumLogicalWidth * scale);
        int renderedLineHeight = Math.round(MATERIAL_ICON_SIZE * scale);
        int totalHeight = controlLines.size() * renderedLineHeight
                + (controlLines.size() - 1) * CONTROL_LINE_GAP;
        int x = (guiGraphics.guiWidth() - width) / 2;
        int y = Math.max(
                8,
                guiGraphics.guiHeight() - CONTROL_HOTBAR_OFFSET
                        - (controlLines.size() - 1) * (renderedLineHeight + CONTROL_LINE_GAP)
        );
        guiGraphics.fill(
                x - CONTROL_PANEL_PADDING,
                y - 3,
                x + width + CONTROL_PANEL_PADDING,
                y + totalHeight + 1,
                BACKGROUND_COLOR
        );

        for (int lineIndex = 0; lineIndex < controlLines.size(); lineIndex++) {
            List<ControlPart> line = controlLines.get(lineIndex);
            int lineWidth = Math.round(controlLineWidth(font, line) * scale);
            int lineX = (guiGraphics.guiWidth() - lineWidth) / 2;
            int lineY = y + lineIndex * (renderedLineHeight + CONTROL_LINE_GAP);
            renderControlLine(guiGraphics, font, line, lineX, lineY, scale);
        }
    }

    /** Divide la linea en el separador que deja ambas mitades mas equilibradas. */
    private static List<List<ControlPart>> splitControlLines(
            Font font,
            List<ControlPart> controls,
            int availableWidth
    ) {
        int totalWidth = controlLineWidth(font, controls);
        if (totalWidth * CONTROL_TEXT_SCALE <= availableWidth) {
            return List.of(List.copyOf(controls));
        }

        int bestSeparator = -1;
        int bestMaximumWidth = Integer.MAX_VALUE;
        int leftWidth = 0;
        for (int index = 0; index < controls.size(); index++) {
            ControlPart part = controls.get(index);
            if (part.isSeparator() && index > 0 && index + 1 < controls.size()) {
                int rightWidth = totalWidth - leftWidth - part.width(font);
                int maximumWidth = Math.max(leftWidth, rightWidth);
                if (maximumWidth < bestMaximumWidth) {
                    bestMaximumWidth = maximumWidth;
                    bestSeparator = index;
                }
            }
            leftWidth += part.width(font);
        }

        if (bestSeparator < 0) {
            return List.of(List.copyOf(controls));
        }
        return List.of(
                List.copyOf(controls.subList(0, bestSeparator)),
                List.copyOf(controls.subList(bestSeparator + 1, controls.size()))
        );
    }

    /** Calcula el ancho logico de una linea antes de aplicar escala. */
    private static int controlLineWidth(Font font, List<ControlPart> controls) {
        return controls.stream().mapToInt(part -> part.width(font)).sum();
    }

    /** Dibuja una linea centrada con la escala adaptativa calculada. */
    private static void renderControlLine(
            GuiGraphics guiGraphics,
            Font font,
            List<ControlPart> controls,
            int x,
            int y,
            float scale
    ) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        try {
            int cursorX = Math.round(x / scale);
            int baselineY = Math.round(y / scale) + 3;
            for (ControlPart part : controls) {
                part.render(guiGraphics, font, cursorX, baselineY);
                cursorX += part.width(font);
            }
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    /** Anade el control contextual y su aviso de ancla auxiliar cuando procede. */
    private static void addManualActionControl(List<ControlPart> controls, ManualAction manualAction) {
        controls.add(ControlPart.separator());
        controls.add(ControlPart.text(Component.translatable(MANUAL_TOOL_HINT).getString() + ": "));
        controls.add(ControlPart.icon(ManualActionIconResolver.iconFor(manualAction)));
        controls.add(ControlPart.text(" \"" + keyName(ClientPipeConnectorKeyMappings.applyManualActionKey()) + "\""));
        if (ManualAnchorClientConfig.willCreateSupportAnchor(manualAction)) {
            controls.add(ControlPart.anchorIndicator());
        }
    }

    /** Dibuja el panel independiente con iconos y cantidades de materiales. */
    private static void renderMaterialPanel(GuiGraphics guiGraphics, Font font, List<MaterialEntry> materialEntries) {
        if (materialEntries.isEmpty()) {
            return;
        }

        int materialWidth = materialEntriesWidth(font, materialEntries);
        int availableWidth = Math.max(1, guiGraphics.guiWidth() - 16);
        float scale = Math.min(1.0F, availableWidth / (float) materialWidth);
        int renderedWidth = Math.round(materialWidth * scale);
        int x = (guiGraphics.guiWidth() - renderedWidth) / 2;
        int y = guiGraphics.guiHeight() - MATERIAL_HOTBAR_OFFSET;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1.0F);
        try {
            renderMaterialEntries(
                    guiGraphics,
                    font,
                    materialEntries,
                    Math.round(x / scale),
                    Math.round((y + 1) / scale)
            );
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    /** Comprueba si el estado actual permite mostrar el HUD. */
    private static boolean shouldRender(Minecraft minecraft) {
        return minecraft.level != null
                && minecraft.player != null
                && minecraft.screen == null
                && !minecraft.options.hideGui
                && ClientPipeConnectorState.isConnectorModeEnabled();
    }

    /** Construye las entradas visibles a partir del resumen de materiales. */
    private static List<MaterialEntry> buildMaterialEntries(ReservedMaterials reservedMaterials) {
        MaterialStatus materialStatus = ClientPipeConnectorState.getMaterialStatus();
        if (materialStatus == null && reservedMaterials.isEmpty()) {
            return List.of();
        }

        List<MaterialEntry> entries = new ArrayList<>();
        if (materialStatus != null) {
            addPrioritizedMaterialEntries(
                    entries,
                    new ItemStack(materialStatus.pipeBlock().asItem()),
                    materialStatus.pipes(),
                    materialStatus.requiredPipes(),
                    materialStatus.creative(),
                    MaterialGroup.PIPES
            );
        }
        for (ReservedStack reservedPipe : reservedMaterials.pipes()) {
            entries.add(MaterialEntry.reserved(reservedPipe.stack(), reservedPipe.count(), MaterialGroup.PIPES));
        }

        Block pumpBlock = PipeConnectorLogic.getMechanicalPumpBlock();
        if (materialStatus != null && materialStatus.requiredPumps() > 0 && pumpBlock != null) {
            addPrioritizedMaterialEntries(
                    entries,
                    new ItemStack(pumpBlock.asItem()),
                    materialStatus.pumps(),
                    materialStatus.requiredPumps(),
                    materialStatus.creative(),
                    MaterialGroup.PUMPS
            );
        }
        if (reservedMaterials.pumps() > 0 && pumpBlock != null) {
            entries.add(MaterialEntry.reserved(
                    new ItemStack(pumpBlock.asItem()),
                    reservedMaterials.pumps(),
                    MaterialGroup.PUMPS
            ));
        }

        Block casingBlock = PipeConnectorLogic.getCopperCasingBlock();
        if (materialStatus != null && materialStatus.requiredCopperCasings() > 0 && casingBlock != null) {
            addSingleSourceMaterialEntry(
                    entries,
                    new ItemStack(casingBlock.asItem()),
                    materialStatus.copperCasings(),
                    materialStatus.requiredCopperCasings(),
                    materialStatus.creative(),
                    MaterialGroup.CASING
            );
        }
        return entries;
    }

    /**
     * Divide tuberias o bombas entre inventario y shulkers siguiendo el orden de consumo.
     */
    private static void addPrioritizedMaterialEntries(
            List<MaterialEntry> entries,
            ItemStack materialStack,
            MaterialAvailability availability,
            int required,
            boolean creative,
            MaterialGroup group
    ) {
        if (creative) {
            entries.add(MaterialEntry.direct(materialStack, required, Integer.MAX_VALUE, true, false, group));
            return;
        }

        int directUsage = Math.min(required, availability.directCount());
        int shulkerRequirement = required - directUsage;
        if (shulkerRequirement <= 0) {
            entries.add(MaterialEntry.direct(
                    materialStack,
                    required,
                    availability.directCount(),
                    false,
                    false,
                    group
            ));
            return;
        }

        ShulkerUsage shulkerUsage = selectShulkerUsage(availability, shulkerRequirement);
        boolean totalMissing = directUsage + shulkerUsage.used() < required;
        if (totalMissing) {
            entries.add(MaterialEntry.direct(
                    materialStack,
                    required - shulkerUsage.used(),
                    availability.directCount(),
                    false,
                    true,
                    group
            ));
        } else if (directUsage > 0) {
            entries.add(MaterialEntry.direct(
                    materialStack,
                    directUsage,
                    availability.directCount(),
                    false,
                    false,
                    group
            ));
        }

        if (shulkerUsage.used() > 0) {
            entries.add(MaterialEntry.shulker(
                    materialStack,
                    shulkerUsage,
                    group
            ));
        }
    }

    /** Muestra el casing desde inventario o, como respaldo, desde una shulker. */
    private static void addSingleSourceMaterialEntry(
            List<MaterialEntry> entries,
            ItemStack materialStack,
            MaterialAvailability availability,
            int required,
            boolean creative,
            MaterialGroup group
    ) {
        if (creative || availability.directCount() > 0 || availability.shulkerCount() <= 0) {
            entries.add(MaterialEntry.direct(
                    materialStack,
                    required,
                    creative ? Integer.MAX_VALUE : availability.directCount(),
                    creative,
                    !creative && availability.directCount() < required,
                    group
            ));
            return;
        }

        ShulkerUsage shulkerUsage = selectShulkerUsage(availability, required);
        if (shulkerUsage.used() > 0) {
            entries.add(MaterialEntry.shulker(materialStack, shulkerUsage, group));
        }
    }

    /** Selecciona solo los shulkers necesarios siguiendo su orden de inventario. */
    private static ShulkerUsage selectShulkerUsage(MaterialAvailability availability, int required) {
        if (required <= 0 || availability.shulkerSources().isEmpty()) {
            return ShulkerUsage.EMPTY;
        }

        List<ItemStack> shulkerStacks = new ArrayList<>();
        int available = 0;
        for (ShulkerMaterialSource source : availability.shulkerSources()) {
            shulkerStacks.add(source.shulkerStack());
            available += source.count();
            if (available >= required) {
                break;
            }
        }
        return new ShulkerUsage(shulkerStacks, Math.min(required, available), available);
    }

    /** Calcula el ancho total necesario para centrar las entradas. */
    private static int materialEntriesWidth(Font font, List<MaterialEntry> entries) {
        if (entries.isEmpty()) {
            return 0;
        }

        int width = 0;
        for (int index = 0; index < entries.size(); index++) {
            MaterialEntry entry = entries.get(index);
            width += entry.iconWidth(font) + MATERIAL_ICON_TEXT_GAP + entry.statusWidth(font);
            if (index + 1 < entries.size()) {
                MaterialEntry nextEntry = entries.get(index + 1);
                width += entry.group() == nextEntry.group()
                        ? MATERIAL_SECTION_GAP
                        : MATERIAL_SECTION_GAP * 2 + font.width("|");
            }
        }
        return width;
    }

    /** Dibuja los iconos, cantidades y colores de disponibilidad. */
    private static void renderMaterialEntries(GuiGraphics guiGraphics, Font font, List<MaterialEntry> entries, int x, int y) {
        int entryX = x;
        for (int index = 0; index < entries.size(); index++) {
            MaterialEntry entry = entries.get(index);
            renderMaterialIcon(guiGraphics, font, entry, entryX, y);
            int textX = entryX + entry.iconWidth(font) + MATERIAL_ICON_TEXT_GAP;
            renderMaterialStatus(guiGraphics, font, entry, textX, y);
            entryX = textX + entry.statusWidth(font);
            if (index + 1 >= entries.size()) {
                continue;
            }

            MaterialEntry nextEntry = entries.get(index + 1);
            if (entry.group() == nextEntry.group()) {
                entryX += MATERIAL_SECTION_GAP;
                continue;
            }

            entryX += MATERIAL_SECTION_GAP;
            guiGraphics.drawString(font, "|", entryX, y + 4, TEXT_COLOR, true);
            entryX += font.width("|") + MATERIAL_SECTION_GAP;
        }
    }

    /** Dibuja el contador o el boton vanilla del beacon usado por el casing. */
    private static void renderMaterialStatus(
            GuiGraphics guiGraphics,
            Font font,
            MaterialEntry entry,
            int x,
            int y
    ) {
        if (entry.group() != MaterialGroup.CASING) {
            renderMaterialCount(guiGraphics, font, entry, x, y + 5);
            return;
        }

        guiGraphics.blitSprite(
                entry.hasEnough() ? BEACON_CONFIRM_SPRITE : BEACON_CANCEL_SPRITE,
                x,
                y + 2,
                BEACON_ICON_RENDER_SIZE,
                BEACON_ICON_RENDER_SIZE
        );
    }

    /** Dibuja por separado cantidad necesaria, separador y fuente disponible. */
    private static void renderMaterialCount(GuiGraphics guiGraphics, Font font, MaterialEntry entry, int x, int y) {
        if (entry.source() == MaterialSource.RESERVED) {
            guiGraphics.drawString(font, entry.countText(), x, y, RESERVED_TEXT_COLOR, true);
            return;
        }

        String requiredText = Integer.toString(entry.required());
        String availableText = entry.creative() ? "\u221E" : Integer.toString(entry.available());
        int requiredColor = entry.missing()
                ? MissingMaterialsAlertHud.missingMaterialColor(MISSING_TEXT_COLOR)
                : REQUIRED_TEXT_COLOR;
        int availableColor = entry.source() == MaterialSource.SHULKER
                ? SHULKER_TEXT_COLOR
                : TEXT_COLOR;

        guiGraphics.drawString(font, requiredText, x, y, requiredColor, true);
        int separatorX = x + font.width(requiredText);
        guiGraphics.drawString(font, "/", separatorX, y, COUNT_SEPARATOR_COLOR, true);
        guiGraphics.drawString(font, availableText, separatorX + font.width("/"), y, availableColor, true);
    }

    /** Dibuja el material a tamano completo delante de sus shulkers solapados. */
    private static void renderMaterialIcon(GuiGraphics guiGraphics, Font font, MaterialEntry entry, int x, int y) {
        if (entry.shulkerStacks().isEmpty()) {
            guiGraphics.renderItem(entry.stack(), x, y);
            return;
        }

        int visibleShulkers = entry.visibleShulkerCount();
        for (int index = visibleShulkers - 1; index >= 0; index--) {
            int shulkerX = x + SHULKER_FIRST_OFFSET + index * SHULKER_STACK_OFFSET;
            int depth = (visibleShulkers - index) * SHULKER_LAYER_DEPTH;
            renderItemAtDepth(guiGraphics, entry.shulkerStacks().get(index), shulkerX, y, depth);
        }

        renderItemAtDepth(guiGraphics, entry.stack(), x, y, MATERIAL_LAYER_DEPTH);

        if (entry.hiddenShulkerCount() > 0) {
            int overflowX = x + entry.shulkerStackWidth() + SHULKER_OVERFLOW_GAP;
            guiGraphics.drawString(font, "+" + entry.hiddenShulkerCount(), overflowX, y + 5, SHULKER_TEXT_COLOR, true);
        }
    }

    /** Dibuja un item en una capa concreta para evitar mezclar modelos solapados. */
    private static void renderItemAtDepth(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int depth) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, depth);
        try {
            guiGraphics.renderItem(stack, x, y);
        } finally {
            guiGraphics.pose().popPose();
        }
    }

    /** Crea una pista localizada con el nombre de su tecla. */
    private static String hint(String keyName, String actionTranslationKey) {
        return Component.translatable(actionTranslationKey).getString() + ": \"" + keyName + "\"";
    }

    /** Devuelve el nombre visible de una asignacion de tecla. */
    private static String keyName(KeyMapping keyMapping) {
        return keyMapping.getTranslatedKeyMessage().getString();
    }

    /** Elemento de texto o icono empleado para componer la linea de controles. */
    private record ControlPart(String text, ItemStack icon, int color) {
        /** Crea un tramo de texto. */
        private static ControlPart text(String text) {
            return new ControlPart(text, ItemStack.EMPTY, TEXT_COLOR);
        }

        /** Crea el separador uniforme entre controles. */
        private static ControlPart separator() {
            return text("  |  ");
        }

        /** Crea un icono de accion manual. */
        private static ControlPart icon(ItemStack icon) {
            return new ControlPart("", icon == null ? ItemStack.EMPTY : icon, TEXT_COLOR);
        }

        /** Crea la marca compacta que avisa de un ancla auxiliar. */
        private static ControlPart anchorIndicator() {
            return new ControlPart(" +A", ItemStack.EMPTY, ANCHOR_INDICATOR_COLOR);
        }

        /** Indica si este tramo puede utilizarse como punto de corte entre lineas. */
        private boolean isSeparator() {
            return icon.isEmpty() && "  |  ".equals(text);
        }

        /** Devuelve el ancho logico previo a aplicar la escala del HUD. */
        private int width(Font font) {
            return icon.isEmpty() ? font.width(text) : MATERIAL_ICON_SIZE;
        }

        /** Dibuja el elemento manteniendo alineados texto e icono. */
        private void render(GuiGraphics guiGraphics, Font font, int x, int baselineY) {
            if (icon.isEmpty()) {
                guiGraphics.drawString(font, text, x, baselineY, color, true);
                return;
            }
            guiGraphics.renderItem(icon, x, baselineY - 4);
        }
    }

    /** Representa un material y las cantidades mostradas por el HUD. */
    private record MaterialEntry(
            ItemStack stack,
            List<ItemStack> shulkerStacks,
            int required,
            int available,
            boolean creative,
            boolean missing,
            MaterialSource source,
            MaterialGroup group
    ) {
        /** Crea una entrada procedente directamente del inventario. */
        private static MaterialEntry direct(
                ItemStack stack,
                int required,
                int available,
                boolean creative,
                boolean missing,
                MaterialGroup group
        ) {
            return new MaterialEntry(
                    stack,
                    List.of(),
                    required,
                    available,
                    creative,
                    missing,
                    MaterialSource.INVENTORY,
                    group
            );
        }

        /** Crea una entrada agregada para las shulkers que aportan material. */
        private static MaterialEntry shulker(ItemStack stack, ShulkerUsage usage, MaterialGroup group) {
            return new MaterialEntry(
                    stack,
                    usage.shulkerStacks(),
                    usage.used(),
                    usage.available(),
                    false,
                    false,
                    MaterialSource.SHULKER,
                    group
            );
        }

        /** Crea una entrada para materiales ya consumidos por una ruta activa. */
        private static MaterialEntry reserved(ItemStack stack, int count, MaterialGroup group) {
            return new MaterialEntry(
                    stack,
                    List.of(),
                    0,
                    count,
                    false,
                    false,
                    MaterialSource.RESERVED,
                    group
            );
        }

        /** Devuelve el numero de shulkers dibujados individualmente. */
        private int visibleShulkerCount() {
            return Math.min(shulkerStacks.size(), MAX_VISIBLE_SHULKERS);
        }

        /** Devuelve cuantos shulkers quedan resumidos mediante el indicador +N. */
        private int hiddenShulkerCount() {
            return Math.max(0, shulkerStacks.size() - MAX_VISIBLE_SHULKERS);
        }

        /** Calcula el ancho ocupado por material, shulkers visibles y resumen. */
        private int iconWidth(Font font) {
            if (shulkerStacks.isEmpty()) {
                return MATERIAL_ICON_SIZE;
            }

            int width = shulkerStackWidth();
            if (hiddenShulkerCount() > 0) {
                width += SHULKER_OVERFLOW_GAP + font.width("+" + hiddenShulkerCount());
            }
            return width;
        }

        /** Calcula el ancho del grupo de iconos solapados. */
        private int shulkerStackWidth() {
            return SHULKER_FIRST_OFFSET
                    + (visibleShulkerCount() - 1) * SHULKER_STACK_OFFSET
                    + MATERIAL_ICON_SIZE;
        }

        /** Calcula el ancho del contador o del boton vanilla del beacon. */
        private int statusWidth(Font font) {
            return group == MaterialGroup.CASING ? BEACON_ICON_RENDER_SIZE : font.width(countText());
        }

        /** Comprueba si esta entrada cubre el material solicitado. */
        private boolean hasEnough() {
            return creative || available >= required;
        }

        /** Formatea el contador requerido/disponible para el jugador. */
        private String countText() {
            if (source == MaterialSource.RESERVED) {
                return "R:" + available;
            }
            return required + "/" + (creative ? "\u221E" : available);
        }
    }

    /** Resume las shulkers necesarias y la cantidad que pueden aportar. */
    private record ShulkerUsage(List<ItemStack> shulkerStacks, int used, int available) {
        private static final ShulkerUsage EMPTY = new ShulkerUsage(List.of(), 0, 0);

        /** Conserva una lista inmutable para el render del HUD. */
        private ShulkerUsage {
            shulkerStacks = List.copyOf(shulkerStacks);
        }
    }

    /** Identifica los grupos separados visualmente dentro del HUD. */
    private enum MaterialGroup {
        PIPES,
        PUMPS,
        CASING
    }

    /** Origen visual de la cantidad disponible mostrada al jugador. */
    private enum MaterialSource {
        INVENTORY,
        SHULKER,
        RESERVED
    }
}
