package com.hlysine.create_connected.client;

import com.hlysine.create_connected.content.brassgearbox.BrassGearboxBlock;
import com.hlysine.create_connected.content.crossconnector.EncasedCrossConnectorBlock;
import com.hlysine.create_connected.content.itemsilo.ItemSiloCTBehaviour;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxBlock;
import com.hlysine.create_connected.registries.CCBlocks;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.AllCasings;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.infrastructure.model.CTModel;

/**
 * Connected textures and casing connectivity.
 *
 * <p>Registrate chained these onto registration in {@code CCBlocks} as a pair:
 * <pre>{@code
 * .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
 * .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
 *         (s, f) -> f.getAxis() == s.getValue(ParallelGearboxBlock.AXIS))))
 * }</pre>
 * They could not stay there: the behaviours and sprite shifts are client classes and
 * {@code CCBlocks} runs on both sides. Create Fly splits the same wiring in two, and both halves are
 * needed — one alone does nothing useful:
 * <ul>
 *   <li>{@code AllModels.register(block, CTModel.of(behaviour))} binds the connected-texture model,
 *       replacing {@code connectedTextures(...)}.</li>
 *   <li>{@code AllCasings.make(block, spriteShift, predicate)} replaces
 *       {@code casingConnectivity(...)}; the predicate is the same {@code (state, face)} test.</li>
 * </ul>
 *
 * <p>The pairing of block to casing for the cross connectors is not guessed: it comes from the
 * {@code casing} and {@code casingShift} arguments their call sites still pass to
 * {@code CCBuilderTransformers.encasedCrossConnector}, which were deliberately kept in that
 * signature for this.
 *
 * <p>The item silo takes a behaviour and no casing connectivity, which is upstream's shape too —
 * its connected texture is what makes a stack of silos read as one vessel rather than a column of
 * separate blocks. {@code FluidVesselCTBehaviour} is not here on purpose: it is constructed inside
 * {@code FluidVesselModel}, which {@code CCModels} registers.
 */
public final class CCConnectedTextures {
    private CCConnectedTextures() {
    }

    public static void register() {
        AllModels.register(CCBlocks.PARALLEL_GEARBOX.get(), CTModel.of(AllCTBehaviours.ANDESITE_CASING));
        AllCasings.make(
                CCBlocks.PARALLEL_GEARBOX.get(),
                AllSpriteShifts.ANDESITE_CASING,
                (state, face) -> face.getAxis() == state.getValue(ParallelGearboxBlock.AXIS));

        AllModels.register(CCBlocks.BRASS_GEARBOX.get(), CTModel.of(AllCTBehaviours.BRASS_CASING));
        AllCasings.make(
                CCBlocks.BRASS_GEARBOX.get(),
                AllSpriteShifts.BRASS_CASING,
                (state, face) -> face.getAxis() == state.getValue(BrassGearboxBlock.AXIS));

        AllModels.register(CCBlocks.ANDESITE_ENCASED_CROSS_CONNECTOR.get(), CTModel.of(AllCTBehaviours.ANDESITE_CASING));
        AllCasings.make(
                CCBlocks.ANDESITE_ENCASED_CROSS_CONNECTOR.get(),
                AllSpriteShifts.ANDESITE_CASING,
                (state, face) -> face.getAxis() == state.getValue(EncasedCrossConnectorBlock.AXIS));

        AllModels.register(CCBlocks.BRASS_ENCASED_CROSS_CONNECTOR.get(), CTModel.of(AllCTBehaviours.BRASS_CASING));
        AllCasings.make(
                CCBlocks.BRASS_ENCASED_CROSS_CONNECTOR.get(),
                AllSpriteShifts.BRASS_CASING,
                (state, face) -> face.getAxis() == state.getValue(EncasedCrossConnectorBlock.AXIS));

        AllModels.register(CCBlocks.ITEM_SILO.get(), CTModel.of(new ItemSiloCTBehaviour()));
    }
}
