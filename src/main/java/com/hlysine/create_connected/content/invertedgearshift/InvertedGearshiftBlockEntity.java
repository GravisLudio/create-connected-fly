package com.hlysine.create_connected.content.invertedgearshift;

import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * See {@link com.hlysine.create_connected.content.invertedclutch.InvertedClutchBlockEntity} -- the
 * gearshift is the same shape and its whole behaviour is the speed modifier below.
 */
public class InvertedGearshiftBlockEntity extends SplitShaftBlockEntity {

    public InvertedGearshiftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && !getBlockState().getValue(BlockStateProperties.POWERED))
                return -1;
        }
        return 1;
    }

}
