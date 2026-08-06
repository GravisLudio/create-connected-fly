package com.hlysine.create_connected.client;

import com.hlysine.create_connected.content.crankwheel.CrankWheelBlockEntity;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.content.kinetics.crank.HandCrankBlock;

/**
 * {@code tickAudio} left the block entity in 26.2 -- sounds are a client behaviour now. Mirrors
 * Create Fly's {@code HandCrankAudioBehaviour}, which is not reusable here because it is typed to
 * {@code HandCrankBlockEntity} and checks for Create's own hand crank block.
 */
public class CrankWheelAudioBehaviour extends KineticAudioBehaviour<CrankWheelBlockEntity> {

    public CrankWheelAudioBehaviour(CrankWheelBlockEntity be) {
        super(be);
    }

    @Override
    public void tickAudio() {
        super.tickAudio();
        if (blockEntity.inUse > 0 && AnimationTickHolder.getTicks() % 10 == 0) {
            if (!(blockEntity.getBlockState().getBlock() instanceof HandCrankBlock))
                return;
            AllSoundEvents.CRANKING.playAt(
                    blockEntity.getLevel(),
                    blockEntity.getBlockPos(),
                    blockEntity.inUse / 2.5f,
                    0.65f + (10 - blockEntity.inUse) / 10.0f,
                    true
            );
        }
    }
}
