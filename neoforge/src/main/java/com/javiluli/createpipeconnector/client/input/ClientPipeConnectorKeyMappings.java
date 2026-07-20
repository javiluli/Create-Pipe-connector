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

    private ClientPipeConnectorKeyMappings() {
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_PREVIEW_LOCK);
        event.register(ADD_ANCHOR);
        event.register(REMOVE_LAST_ANCHOR);
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

    public static void drainPlacementClicks() {
        while (consumePreviewLockToggle()) {
        }
        while (consumeAddAnchor()) {
        }
        while (consumeRemoveLastAnchor()) {
        }
    }
}
