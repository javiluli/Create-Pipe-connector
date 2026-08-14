package com.javiluli.createpipeconnector.feature.material;

import com.javiluli.createpipeconnector.core.create.CreatePipeBlocks;
import com.javiluli.createpipeconnector.core.model.ConnectionPlan;
import com.javiluli.createpipeconnector.feature.material.shulker.ShulkerMaterialBridge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuenta y consume materiales desde el inventario, la mano secundaria y shulkers.
 *
 * <p>Los jugadores en creativo conservan el comportamiento de recursos infinitos.</p>
 */
public final class PipeInventory {
    /** Impide crear instancias del servicio de inventario. */
    private PipeInventory() {
    }

    /**
     * Inspecciona todos los materiales del conector recorriendo cada shulker una sola vez.
     *
     * @return cantidades directas, cantidades guardadas y primer shulker util por material
     */
    public static MaterialSnapshot inspectMaterials(Player player, Block pipeBlock, boolean includeShulkers) {
        if (player.getAbilities().instabuild) {
            MaterialAvailability unlimited = MaterialAvailability.unlimited();
            return new MaterialSnapshot(unlimited, unlimited, unlimited);
        }

        Block pumpBlock = CreatePipeBlocks.getMechanicalPumpBlock();
        Block casingBlock = CreatePipeBlocks.getCopperCasingBlock();
        AvailabilityAccumulator pipes = new AvailabilityAccumulator(pipeBlock.asItem());
        AvailabilityAccumulator pumps = new AvailabilityAccumulator(
                pumpBlock == null ? Items.AIR : pumpBlock.asItem()
        );
        AvailabilityAccumulator casings = new AvailabilityAccumulator(
                casingBlock == null ? Items.AIR : casingBlock.asItem()
        );
        List<AvailabilityAccumulator> accumulators = List.of(pipes, pumps, casings);

        inspectStacks(player.getInventory().items, accumulators, includeShulkers);
        inspectStacks(player.getInventory().offhand, accumulators, includeShulkers);
        return new MaterialSnapshot(pipes.snapshot(), pumps.snapshot(), casings.snapshot());
    }

    /** Consume de forma conjunta los materiales usando una instantanea ya validada. */
    public static boolean consumeItems(
            Player player,
            Block pipeBlock,
            ConnectionPlan plan,
            MaterialSnapshot materials
    ) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (!materials.hasEnough(plan)) {
            return false;
        }

        int remainingPipes = plan.requiredPipes();
        if (remainingPipes > 0) {
            Item pipeItem = pipeBlock.asItem();
            remainingPipes = consumeMatchingStacks(player.getInventory().items, pipeItem, remainingPipes);
            remainingPipes = consumeMatchingStacks(player.getInventory().offhand, pipeItem, remainingPipes);
        }

        int remainingPumps = plan.requiredPumps();
        Block pumpBlock = CreatePipeBlocks.getMechanicalPumpBlock();
        if (remainingPumps > 0 && pumpBlock != null) {
            Item pumpItem = pumpBlock.asItem();
            remainingPumps = consumeMatchingStacks(player.getInventory().items, pumpItem, remainingPumps);
            remainingPumps = consumeMatchingStacks(player.getInventory().offhand, pumpItem, remainingPumps);
        }

        Item pumpItem = pumpBlock == null ? Items.AIR : pumpBlock.asItem();
        ShulkerMaterialBridge.Consumption remainingMaterials = consumeMatchingShulkerContents(
                player.getInventory().items,
                pipeBlock.asItem(),
                remainingPipes,
                pumpItem,
                remainingPumps
        );
        remainingMaterials = consumeMatchingShulkerContents(
                player.getInventory().offhand,
                pipeBlock.asItem(),
                remainingMaterials.pipes(),
                pumpItem,
                remainingMaterials.pumps()
        );

        player.getInventory().setChanged();
        return remainingMaterials.pipes() == 0 && remainingMaterials.pumps() == 0;
    }

    /** Devuelve tuberias y bombas reservadas que finalmente no fueron colocadas. */
    public static void refundItems(Player player, Block pipeBlock, int pipes, int pumps) {
        if (player.getAbilities().instabuild) {
            return;
        }

        giveItems(player, pipeBlock.asItem(), pipes);
        Block pumpBlock = CreatePipeBlocks.getMechanicalPumpBlock();
        if (pumpBlock != null) {
            giveItems(player, pumpBlock.asItem(), pumps);
        }
        player.getInventory().setChanged();
    }

    /** Acumula pilas directas y contenido de shulkers para varios materiales. */
    private static void inspectStacks(
            List<ItemStack> stacks,
            List<AvailabilityAccumulator> accumulators,
            boolean includeShulkers
    ) {
        for (ItemStack stack : stacks) {
            for (AvailabilityAccumulator accumulator : accumulators) {
                accumulator.addDirect(stack);
            }
        }
        if (!includeShulkers) {
            return;
        }
        ShulkerMaterialBridge.visitContents(stacks, (containedStack, shulkerStack) -> {
            for (AvailabilityAccumulator accumulator : accumulators) {
                accumulator.addShulker(containedStack, shulkerStack);
            }
        });
    }

    /** Retira unidades coincidentes y devuelve la cantidad pendiente. */
    private static int consumeMatchingStacks(List<ItemStack> stacks, Item item, int remaining) {
        for (ItemStack stack : stacks) {
            if (remaining <= 0) {
                return 0;
            }
            if (!stack.is(item)) {
                continue;
            }
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
        }
        return remaining;
    }

    /** Retira tuberias y bombas cargando cada shulker una sola vez. */
    private static ShulkerMaterialBridge.Consumption consumeMatchingShulkerContents(
            List<ItemStack> stacks,
            Item pipeItem,
            int remainingPipes,
            Item pumpItem,
            int remainingPumps
    ) {
        return ShulkerMaterialBridge.consume(
                stacks,
                pipeItem,
                remainingPipes,
                pumpItem,
                remainingPumps
        );
    }

    /** Inserta objetos en el inventario y deja caer cualquier resto a los pies. */
    private static void giveItems(Player player, Item item, int amount) {
        if (item == Items.AIR || amount <= 0) {
            return;
        }

        int remaining = amount;
        while (remaining > 0) {
            int stackSize = Math.min(remaining, item.getMaxStackSize());
            ItemStack stack = new ItemStack(item, stackSize);
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }
            remaining -= stackSize;
        }
    }

    /** Resumen de los tres materiales usados por una ruta. */
    public record MaterialSnapshot(
            MaterialAvailability pipes,
            MaterialAvailability pumps,
            MaterialAvailability copperCasings
    ) {
        /** Comprueba si la instantanea cubre todos los requisitos del plan. */
        public boolean hasEnough(ConnectionPlan plan) {
            return pipes.totalCount() >= plan.requiredPipes()
                    && pumps.totalCount() >= plan.requiredPumps()
                    && copperCasings.totalCount() >= plan.requiredCopperCasings();
        }
    }

    /** Cantidades disponibles y shulkers que aportan un material. */
    public record MaterialAvailability(
            int directCount,
            int shulkerCount,
            List<ShulkerMaterialSource> shulkerSources
    ) {
        /** Conserva una copia inmutable de las fuentes detectadas. */
        public MaterialAvailability {
            shulkerSources = shulkerSources == null ? List.of() : List.copyOf(shulkerSources);
        }

        /** Crea una disponibilidad ilimitada para el modo creativo. */
        private static MaterialAvailability unlimited() {
            return new MaterialAvailability(Integer.MAX_VALUE, 0, List.of());
        }

        /** Devuelve la suma de inventario directo y shulkers. */
        public int totalCount() {
            if (directCount == Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
            return directCount + shulkerCount;
        }

    }

    /** Identifica una shulker y la cantidad de material que contiene. */
    public record ShulkerMaterialSource(ItemStack shulkerStack, int count) {
    }

    /** Acumulador mutable limitado al recorrido interno del inventario. */
    private static final class AvailabilityAccumulator {
        private final Item item;
        private int directCount;
        private int shulkerCount;
        private final List<MutableShulkerSource> shulkerSources = new ArrayList<>();
        private ItemStack lastSourceContainer = ItemStack.EMPTY;

        private AvailabilityAccumulator(Item item) {
            this.item = item;
        }

        /** Suma una pila situada directamente en el inventario. */
        private void addDirect(ItemStack stack) {
            if (item != Items.AIR && stack.is(item)) {
                directCount += stack.getCount();
            }
        }

        /** Suma una pila interna y agrupa su cantidad por shulker de origen. */
        private void addShulker(ItemStack stack, ItemStack shulkerStack) {
            if (item == Items.AIR || !stack.is(item)) {
                return;
            }
            shulkerCount += stack.getCount();
            if (lastSourceContainer != shulkerStack) {
                shulkerSources.add(new MutableShulkerSource(new ItemStack(shulkerStack.getItem())));
                lastSourceContainer = shulkerStack;
            }
            shulkerSources.get(shulkerSources.size() - 1).count += stack.getCount();
        }

        /** Convierte el acumulador en un resultado inmutable. */
        private MaterialAvailability snapshot() {
            List<ShulkerMaterialSource> sources = new ArrayList<>(shulkerSources.size());
            for (MutableShulkerSource source : shulkerSources) {
                sources.add(new ShulkerMaterialSource(source.shulkerStack, source.count));
            }
            return new MaterialAvailability(directCount, shulkerCount, sources);
        }

        /** Fuente mutable usada solo durante el recorrido de inventario. */
        private static final class MutableShulkerSource {
            private final ItemStack shulkerStack;
            private int count;

            private MutableShulkerSource(ItemStack shulkerStack) {
                this.shulkerStack = shulkerStack;
            }
        }
    }

}
