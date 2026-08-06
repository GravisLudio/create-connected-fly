package com.hlysine.create_connected.content.crankwheel;

import com.hlysine.create_connected.registries.CCPartialModels;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

public class CrankWheelBlockEntity extends HandCrankBlockEntity {
    public CrankWheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
        if (!ICogWheel.isLargeCog(state))
            return super.addPropagationLocations(block, state, neighbours);

        BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1))
                .forEach(offset -> {
                    if (offset.distSqr(BlockPos.ZERO) == 2)
                        neighbours.add(worldPosition.offset(offset));
                });
        return neighbours;
    }

    // Two client hooks that used to live on the block entity are gone in 26.2:
    //
    // getRenderedHandle: HandCrankRenderer no longer asks the block entity for its handle model,
    // it names AllPartialModels.HAND_CRANK_HANDLE directly. The crank wheel therefore needs its own
    // BlockEntityRenderer to draw its handle -- listed with the other missing renderers in
    // client/CCBlockEntityRenders. Flywheel visuals (CrankWheelVisual) still draw it correctly.
    //
    // tickAudio: moved to a client AudioBehaviour, so the cranking sound is
    // CrankWheelAudioBehaviour, registered in CCBlockEntityBehaviours.
}
