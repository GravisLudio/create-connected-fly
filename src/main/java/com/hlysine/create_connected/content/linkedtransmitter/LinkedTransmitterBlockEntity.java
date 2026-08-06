package com.hlysine.create_connected.content.linkedtransmitter;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class LinkedTransmitterBlockEntity extends SmartBlockEntity {

    private int transmittedSignal;
    /**
     * set to false if the module item is already returned to player via wrenching
     */
    public boolean containsBase = true;
    private ServerLinkBehaviour link;

    public LinkedTransmitterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        createLink();
        behaviours.add(link);
    }

    protected void createLink() {
        // Server half only -- the value box slots go with the client LinkBehaviour, registered in
        // CCBlockEntityBehaviours. Same split as the scroll values.
        link = ServerLinkBehaviour.transmitter(this, this::getSignal);
    }

    @Override
    public void initialize() {
        super.initialize();
        transmit(getBlockState().getSignal(getLevel(), getBlockPos(), getBlockState().getValue(HorizontalDirectionalBlock.FACING)));
    }

    public int getSignal() {
        return transmittedSignal;
    }

    public void transmit(int strength) {
        transmittedSignal = strength;
        if (link != null)
            link.notifySignalChange();
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        tag.putInt("Transmit", transmittedSignal);
        super.write(tag, clientPacket);
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        if (level == null || level.isClientSide() || !link.newPosition)
            transmittedSignal = tag.getIntOr("Transmit", 0);
    }
}
