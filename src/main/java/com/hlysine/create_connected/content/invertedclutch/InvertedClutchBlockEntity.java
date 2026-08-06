package com.hlysine.create_connected.content.invertedclutch;

import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Create Fly's ClutchBlockEntity hard-codes its own block entity type, so this extends the shared
 * SplitShaftBlockEntity instead -- the clutch adds nothing beyond the speed modifier, which is
 * overridden here anyway.
 */
public class InvertedClutchBlockEntity extends SplitShaftBlockEntity {

    public InvertedClutchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && !getBlockState().getValue(BlockStateProperties.POWERED))
                return 0;
        }
        return 1;
    }

}
