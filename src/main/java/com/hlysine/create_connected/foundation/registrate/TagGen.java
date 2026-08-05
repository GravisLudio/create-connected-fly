package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.world.level.block.Block;

import java.util.function.UnaryOperator;

/**
 * No-op stand-ins for Create's {@code TagGen} transforms.
 * <p>
 * Upstream these added {@code mineable/axe} and {@code mineable/pickaxe} tags at datagen time. The
 * generated tag JSONs are already committed under {@code src/generated/resources}, and tags are
 * data-driven at runtime, so nothing needs to happen here -- the transforms just have to exist so
 * the registration chains keep compiling.
 */
public final class TagGen {
    private TagGen() {
    }

    public static <T extends Block> UnaryOperator<BlockBuilder<T>> axeOrPickaxe() {
        return b -> b;
    }

    public static <T extends Block> UnaryOperator<BlockBuilder<T>> pickaxeOnly() {
        return b -> b;
    }

    public static <T extends Block> UnaryOperator<BlockBuilder<T>> axeOnly() {
        return b -> b;
    }
}
