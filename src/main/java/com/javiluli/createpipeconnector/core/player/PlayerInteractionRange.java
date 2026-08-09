package com.javiluli.createpipeconnector.core.player;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Resuelve el alcance de interaccion con bloques entre las API compatibles de Forge.
 */
public final class PlayerInteractionRange {
    private static final String FORGE_MOD_CLASS = "net.minecraftforge.common.ForgeMod";
    private static final String BLOCK_INTERACTION_RANGE_METHOD = "blockInteractionRange";
    private static final String BLOCK_REACH_MEMBER = "BLOCK_REACH";
    private static final String REGISTRY_GET_METHOD = "get";
    private static final double DEFAULT_BLOCK_REACH = 5.0D;
    private static final Method MODERN_RANGE_METHOD = findModernRangeMethod();
    private static final Attribute FORGE_BLOCK_REACH_ATTRIBUTE = findForgeBlockReachAttribute();

    /** Impide crear instancias del adaptador de alcance. */
    private PlayerInteractionRange() {
    }

    /**
     * Devuelve el alcance efectivo o utiliza el alcance historico de Minecraft
     * cuando ninguna API compatible esta disponible.
     */
    public static double resolve(Player player) {
        Double modernRange = invokeModernRange(player);
        if (modernRange != null) {
            return modernRange;
        }
        return FORGE_BLOCK_REACH_ATTRIBUTE == null
                ? DEFAULT_BLOCK_REACH
                : player.getAttributeValue(FORGE_BLOCK_REACH_ATTRIBUTE);
    }

    /** Intenta consultar el metodo moderno de alcance del jugador. */
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

    /** Localiza el metodo moderno sin introducir una dependencia obligatoria. */
    private static Method findModernRangeMethod() {
        try {
            return Player.class.getMethod(BLOCK_INTERACTION_RANGE_METHOD);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Localiza el atributo de alcance expuesto mediante registro en Forge 1.20.1.
     * La reflexion mantiene el modulo comun independiente de Forge al compilar.
     */
    private static Attribute findForgeBlockReachAttribute() {
        try {
            Class<?> forgeMod = Class.forName(FORGE_MOD_CLASS);
            Object registryObject = forgeRegistryObject(forgeMod);
            if (registryObject == null) {
                return null;
            }
            Method get = registryObject.getClass().getMethod(REGISTRY_GET_METHOD);
            Object attribute = get.invoke(registryObject);
            return attribute instanceof Attribute blockReachAttribute ? blockReachAttribute : null;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }
    }

    /** Obtiene por campo o metodo el objeto de registro estatico de Forge. */
    private static Object forgeRegistryObject(Class<?> forgeMod)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            Field blockReach = forgeMod.getField(BLOCK_REACH_MEMBER);
            return blockReach.get(null);
        } catch (NoSuchFieldException ignored) {
            Method blockReach = forgeMod.getMethod(BLOCK_REACH_MEMBER);
            return blockReach.invoke(null);
        }
    }
}
