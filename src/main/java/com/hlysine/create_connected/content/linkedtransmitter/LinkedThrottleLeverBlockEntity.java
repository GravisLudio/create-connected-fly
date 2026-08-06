package com.hlysine.create_connected.content.linkedtransmitter;

import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.simulated_team.simulated.content.blocks.throttle_lever.ThrottleLeverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class LinkedThrottleLeverBlockEntity extends ThrottleLeverBlockEntity {
    /**
     * set to false if the module item is already returned to player via wrenching
     */
    public boolean containsBase = true;
    private ServerLinkBehaviour link;

    public LinkedThrottleLeverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        createLink();
        behaviours.add(link);
    }

    @Override
    public void initialize() {
        super.initialize();
        transmit();
    }

    protected void createLink() {
        // Server half only -- the value box slots go with the client LinkBehaviour, registered in
        // CCBlockEntityBehaviours. Same split as the scroll values.
        link = ServerLinkBehaviour.transmitter(this, this::getState);
    }

    public void transmit() {
        if (link != null)
            link.notifySignalChange();
    }

    @Override
    public void setSignal(int signal) {
        super.setSignal(signal);
        transmit();
        level.setBlock(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, getState() > 0), Block.UPDATE_ALL);
    }
}
