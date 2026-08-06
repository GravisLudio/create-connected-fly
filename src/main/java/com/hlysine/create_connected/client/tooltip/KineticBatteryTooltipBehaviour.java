package com.hlysine.create_connected.client.tooltip;

import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockEntity;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * The battery's own charge/consumption lines plus the generator stats that used to arrive through
 * {@code super.addToGoggleTooltip} on the block entity. See {@link FluidVesselTooltipBehaviour} for
 * why goggle tooltips are behaviours now.
 */
public class KineticBatteryTooltipBehaviour extends GeneratingKineticTooltipBehaviour<KineticBatteryBlockEntity> {

    public KineticBatteryTooltipBehaviour(KineticBatteryBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        blockEntity.addToGoggleTooltip(tooltip, isPlayerSneaking);
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return true;
    }
}
