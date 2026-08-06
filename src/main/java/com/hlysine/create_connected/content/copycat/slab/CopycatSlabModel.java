package com.hlysine.create_connected.content.copycat.slab;

import com.hlysine.create_connected.content.copycat.CCCopycatModel;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CopycatSlabModel extends CCCopycatModel {

    protected static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);

    public CopycatSlabModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    protected void assembleQuads(BlockState state, Direction face, List<BakedQuad> source, List<BakedQuad> dest) {
        if (source.isEmpty())
            return;

        Direction facing = state.getOptionalValue(CopycatSlabBlock.SLAB_TYPE).isPresent()
                ? CopycatSlabBlock.getApparentDirection(state)
                : Direction.UP;
        boolean isDouble = state.getOptionalValue(CopycatSlabBlock.SLAB_TYPE).orElse(SlabType.BOTTOM) == SlabType.DOUBLE;

        // 2 pieces
        for (boolean front : Iterate.trueAndFalse) {
            assemblePiece(facing, source, dest, front, false, isDouble);
        }

        // 2 more pieces for double slabs
        if (isDouble) {
            for (boolean front : Iterate.trueAndFalse) {
                assemblePiece(facing, source, dest, front, true, isDouble);
            }
        }
    }

    private static void assemblePiece(Direction facing, List<BakedQuad> templateQuads, List<BakedQuad> quads, boolean front, boolean topSlab, boolean isDouble) {
        Vec3 normal = Vec3.atLowerCornerOf(facing.getUnitVec3i());
        Vec3 normalScaled12 = normal.scale(12 / 16f);
        Vec3 normalScaledN8 = topSlab ? normal.scale((front ? 0 : -8) / 16f) : normal.scale((front ? 8 : 0) / 16f);
        float contract = 12;
        AABB bb = CUBE_AABB.contract(normal.x * contract / 16, normal.y * contract / 16, normal.z * contract / 16);
        if (!front)
            bb = bb.move(normalScaled12);

        for (BakedQuad quad : templateQuads) {
            Direction direction = quad.direction();

            if (front && direction == facing)
                continue;
            if (!front && direction == facing.getOpposite())
                continue;
            if (isDouble && topSlab && direction == facing)
                continue;
            if (isDouble && !topSlab && direction == facing.getOpposite())
                continue;

            quads.add(BakedModelHelper.cropAndMove(quad, bb, normalScaledN8));
        }
    }
}
