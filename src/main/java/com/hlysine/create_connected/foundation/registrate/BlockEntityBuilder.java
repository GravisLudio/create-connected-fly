package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Registrate-shaped block entity builder.
 *
 * <h2>No renderer() or visual()</h2>
 * Registrate chained client registration onto the same call:
 * <pre>{@code
 * REGISTRATE.blockEntity("crank_wheel", CrankWheelBlockEntity::new)
 *     .visual(() -> CrankWheelVisual::new)
 *     .renderer(() -> HandCrankRenderer::new)
 *     .validBlocks(CCBlocks.CRANK_WHEEL)
 *     .register();
 * }</pre>
 * That cannot work here. Create Fly compiles client code in a separate Gradle source set, so a
 * class in {@code main} cannot reference {@code CrankWheelVisual} or {@code HandCrankRenderer} at
 * all -- not even to pass them along. The renderer and visual registrations therefore live in
 * {@code CCBlockEntityRenders} in the client source set, which is exactly how Create Fly separates
 * {@code AllBlockEntityTypes} from {@code AllBlockEntityRenders}.
 */
public final class BlockEntityBuilder<T extends BlockEntity> {
    private final CCRegistrate parent;
    private final String name;
    private final BlockEntityType.BlockEntitySupplier<T> factory;
    private final Set<Block> validBlocks = new LinkedHashSet<>();

    BlockEntityBuilder(
            CCRegistrate parent,
            String name,
            BlockEntityType.BlockEntitySupplier<T> factory
    ) {
        this.parent = parent;
        this.name = name;
        this.factory = factory;
    }

    public BlockEntityBuilder<T> validBlocks(BlockEntry<?>... entries) {
        for (BlockEntry<?> entry : entries) {
            validBlocks.add(entry.get());
        }
        return this;
    }

    public BlockEntityBuilder<T> validBlocks(Block... blocks) {
        java.util.Collections.addAll(validBlocks, blocks);
        return this;
    }

    public BlockEntityEntry<T> register() {
        Identifier id = parent.id(name);
        BlockEntityType<T> type = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                new BlockEntityType<>(factory, Set.copyOf(validBlocks))
        );

        BlockEntityEntry<T> entry = new BlockEntityEntry<>(id, type);
        parent.trackBlockEntity(entry);
        return entry;
    }
}
