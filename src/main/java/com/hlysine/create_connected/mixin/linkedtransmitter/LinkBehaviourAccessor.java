package com.hlysine.create_connected.mixin.linkedtransmitter;

import com.zurrtum.create.client.content.redstone.link.LinkBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Opens up the client {@code LinkBehaviour}'s two slot transforms.
 *
 * <p>Its only constructor hard-codes {@code RedstoneLinkFrequencySlot}, whose {@code getLocalOffset}
 * reads a six-valued {@code FACING} property off the block state. Create's redstone link has one; a
 * lever does not, so handing a linked transmitter the default slots crashes the client on the very
 * next tick — {@code LinkRenderer.tick} hit-tests both slots every frame, with no block check in
 * front of it.
 *
 * <p>Upstream passed its own slots at construction, through
 * {@code LinkBehaviour.transmitter(be, slots, ::getSignal)}. That overload is gone: Create Fly split
 * the behaviour, and the client half's slot fields are package-private with no way in. Widening them
 * is the smallest change that restores what upstream expressed directly.
 */
@Mixin(value = LinkBehaviour.class, remap = false)
public interface LinkBehaviourAccessor {

    @Accessor("firstSlot")
    void create_connected$setFirstSlot(ValueBoxTransform slot);

    @Accessor("secondSlot")
    void create_connected$setSecondSlot(ValueBoxTransform slot);
}
