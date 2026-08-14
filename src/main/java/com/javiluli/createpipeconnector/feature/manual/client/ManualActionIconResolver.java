package com.javiluli.createpipeconnector.feature.manual.client;

import com.javiluli.createpipeconnector.feature.connector.PipeConnectorLogic;
import com.javiluli.createpipeconnector.feature.manual.ManualAction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

/** Resuelve el icono compartido por el radial y el HUD para cada accion manual. */
public final class ManualActionIconResolver {
    /** Impide crear instancias del resolvedor estatico. */
    private ManualActionIconResolver() {
    }

    /** Devuelve un icono estable incluso si Create aun no resolvio alguno de sus bloques. */
    public static ItemStack iconFor(ManualAction action) {
        return switch (action == null ? ManualAction.ANCHOR : action) {
            case ANCHOR -> new ItemStack(Items.CHAIN);
            case MECHANICAL_PUMP -> blockIcon(PipeConnectorLogic.getMechanicalPumpBlock(), Items.IRON_INGOT);
            case COPPER_CASING -> blockIcon(PipeConnectorLogic.getCopperCasingBlock(), Items.COPPER_INGOT);
        };
    }

    /** Convierte un bloque opcional en icono y usa un objeto vanilla como respaldo. */
    private static ItemStack blockIcon(Block block, Item fallback) {
        return block == null ? new ItemStack(fallback) : new ItemStack(block.asItem());
    }
}
