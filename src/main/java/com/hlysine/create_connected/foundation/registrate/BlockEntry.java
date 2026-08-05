package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Stand-in for Registrate's {@code BlockEntry}. Registrate does not exist on 26.2, and Create Fly
 * registers straight through vanilla, so entries are just thin holders around an already-registered
 * block rather than deferred suppliers.
 * <p>
 * This means entries are eagerly resolved: by the time one exists, the block is in the registry.
 * Registrate's laziness existed to cope with Forge's deferred registration, which has no equivalent
 * here.
 */
public final class BlockEntry<T extends Block> {
    private final Identifier id;
    private final T block;

    BlockEntry(Identifier id, T block) {
        this.id = id;
        this.block = block;
    }

    public T get() {
        return block;
    }

    public Identifier getId() {
        return id;
    }

    /** Registrate called this {@code getKey}; kept for call-site compatibility. */
    public Identifier getKey() {
        return id;
    }

    public BlockState getDefaultState() {
        return block.defaultBlockState();
    }

    public Item asItem() {
        return block.asItem();
    }

    public ItemStack asStack() {
        return new ItemStack(block);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(block, count);
    }

    /** True when {@code state} is this block. */
    public boolean has(BlockState state) {
        return state.is(block);
    }

    /** True when {@code stack} holds this block's item. Registrate inherited this from ItemProviderEntry. */
    public boolean isIn(ItemStack stack) {
        return stack.is(block.asItem());
    }

    public boolean isPresent() {
        return BuiltInRegistries.BLOCK.getOptional(id).isPresent();
    }

    @Override
    public String toString() {
        return "BlockEntry[" + id + "]";
    }
}
