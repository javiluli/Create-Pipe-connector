package com.javiluli.createpipeconnector.feature.material.shulker;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.List;

/**
 * Adapta el componente de inventario de las shulkers vanilla al sistema de materiales.
 *
 * <p>Esta clase concentra la dependencia con el formato de Minecraft 1.21.1.
 * El resto del conector solo recorre o consume pilas almacenadas.</p>
 */
public final class ShulkerMaterialBridge {
    private static final int SHULKER_SIZE = 27;

    /** Impide crear instancias del puente de inventario. */
    private ShulkerMaterialBridge() {
    }

    /** Recorre el contenido de cada shulker junto a su pila contenedora. */
    public static void visitContents(List<ItemStack> stacks, StoredStackVisitor visitor) {
        for (ItemStack shulkerStack : stacks) {
            NonNullList<ItemStack> contents = loadContents(shulkerStack);
            if (contents == null) {
                continue;
            }
            for (ItemStack containedStack : contents) {
                if (!containedStack.isEmpty()) {
                    visitor.visit(containedStack, shulkerStack);
                }
            }
        }
    }

    /**
     * Consume dos materiales en una sola pasada y conserva color y componentes.
     *
     * @return cantidades que siguen pendientes despues de revisar los shulkers
     */
    public static Consumption consume(
            List<ItemStack> stacks,
            Item pipeItem,
            int remainingPipes,
            Item pumpItem,
            int remainingPumps
    ) {
        int pendingPipes = Math.max(0, remainingPipes);
        int pendingPumps = Math.max(0, remainingPumps);
        for (ItemStack shulkerStack : stacks) {
            if (pendingPipes == 0 && pendingPumps == 0) {
                break;
            }

            NonNullList<ItemStack> contents = loadContents(shulkerStack);
            if (contents == null) {
                continue;
            }

            boolean changed = false;
            for (ItemStack containedStack : contents) {
                if (pendingPipes > 0 && pipeItem != Items.AIR && containedStack.is(pipeItem)) {
                    int consumed = Math.min(pendingPipes, containedStack.getCount());
                    containedStack.shrink(consumed);
                    pendingPipes -= consumed;
                    changed = true;
                } else if (pendingPumps > 0 && pumpItem != Items.AIR && containedStack.is(pumpItem)) {
                    int consumed = Math.min(pendingPumps, containedStack.getCount());
                    containedStack.shrink(consumed);
                    pendingPumps -= consumed;
                    changed = true;
                }
            }
            if (changed) {
                shulkerStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(contents));
            }
        }
        return new Consumption(pendingPipes, pendingPumps);
    }

    /** Carga una shulker vanilla cuyo contenido ya fue generado. */
    private static NonNullList<ItemStack> loadContents(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock)
                || stack.has(DataComponents.CONTAINER_LOOT)) {
            return null;
        }

        ItemContainerContents container = stack.get(DataComponents.CONTAINER);
        if (container == null) {
            return null;
        }
        NonNullList<ItemStack> contents = NonNullList.withSize(SHULKER_SIZE, ItemStack.EMPTY);
        container.copyInto(contents);
        return contents;
    }

    /** Recibe una pila almacenada y la shulker de la que procede. */
    @FunctionalInterface
    public interface StoredStackVisitor {
        void visit(ItemStack containedStack, ItemStack shulkerStack);
    }

    /** Cantidades pendientes tras consumir materiales almacenados. */
    public record Consumption(int pipes, int pumps) {
    }
}
