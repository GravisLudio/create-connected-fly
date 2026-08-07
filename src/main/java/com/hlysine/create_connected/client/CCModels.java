package com.hlysine.create_connected.client;

import com.hlysine.create_connected.content.copycat.beam.CopycatBeamModel;
import com.hlysine.create_connected.content.copycat.block.CopycatBlockModel;
import com.hlysine.create_connected.content.copycat.board.CopycatBoardModel;
import com.hlysine.create_connected.content.copycat.fence.CopycatFenceModel;
import com.hlysine.create_connected.content.copycat.fencegate.CopycatFenceGateModel;
import com.hlysine.create_connected.content.copycat.slab.CopycatSlabModel;
import com.hlysine.create_connected.content.copycat.stairs.CopycatStairsModel;
import com.hlysine.create_connected.content.copycat.verticalstep.CopycatVerticalStepModel;
import com.hlysine.create_connected.content.copycat.wall.CopycatWallModel;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselModel;
import com.hlysine.create_connected.registries.CCBlocks;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.infrastructure.model.BracketedKineticBlockModel;

/**
 * Custom block models.
 *
 * <p>Registrate chained these onto registration in {@code CCBlocks} as
 * {@code .onRegister(CreateRegistrate.blockModel(() -> CopycatSlabModel::new))}. They cannot live
 * there: model classes are client-only and {@code CCBlocks} runs on both sides. Create Fly keeps the
 * equivalent in {@code AllModels}, a plain {@code Map<Block, BiFunction<BlockState, UnbakedRoot,
 * UnbakedRoot>>} that its {@code BlockStateModelLoaderMixin} consults while baking. The map is
 * public, so registering into it from here is all that is needed.
 *
 * <p>Writing the model classes was only half the job. Without this every copycat fell back to its
 * blockstate model, which carries no material and therefore no quads — the blocks were **invisible**
 * in the world and in the inventory, with no warning in the log, because a model that bakes to
 * nothing is not a missing model. Same failure mode as {@code CCBlockEntityRenders} before it was
 * filled in; if a class in this port is only ever referenced by its own file, suspect this.
 *
 * <p>The list is upstream's, taken from {@code git show b5e21592:…/CCBlocks.java | grep blockModel}.
 * Note it is not only the copycats: the fluid vessel binds two variants through static factories
 * rather than a constructor, and the shear pin borrows Create's own bracketed-kinetic model.
 */
public final class CCModels {
    private CCModels() {
    }

    public static void register() {
        AllModels.register(CCBlocks.COPYCAT_SLAB.get(), CopycatSlabModel::new);
        AllModels.register(CCBlocks.COPYCAT_BLOCK.get(), CopycatBlockModel::new);
        AllModels.register(CCBlocks.COPYCAT_BEAM.get(), CopycatBeamModel::new);
        AllModels.register(CCBlocks.COPYCAT_VERTICAL_STEP.get(), CopycatVerticalStepModel::new);
        AllModels.register(CCBlocks.COPYCAT_STAIRS.get(), CopycatStairsModel::new);
        AllModels.register(CCBlocks.COPYCAT_FENCE.get(), CopycatFenceModel::new);
        AllModels.register(CCBlocks.COPYCAT_WALL.get(), CopycatWallModel::new);
        AllModels.register(CCBlocks.COPYCAT_FENCE_GATE.get(), CopycatFenceGateModel::new);
        AllModels.register(CCBlocks.COPYCAT_BOARD.get(), CopycatBoardModel::new);

        AllModels.register(CCBlocks.FLUID_VESSEL.get(), FluidVesselModel.standard());
        AllModels.register(CCBlocks.CREATIVE_FLUID_VESSEL.get(), FluidVesselModel.creative());

        AllModels.register(CCBlocks.SHEAR_PIN.get(), BracketedKineticBlockModel::new);
    }
}
