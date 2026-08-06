package com.hlysine.create_connected.content.fluidvessel;

import com.zurrtum.create.content.fluids.tank.CreativeFluidTankBlockEntity;
import com.zurrtum.create.foundation.fluid.FluidTank;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class CreativeFluidVesselBlockEntity extends FluidVesselBlockEntity {

    public CreativeFluidVesselBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * Create's {@code CreativeSmartFluidTank} is Create Fly's
     * {@code CreativeFluidTankBlockEntity.CreativeFluidTankInventory} -- same bottomless behaviour
     * and same update callback, expressed over {@link FluidTank} instead of the dropped
     * {@code SmartFluidTank}.
     */
    @Override
    protected FluidTank createInventory() {
        return new CreativeFluidTankBlockEntity.CreativeFluidTankInventory(getCapacityMultiplier(), this::onFluidStackChanged);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return false;
    }

}
