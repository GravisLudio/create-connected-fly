package com.hlysine.create_connected.content.fluidvessel;

import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.foundation.block.connected.CTSpriteShiftEntry;
import com.zurrtum.create.client.infrastructure.model.CTModel;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.BiFunction;

import static net.minecraft.core.Direction.Axis;

/**
 * A vessel hides the faces where it meets another vessel, and renders the rest regardless of what
 * vanilla would cull — the interior faces of a multiblock would otherwise disappear.
 * <p>
 * Upstream carried that decision between two methods using NeoForge's {@code ModelData}: one pass
 * computed which faces were connected and stashed it in a {@code ModelProperty}, a second read it
 * back while emitting quads. 26.2 has no such side channel, and does not need one — the new
 * {@code addPartsWithInfo} receives the world and position, so the connectivity check happens right
 * where it is used.
 * <p>
 * <b>Not registered yet.</b> Create Fly wires models with {@code AllModels.register(block, ...)};
 * see {@code CCConnectedTextures} for the rest of that debt.
 */
public class FluidVesselModel extends CTModel {

    public static BiFunction<BlockState, UnbakedRoot, UnbakedRoot> standard() {
        return of(AllSpriteShifts.FLUID_TANK, AllSpriteShifts.FLUID_TANK_TOP, AllSpriteShifts.FLUID_TANK_INNER);
    }

    public static BiFunction<BlockState, UnbakedRoot, UnbakedRoot> creative() {
        return of(AllSpriteShifts.CREATIVE_FLUID_TANK, AllSpriteShifts.CREATIVE_CASING, AllSpriteShifts.CREATIVE_CASING);
    }

    private static BiFunction<BlockState, UnbakedRoot, UnbakedRoot> of(
            CTSpriteShiftEntry side,
            CTSpriteShiftEntry top,
            CTSpriteShiftEntry inner
    ) {
        return (state, unbaked) -> new FluidVesselModel(state, unbaked, side, top, inner);
    }

    private FluidVesselModel(
            BlockState state,
            UnbakedRoot unbaked,
            CTSpriteShiftEntry side,
            CTSpriteShiftEntry top,
            CTSpriteShiftEntry inner
    ) {
        super(state, unbaked, new FluidVesselCTBehaviour(side, top, inner));
    }

    @Override
    public void addPartsWithInfo(
            BlockAndTintGetter world,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            List<BlockStateModelPart> parts
    ) {
        Axis axis = state.getValue(FluidVesselBlock.AXIS);
        boolean[] connected = new boolean[6];
        for (Direction d : Iterate.directions) {
            if (d.getAxis() == axis)
                continue;
            connected[d.get3DDataValue()] = ConnectivityHandler.isConnected(world, pos, pos.relative(d));
        }

        int[] indices = createCTData(world, pos, state);
        List<BlockStateModelPart> modelParts = new ObjectArrayList<>();
        model.collectParts(random, modelParts);

        for (BlockStateModelPart part : modelParts) {
            QuadCollection.Builder builder = new QuadCollection.Builder();

            for (BakedQuad quad : part.getQuads(null)) {
                builder.addUnculledFace(replaceQuad(state, random, indices[quad.direction().get3DDataValue()], quad));
            }

            for (Direction direction : Iterate.directions) {
                if (connected[direction.get3DDataValue()])
                    continue;
                // Deliberately unculled: a face between two blocks of the same multiblock is not
                // one vanilla should be deciding about.
                int index = indices[direction.get3DDataValue()];
                for (BakedQuad quad : part.getQuads(direction)) {
                    builder.addUnculledFace(replaceQuad(state, random, index, quad));
                }
            }

            parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }
}
