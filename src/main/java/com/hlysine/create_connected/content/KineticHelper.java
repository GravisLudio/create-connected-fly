package com.hlysine.create_connected.content;

import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class KineticHelper {
    public static void updateKineticBlock(KineticBlockEntity kineticTE) {
        if (kineticTE.hasNetwork())
            kineticTE.getOrCreateNetwork().remove(kineticTE);
        kineticTE.detachKinetics();
        kineticTE.removeSource();
        BlockState state = kineticTE.getBlockState();
        BlockPos pos = kineticTE.getBlockPos();
        Level level = Objects.requireNonNull(kineticTE.getLevel());
        // Level.markAndNotifyBlock is gone; the block did not actually change, so the point was
        // only to push an update out to neighbours and the renderer.
        level.sendBlockUpdated(pos, state, state, 3);
        level.updateNeighborsAt(pos, state.getBlock(), null);

        // Ask this block entity to re-attach on its next tick. detachKinetics/removeSource above
        // leave it with no source and no reason to look for one: KineticBlockEntity.tick only calls
        // attachKinetics when needsSpeedUpdate() is true, and updateSpeed is otherwise set just once,
        // in the constructor. Without this the block stays stopped until it is broken and replaced --
        // which is exactly what re-running the constructor does, and exactly the workaround that made
        // the bug look like a rendering problem.
        //
        // Upstream got this for free from markAndNotifyBlock's flag-1 path and its 512-deep shape
        // update recursion, neither of which survives in the two calls above.
        kineticTE.updateSpeed = true;
        if (kineticTE instanceof GeneratingKineticBlockEntity generatingBlockEntity) {
            generatingBlockEntity.reActivateSource = true;
        }
    }
}
