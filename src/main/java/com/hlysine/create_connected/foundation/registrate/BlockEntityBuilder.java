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

    /**
     * A block entity constructor of the shape Create's subclasses use:
     * {@code (BlockEntityType<?>, BlockPos, BlockState)}.
     * <p>
     * 26.2's {@code BlockEntityType.BlockEntitySupplier} only passes {@code (BlockPos, BlockState)}
     * -- the type argument is gone. Create Fly deals with this by giving each block entity a
     * two-argument constructor, or by adding static factories that fill the type in. Connected's 26
     * block entities all take three arguments, so rather than rewrite every one of them,
     * {@link #register()} adapts here.
     */
    @FunctionalInterface
    public interface Factory<T extends BlockEntity> {
        T create(BlockEntityType<?> type, net.minecraft.core.BlockPos pos,
                 net.minecraft.world.level.block.state.BlockState state);
    }

    private final CCRegistrate parent;
    private final String name;
    private final Factory<T> factory;
    private final Set<Block> validBlocks = new LinkedHashSet<>();

    BlockEntityBuilder(
            CCRegistrate parent,
            String name,
            Factory<T> factory
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

        // The constructor needs the type, and the type needs the constructor. The supplier is only
        // called when a block entity is actually created, long after registration returns, so a
        // one-slot holder closes the loop. Create Fly solves the same problem by referencing the
        // already-assigned AllBlockEntityTypes field from a static factory.
        @SuppressWarnings("unchecked")
        final BlockEntityType<T>[] self = new BlockEntityType[1];

        BlockEntityType<T> type = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                id,
                new BlockEntityType<>(
                        (pos, state) -> factory.create(self[0], pos, state),
                        Set.copyOf(validBlocks)
                )
        );
        self[0] = type;

        BlockEntityEntry<T> entry = new BlockEntityEntry<>(id, type);
        parent.trackBlockEntity(entry);
        return entry;
    }
}
