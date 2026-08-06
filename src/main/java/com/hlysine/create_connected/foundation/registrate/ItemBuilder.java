package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Registrate-shaped item builder backed by {@code Registry.register}.
 * As with {@link BlockBuilder}, datagen methods taking lambdas are deliberately absent.
 */
public final class ItemBuilder<T extends Item> implements NamedBuilder {
    private final CCRegistrate parent;
    private final String name;
    private final Function<Item.Properties, T> factory;

    private UnaryOperator<Item.Properties> propertyOps = UnaryOperator.identity();
    private final List<Consumer<T>> onRegister = new ArrayList<>();

    ItemBuilder(CCRegistrate parent, String name, Function<Item.Properties, T> factory) {
        this.parent = parent;
        this.name = name;
        this.factory = factory;
    }

    @Override
    public String getModid() {
        return parent.getModid();
    }

    @Override
    public String getName() {
        return name;
    }

    public ItemBuilder<T> properties(UnaryOperator<Item.Properties> op) {
        UnaryOperator<Item.Properties> prev = this.propertyOps;
        this.propertyOps = p -> op.apply(prev.apply(p));
        return this;
    }

    public ItemBuilder<T> transform(UnaryOperator<ItemBuilder<T>> op) {
        return op.apply(this);
    }

    public ItemBuilder<T> onRegister(Consumer<T> callback) {
        onRegister.add(callback);
        return this;
    }

    // --- accepted and ignored ---

    public ItemBuilder<T> tag(TagKey<?> ignored) {
        return this;
    }

    public final ItemBuilder<T> tag(TagKey<?>... ignored) {
        return this;
    }

    public ItemBuilder<T> lang(String ignored) {
        return this;
    }

    public ItemEntry<T> register() {
        Identifier id = parent.id(name);
        T item = Registry.register(
                BuiltInRegistries.ITEM,
                id,
                factory.apply(propertyOps.apply(new Item.Properties()))
        );

        for (Consumer<T> callback : onRegister) {
            callback.accept(item);
        }

        ItemEntry<T> entry = new ItemEntry<>(id, item);
        parent.trackItem(entry);
        return entry;
    }
}
