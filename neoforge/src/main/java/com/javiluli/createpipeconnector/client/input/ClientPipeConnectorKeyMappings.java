package com.javiluli.createpipeconnector.client.input;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientPipeConnectorKeyMappings {
    private static final String CATEGORY = "key.categories.createpipeconnector";

    private static final KeyMapping TOGGLE_CONNECTOR_MODE = new KeyMapping(
            "key.createpipeconnector.toggle_connector_mode",
            GLFW.GLFW_KEY_B,
            CATEGORY
    );
    private static final KeyMapping TOGGLE_PREVIEW_LOCK = new KeyMapping(
            "key.createpipeconnector.toggle_preview_lock",
            GLFW.GLFW_KEY_LEFT_ALT,
            CATEGORY
    );
    private static final KeyMapping ADD_ANCHOR = new KeyMapping(
            "key.createpipeconnector.add_anchor",
            GLFW.GLFW_KEY_C,
            CATEGORY
    );
    private static final KeyMapping REMOVE_LAST_ANCHOR = new KeyMapping(
            "key.createpipeconnector.remove_last_anchor",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping TOGGLE_COPPER_CASING = new KeyMapping(
            "key.createpipeconnector.toggle_copper_casing",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping REMOVE_LAST_COPPER_CASING = new KeyMapping(
            "key.createpipeconnector.remove_last_copper_casing",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping TOGGLE_MANUAL_PUMP = new KeyMapping(
            "key.createpipeconnector.toggle_manual_pump",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping REMOVE_LAST_MANUAL_PUMP = new KeyMapping(
            "key.createpipeconnector.remove_last_manual_pump",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping TOGGLE_AUTO_PUMPS = new KeyMapping(
            "key.createpipeconnector.toggle_auto_pumps",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping CYCLE_COPPER_CASING_MODE = new KeyMapping(
            "key.createpipeconnector.cycle_copper_casing_mode",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping CYCLE_PIPE_STYLE_MODE = new KeyMapping(
            "key.createpipeconnector.cycle_pipe_style_mode",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping REVERSE_AUTO_PUMP_DIRECTION = new KeyMapping(
            "key.createpipeconnector.reverse_auto_pump_direction",
            GLFW.GLFW_KEY_UNKNOWN,
            CATEGORY
    );
    private static final KeyMapping CYCLE_ROUTE_PRIORITY = new KeyMapping(
            "key.createpipeconnector.cycle_route_priority",
            GLFW.GLFW_KEY_N,
            CATEGORY
    );
    private ClientPipeConnectorKeyMappings() {
    }

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

    public static boolean consumeConnectorModeToggle() {
        return TOGGLE_CONNECTOR_MODE.consumeClick();
    }

    public static boolean consumePreviewLockToggle() {
        return TOGGLE_PREVIEW_LOCK.consumeClick();
    }

    public static boolean consumeAddAnchor() {
        return ADD_ANCHOR.consumeClick();
    }

    public static boolean consumeRemoveLastAnchor() {
        return REMOVE_LAST_ANCHOR.consumeClick();
    }

    public static boolean consumeCopperCasingToggle() {
        return TOGGLE_COPPER_CASING.consumeClick();
    }

    public static boolean consumeManualPumpToggle() {
        return TOGGLE_MANUAL_PUMP.consumeClick();
    }

    public static boolean consumeRemoveLastManualPump() {
        return REMOVE_LAST_MANUAL_PUMP.consumeClick();
    }

    public static boolean consumeRemoveLastCopperCasing() {
        return REMOVE_LAST_COPPER_CASING.consumeClick();
    }

    public static boolean consumeAutoPumpsToggle() {
        return TOGGLE_AUTO_PUMPS.consumeClick();
    }

    public static boolean consumeCopperCasingModeCycle() {
        return CYCLE_COPPER_CASING_MODE.consumeClick();
    }

    public static boolean consumePipeStyleModeCycle() {
        return CYCLE_PIPE_STYLE_MODE.consumeClick();
    }

    public static boolean consumeAutoPumpDirectionReverse() {
        return REVERSE_AUTO_PUMP_DIRECTION.consumeClick();
    }

    public static boolean consumeRoutePriorityCycle() {
        return CYCLE_ROUTE_PRIORITY.consumeClick();
    }

    public static KeyMapping toggleConnectorModeKey() {
        return TOGGLE_CONNECTOR_MODE;
    }

    public static KeyMapping togglePreviewLockKey() {
        return TOGGLE_PREVIEW_LOCK;
    }

    public static KeyMapping addAnchorKey() {
        return ADD_ANCHOR;
    }

    public static KeyMapping removeLastAnchorKey() {
        return REMOVE_LAST_ANCHOR;
    }

    public static KeyMapping toggleCopperCasingKey() {
        return TOGGLE_COPPER_CASING;
    }

    public static KeyMapping toggleManualPumpKey() {
        return TOGGLE_MANUAL_PUMP;
    }

    public static KeyMapping removeLastManualPumpKey() {
        return REMOVE_LAST_MANUAL_PUMP;
    }

    public static KeyMapping removeLastCopperCasingKey() {
        return REMOVE_LAST_COPPER_CASING;
    }

    public static KeyMapping toggleAutoPumpsKey() {
        return TOGGLE_AUTO_PUMPS;
    }

    public static KeyMapping cycleCopperCasingModeKey() {
        return CYCLE_COPPER_CASING_MODE;
    }

    public static KeyMapping cyclePipeStyleModeKey() {
        return CYCLE_PIPE_STYLE_MODE;
    }

    public static KeyMapping reverseAutoPumpDirectionKey() {
        return REVERSE_AUTO_PUMP_DIRECTION;
    }

    public static KeyMapping cycleRoutePriorityKey() {
        return CYCLE_ROUTE_PRIORITY;
    }

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
