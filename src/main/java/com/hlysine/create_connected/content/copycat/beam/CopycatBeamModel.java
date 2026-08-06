package com.hlysine.create_connected.content.copycat.beam;

import com.hlysine.create_connected.content.copycat.CCCopycatModel;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static net.minecraft.core.Direction.Axis;
import static net.minecraft.core.Direction.AxisDirection;

public class CopycatBeamModel extends CCCopycatModel {
    protected static final AABB CUBE_AABB = new AABB(BlockPos.ZERO);

    public CopycatBeamModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    protected void assembleQuads(BlockState state, Direction face, List<BakedQuad> source, List<BakedQuad> dest) {
        if (source.isEmpty())
            return;

        Axis axis = state.getOptionalValue(CopycatBeamBlock.AXIS).orElse(Axis.Y);

        Vec3 normal = Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE).getUnitVec3i());
        Vec3 rowNormal = axis.isVertical() ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 columnNormal = axis.isVertical() || axis == Axis.X ? new Vec3(0, 0, 1) : new Vec3(1, 0, 0);
        AABB bb = CUBE_AABB.contract((1 - normal.x) * 12 / 16, (1 - normal.y) * 12 / 16, (1 - normal.z) * 12 / 16);

        // 4 Pieces
        for (boolean row : Iterate.trueAndFalse) {
            for (boolean column : Iterate.trueAndFalse) {

                AABB bb1 = bb;
                if (row)
                    bb1 = bb1.move(rowNormal.scale(12 / 16.0));
                if (column)
                    bb1 = bb1.move(columnNormal.scale(12 / 16.0));

                Vec3 offset = Vec3.ZERO;
                Vec3 rowShift = rowNormal.scale(row ? -4 / 16.0 : 4 / 16.0);
                Vec3 columnShift = columnNormal.scale(column ? -4 / 16.0 : 4 / 16.0);
                offset = offset.add(rowShift);
                offset = offset.add(columnShift);

                rowShift = rowShift.normalize();
                columnShift = columnShift.normalize();
                Vec3i rowShiftNormal = new Vec3i((int) rowShift.x, (int) rowShift.y, (int) rowShift.z);
                Vec3i columnShiftNormal = new Vec3i((int) columnShift.x, (int) columnShift.y, (int) columnShift.z);

                for (BakedQuad quad : source) {
                    Direction direction = quad.direction();

                    if (rowShiftNormal.equals(direction.getUnitVec3i()))
                        continue;
                    if (columnShiftNormal.equals(direction.getUnitVec3i()))
                        continue;

                    dest.add(BakedModelHelper.cropAndMove(quad, bb1, offset));
                }
            }
        }
    }
}
