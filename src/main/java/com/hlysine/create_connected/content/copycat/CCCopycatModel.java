package com.hlysine.create_connected.content.copycat;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Base for Connected's copycat models under 26.2's model dispatch.
 * <p>
 * The old API handed a model one flat {@code List<BakedQuad>} per culling face and took a list back.
 * The new one works in {@link BlockStateModelPart}s, each of which carries its own quads split by
 * face, its ambient-occlusion flag and its particle material -- so every model has to walk the
 * material's parts, rebuild a {@link QuadCollection} per part, and preserve those two flags. Create
 * Fly writes that loop out in full in each of its copycat models; here it lives once, and
 * subclasses supply only the per-quad geometry through {@link #assembleQuads}.
 * <p>
 * Faces the block reports through {@code shouldFaceAlwaysRender} stay unculled, which is what
 * {@code gatherDirectionData} on the parent is for.
 */
public abstract class CCCopycatModel extends CopycatModel implements ISimpleCopycatModel {

    protected CCCopycatModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    protected void addPartsWithInfo(
            BlockAndTintGetter world,
            BlockPos pos,
            BlockState state,
            CopycatBlock block,
            BlockState material,
            RandomSource random,
            List<BlockStateModelPart> parts
    ) {
        List<BlockStateModelPart> materialParts = getMaterialParts(world, pos, material, random, getModelOf(material));
        if (materialParts.isEmpty())
            return;

        DirectionData directionData = gatherDirectionData(block, state);

        for (BlockStateModelPart part : materialParts) {
            QuadCollection.Builder builder = new QuadCollection.Builder();

            // Quads with no culling face of their own carry straight through.
            List<BakedQuad> assembled = new ArrayList<>();
            assembleQuads(state, null, part.getQuads(null), assembled);
            assembled.forEach(builder::addUnculledFace);

            for (Direction direction : Iterate.directions) {
                assembled.clear();
                assembleQuads(state, direction, part.getQuads(direction), assembled);
                if (directionData.isUncull(direction)) {
                    assembled.forEach(builder::addUnculledFace);
                } else {
                    for (BakedQuad quad : assembled) {
                        builder.addCulledFace(direction, quad);
                    }
                }
            }

            parts.add(new SimpleModelWrapper(builder.build(), part.useAmbientOcclusion(), part.particleMaterial()));
        }
    }

    /**
     * Crop and place one face's worth of the material's quads.
     *
     * @param state  the copycat's own state, for whatever shape properties the subclass reads
     * @param face   the culling face these quads came from, or null for the unculled bucket
     * @param source the material's quads for that face -- do not mutate
     * @param dest   where to append the assembled quads
     */
    protected abstract void assembleQuads(
            BlockState state,
            Direction face,
            List<BakedQuad> source,
            List<BakedQuad> dest
    );
}
