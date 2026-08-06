package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.foundation.registrate.SharedProperties;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.brake.BrakeBlockEntity;
import com.hlysine.create_connected.content.brasschute.BrassChuteBlockEntity;
import com.hlysine.create_connected.content.brassgearbox.BrassGearboxBlockEntity;
import com.hlysine.create_connected.content.brassgearbox.BrassGearboxRenderer;
import com.hlysine.create_connected.content.brassgearbox.BrassGearboxVisual;
import com.hlysine.create_connected.content.centrifugalclutch.CentrifugalClutchBlockEntity;
import com.hlysine.create_connected.content.crankwheel.CrankWheelBlockEntity;
import com.hlysine.create_connected.content.crankwheel.CrankWheelVisual;
import com.hlysine.create_connected.content.dashboard.DashboardBlockEntity;
import com.hlysine.create_connected.content.dashboard.DashboardRenderer;
import com.hlysine.create_connected.content.fancatalyst.FanCatalystRotatingHeadBlockEntity;
import com.hlysine.create_connected.content.fancatalyst.FanCatalystRotatingHeadRenderer;
import com.hlysine.create_connected.content.fluidvessel.CreativeFluidVesselBlockEntity;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselBlockEntity;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselRenderer;
import com.hlysine.create_connected.content.freewheelclutch.FreewheelClutchBlockEntity;
import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlockEntity;
import com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlockEntity;
import com.hlysine.create_connected.content.invertedclutch.InvertedClutchBlockEntity;
import com.hlysine.create_connected.content.invertedgearshift.InvertedGearshiftBlockEntity;
import com.hlysine.create_connected.content.itemsilo.ItemSiloBlockEntity;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockEntity;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryRenderer;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryVisual;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeBlockEntity;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeDestinationBlockEntity;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeRenderer;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeVisual;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedAnalogLeverBlockEntity;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedAnalogLeverRenderer;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterBlockEntity;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlockEntity;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxBlockEntity;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxRenderer;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxVisual;
import com.hlysine.create_connected.content.sequencedpulsegenerator.SequencedPulseGeneratorBlockEntity;
import com.hlysine.create_connected.content.shearpin.ShearPinBlockEntity;
import com.hlysine.create_connected.content.shearpin.ShearPinVisual;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxBlockEntity;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxRenderer;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxVisual;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.client.content.kinetics.crank.HandCrankRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftRenderer;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftVisual;
import com.zurrtum.create.client.content.logistics.chute.ChuteRenderer;
import com.zurrtum.create.client.content.redstone.analogLever.AnalogLeverVisual;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.hlysine.create_connected.foundation.registrate.CCRegistrate;
import com.hlysine.create_connected.foundation.registrate.BlockEntityEntry;
import com.hlysine.create_connected.foundation.registrate.BlockEntry;
import com.zurrtum.create.AllBlockEntityTypes;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;

import java.util.List;

public class CCBlockEntityTypes {
    private static final CCRegistrate REGISTRATE = CreateConnected.getRegistrate();

    public static final BlockEntityEntry<SimpleKineticBlockEntity> ENCASED_CHAIN_COGWHEEL = REGISTRATE
            .blockEntity("encased_chain_cogwheel", SimpleKineticBlockEntity::new)
            .validBlocks(CCBlocks.ENCASED_CHAIN_COGWHEEL)
            .register();

    public static final BlockEntityEntry<CrankWheelBlockEntity> CRANK_WHEEL = REGISTRATE
            .blockEntity("crank_wheel", CrankWheelBlockEntity::new)
            .validBlocks(CCBlocks.CRANK_WHEEL, CCBlocks.LARGE_CRANK_WHEEL)
            .register();

    public static final BlockEntityEntry<ParallelGearboxBlockEntity> PARALLEL_GEARBOX = REGISTRATE
            .blockEntity("parallel_gearbox", ParallelGearboxBlockEntity::new)
            .validBlocks(CCBlocks.PARALLEL_GEARBOX)
            .register();

    public static final BlockEntityEntry<SixWayGearboxBlockEntity> SIX_WAY_GEARBOX = REGISTRATE
            .blockEntity("six_way_gearbox", SixWayGearboxBlockEntity::new)
            .validBlocks(CCBlocks.SIX_WAY_GEARBOX)
            .register();


    public static final BlockEntityEntry<OverstressClutchBlockEntity> OVERSTRESS_CLUTCH = REGISTRATE
            .blockEntity("overstress_clutch", OverstressClutchBlockEntity::new)
            .validBlocks(CCBlocks.OVERSTRESS_CLUTCH)
            .register();


    public static final BlockEntityEntry<ShearPinBlockEntity> SHEAR_PIN = REGISTRATE
            .blockEntity("shear_pin", ShearPinBlockEntity::new)
            .validBlocks(CCBlocks.SHEAR_PIN)
            .register();

    public static final BlockEntityEntry<InvertedClutchBlockEntity> INVERTED_CLUTCH = REGISTRATE
            .blockEntity("inverted_clutch", InvertedClutchBlockEntity::new)
            .validBlocks(CCBlocks.INVERTED_CLUTCH)
            .register();

    public static final BlockEntityEntry<InvertedGearshiftBlockEntity> INVERTED_GEARSHIFT = REGISTRATE
            .blockEntity("inverted_gearshift", InvertedGearshiftBlockEntity::new)
            .validBlocks(CCBlocks.INVERTED_GEARSHIFT)
            .register();

    public static final BlockEntityEntry<CentrifugalClutchBlockEntity> CENTRIFUGAL_CLUTCH = REGISTRATE
            .blockEntity("centrifugal_clutch", CentrifugalClutchBlockEntity::new)
            .validBlocks(CCBlocks.CENTRIFUGAL_CLUTCH)
            .register();

    public static final BlockEntityEntry<FreewheelClutchBlockEntity> FREEWHEEL_CLUTCH = REGISTRATE
            .blockEntity("freewheel_clutch", FreewheelClutchBlockEntity::new)
            .validBlocks(CCBlocks.FREEWHEEL_CLUTCH)
            .register();

    public static final BlockEntityEntry<KineticBridgeBlockEntity> KINETIC_BRIDGE = REGISTRATE
            .blockEntity("kinetic_bridge", KineticBridgeBlockEntity::new)
            .validBlocks(CCBlocks.KINETIC_BRIDGE)
            .register();

    public static final BlockEntityEntry<KineticBridgeDestinationBlockEntity> KINETIC_BRIDGE_DESTINATION = REGISTRATE
            .blockEntity("kinetic_bridge_destination", KineticBridgeDestinationBlockEntity::new)
            .validBlocks(CCBlocks.KINETIC_BRIDGE_DESTINATION)
            .register();

    public static final BlockEntityEntry<BrassGearboxBlockEntity> BRASS_GEARBOX = REGISTRATE
            .blockEntity("brass_gearbox", BrassGearboxBlockEntity::new)
            .validBlocks(CCBlocks.BRASS_GEARBOX)
            .register();

    public static final BlockEntityEntry<BrakeBlockEntity> BRAKE = REGISTRATE
            .blockEntity("brake", BrakeBlockEntity::new)
            .validBlocks(CCBlocks.BRAKE)
            .register();

    public static final BlockEntityEntry<KineticBatteryBlockEntity> KINETIC_BATTERY = REGISTRATE
            .blockEntity("kinetic_battery", KineticBatteryBlockEntity::new)
            .validBlocks(CCBlocks.KINETIC_BATTERY)
            .register();

    public static final BlockEntityEntry<ItemSiloBlockEntity> ITEM_SILO = REGISTRATE
            .blockEntity("item_silo", ItemSiloBlockEntity::new)
            .validBlocks(CCBlocks.ITEM_SILO)
            .register();

    public static final BlockEntityEntry<FluidVesselBlockEntity> FLUID_VESSEL = REGISTRATE
            .blockEntity("fluid_vessel", FluidVesselBlockEntity::new)
            .validBlocks(CCBlocks.FLUID_VESSEL)
            .register();

    public static final BlockEntityEntry<CreativeFluidVesselBlockEntity> CREATIVE_FLUID_VESSEL = REGISTRATE
            .blockEntity("creative_fluid_vessel", CreativeFluidVesselBlockEntity::new)
            .validBlocks(CCBlocks.CREATIVE_FLUID_VESSEL)
            .register();

    public static final BlockEntityEntry<InventoryAccessPortBlockEntity> INVENTORY_ACCESS_PORT = REGISTRATE
            .blockEntity("inventory_access_port", InventoryAccessPortBlockEntity::new)
            .validBlocks(CCBlocks.INVENTORY_ACCESS_PORT)
            .register();

    public static final BlockEntityEntry<InventoryBridgeBlockEntity> INVENTORY_BRIDGE = REGISTRATE
            .blockEntity("inventory_bridge", InventoryBridgeBlockEntity::new)
            .validBlocks(CCBlocks.INVENTORY_BRIDGE)
            .register();

    public static final BlockEntityEntry<SequencedPulseGeneratorBlockEntity> SEQUENCED_PULSE_GENERATOR = REGISTRATE
            .blockEntity("sequenced_pulse_generator", SequencedPulseGeneratorBlockEntity::new)
            .validBlocks(CCBlocks.SEQUENCED_PULSE_GENERATOR)
            .register();

    public static final BlockEntityEntry<LinkedTransmitterBlockEntity> LINKED_TRANSMITTER = REGISTRATE
            .blockEntity("linked_transmitter", LinkedTransmitterBlockEntity::new)
            .transform(b -> {
                CCBlocks.LINKED_BUTTONS.values().forEach(b::validBlock);
                return b;
            })
            .validBlocks(CCBlocks.LINKED_LEVER)
            .register();

    public static final BlockEntityEntry<LinkedAnalogLeverBlockEntity> LINKED_ANALOG_LEVER = REGISTRATE
            .blockEntity("linked_analog_lever", LinkedAnalogLeverBlockEntity::new)
            .validBlocks(CCBlocks.LINKED_ANALOG_LEVER)
            .register();

    public static final BlockEntityEntry<BrassChuteBlockEntity> BRASS_CHUTE = REGISTRATE
            .blockEntity("brass_chute", BrassChuteBlockEntity::new)
            .validBlocks(CCBlocks.BRASS_CHUTE)
            .register();

    public static final BlockEntityEntry<DashboardBlockEntity> DASHBOARD =
            REGISTRATE.blockEntity("dashboard", DashboardBlockEntity::new)
                    .validBlocks(CCBlocks.DASHBOARD)
                    .register();

    /**
     * Upstream registered its own copycat block entity type over Create's {@code CopycatBlockEntity}
     * class, which took the type as a constructor argument. Create Fly's version hard-codes
     * {@code AllBlockEntityTypes.COPYCAT} instead, so a separate type is no longer expressible
     * without duplicating the whole class. Connected's copycat blocks join Create's type instead,
     * through Fabric's {@code addValidBlock}.
     */
    private static void registerCopycatBlocks() {
        FabricBlockEntityType copycat = (FabricBlockEntityType) (Object) AllBlockEntityTypes.COPYCAT;
        for (BlockEntry<?> entry : List.of(
                CCBlocks.COPYCAT_BLOCK,
                CCBlocks.COPYCAT_SLAB,
                CCBlocks.COPYCAT_BEAM,
                CCBlocks.COPYCAT_VERTICAL_STEP,
                CCBlocks.COPYCAT_STAIRS,
                CCBlocks.COPYCAT_FENCE,
                CCBlocks.COPYCAT_FENCE_GATE,
                CCBlocks.COPYCAT_WALL,
                CCBlocks.COPYCAT_BOARD
        )) {
            copycat.addValidBlock(entry.get());
        }
    }

    public static final BlockEntityEntry<FanCatalystRotatingHeadBlockEntity> FAN_ENDING_CATALYST_DRAGON_HEAD = REGISTRATE
            .blockEntity("fan_ending_catalyst_dragon_head", FanCatalystRotatingHeadBlockEntity::new)
            .validBlocks(CCBlocks.FAN_ENDING_CATALYST_DRAGON_HEAD)
            .register();

    public static final BlockEntityEntry<FanCatalystRotatingHeadBlockEntity> FAN_EXPLODING_CATALYST = REGISTRATE
            .blockEntity("fan_exploding_catalyst", FanCatalystRotatingHeadBlockEntity::new)
            .validBlocks(CCBlocks.FAN_EXPLODING_CATALYST)
            .register();


    public static void register() {
        registerCopycatBlocks();
    }
}
