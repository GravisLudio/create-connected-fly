package com.hlysine.create_connected.content.kineticbattery;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

/**
 * Stands in for {@code InteractionResultHolder<ItemStack>}, which 26.2 removed. Vanilla folded the
 * held-item payload into {@link InteractionResult.Success#heldItemTransformedTo(ItemStack)}, but the
 * failure cases here also carry a stack, so the pair is kept explicit.
 */
public record ChargeTransfer(InteractionResult result, ItemStack leftover) {
    public static ChargeTransfer fail() {
        return new ChargeTransfer(InteractionResult.FAIL, ItemStack.EMPTY);
    }

    public static ChargeTransfer success(ItemStack leftover) {
        return new ChargeTransfer(InteractionResult.SUCCESS, leftover);
    }
}
