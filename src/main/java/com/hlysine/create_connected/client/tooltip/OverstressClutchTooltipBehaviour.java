package com.hlysine.create_connected.client.tooltip;

import com.hlysine.create_connected.ConnectedLang;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock.ClutchState;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlockEntity;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import net.minecraft.network.chat.Component;

import java.util.List;

import static net.minecraft.ChatFormatting.GOLD;
import static com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock.STATE;

/**
 * The hovering (no-goggles) tooltip, which used to be {@code addToTooltip} on the block entity. Both
 * tooltip kinds are client behaviours in 26.2 -- see {@link FluidVesselTooltipBehaviour}.
 */
public class OverstressClutchTooltipBehaviour extends KineticTooltipBehaviour<OverstressClutchBlockEntity> {

    public OverstressClutchTooltipBehaviour(OverstressClutchBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToTooltip(tooltip, isPlayerSneaking);

        if (blockEntity.getBlockState().getValue(STATE) == ClutchState.UNCOUPLED) {
            ConnectedLang.translate("gui.overstress_clutch.uncoupled")
                    .style(GOLD)
                    .forGoggles(tooltip);
            Component hint = ConnectedLang.translateDirect("gui.overstress_clutch.uncoupled_explanation");
            List<Component> cutString = TooltipHelper.cutTextComponent(hint, FontHelper.Palette.GRAY_AND_WHITE);
            for (Component component : cutString)
                ConnectedLang.builder()
                        .add(component.copy())
                        .forGoggles(tooltip);
            added = true;
        }

        return added;
    }
}
