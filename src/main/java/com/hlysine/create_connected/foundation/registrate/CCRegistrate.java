package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Minimal replacement for Create's {@code CreateRegistrate}.
 * <p>
 * Registrate does not exist for 26.2 and Create Fly does not use it -- it registers through vanilla
 * directly ({@code Blocks.register}, {@code Registry.register}). Rather than rewriting all 25 files
 * under {@code registries/}, this reproduces the slice of Registrate's fluent API that Create:
 * Connected actually calls, backed by those vanilla calls.
 * <p>
 * The one shape that could not be preserved is block entity rendering: Registrate chained
 * {@code renderer()} and {@code visual()} onto registration, but Create Fly puts client code in a
 * separate source set, so those move to {@code CCBlockEntityRenders} on the client side.
 *
 * @see BlockBuilder for which datagen methods were dropped and why
 */
public final class CCRegistrate {
    private final String modid;

    private final Map<Identifier, BlockEntry<?>> blocks = new LinkedHashMap<>();
    private final Map<Identifier, ItemEntry<?>> items = new LinkedHashMap<>();
    private final Map<Identifier, BlockEntityEntry<?>> blockEntities = new LinkedHashMap<>();

    private CCRegistrate(String modid) {
        this.modid = modid;
    }

    public static CCRegistrate create(String modid) {
        return new CCRegistrate(modid);
    }

    public String getModid() {
        return modid;
    }

    public Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(modid, name);
    }

    // --- entry points ---

    public <T extends Block> BlockBuilder<T> block(String name, Function<Properties, T> factory) {
        return new BlockBuilder<>(this, name, factory);
    }

    public <T extends Item> ItemBuilder<T> item(String name, Function<Item.Properties, T> factory) {
        return new ItemBuilder<>(this, name, factory);
    }

    public <T extends BlockEntity> BlockEntityBuilder<T> blockEntity(
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory
    ) {
        return new BlockEntityBuilder<>(this, name, factory);
    }

    // --- bookkeeping, so callers can iterate everything this mod registered ---

    void trackBlock(BlockEntry<?> entry) {
        blocks.put(entry.getId(), entry);
    }

    void trackItem(ItemEntry<?> entry) {
        items.put(entry.getId(), entry);
    }

    void trackBlockEntity(BlockEntityEntry<?> entry) {
        blockEntities.put(entry.getId(), entry);
    }

    public Collection<BlockEntry<?>> getAllBlocks() {
        return Collections.unmodifiableCollection(blocks.values());
    }

    public Collection<ItemEntry<?>> getAllItems() {
        return Collections.unmodifiableCollection(items.values());
    }

    public Collection<BlockEntityEntry<?>> getAllBlockEntities() {
        return Collections.unmodifiableCollection(blockEntities.values());
    }
}
