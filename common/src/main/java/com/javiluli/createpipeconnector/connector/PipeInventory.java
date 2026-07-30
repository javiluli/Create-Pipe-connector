package com.javiluli.createpipeconnector.connector;

import com.javiluli.createpipeconnector.connector.PipeConnectorLogic.ConnectionPlan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.List;

final class PipeInventory {
    private PipeInventory() {
    }

    static int countAvailablePipes(Player player, Block pipeBlock) {
        if (player.getAbilities().instabuild) {
            return Integer.MAX_VALUE;
        }

        Item pipeItem = pipeBlock.asItem();
        if (pipeItem == Items.AIR) {
            return 0;
        }

        return countAvailableItems(player, pipeItem);
    }

    static int countAvailablePumps(Player player) {
        Block pumpBlock = CreatePipeBlocks.getMechanicalPumpBlock();
        if (pumpBlock == null) {
            return 0;
        }

        return countAvailableItems(player, pumpBlock.asItem());
    }

    static int countAvailableCopperCasings(Player player) {
        Block casingBlock = CreatePipeBlocks.getCopperCasingBlock();
        if (casingBlock == null) {
            return 0;
        }

        return countAvailableItems(player, casingBlock.asItem());
    }

    static boolean hasEnoughItems(Player player, Block pipeBlock, ConnectionPlan plan) {
        return player.getAbilities().instabuild
                || (countAvailablePipes(player, pipeBlock) >= plan.requiredPipes()
                && countAvailablePumps(player) >= plan.requiredPumps()
                && countAvailableCopperCasings(player) >= plan.requiredCopperCasings());
    }

    static boolean consumePipes(Player player, Block pipeBlock, int requiredPipes) {
        if (requiredPipes <= 0 || player.getAbilities().instabuild) {
            return true;
        }

        if (countAvailablePipes(player, pipeBlock) < requiredPipes) {
            return false;
        }

        Item pipeItem = pipeBlock.asItem();
        int remaining = requiredPipes;
        remaining = consumeMatchingStacks(player.getInventory().items, pipeItem, remaining);
        remaining = consumeMatchingStacks(player.getInventory().offhand, pipeItem, remaining);
        player.getInventory().setChanged();
        return remaining == 0;
    }

    static boolean consumeItems(Player player, Block pipeBlock, ConnectionPlan plan) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        if (!hasEnoughItems(player, pipeBlock, plan)) {
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

        player.getInventory().setChanged();
        return remainingPipes == 0 && remainingPumps == 0;
    }

    private static int countAvailableItems(Player player, Item item) {
        if (player.getAbilities().instabuild) {
            return Integer.MAX_VALUE;
        }
        if (item == Items.AIR) {
            return 0;
        }

        int count = 0;
        count += countMatchingStacks(player.getInventory().items, item);
        count += countMatchingStacks(player.getInventory().offhand, item);
        return count;
    }

    private static int countMatchingStacks(List<ItemStack> stacks, Item item) {
        int count = 0;
        for (ItemStack stack : stacks) {
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

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
}
