package com.javiluli.createpipeconnector.feature.material.shulker;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.List;

/**
 * Adapta el inventario NBT de las shulkers vanilla al sistema de materiales.
 *
 * <p>Esta clase concentra la dependencia con el formato interno de Minecraft.
 * El resto del conector solo necesita recorrer o consumir pilas almacenadas.</p>
 */
public final class ShulkerMaterialBridge {
    private static final int SHULKER_SIZE = 27;

    /** Impide crear instancias del puente de inventario. */
    private ShulkerMaterialBridge() {
    }

    /**
     * Recorre el contenido de cada shulker junto a su pila contenedora.
     *
     * <p>Los shulkers con una tabla de loot pendiente se omiten para no generar
     * ni modificar su contenido antes de que Minecraft los abra.</p>
     */
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
     * Consume dos materiales en una sola pasada y conserva color, nombre y NBT.
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
                saveContents(shulkerStack, contents);
            }
        }
        return new Consumption(pendingPipes, pendingPumps);
    }

    /** Carga una shulker vanilla con contenido ya definido. */
    private static NonNullList<ItemStack> loadContents(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)
                || !(blockItem.getBlock() instanceof ShulkerBoxBlock)) {
            return null;
        }

        CompoundTag blockEntityTag = stack.getTagElement("BlockEntityTag");
        if (blockEntityTag == null
                || blockEntityTag.contains("LootTable", Tag.TAG_STRING)
                || !blockEntityTag.contains("Items", Tag.TAG_LIST)) {
            return null;
        }

        NonNullList<ItemStack> contents = NonNullList.withSize(SHULKER_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(blockEntityTag, contents);
        return contents;
    }

    /** Guarda el inventario modificado sin reemplazar otros datos de la shulker. */
    private static void saveContents(ItemStack shulkerStack, NonNullList<ItemStack> contents) {
        CompoundTag currentTag = shulkerStack.getTagElement("BlockEntityTag");
        CompoundTag blockEntityTag = currentTag == null ? new CompoundTag() : currentTag.copy();
        blockEntityTag.remove("Items");
        ContainerHelper.saveAllItems(blockEntityTag, contents);
        shulkerStack.addTagElement("BlockEntityTag", blockEntityTag);
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
