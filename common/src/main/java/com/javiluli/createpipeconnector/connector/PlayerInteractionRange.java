package com.javiluli.createpipeconnector.connector;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Resolves the player's block interaction range across the supported Minecraft
 * and Forge APIs.
 */
final class PlayerInteractionRange {
    private static final double DEFAULT_BLOCK_REACH = 5.0D;
    private static final Method MODERN_RANGE_METHOD = findModernRangeMethod();
    private static final Attribute FORGE_BLOCK_REACH_ATTRIBUTE = findForgeBlockReachAttribute();

    private PlayerInteractionRange() {
    }

    /**
     * Returns the effective block interaction range and falls back to vanilla's
     * historical five-block reach when neither API is available.
     */
    static double resolve(Player player) {
        Double modernRange = invokeModernRange(player);
        if (modernRange != null) {
            return modernRange;
        }
        return FORGE_BLOCK_REACH_ATTRIBUTE == null
                ? DEFAULT_BLOCK_REACH
                : player.getAttributeValue(FORGE_BLOCK_REACH_ATTRIBUTE);
    }

    private static Double invokeModernRange(Player player) {
        if (MODERN_RANGE_METHOD == null) {
            return null;
        }
        try {
            Object value = MODERN_RANGE_METHOD.invoke(player);
            return value instanceof Number number ? number.doubleValue() : null;
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Method findModernRangeMethod() {
        try {
            return Player.class.getMethod("blockInteractionRange");
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Forge 1.20.1 exposes block reach through a registry object while newer
     * loaders expose a direct player method. Reflection keeps the shared module
     * independent from either loader at compile time.
     */
    private static Attribute findForgeBlockReachAttribute() {
        try {
            Class<?> forgeMod = Class.forName("net.minecraftforge.common.ForgeMod");
            Object registryObject = forgeRegistryObject(forgeMod);
            if (registryObject == null) {
                return null;
            }
            Method get = registryObject.getClass().getMethod("get");
            Object attribute = get.invoke(registryObject);
            return attribute instanceof Attribute blockReachAttribute ? blockReachAttribute : null;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    private static Object forgeRegistryObject(Class<?> forgeMod)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            Field blockReach = forgeMod.getField("BLOCK_REACH");
            return blockReach.get(null);
        } catch (NoSuchFieldException ignored) {
            Method blockReach = forgeMod.getMethod("BLOCK_REACH");
            return blockReach.invoke(null);
        }
    }
}
