package com.hlysine.create_connected.ponder;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

/**
 * Ponder scenes seed block entity NBT directly, which needs a serialised stack.
 * {@code ItemStack.saveOptional(provider)} is gone in 26.2 -- the codec is the only route now.
 */
public final class PonderNbt {
    private PonderNbt() {
    }

    /** Replaces {@code stack.saveOptional(provider)}: an empty stack encodes to an empty compound. */
    public static Tag saveStack(HolderLookup.Provider provider, ItemStack stack) {
        RegistryOps<Tag> ops = provider.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.OPTIONAL_CODEC.encodeStart(ops, stack).getOrThrow();
    }
}
