package com.javiluli.createpipeconnector.feature.connector.client;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Registra las teclas configurables del modo conector.
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public final class ClientPipeConnectorKeyMappings {
    private static final KeyMapping TOGGLE_CONNECTOR_MODE = new KeyMapping(
            Constants.TOGGLE_CONNECTOR_MODE,
            GLFW.GLFW_KEY_B,
            Constants.CATEGORY
    );
    private static final KeyMapping TOGGLE_PREVIEW_LOCK = new KeyMapping(
            Constants.TOGGLE_PREVIEW_LOCK,
            GLFW.GLFW_KEY_LEFT_ALT,
            Constants.CATEGORY
    );
    private static final KeyMapping ADD_ANCHOR = new KeyMapping(
            Constants.ADD_ANCHOR,
            GLFW.GLFW_KEY_C,
            Constants.CATEGORY
    );
    private static final KeyMapping REMOVE_LAST_ANCHOR = new KeyMapping(
            Constants.REMOVE_LAST_ANCHOR,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping TOGGLE_COPPER_CASING = new KeyMapping(
            Constants.TOGGLE_COPPER_CASING,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping REMOVE_LAST_COPPER_CASING = new KeyMapping(
            Constants.REMOVE_LAST_COPPER_CASING,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping TOGGLE_MANUAL_PUMP = new KeyMapping(
            Constants.TOGGLE_MANUAL_PUMP,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping REMOVE_LAST_MANUAL_PUMP = new KeyMapping(
            Constants.REMOVE_LAST_MANUAL_PUMP,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping TOGGLE_AUTO_PUMPS = new KeyMapping(
            Constants.TOGGLE_AUTO_PUMPS,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping CYCLE_COPPER_CASING_MODE = new KeyMapping(
            Constants.CYCLE_COPPER_CASING_MODE,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping CYCLE_PIPE_STYLE_MODE = new KeyMapping(
            Constants.CYCLE_PIPE_STYLE_MODE,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping REVERSE_AUTO_PUMP_DIRECTION = new KeyMapping(
            Constants.REVERSE_AUTO_PUMP_DIRECTION,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping CYCLE_ROUTE_PRIORITY = new KeyMapping(
            Constants.CYCLE_ROUTE_PRIORITY,
            GLFW.GLFW_KEY_N,
            Constants.CATEGORY
    );
    /** Impide crear instancias del registro de controles. */
    private ClientPipeConnectorKeyMappings() {
    }

    /**
     * Registra todas las acciones en la pantalla de controles de Minecraft.
     */
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CONNECTOR_MODE);
        event.register(TOGGLE_PREVIEW_LOCK);
        event.register(ADD_ANCHOR);
        event.register(REMOVE_LAST_ANCHOR);
        event.register(TOGGLE_COPPER_CASING);
        event.register(REMOVE_LAST_COPPER_CASING);
        event.register(TOGGLE_MANUAL_PUMP);
        event.register(REMOVE_LAST_MANUAL_PUMP);
        event.register(TOGGLE_AUTO_PUMPS);
        event.register(CYCLE_COPPER_CASING_MODE);
        event.register(CYCLE_PIPE_STYLE_MODE);
        event.register(REVERSE_AUTO_PUMP_DIRECTION);
        event.register(CYCLE_ROUTE_PRIORITY);
    }

    /** Consume una pulsacion del interruptor del modo conector. */
    public static boolean consumeConnectorModeToggle() {
        return TOGGLE_CONNECTOR_MODE.consumeClick();
    }

    /** Consume una pulsacion del bloqueo de preview. */
    public static boolean consumePreviewLockToggle() {
        return TOGGLE_PREVIEW_LOCK.consumeClick();
    }

    /** Consume una pulsacion para anadir un ancla. */
    public static boolean consumeAddAnchor() {
        return ADD_ANCHOR.consumeClick();
    }

    /** Consume una pulsacion para retirar la ultima ancla. */
    public static boolean consumeRemoveLastAnchor() {
        return REMOVE_LAST_ANCHOR.consumeClick();
    }

    /** Consume una pulsacion para alternar revestimiento manual. */
    public static boolean consumeCopperCasingToggle() {
        return TOGGLE_COPPER_CASING.consumeClick();
    }

    /** Consume una pulsacion para alternar una bomba manual. */
    public static boolean consumeManualPumpToggle() {
        return TOGGLE_MANUAL_PUMP.consumeClick();
    }

    /** Consume una pulsacion para retirar la ultima bomba manual. */
    public static boolean consumeRemoveLastManualPump() {
        return REMOVE_LAST_MANUAL_PUMP.consumeClick();
    }

    /** Consume una pulsacion para retirar el ultimo revestimiento manual. */
    public static boolean consumeRemoveLastCopperCasing() {
        return REMOVE_LAST_COPPER_CASING.consumeClick();
    }

    /** Consume una pulsacion del ajuste heredado de bombas automaticas. */
    public static boolean consumeAutoPumpsToggle() {
        return TOGGLE_AUTO_PUMPS.consumeClick();
    }

    /** Consume una pulsacion para cambiar el modo de revestimiento. */
    public static boolean consumeCopperCasingModeCycle() {
        return CYCLE_COPPER_CASING_MODE.consumeClick();
    }

    /** Consume una pulsacion para cambiar el estilo de tuberias. */
    public static boolean consumePipeStyleModeCycle() {
        return CYCLE_PIPE_STYLE_MODE.consumeClick();
    }

    /** Consume una pulsacion para invertir las bombas automaticas. */
    public static boolean consumeAutoPumpDirectionReverse() {
        return REVERSE_AUTO_PUMP_DIRECTION.consumeClick();
    }

    /** Consume una pulsacion para cambiar la prioridad de ruta. */
    public static boolean consumeRoutePriorityCycle() {
        return CYCLE_ROUTE_PRIORITY.consumeClick();
    }

    /** Devuelve el control que activa el modo conector. */
    public static KeyMapping toggleConnectorModeKey() {
        return TOGGLE_CONNECTOR_MODE;
    }

    /** Devuelve el control que bloquea el preview. */
    public static KeyMapping togglePreviewLockKey() {
        return TOGGLE_PREVIEW_LOCK;
    }

    /** Devuelve el control que anade anclas. */
    public static KeyMapping addAnchorKey() {
        return ADD_ANCHOR;
    }

    /** Devuelve el control que abre o recorre las opciones de ruta. */
    public static KeyMapping cycleRoutePriorityKey() {
        return CYCLE_ROUTE_PRIORITY;
    }

    /** Descarta pulsaciones pendientes para evitar acciones al cambiar de contexto. */
    public static void drainPlacementClicks() {
        while (consumeConnectorModeToggle()) {
        }
        while (consumePreviewLockToggle()) {
        }
        while (consumeAddAnchor()) {
        }
        while (consumeRemoveLastAnchor()) {
        }
        while (consumeCopperCasingToggle()) {
        }
        while (consumeRemoveLastCopperCasing()) {
        }
        while (consumeManualPumpToggle()) {
        }
        while (consumeRemoveLastManualPump()) {
        }
        while (consumeAutoPumpsToggle()) {
        }
        while (consumeCopperCasingModeCycle()) {
        }
        while (consumePipeStyleModeCycle()) {
        }
        while (consumeAutoPumpDirectionReverse()) {
        }
        while (consumeRoutePriorityCycle()) {
        }
    }
}
