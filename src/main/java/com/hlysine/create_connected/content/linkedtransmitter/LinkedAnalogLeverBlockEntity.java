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

    /** Unused: the type is read from the registry entry below, not carried through the constructor.
     *  The signature stays three-argument because BlockEntityBuilder's factory demands it. */
    public LinkedAnalogLeverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(pos, state);
    }

    // Create Fly's AnalogLeverBlockEntity only has a two-argument constructor and hard-codes
    // AllBlockEntityTypes.ANALOG_LEVER into it, so the type this instance actually belongs to is
    // resolved here instead. BlockEntity reads its private type field from exactly three places --
    // getType, typeHolder (which writes the saved id) and isValidBlockState -- and all three are
    // overridable, so redirecting them is enough. Miss one and the block entity saves under
    // Create's id and comes back as a plain analog lever, silently.
    //
    // Do not turn this back into a field assigned in the constructor. BlockEntity's own constructor
    // calls validateBlockState, so isValidBlockState below runs while super() is still executing --
    // before any field declared here has been assigned. A field is still null on that first call and
    // the game crashes the moment the block is placed.

    @Override
    public BlockEntityType<?> getType() {
        return CCBlockEntityTypes.LINKED_ANALOG_LEVER.get();
    }

    @Override
    public Holder<BlockEntityType<?>> typeHolder() {
        return getType().builtInRegistryHolder();
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return getType().isValid(state);
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
