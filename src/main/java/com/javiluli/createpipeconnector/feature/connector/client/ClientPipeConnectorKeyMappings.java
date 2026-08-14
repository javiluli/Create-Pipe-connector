package com.javiluli.createpipeconnector.feature.connector.client;

import com.javiluli.createpipeconnector.core.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Declara los controles configurables y permite consumir sus pulsaciones.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ClientPipeConnectorKeyMappings {
    private static final KeyMapping TOGGLE_PIPE_CONNECTOR_MODE = new KeyMapping(
            Constants.TOGGLE_PIPE_CONNECTOR_MODE,
            GLFW.GLFW_KEY_B,
            Constants.CATEGORY
    );
    private static final KeyMapping TOGGLE_PREVIEW_LOCK = new KeyMapping(
            Constants.TOGGLE_PREVIEW_LOCK,
            GLFW.GLFW_KEY_LEFT_ALT,
            Constants.CATEGORY
    );
    private static final KeyMapping APPLY_MANUAL_ACTION = new KeyMapping(
            Constants.APPLY_MANUAL_ACTION,
            GLFW.GLFW_KEY_C,
            Constants.CATEGORY
    );
    private static final KeyMapping CYCLE_MANUAL_ACTION = new KeyMapping(
            Constants.CYCLE_MANUAL_ACTION,
            GLFW.GLFW_KEY_UNKNOWN,
            Constants.CATEGORY
    );
    private static final KeyMapping UNDO_LAST_ROUTE_ACTION = new KeyMapping(
            Constants.UNDO_LAST_ROUTE_ACTION,
            GLFW.GLFW_KEY_V,
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
    private static final KeyMapping CYCLE_PUMP_MODE = new KeyMapping(
            Constants.CYCLE_PUMP_MODE,
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
    private static final KeyMapping REVERSE_PUMP_DIRECTION = new KeyMapping(
            Constants.REVERSE_PUMP_DIRECTION,
            GLFW.GLFW_KEY_R,
            Constants.CATEGORY
    );
    private static final KeyMapping OPEN_PIPE_CONNECTOR_OPTIONS = new KeyMapping(
            Constants.OPEN_PIPE_CONNECTOR_OPTIONS,
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
        event.register(TOGGLE_PIPE_CONNECTOR_MODE);
        event.register(TOGGLE_PREVIEW_LOCK);
        event.register(APPLY_MANUAL_ACTION);
        event.register(CYCLE_MANUAL_ACTION);
        event.register(UNDO_LAST_ROUTE_ACTION);
        event.register(TOGGLE_COPPER_CASING);
        event.register(REMOVE_LAST_COPPER_CASING);
        event.register(TOGGLE_MANUAL_PUMP);
        event.register(REMOVE_LAST_MANUAL_PUMP);
        event.register(CYCLE_PUMP_MODE);
        event.register(CYCLE_COPPER_CASING_MODE);
        event.register(CYCLE_PIPE_STYLE_MODE);
        event.register(REVERSE_PUMP_DIRECTION);
        event.register(OPEN_PIPE_CONNECTOR_OPTIONS);
    }

    /** Consume una pulsacion del interruptor del modo Pipe Connector. */
    public static boolean consumePipeConnectorModeToggle() {
        return TOGGLE_PIPE_CONNECTOR_MODE.consumeClick();
    }

    /** Consume una pulsacion del bloqueo de preview. */
    public static boolean consumePreviewLockToggle() {
        return TOGGLE_PREVIEW_LOCK.consumeClick();
    }

    /** Consume una pulsacion para aplicar la accion manual seleccionada. */
    public static boolean consumeApplyManualAction() {
        return APPLY_MANUAL_ACTION.consumeClick();
    }

    /** Consume una pulsacion para cambiar rapidamente la herramienta manual. */
    public static boolean consumeManualActionCycle() {
        return CYCLE_MANUAL_ACTION.consumeClick();
    }

    /** Consume una pulsacion para deshacer la ultima accion de la ruta. */
    public static boolean consumeUndoLastRouteAction() {
        return UNDO_LAST_ROUTE_ACTION.consumeClick();
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

    /** Consume una pulsacion para recorrer los modos de bombas automaticas. */
    public static boolean consumePumpModeCycle() {
        return CYCLE_PUMP_MODE.consumeClick();
    }

    /** Consume una pulsacion para cambiar el modo de revestimiento. */
    public static boolean consumeCopperCasingModeCycle() {
        return CYCLE_COPPER_CASING_MODE.consumeClick();
    }

    /** Consume una pulsacion para cambiar el estilo de tuberias. */
    public static boolean consumePipeStyleModeCycle() {
        return CYCLE_PIPE_STYLE_MODE.consumeClick();
    }

    /** Consume una pulsacion para invertir las bombas de la ruta. */
    public static boolean consumePumpDirectionReverse() {
        return REVERSE_PUMP_DIRECTION.consumeClick();
    }

    /** Consume una pulsacion para abrir las opciones de Pipe Connector. */
    public static boolean consumeOpenPipeConnectorOptions() {
        return OPEN_PIPE_CONNECTOR_OPTIONS.consumeClick();
    }

    /** Devuelve el control que activa el modo Pipe Connector. */
    public static KeyMapping togglePipeConnectorModeKey() {
        return TOGGLE_PIPE_CONNECTOR_MODE;
    }

    /** Devuelve el control que bloquea el preview. */
    public static KeyMapping togglePreviewLockKey() {
        return TOGGLE_PREVIEW_LOCK;
    }

    /** Devuelve el control contextual que aplica la accion manual. */
    public static KeyMapping applyManualActionKey() {
        return APPLY_MANUAL_ACTION;
    }

    /** Devuelve el control que deshace la ultima accion de la ruta. */
    public static KeyMapping undoLastRouteActionKey() {
        return UNDO_LAST_ROUTE_ACTION;
    }

    /** Devuelve el control que invierte todas las bombas de la ruta. */
    public static KeyMapping reversePumpDirectionKey() {
        return REVERSE_PUMP_DIRECTION;
    }

    /** Devuelve el control que abre las opciones de Pipe Connector. */
    public static KeyMapping openPipeConnectorOptionsKey() {
        return OPEN_PIPE_CONNECTOR_OPTIONS;
    }

    /** Descarta pulsaciones pendientes para evitar acciones al cambiar de contexto. */
    public static void drainPlacementClicks() {
        while (consumePipeConnectorModeToggle()) {
        }
        drainRouteClicks();
    }

    /** Descarta pulsaciones pendientes que solo tienen sentido dentro del modo. */
    public static void drainRouteClicks() {
        while (consumePreviewLockToggle()) {
        }
        while (consumeApplyManualAction()) {
        }
        while (consumeManualActionCycle()) {
        }
        while (consumeUndoLastRouteAction()) {
        }
        while (consumeCopperCasingToggle()) {
        }
        while (consumeRemoveLastCopperCasing()) {
        }
        while (consumeManualPumpToggle()) {
        }
        while (consumeRemoveLastManualPump()) {
        }
        while (consumePumpModeCycle()) {
        }
        while (consumeCopperCasingModeCycle()) {
        }
        while (consumePipeStyleModeCycle()) {
        }
        while (consumePumpDirectionReverse()) {
        }
        while (consumeOpenPipeConnectorOptions()) {
        }
    }
}
