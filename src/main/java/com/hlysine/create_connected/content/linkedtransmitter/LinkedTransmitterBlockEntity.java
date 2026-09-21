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
import com.hlysine.create_connected.registries.CCItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
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

    /**
     * Upstream did both of these in the linked button's and lever's {@code onRemove}. Since 1.21.5
     * the block's {@code affectNeighborsAfterRemoval} runs after the block entity is removed, so the
     * port's copy of that code never saw one: breaking a linked button or lever did not give the
     * Linked Transmitter back, and a lever broken while on kept its frequency powered.
     * <p>
     * Wrenching the transmitter off clears {@code containsBase} before the block turns back into the
     * vanilla one, so that conversion -- which also lands here -- does not hand out a second item.
     * Contraptions skip block entity side effects when they pick a block up, which covers upstream's
     * {@code !isMoving}.
     */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (containsBase)
            Block.popResource(level, pos, new ItemStack(CCItems.LINKED_TRANSMITTER.get()));
        // Before super, which destroys the behaviours -- transmit goes through the link behaviour.
        transmit(0);
        super.preRemoveSideEffects(pos, state);
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
