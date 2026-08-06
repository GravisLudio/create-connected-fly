package com.hlysine.create_connected.content.linkedtransmitter;

import com.hlysine.create_connected.mixin.linkedtransmitter.AnalogLeverBlockEntityAccessor;
import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

public class LinkedAnalogLeverBlockEntity extends AnalogLeverBlockEntity {
    /**
     * set to false if the module item is already returned to player via wrenching
     */
    public boolean containsBase = true;
    private ServerLinkBehaviour link;

    private final BlockEntityType<?> type;

    public LinkedAnalogLeverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(pos, state);
        this.type = type;
    }

    // Create Fly's AnalogLeverBlockEntity only has a two-argument constructor and hard-codes
    // AllBlockEntityTypes.ANALOG_LEVER into it, so the type this instance actually belongs to is
    // carried here instead. BlockEntity reads its private type field from exactly three places --
    // getType, typeHolder (which writes the saved id) and isValidBlockState -- and all three are
    // overridable, so redirecting them is enough. Miss one and the block entity saves under
    // Create's id and comes back as a plain analog lever, silently.

    @Override
    public BlockEntityType<?> getType() {
        return type;
    }

    @Override
    public Holder<BlockEntityType<?>> typeHolder() {
        return type.builtInRegistryHolder();
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return type.isValid(state);
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

    private int lastChange() {
        return ((AnalogLeverBlockEntityAccessor) this).getLastChange();
    }

    @Override
    public void tick() {
        int prevTick = lastChange();
        super.tick();
        if (prevTick > 0 && lastChange() == 0) {
            if (!level.isClientSide()) {
                transmit();
                level.setBlock(worldPosition, getBlockState().setValue(BlockStateProperties.POWERED, getState() > 0), Block.UPDATE_ALL);
            }
        }
    }
}
