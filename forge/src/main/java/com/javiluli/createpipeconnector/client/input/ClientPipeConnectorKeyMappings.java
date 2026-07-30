package com.javiluli.createpipeconnector.client.input;

import com.javiluli.createpipeconnector.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Declares configurable Minecraft controls and exposes consume helpers used by
 * the client input loop.
 */
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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
    private ClientPipeConnectorKeyMappings() {
    }

    /**
     * Registers every connector action in Minecraft's controls screen.
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
