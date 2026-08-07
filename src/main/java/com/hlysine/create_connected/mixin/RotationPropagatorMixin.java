package com.hlysine.create_connected.mixin;

import com.hlysine.create_connected.content.IConnectionForwardingBlock;
import com.hlysine.create_connected.content.ISplitShaftBlockEntity;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(RotationPropagator.class)
public abstract class RotationPropagatorMixin {

    @Inject(
            method = "getAxisModifier",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void getSplitShaftModifier(KineticBlockEntity be, Direction direction, CallbackInfoReturnable<Float> cir) {
        if (!(be.hasSource() || be.isSource()))
            return;

        if (be instanceof ISplitShaftBlockEntity splitShaftBE) {
            cir.setReturnValue(splitShaftBE.getRotationSpeedModifier(direction));
        }
    }

    @Inject(
            method = "getPotentialNeighbourLocations",
            at = @At("RETURN")
    )
    private static void forwardConnection(KineticBlockEntity be, CallbackInfoReturnable<List<BlockPos>> cir) {
        List<BlockPos> originalPositions = cir.getReturnValue();
        List<BlockPos> positions = new ArrayList<>(originalPositions);
        Level level = be.getLevel();
        for (int i = 0; i < positions.size(); i++) {
            BlockPos sourcePos = be.getBlockPos();
            BlockPos neighborPos = positions.get(i);
            if (neighborPos.getClass() != BlockPos.class)
                continue;

            while (!sourcePos.equals(neighborPos) && level.getBlockState(neighborPos).getBlock() instanceof IConnectionForwardingBlock forwardingBlock) {
                BlockPos tempSource = sourcePos;
                sourcePos = neighborPos;
                neighborPos = forwardingBlock.forwardConnection(level, tempSource, sourceState(be, level, tempSource), neighborPos);
            }

            positions.set(i, neighborPos);
        }
        originalPositions.clear();
        originalPositions.addAll(positions);
    }

    /**
     * The state to hand {@code forwardConnection} for the block the hop starts from.
     * <p>
     * Reading the world is wrong for the first hop. This runs from
     * {@code getPotentialNeighbourLocations}, and one of its callers is
     * {@code RotationPropagator.handleRemoved} by way of {@code KineticBlockEntity.detachKinetics},
     * which fires from the block entity's {@code remove()} -- after the chunk has already replaced
     * the block with air. {@code CrossConnectorBlock.forwardConnection} opens with an
     * {@code instanceof IRotate} test on this state, so air makes it refuse to forward and the
     * chain stops at the connector: attaching a source propagated across the connector, detaching
     * one did not, and everything past it kept its old speed for good.
     * <p>
     * The block entity's cached state is still the real one at that point.
     * {@code CrossConnectorBlock.updateConnections} guards the same way, for the same reason.
     */
    @Unique
    private static BlockState sourceState(KineticBlockEntity be, Level level, BlockPos pos) {
        return pos.equals(be.getBlockPos()) ? be.getBlockState() : level.getBlockState(pos);
    }
}
