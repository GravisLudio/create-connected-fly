package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Stand-in for Registrate's {@code BlockEntityEntry}.
 * <p>
 * Deliberately has no {@code renderer()} or {@code visual()}. In Registrate those were part of the
 * same fluent chain as registration, but Create Fly splits client code into its own Gradle source
 * set, so a main-source-set class cannot even name a renderer or a Flywheel visual. Those
 * registrations live in {@code CCBlockEntityRenders} on the client side instead, mirroring how
 * Create Fly separates {@code AllBlockEntityTypes} from {@code AllBlockEntityRenders}.
 */
public final class BlockEntityEntry<T extends BlockEntity> {
    private final Identifier id;
    private final BlockEntityType<T> type;

    BlockEntityEntry(Identifier id, BlockEntityType<T> type) {
        this.id = id;
        this.type = type;
    }

    public BlockEntityType<T> get() {
        return type;
    }

    public Identifier getId() {
        return id;
    }

    public Identifier getKey() {
        return id;
    }

    @Override
    public String toString() {
        return "BlockEntityEntry[" + id + "]";
    }
}
