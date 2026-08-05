package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * The sub-builder Registrate handed back from {@code BlockBuilder.item()}. It exists so that
 * {@code .properties(...)} after {@code .item(...)} configures the <i>item</i> rather than the
 * block -- the two take different {@code Properties} types, so a single method on
 * {@link BlockBuilder} could not serve both.
 * <p>
 * Registrate let chains end either with {@code build().register()} or with a bare
 * {@code register()}, because the datagen transforms that used to sit in between closed the
 * sub-builder themselves. Both spellings are kept, and both return the block entry.
 */
public final class BlockItemBuilder<T extends net.minecraft.world.level.block.Block, I extends BlockItem> {
    private final BlockBuilder<T> parent;

    BlockItemBuilder(BlockBuilder<T> parent) {
        this.parent = parent;
    }

    public BlockItemBuilder<T, I> properties(UnaryOperator<Item.Properties> op) {
        parent.itemProperties(op);
        return this;
    }

    @SuppressWarnings("unchecked")
    public BlockItemBuilder<T, I> onRegister(Consumer<I> callback) {
        parent.onItemRegister(item -> callback.accept((I) item));
        return this;
    }

    public BlockItemBuilder<T, I> transform(UnaryOperator<BlockItemBuilder<T, I>> op) {
        return op.apply(this);
    }

    // --- accepted and ignored ---

    public BlockItemBuilder<T, I> tag(TagKey<?> ignored) {
        return this;
    }

    @SafeVarargs
    public final BlockItemBuilder<T, I> tag(TagKey<?>... ignored) {
        return this;
    }

    public BlockItemBuilder<T, I> lang(String ignored) {
        return this;
    }


    public BlockBuilder<T> build() {
        return parent;
    }

    public BlockEntry<T> register() {
        return parent.register();
    }
}
