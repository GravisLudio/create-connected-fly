package com.hlysine.create_connected.content.copycat.block;

import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * The plain copycat block renders its material unchanged, so there is nothing to crop.
 * <p>
 * Under the old API that still meant walking the material model's quads and cloning each one;
 * 26.2's model dispatch works in {@link BlockStateModelPart}s, and appending the material's parts
 * expresses the same thing directly.
 */
public class CopycatBlockModel extends CopycatModel {

    public CopycatBlockModel(BlockState state, UnbakedRoot unbaked) {
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
        addModelParts(world, pos, material, random, getModelOf(material), parts);
    }
}
