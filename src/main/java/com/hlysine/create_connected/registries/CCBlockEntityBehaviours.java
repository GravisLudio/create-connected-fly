package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.ConnectedLang;
import com.hlysine.create_connected.content.ClutchValueBox;
import com.hlysine.create_connected.content.RotationScrollValueBehaviour;
import com.hlysine.create_connected.content.centrifugalclutch.CentrifugalClutchBlockEntity;
import com.hlysine.create_connected.client.CrankWheelAudioBehaviour;
import com.hlysine.create_connected.client.tooltip.FluidVesselTooltipBehaviour;
import com.hlysine.create_connected.client.tooltip.KineticBatteryTooltipBehaviour;
import com.hlysine.create_connected.client.tooltip.OverstressClutchTooltipBehaviour;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockEntity;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryValueBox;
import com.hlysine.create_connected.content.freewheelclutch.FreewheelClutchBlockEntity;
import com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlockEntity;
import com.hlysine.create_connected.content.inventorybridge.InventoryBridgeFilterSlot;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeBlockEntity;
import com.hlysine.create_connected.content.kineticbridge.StressImpactScrollValueBehaviour;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterFrequencySlot;
import com.hlysine.create_connected.mixin.linkedtransmitter.LinkBehaviourAccessor;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlockEntity;
import com.hlysine.create_connected.content.overstressclutch.TimeDelayScrollValueBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.content.redstone.link.LinkBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.RotationDirectionScrollBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

/**
 * Create Fly keeps client-side block entity behaviours in a registry keyed by block entity type,
 * separate from the server-side ones a block entity adds in {@code addBehaviours}. Value boxes --
 * the scroll widgets you point at with a wrench -- live on that side, so each of Connected's three
 * scroll values is registered here while its value lives in a {@code Server*} behaviour.
 * <p>
 * This mirrors Create Fly's own {@code AllBlockEntityBehaviours}.
 */
@Environment(EnvType.CLIENT)
public class CCBlockEntityBehaviours {

    @SafeVarargs
    @SuppressWarnings("unchecked")
    private static <T extends SmartBlockEntity> void add(
            BlockEntityType<T> type,
            Function<T, BlockEntityBehaviour<?>>... factories
    ) {
        for (Function<T, BlockEntityBehaviour<?>> factory : factories) {
            BlockEntityBehaviour.CLIENT_REGISTRY.add(
                    type,
                    (Function<SmartBlockEntity, BlockEntityBehaviour<?>>) factory
            );
        }
    }

    public static void register() {
        add(CCBlockEntityTypes.CENTRIFUGAL_CLUTCH.get(), CCBlockEntityBehaviours::centrifugalClutch);
        add(CCBlockEntityTypes.KINETIC_BRIDGE.get(), CCBlockEntityBehaviours::kineticBridge);
        add(CCBlockEntityTypes.OVERSTRESS_CLUTCH.get(), CCBlockEntityBehaviours::overstressClutch);
        add(CCBlockEntityTypes.KINETIC_BATTERY.get(),
                CCBlockEntityBehaviours::kineticBatteryDirection,
                KineticBatteryTooltipBehaviour::new);
        add(CCBlockEntityTypes.INVENTORY_BRIDGE.get(), CCBlockEntityBehaviours::inventoryBridgeFilters);
        add(CCBlockEntityTypes.FREEWHEEL_CLUTCH.get(), CCBlockEntityBehaviours::freewheelClutchDirection);
        add(CCBlockEntityTypes.CRANK_WHEEL.get(), CrankWheelAudioBehaviour::new);
        add(CCBlockEntityTypes.OVERSTRESS_CLUTCH.get(), OverstressClutchTooltipBehaviour::new);
        // Goggle tooltips are read off a TooltipBehaviour now, not off the block entity.
        add(CCBlockEntityTypes.FLUID_VESSEL.get(), FluidVesselTooltipBehaviour::new);
        add(CCBlockEntityTypes.CREATIVE_FLUID_VESSEL.get(), FluidVesselTooltipBehaviour::new);

        // The link half. ServerLinkBehaviour, which the block entities add themselves in
        // addBehaviours, carries the frequency and does the transmitting; this is the other half --
        // the two coloured slots you right-click to set that frequency. Without it a linked lever
        // works internally and offers the player no way to tune it, which reads as "the block is
        // dead". Create Fly registers the same class for its own REDSTONE_LINK.
        //
        // LINKED_TRANSMITTER covers the linked lever and all fourteen linked buttons; the analog
        // lever has its own block entity type.
        add(CCBlockEntityTypes.LINKED_TRANSMITTER.get(), CCBlockEntityBehaviours::link);
        add(CCBlockEntityTypes.LINKED_ANALOG_LEVER.get(), CCBlockEntityBehaviours::link);
    }

    /**
     * The client half of the redstone link, with Connected's own slot placement.
     * <p>
     * {@code LinkBehaviour}'s constructor installs {@code RedstoneLinkFrequencySlot}, which reads a
     * six-valued {@code FACING} off the block state. A lever does not have that property, so the
     * default slots crash the client one tick later inside {@code LinkRenderer.tick}. Upstream
     * avoided this by passing its own slots to {@code LinkBehaviour.transmitter(...)}; that overload
     * went with the server/client split, so the slots are swapped in afterwards instead — see
     * {@code mixin/linkedtransmitter/LinkBehaviourAccessor}.
     */
    private static BlockEntityBehaviour<?> link(SmartBlockEntity be) {
        LinkBehaviour behaviour = new LinkBehaviour(be);
        LinkBehaviourAccessor slots = (LinkBehaviourAccessor) behaviour;
        slots.create_connected$setFirstSlot(new LinkedTransmitterFrequencySlot(true));
        slots.create_connected$setSecondSlot(new LinkedTransmitterFrequencySlot(false));
        return behaviour;
    }

    private static BlockEntityBehaviour<?> kineticBatteryDirection(KineticBatteryBlockEntity be) {
        return new RotationDirectionScrollBehaviour(
                be,
                ConnectedLang.translateDirect("battery.rotation_direction"),
                new KineticBatteryValueBox(3)
        );
    }

    private static BlockEntityBehaviour<?> freewheelClutchDirection(FreewheelClutchBlockEntity be) {
        return new RotationDirectionScrollBehaviour(
                be,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"),
                new ClutchValueBox()
        );
    }

    private static BlockEntityBehaviour<?> inventoryBridgeFilters(InventoryBridgeBlockEntity be) {
        return new SidedFilteringBehaviour(be, new InventoryBridgeFilterSlot());
    }

    private static BlockEntityBehaviour<?> centrifugalClutch(CentrifugalClutchBlockEntity be) {
        return new RotationScrollValueBehaviour<>(
                ConnectedLang.translateDirect("centrifugal_clutch.speed_threshold"),
                be,
                new ClutchValueBox()
        );
    }

    private static BlockEntityBehaviour<?> kineticBridge(KineticBridgeBlockEntity be) {
        return new StressImpactScrollValueBehaviour<>(
                ConnectedLang.translateDirect("kinetic_bridge.stress_impact"),
                be,
                new KineticBatteryValueBox(8)
        );
    }

    private static BlockEntityBehaviour<?> overstressClutch(OverstressClutchBlockEntity be) {
        return new TimeDelayScrollValueBehaviour<>(
                Component.translatable("create_connected.overstress_clutch.uncouple_delay"),
                be,
                new CenteredSideValueBoxTransform((state, d) -> {
                    Direction.Axis axis = d.getAxis();
                    Direction.Axis bearingAxis = state.getValue(OverstressClutchBlock.AXIS);
                    return bearingAxis != axis;
                })
        );
    }
}
