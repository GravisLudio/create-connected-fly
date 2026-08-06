package com.hlysine.create_connected.content.copycat.block;

import com.hlysine.create_connected.content.copycat.ICopycatWithWrappedBlock;
import com.hlysine.create_connected.content.copycat.MigratingCopycatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class CopycatBlockBlock extends MigratingCopycatBlock implements ICopycatWithWrappedBlock {

    public CopycatBlockBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Block getWrappedBlock() {
        return Blocks.STONE;
    }

    @Override
    public boolean canConnectTexturesToward(BlockAndLightGetter reader, BlockPos fromPos, BlockPos toPos, BlockState state) {
        return true;
    }

    @Override
    public boolean canFaceBeOccluded(BlockState state, Direction face) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        return Shapes.block();
    }


    // Removed with the NeoForge block extensions: supportsExternalFaceHiding, hidesNeighborFace and
    // collisionExtendsVertically have no Fabric or vanilla equivalent in 26.2. Create Fly reached the
    // same conclusion and left its own copies commented out in CopycatPanelBlock and CopycatStepBlock.
    // Consequence: touching copycat blocks no longer hide each other's shared faces, so there is some
    // overdraw where they meet. Cosmetic, and it raises no error.
}

