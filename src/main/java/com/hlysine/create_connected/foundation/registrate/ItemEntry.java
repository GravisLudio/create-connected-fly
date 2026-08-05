package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Stand-in for Registrate's {@code ItemEntry}. See {@link BlockEntry} for why these are eager.
 */
public final class ItemEntry<T extends Item> {
    private final Identifier id;
    private final T item;

    ItemEntry(Identifier id, T item) {
        this.id = id;
        this.item = item;
    }

    public T get() {
        return item;
    }

    public Identifier getId() {
        return id;
    }

    public Identifier getKey() {
        return id;
    }

    public ItemStack asStack() {
        return new ItemStack(item);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(item, count);
    }

    public boolean isIn(ItemStack stack) {
        return stack.is(item);
    }

    public boolean isPresent() {
        return BuiltInRegistries.ITEM.getOptional(id).isPresent();
    }

    @Override
    public String toString() {
        return "ItemEntry[" + id + "]";
    }
}
