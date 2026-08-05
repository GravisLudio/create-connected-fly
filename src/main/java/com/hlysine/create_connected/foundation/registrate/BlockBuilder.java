package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Registrate-shaped builder backed by vanilla registration.
 *
 * <h2>What is intentionally missing</h2>
 * Registrate's datagen methods that took lambdas -- {@code blockstate}, {@code model},
 * {@code blockModel}, {@code simpleBlock}, {@code loot}, {@code recipe} -- are <b>not</b> defined
 * here, on purpose. Their lambda bodies reference Registrate datagen types
 * ({@code DataGenContext}, {@code RegistrateBlockstateProvider}, ...) that do not exist on 26.2, so
 * a permissive no-op overload would not make those call sites compile anyway. Leaving them out
 * makes javac point at every chain that still needs stripping instead of hiding the work.
 * <p>
 * We can drop them outright because the 1025 JSONs Registrate generated for 1.21.1 are committed
 * under {@code src/generated/resources} and are migrated by hand -- there is no datagen to re-run.
 *
 * <h2>Methods that are real no-ops</h2>
 * {@link #tag}, {@link #addLayer} and {@link #lang} take plain values rather than datagen lambdas,
 * so they can be accepted and ignored. Tags and lang are data-driven and already present as JSON.
 * {@code addLayer} is safe to ignore because 26.2 no longer takes per-block chunk render layers
 * from mods -- Create Fly ports all of Create, including its many cutout blocks, without assigning
 * a render layer anywhere.
 */
public final class BlockBuilder<T extends Block> {
    private final CCRegistrate parent;
    private final String name;
    private final Function<Properties, T> factory;

    private Supplier<Block> initialProperties;
    private UnaryOperator<Properties> propertyOps = UnaryOperator.identity();
    private final List<Consumer<T>> onRegister = new ArrayList<>();

    private boolean createItem;
    private BiFunction<Block, Item.Properties, ? extends BlockItem> itemFactory;

    BlockBuilder(CCRegistrate parent, String name, Function<Properties, T> factory) {
        this.parent = parent;
        this.name = name;
        this.factory = factory;
    }

    /** Copies properties from the block supplied (Create's {@code SharedProperties::stone} etc). */
    public BlockBuilder<T> initialProperties(Supplier<Block> from) {
        this.initialProperties = from;
        return this;
    }

    public BlockBuilder<T> properties(UnaryOperator<Properties> op) {
        UnaryOperator<Properties> prev = this.propertyOps;
        this.propertyOps = p -> op.apply(prev.apply(p));
        return this;
    }

    public BlockBuilder<T> transform(UnaryOperator<BlockBuilder<T>> op) {
        return op.apply(this);
    }

    public BlockBuilder<T> onRegister(Consumer<T> callback) {
        onRegister.add(callback);
        return this;
    }

    // --- accepted and ignored; see class docs ---

    /**
     * Takes {@code TagKey<?>} rather than {@code TagKey<Block>} because Registrate's chain applied
     * item tags after {@code item()} as well, and both erase to {@code TagKey} -- they cannot be
     * separate overloads. Since this is a no-op either way, one permissive signature covers both.
     */
    public BlockBuilder<T> tag(TagKey<?> ignored) {
        return this;
    }

    public final BlockBuilder<T> tag(TagKey<?>... ignored) {
        return this;
    }

    public BlockBuilder<T> addLayer(Supplier<?> ignored) {
        return this;
    }

    public BlockBuilder<T> lang(String ignored) {
        return this;
    }

    // --- item ---

    /** Registers a plain {@link BlockItem} alongside the block. */
    public BlockBuilder<T> item() {
        this.createItem = true;
        this.itemFactory = BlockItem::new;
        return this;
    }

    /** Registers a custom block item, e.g. {@code .item(CrankWheelItem::new)}. */
    public BlockBuilder<T> item(BiFunction<Block, Item.Properties, ? extends BlockItem> custom) {
        this.createItem = true;
        this.itemFactory = custom;
        return this;
    }

    /**
     * Registrate returned an item sub-builder here that call sites closed with {@code build()}.
     * Both are folded into this builder, so {@code build()} just hands it back.
     */
    public BlockBuilder<T> build() {
        return this;
    }

    public BlockEntry<T> register() {
        Identifier id = parent.id(name);

        Properties props = initialProperties == null
                ? Properties.of()
                : Properties.ofFullCopy(initialProperties.get());
        props = propertyOps.apply(props);

        // Create Fly uses the BlockItemId overload, which also registers the block item -- but that
        // one is private in Blocks and it only reaches it by widening it in its own classtweaker.
        // Access wideners are not transitive, so we take the public ResourceKey overload instead
        // and register the block item ourselves below.
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);

        @SuppressWarnings("unchecked")
        T block = (T) net.minecraft.world.level.block.Blocks.register(key, factory::apply, props);

        if (createItem) {
            Registry.register(
                    BuiltInRegistries.ITEM,
                    id,
                    itemFactory.apply(block, new Item.Properties())
            );
        }

        for (Consumer<T> callback : onRegister) {
            callback.accept(block);
        }

        BlockEntry<T> entry = new BlockEntry<>(id, block);
        parent.trackBlock(entry);
        return entry;
    }
}
