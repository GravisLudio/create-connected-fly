package com.hlysine.create_connected.content.copycat.fence;

import com.hlysine.create_connected.content.copycat.CCCopycatModel;
import com.zurrtum.create.catnip.data.Iterate;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;


import java.util.List;

import static com.hlysine.create_connected.content.copycat.ISimpleCopycatModel.MutableCullFace.*;
import static com.hlysine.create_connected.content.copycat.fence.CopycatFenceBlock.byDirection;

public class CopycatFenceModel extends CCCopycatModel {

    public CopycatFenceModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    protected void assembleQuads(BlockState state, Direction face, List<BakedQuad> source, List<BakedQuad> dest) {
        if (source.isEmpty())
            return;


        for (Direction direction : Iterate.horizontalDirections) {
            assemblePiece(source, dest, (int) direction.toYRot(), false,
                    vec3(6, 0, 6),
                    aabb(2, 16, 2),
                    cull(SOUTH | EAST)
            );
        }

        for (Direction direction : Iterate.horizontalDirections) {
            if (!state.getValue(byDirection(direction))) continue;

            int rot = (int) direction.toYRot();
            assemblePiece(source, dest, rot, false,
                    vec3(7, 6, 10),
                    aabb(1, 1, 6),
                    cull(UP | NORTH | EAST)
            );
            assemblePiece(source, dest, rot, false,
                    vec3(8, 6, 10),
                    aabb(1, 1, 6).move(15, 0, 0),
                    cull(UP | NORTH | WEST)
            );
            assemblePiece(source, dest, rot, false,
                    vec3(7, 7, 10),
                    aabb(1, 2, 6).move(0, 14, 0),
                    cull(DOWN | NORTH | EAST)
            );
            assemblePiece(source, dest, rot, false,
                    vec3(8, 7, 10),
                    aabb(1, 2, 6).move(15, 14, 0),
                    cull(DOWN | NORTH | WEST)
            );

            assemblePiece(source, dest, rot, false,
                    vec3(7, 12, 10),
                    aabb(1, 1, 6),
                    cull(UP | NORTH | EAST)
            );
            assemblePiece(source, dest, rot, false,
                    vec3(8, 12, 10),
                    aabb(1, 1, 6).move(15, 0, 0),
                    cull(UP | NORTH | WEST)
            );
            assemblePiece(source, dest, rot, false,
                    vec3(7, 13, 10),
                    aabb(1, 2, 6).move(0, 14, 0),
                    cull(DOWN | NORTH | EAST)
            );
            assemblePiece(source, dest, rot, false,
                    vec3(8, 13, 10),
                    aabb(1, 2, 6).move(15, 14, 0),
                    cull(DOWN | NORTH | WEST)
            );
        }
    }

}
