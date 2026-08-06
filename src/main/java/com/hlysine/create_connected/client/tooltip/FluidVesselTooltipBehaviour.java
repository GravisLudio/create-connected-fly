package com.hlysine.create_connected.client.tooltip;

import com.hlysine.create_connected.content.fluidvessel.FluidVesselBlockEntity;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * 26.2 moved goggle tooltips off the block entity: {@code GoggleOverlayRenderer} looks up a
 * {@link TooltipBehaviour} at the position and never touches the block entity, so a block entity
 * that merely implements {@code IHaveGoggleInformation} shows nothing at all -- silently, with no
 * compile error. Create Fly does the same for its own tank via {@code FluidTankTooltipBehaviour}.
 * <p>
 * The tooltip body stays on {@link FluidVesselBlockEntity}; this only routes it.
 */
public class FluidVesselTooltipBehaviour extends TooltipBehaviour<FluidVesselBlockEntity> implements IHaveGoggleInformation {

    public FluidVesselTooltipBehaviour(FluidVesselBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return blockEntity.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }
}
