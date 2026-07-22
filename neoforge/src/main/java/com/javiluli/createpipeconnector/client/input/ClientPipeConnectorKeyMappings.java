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
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
    private static final KeyMapping TOGGLE_AUTO_PUMPS = new KeyMapping(
            "key.createpipeconnector.toggle_auto_pumps",
            GLFW.GLFW_KEY_P,
            CATEGORY
    );
    private static final KeyMapping REVERSE_AUTO_PUMP_DIRECTION = new KeyMapping(
            "key.createpipeconnector.reverse_auto_pump_direction",
            GLFW.GLFW_KEY_R,
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
        event.register(TOGGLE_AUTO_PUMPS);
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

    public static boolean consumeAutoPumpsToggle() {
        return TOGGLE_AUTO_PUMPS.consumeClick();
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

    public static KeyMapping toggleAutoPumpsKey() {
        return TOGGLE_AUTO_PUMPS;
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
        while (consumeAutoPumpsToggle()) {
        }
        while (consumeAutoPumpDirectionReverse()) {
        }
        while (consumeRoutePriorityCycle()) {
        }
    }
}
