package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.foundation.registrate.BlockBuilder;
import com.hlysine.create_connected.foundation.registrate.SharedProperties;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Shared registration steps for encased blocks.
 * <p>
 * Two things changed from the Registrate version. Registrate's builders carried a parent type
 * parameter for the fluent chain ({@code BlockBuilder<B, P>}); the replacement builder has no
 * parent to return to, so it takes one type argument. And {@code UnaryOperator} was only an
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
        return builder -> encasedBase(builder).item().build();
    }

    /**
     * Stand-in for Create's {@code BuilderTransformers.copycat()}.
     * <p>
     * Create Fly has no {@code BuilderTransformers}, so this reproduces the property set it inlines
     * at each of its own copycat registrations:
     * <pre>{@code
     * Properties.ofFullCopy(Blocks.GOLD_BLOCK).noOcclusion().mapColor(MapColor.NONE)
     *     .isValidSpawn(Blocks::never).emissiveRendering(CopycatPanelBlock::hasEmissiveLighting)
     * }</pre>
     * {@code SharedProperties.softMetal()} is already {@code GOLD_BLOCK}, so the base copy matches.
     * <p>
     * <b>This was a no-op for a while, and it was not harmless.</b> An earlier comment here claimed
     * upstream's version only set up the blockstate and item model, both datagen — that was wrong.
     * Without {@code noOcclusion()} a copycat claims to be a solid full cube, so its neighbours cull
     * the faces touching it while its own model is a slab, a wall or a fence. The result is holes:
     * you see through the block and light leaks past it. It looked like a model bug and cost three
     * wrong hypotheses in the model layer before anyone checked the block properties.
     * <p>
     * Per-shape extras still compose on top — two call sites add {@code forceSolidOn()} after this,
     * which is exactly how Create Fly does {@code COPYCAT_STEP}.
     */
    public static <B extends Block> UnaryOperator<BlockBuilder<B>> copycat() {
        return builder -> builder
                .initialProperties(SharedProperties::softMetal)
                .properties(p -> p
                        .noOcclusion()
                        .mapColor(MapColor.NONE)
                        .isValidSpawn(Blocks::never));
        // Deliberately NOT .emissiveRendering(CopycatBlock::hasEmissiveLighting), which Create Fly
        // passes for its own copycats. That predicate reads CopycatBlock.EMISSIVE, and nothing in
        // this mod ever sets it: BooleanProperty.create lists true before false, StateDefinition
        // takes the first value for the default state, and CopycatBlock's constructor does not
        // override it -- so every Connected copycat defaults to emissive and glows in the dark.
        // Fixing it properly means adding setValue(CopycatBlock.EMISSIVE, false) to the
        // registerDefaultState call in all nine block classes. Until then, leaving the predicate
        // off costs only the feature (a copycat of glowstone will not glow), which never worked
        // here anyway because nothing read the property.
    }

    private static <B extends Block> BlockBuilder<B> encasedBase(BlockBuilder<B> b) {
        return b.initialProperties(SharedProperties::stone)
                .properties(BlockBehaviour.Properties::noOcclusion);
    }
}
