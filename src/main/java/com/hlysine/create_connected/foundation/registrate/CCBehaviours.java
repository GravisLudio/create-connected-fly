package com.hlysine.create_connected.foundation.registrate;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * Create used to hang these on the registrate chain as static transforms
 * ({@code DisplaySource.displaySource(...)}). Create Fly dropped them: its own {@code All*} classes
 * fill the block-keyed registries directly after registration instead. These wrappers keep the
 * chained call sites in {@link com.hlysine.create_connected.registries.CCBlocks} intact by doing
 * that registration from {@code onRegister}.
 */
public final class CCBehaviours {
    private CCBehaviours() {
    }

    public static <T extends Block> UnaryOperator<BlockBuilder<T>> displaySource(DisplaySource source) {
        return builder -> builder.onRegister(block -> DisplaySource.BY_BLOCK.add(block, source));
    }

    public static <T extends Block> UnaryOperator<BlockBuilder<T>> displayTarget(DisplayTarget target) {
        return builder -> builder.onRegister(block -> DisplayTarget.BY_BLOCK.register(block, target));
    }

    public static <T extends Block> UnaryOperator<BlockBuilder<T>> mountedItemStorage(MountedItemStorageType<?> type) {
        return builder -> builder.onRegister(block -> MountedItemStorageType.REGISTRY.register(block, type));
    }

    public static <T extends Block> UnaryOperator<BlockBuilder<T>> mountedFluidStorage(MountedFluidStorageType<?> type) {
        return builder -> builder.onRegister(block -> MountedFluidStorageType.REGISTRY.register(block, type));
    }

    /** Used with {@code onRegister}, not {@code transform} -- matching how upstream called it. */
    public static <T extends Block> Consumer<T> movementBehaviour(MovementBehaviour behaviour) {
        return block -> MovementBehaviour.REGISTRY.register(block, behaviour);
    }
}
