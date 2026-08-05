package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.foundation.registrate.BlockBuilder;
import com.hlysine.create_connected.foundation.registrate.SharedProperties;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Shared registration steps for encased blocks.
 * <p>
 * Two things changed from the Registrate version. Registrate's builders carried a parent type
 * parameter for the fluent chain ({@code BlockBuilder<B, P>}); the replacement builder has no
 * parent to return to, so it takes one type argument. And {@code NonNullUnaryOperator} was only an
 * annotated {@link UnaryOperator}, so it maps straight across.
 * <p>
 * Dropped from these transformers: the loot table (data, already generated), the item model (same),
 * and the connected-texture calls, which reference client classes and now live in
 * {@code client/CCConnectedTextures}.
 */
public class CCBuilderTransformers {

    public static <B extends Block> UnaryOperator<BlockBuilder<B>> encasedCrossConnector(
            String casing,
            Supplier<CTSpriteShiftEntry> casingShift
    ) {
        // casing/casingShift are kept in the signature so the call sites still say which casing
        // they mean; CCConnectedTextures needs that pairing when the client wiring is written.
        return builder -> encasedBase(builder).item();
    }

    /**
     * Stand-in for Create's {@code BuilderTransformers.copycat()}.
     * <p>
     * Create Fly has no {@code BuilderTransformers}. Upstream this only set up the blockstate and
     * item model, both of which are datagen and already committed as JSON, so there is nothing left
     * for it to do at registration time.
     */
    public static <B extends Block> UnaryOperator<BlockBuilder<B>> copycat() {
        return builder -> builder;
    }

    private static <B extends Block> BlockBuilder<B> encasedBase(BlockBuilder<B> b) {
        return b.initialProperties(SharedProperties::stone)
                .properties(BlockBehaviour.Properties::noOcclusion);
    }
}
