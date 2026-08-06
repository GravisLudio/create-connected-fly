package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.foundation.registrate.SharedProperties;
import com.hlysine.create_connected.foundation.registrate.CCRegistrate;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.compat.Mods;
import com.hlysine.create_connected.config.CStress;
import com.hlysine.create_connected.config.FeatureCategory;
import com.hlysine.create_connected.config.FeatureToggle;
import com.hlysine.create_connected.content.WrenchableBlock;
import com.hlysine.create_connected.content.brake.BrakeBlock;
import com.hlysine.create_connected.content.brasschute.BrassChuteBlock;
import com.hlysine.create_connected.content.brassgearbox.BrassGearboxBlock;
import com.hlysine.create_connected.content.centrifugalclutch.CentrifugalClutchBlock;
import com.hlysine.create_connected.content.chaincogwheel.ChainCogwheelBlock;
import com.hlysine.create_connected.content.copycat.beam.CopycatBeamBlock;
import com.hlysine.create_connected.content.copycat.beam.CopycatBeamModel;
import com.hlysine.create_connected.content.copycat.block.CopycatBlockBlock;
import com.hlysine.create_connected.content.copycat.block.CopycatBlockModel;
import com.hlysine.create_connected.content.copycat.board.CopycatBoardBlock;
import com.hlysine.create_connected.content.copycat.board.CopycatBoardModel;
import com.hlysine.create_connected.content.copycat.fence.CopycatFenceBlock;
import com.hlysine.create_connected.content.copycat.fence.CopycatFenceModel;
import com.hlysine.create_connected.content.copycat.fence.WrappedFenceBlock;
import com.hlysine.create_connected.content.copycat.fencegate.CopycatFenceGateBlock;
import com.hlysine.create_connected.content.copycat.fencegate.CopycatFenceGateModel;
import com.hlysine.create_connected.content.copycat.fencegate.WrappedFenceGateBlock;
import com.hlysine.create_connected.content.copycat.slab.CopycatSlabBlock;
import com.hlysine.create_connected.content.copycat.slab.CopycatSlabModel;
import com.hlysine.create_connected.content.copycat.stairs.CopycatStairsBlock;
import com.hlysine.create_connected.content.copycat.stairs.CopycatStairsModel;
import com.hlysine.create_connected.content.copycat.stairs.WrappedStairsBlock;
import com.hlysine.create_connected.content.copycat.verticalstep.CopycatVerticalStepBlock;
import com.hlysine.create_connected.content.copycat.verticalstep.CopycatVerticalStepModel;
import com.hlysine.create_connected.content.copycat.wall.CopycatWallBlock;
import com.hlysine.create_connected.content.copycat.wall.CopycatWallModel;
import com.hlysine.create_connected.content.copycat.wall.WrappedWallBlock;
import com.hlysine.create_connected.content.crankwheel.CrankWheelBlock;
import com.hlysine.create_connected.content.crankwheel.CrankWheelItem;
import com.hlysine.create_connected.content.crossconnector.CrossConnectorBlock;
import com.hlysine.create_connected.content.crossconnector.EncasedCrossConnectorBlock;
import com.hlysine.create_connected.content.dashboard.DashboardBlock;
import com.hlysine.create_connected.content.fancatalyst.FanCatalystRotatingHeadBlock;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselBlock;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselItem;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselModel;
import com.hlysine.create_connected.content.freewheelclutch.FreewheelClutchBlock;
import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlock;
import com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlock;
import com.hlysine.create_connected.content.invertedclutch.InvertedClutchBlock;
import com.hlysine.create_connected.content.invertedgearshift.InvertedGearshiftBlock;
import com.hlysine.create_connected.content.itemsilo.ItemSiloBlock;
import com.hlysine.create_connected.content.itemsilo.ItemSiloCTBehaviour;
import com.hlysine.create_connected.content.itemsilo.ItemSiloItem;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlock;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockItem;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryOverrides;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeBlock;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeBlockItem;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeDestinationBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedAnalogLeverBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedButtonBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedLeverBlock;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterItem;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxBlock;
import com.hlysine.create_connected.content.sequencedpulsegenerator.SequencedPulseGeneratorBlock;
import com.hlysine.create_connected.content.shearpin.ShearPinBlock;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxBlock;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.client.content.decoration.encasing.EncasedCTBehaviour;
import com.zurrtum.create.content.decoration.encasing.EncasingRegistry;
import com.zurrtum.create.content.fluids.tank.FluidTankMovementBehavior;
import com.zurrtum.create.client.infrastructure.model.BracketedKineticBlockModel;
import com.zurrtum.create.content.logistics.chute.ChuteItem;
import com.hlysine.create_connected.foundation.registrate.BlockEntry;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import static com.hlysine.create_connected.foundation.registrate.CCBehaviours.displaySource;
import static com.hlysine.create_connected.foundation.registrate.CCBehaviours.displayTarget;
import static com.hlysine.create_connected.foundation.registrate.CCBehaviours.movementBehaviour;
import static com.hlysine.create_connected.foundation.registrate.CCBehaviours.mountedFluidStorage;
import static com.hlysine.create_connected.foundation.registrate.CCBehaviours.mountedItemStorage;
import static com.hlysine.create_connected.foundation.registrate.TagGen.axeOrPickaxe;
import static com.hlysine.create_connected.foundation.registrate.TagGen.pickaxeOnly;

@SuppressWarnings("removal")
public class CCBlocks {
    private static final CCRegistrate REGISTRATE = CreateConnected.getRegistrate();

    public static final BlockEntry<ChainCogwheelBlock> ENCASED_CHAIN_COGWHEEL =
            REGISTRATE.block("encased_chain_cogwheel", ChainCogwheelBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
                    .transform(CStress.setNoImpact())
                    .transform(FeatureToggle.register(FeatureCategory.KINETIC))
                    .transform(axeOrPickaxe())

                    .item()

                    .register();

    public static final BlockEntry<CrankWheelBlock.Small> CRANK_WHEEL = REGISTRATE.block("crank_wheel", CrankWheelBlock.Small::new)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(axeOrPickaxe())

            .transform(CStress.setCapacity(8.0))
            .onRegister(BlockStressValues.setGeneratorSpeed(32))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .tag(AllBlockTags.BRITTLE)
            .item(CrankWheelItem::new)

            .register();

    public static final BlockEntry<CrankWheelBlock.Large> LARGE_CRANK_WHEEL = REGISTRATE.block("large_crank_wheel", CrankWheelBlock.Large::new)
            .initialProperties(SharedProperties::wooden)
            .properties(p -> p.mapColor(MapColor.PODZOL))
            .transform(axeOrPickaxe())

            .transform(CStress.setCapacity(8.0))
            .onRegister(BlockStressValues.setGeneratorSpeed(32))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .tag(AllBlockTags.BRITTLE)
            .item(CrankWheelItem::new)

            .register();

    public static final BlockEntry<ParallelGearboxBlock> PARALLEL_GEARBOX = REGISTRATE.block("parallel_gearbox", ParallelGearboxBlock::new)
            .initialProperties(SharedProperties::stone)
            // getPistonPushReaction is not an override any more -- it is baked into the properties.
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL).pushReaction(PushReaction.PUSH_ONLY))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            // Connected textures + casing connectivity moved to client/CCConnectedTextures.

            .item()

            .register();

    public static final BlockEntry<SixWayGearboxBlock> SIX_WAY_GEARBOX = REGISTRATE.block("six_way_gearbox", SixWayGearboxBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL).pushReaction(PushReaction.PUSH_ONLY))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .lang("6-way Gearbox")

            .item()

            .register();

    public static final BlockEntry<CrossConnectorBlock> CROSS_CONNECTOR = REGISTRATE.block("cross_connector", CrossConnectorBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())

            .item()

            .register();


    public static final BlockEntry<EncasedCrossConnectorBlock> ANDESITE_ENCASED_CROSS_CONNECTOR =
            REGISTRATE.block("andesite_encased_cross_connector", p -> new EncasedCrossConnectorBlock(p, () -> AllBlocks.ANDESITE_CASING))
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(CCBuilderTransformers.encasedCrossConnector("andesite", () -> AllSpriteShifts.ANDESITE_CASING))
                    .onRegister(b -> EncasingRegistry.addVariant(CCBlocks.CROSS_CONNECTOR.get(), b))
                    .transform(FeatureToggle.registerDependent(CCBlocks.CROSS_CONNECTOR, FeatureCategory.KINETIC))
                    .transform(axeOrPickaxe())
                    .register();

    public static final BlockEntry<EncasedCrossConnectorBlock> BRASS_ENCASED_CROSS_CONNECTOR =
            REGISTRATE.block("brass_encased_cross_connector", p -> new EncasedCrossConnectorBlock(p, () -> AllBlocks.BRASS_CASING))
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN))
                    .transform(CCBuilderTransformers.encasedCrossConnector("brass", () -> AllSpriteShifts.BRASS_CASING))
                    .onRegister(b -> EncasingRegistry.addVariant(CCBlocks.CROSS_CONNECTOR.get(), b))
                    .transform(FeatureToggle.registerDependent(CCBlocks.CROSS_CONNECTOR, FeatureCategory.KINETIC))
                    .transform(axeOrPickaxe())
                    .register();


    public static final BlockEntry<OverstressClutchBlock> OVERSTRESS_CLUTCH = REGISTRATE.block("overstress_clutch", OverstressClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())

            .item()

            .register();


    public static final BlockEntry<ShearPinBlock> SHEAR_PIN = REGISTRATE.block("shear_pin", ShearPinBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.mapColor(MapColor.METAL).forceSolidOn())
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(pickaxeOnly())

            .simpleItem()
            .register();

    public static final BlockEntry<InvertedClutchBlock> INVERTED_CLUTCH = REGISTRATE.block("inverted_clutch", InvertedClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())

            .item()

            .register();

    public static final BlockEntry<InvertedGearshiftBlock> INVERTED_GEARSHIFT = REGISTRATE.block("inverted_gearshift", InvertedGearshiftBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())

            .item()

            .register();


    public static final BlockEntry<CentrifugalClutchBlock> CENTRIFUGAL_CLUTCH = REGISTRATE.block("centrifugal_clutch", CentrifugalClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())

            .item()

            .register();


    public static final BlockEntry<FreewheelClutchBlock> FREEWHEEL_CLUTCH = REGISTRATE.block("freewheel_clutch", FreewheelClutchBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())

            .item()

            .register();


    public static final BlockEntry<KineticBridgeBlock> KINETIC_BRIDGE = REGISTRATE.block("kinetic_bridge", KineticBridgeBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (!(state.getBlock() instanceof KineticBridgeBlock))
                    return BlockMovementChecks.CheckResult.PASS;
                if (state.getValue(KineticBridgeBlock.FACING) != direction)
                    return BlockMovementChecks.CheckResult.PASS;
                return BlockMovementChecks.CheckResult.SUCCESS;
            }))
            .onRegister(b -> BlockMovementChecks.registerBrittleCheck(state -> {
                if (!(state.getBlock() instanceof KineticBridgeBlock))
                    return BlockMovementChecks.CheckResult.PASS;
                return BlockMovementChecks.CheckResult.SUCCESS;
            }))

            .item(KineticBridgeBlockItem::new)

            .register();


    public static final BlockEntry<KineticBridgeDestinationBlock> KINETIC_BRIDGE_DESTINATION = REGISTRATE.block("kinetic_bridge_destination", KineticBridgeDestinationBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN))
            .transform(FeatureToggle.registerDependent(CCBlocks.KINETIC_BRIDGE, FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (!(state.getBlock() instanceof KineticBridgeDestinationBlock))
                    return BlockMovementChecks.CheckResult.PASS;
                if (state.getValue(KineticBridgeDestinationBlock.FACING).getOpposite() != direction)
                    return BlockMovementChecks.CheckResult.PASS;
                return BlockMovementChecks.CheckResult.SUCCESS;
            }))

            .lang("Kinetic Bridge")
            .register();

    public static final BlockEntry<BrassGearboxBlock> BRASS_GEARBOX = REGISTRATE.block("brass_gearbox", BrassGearboxBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN).pushReaction(PushReaction.PUSH_ONLY))
            .transform(CStress.setNoImpact())
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())
            // Connected textures + casing connectivity moved to client/CCConnectedTextures.

            .item()

            .register();

    public static final BlockEntry<BrakeBlock> BRAKE = REGISTRATE.block("brake", BrakeBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.PODZOL))
            .transform(CStress.setNoImpact()) // active stress is a separate config
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(axeOrPickaxe())

            .item()

            .register();

    public static final BlockEntry<KineticBatteryBlock> KINETIC_BATTERY = REGISTRATE.block("kinetic_battery", KineticBatteryBlock::new)
            .initialProperties(SharedProperties::stone)
            .properties(p -> p.noOcclusion().mapColor(MapColor.TERRACOTTA_BROWN))
            .transform(CStress.setCapacity(32.0))
            .transform(CStress.setImpact(64.0))
            .transform(FeatureToggle.register(FeatureCategory.KINETIC))
            .transform(displaySource(CCDisplaySources.KINETIC_BATTERY))
            .transform(axeOrPickaxe())

            .item(KineticBatteryBlockItem::new)
            .properties(p -> p.component(CCDataComponents.KINETIC_BATTERY_CHARGE, 0.0))
            .onRegister(KineticBatteryBlockItem::registerModelOverrides)
            
            .build()
            .register();

    public static final BlockEntry<SequencedPulseGeneratorBlock> SEQUENCED_PULSE_GENERATOR =
            REGISTRATE.block("sequenced_pulse_generator", SequencedPulseGeneratorBlock::new)
                    .initialProperties(() -> Blocks.REPEATER)
                    .tag(AllBlockTags.SAFE_NBT)

                    .transform(FeatureToggle.register(FeatureCategory.REDSTONE))
                    .simpleItem()
                    .register();

    public static final Map<BlockSetType, BlockEntry<LinkedButtonBlock>> LINKED_BUTTONS = new HashMap<>();

    static {
        BlockSetType.values().forEach(type -> {
            Block button = RegisteredObjectsHelper.getBlock(Identifier.parse(type.name() + "_button"));
            if (button == null) return;
            if (!(button instanceof ButtonBlock buttonBlock))
                return;
            String namePath = type.name().contains(":") ? type.name().replace(':', '_') : type.name();
            LINKED_BUTTONS.put(type, REGISTRATE
                    .block("linked_" + namePath + "_button", properties -> new LinkedButtonBlock(properties, buttonBlock))
                    .initialProperties(() -> buttonBlock)
                    .tag(AllBlockTags.SAFE_NBT)
                    .transform(LinkedTransmitterItem.register())
                    .onRegister(PreciseItemUseOverrides::addBlock)

                    .register());
        });
    }

    public static final BlockEntry<LinkedLeverBlock> LINKED_LEVER = REGISTRATE
            .block("linked_lever", properties -> new LinkedLeverBlock(properties, (LeverBlock) Blocks.LEVER))
            .initialProperties(() -> Blocks.LEVER)
            .tag(AllBlockTags.SAFE_NBT)
            .transform(LinkedTransmitterItem.register())
            .onRegister(PreciseItemUseOverrides::addBlock)

            .register();

    public static final BlockEntry<LinkedAnalogLeverBlock> LINKED_ANALOG_LEVER = REGISTRATE
            .block("linked_analog_lever", properties -> new LinkedAnalogLeverBlock(properties, () -> AllBlocks.ANALOG_LEVER))
            .initialProperties(() -> Blocks.LEVER)
            .tag(AllBlockTags.SAFE_NBT)
            .transform(LinkedTransmitterItem.register())
            .onRegister(PreciseItemUseOverrides::addBlock)

            .register();

    public static final BlockEntry<WrenchableBlock> EMPTY_FAN_CATALYST = REGISTRATE.block("empty_fan_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_BLASTING_CATALYST = REGISTRATE.block("fan_blasting_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SMOKING_CATALYST = REGISTRATE.block("fan_smoking_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SPLASHING_CATALYST = REGISTRATE.block("fan_splashing_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))

            .lang("Fan Washing Catalyst")
            .tag(AllBlockTags.FAN_TRANSPARENT)
            .tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_SPLASHING)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_HAUNTING_CATALYST = REGISTRATE.block("fan_haunting_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 5)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .tag(AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_FREEZING_CATALYST = REGISTRATE.block("fan_freezing_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(() -> Mods.GARNISHED.isLoaded() || Mods.DREAMS_DESIRES.isLoaded() || Mods.DRAGONS_PLUS.isLoaded()))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SEETHING_CATALYST = REGISTRATE.block("fan_seething_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 12)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.DREAMS_DESIRES::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SANDING_CATALYST = REGISTRATE.block("fan_sanding_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(() -> Mods.DREAMS_DESIRES.isLoaded() || Mods.DRAGONS_PLUS.isLoaded()))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_ENRICHED_CATALYST = REGISTRATE.block("fan_enriched_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 13)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.NUCLEAR::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_ENDING_CATALYST_DRAGONS_BREATH = REGISTRATE.block("fan_ending_catalyst_dragons_breath", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 15)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.DRAGONS_PLUS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .lang("Fan Ending Catalyst with Dragon's Breath")
            .item()

            .lang("Fan Ending Catalyst with Dragon's Breath")
            .register();

    public static final BlockEntry<FanCatalystRotatingHeadBlock> FAN_ENDING_CATALYST_DRAGON_HEAD = REGISTRATE
            .block("fan_ending_catalyst_dragon_head", properties -> new FanCatalystRotatingHeadBlock(properties, CCBlockEntityTypes.FAN_ENDING_CATALYST_DRAGON_HEAD))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 0)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.DRAGONS_PLUS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .lang("Fan Ending Catalyst with Dragon Head")
            .item()

            .lang("Fan Ending Catalyst with Dragon Head")
            .register();

    public static final BlockEntry<WrenchableBlock> FAN_WITHERING_CATALYST = REGISTRATE.block("fan_withering_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 0)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(() -> false)) // No mods support bulk withering in 1.21.1

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_CHOCOLATE_COATING_CATALYST = REGISTRATE.block("fan_chocolate_coating_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_HONEY_COATING_CATALYST = REGISTRATE.block("fan_honey_coating_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<FanCatalystRotatingHeadBlock> FAN_EXPLODING_CATALYST = REGISTRATE
            .block("fan_exploding_catalyst", properties -> new FanCatalystRotatingHeadBlock(properties, CCBlockEntityTypes.FAN_EXPLODING_CATALYST))
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_RESONANCE_CATALYST = REGISTRATE.block("fan_resonance_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 3)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SCULKING_CATALYST = REGISTRATE.block("fan_sculking_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 4)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_PURIFYING_CATALYST = REGISTRATE.block("fan_purifying_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 14)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.MORE_CATALYSTS::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_TRANSMUTATION_CATALYST = REGISTRATE.block("fan_transmutation_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.SHIMMER::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_GLOOMING_CATALYST = REGISTRATE.block("fan_glooming_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(s -> 10)
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.SHIMMER::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final BlockEntry<WrenchableBlock> FAN_SOUL_STRIPPING_CATALYST = REGISTRATE.block("fan_soul_stripping_catalyst", WrenchableBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p
                    .mapColor(MapColor.TERRACOTTA_YELLOW)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .isRedstoneConductor((state, level, pos) -> false)
            )
            .transform(pickaxeOnly())
            .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
            .transform(FeatureToggle.addCondition(Mods.NETHER_INDUSTRY::isLoaded))

            .tag(AllBlockTags.FAN_TRANSPARENT)
            .item()

            .register();

    public static final Map<DyeColor, BlockEntry<WrenchableBlock>> FAN_DYEING_CATALYSTS = new TreeMap<>();

    static {
        for (DyeColor color : DyeColor.values()) {
            // DyeDepotCompat is excluded from the build (Dye Depot has no 26.2 release), and it
            // only ever returned a non-vanilla namespace for that mod's extra dyes. With it gone
            // every colour is vanilla, so the naming collapses to the vanilla branch. Restore the
            // call along with the compat file if Dye Depot is ported.
            FAN_DYEING_CATALYSTS.put(color, REGISTRATE.block(color.getName() + "_fan_dyeing_catalyst", WrenchableBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p
                            .mapColor(MapColor.TERRACOTTA_YELLOW)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                            .isRedstoneConductor((state, level, pos) -> false)
                    )
                    .transform(pickaxeOnly())
                    .transform(FeatureToggle.registerDependent(CCBlocks.EMPTY_FAN_CATALYST))
                    // isVanilla is always true now: DyeDepotCompat is excluded, so every colour
                    // uses the vanilla namespace. The second half of the condition collapses.
                    .transform(FeatureToggle.addCondition(() -> Mods.DRAGONS_PLUS.isLoaded() || Mods.GARNISHED.isLoaded()))

                    .lang(color.getName() + "_fan_dyeing_catalyst")
                    .tag(AllBlockTags.FAN_TRANSPARENT)
                    .asOptional()
                    .simpleItem()
                    .register());
        }
    }

    public static final BlockEntry<ItemSiloBlock> ITEM_SILO = REGISTRATE.block("item_silo", ItemSiloBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_BLUE).sound(SoundType.NETHERITE_BLOCK)
                    .explosionResistance(1200))
            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))

            // Connected textures moved to client/CCConnectedTextures (ItemSiloCTBehaviour).
            .transform(mountedItemStorage(CCMountedStorageTypes.SILO))
            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (state.getBlock() instanceof ItemSiloBlock)
                    return BlockMovementChecks.CheckResult.of(ConnectivityHandler.isConnected(world, pos, pos.relative(direction)));
                return BlockMovementChecks.CheckResult.PASS;
            }))
            .item(ItemSiloItem::new)
            .build()
            .register();

    public static final BlockEntry<FluidVesselBlock> FLUID_VESSEL = REGISTRATE.block("fluid_vessel", FluidVesselBlock::regular)
            .initialProperties(SharedProperties::copperMetal)
            .properties(p -> p.noOcclusion().isRedstoneConductor((p1, p2, p3) -> true).lightLevel(FluidVesselBlock::getLight))
            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))

            .onRegister(b -> BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
                if (state.getBlock() instanceof FluidVesselBlock)
                    return BlockMovementChecks.CheckResult.of(ConnectivityHandler.isConnected(world, pos, pos.relative(direction)));
                return BlockMovementChecks.CheckResult.PASS;
            }))
            .transform(displaySource(CCDisplaySources.BOILER_STATUS))
            .transform(mountedFluidStorage(CCMountedStorageTypes.FLUID_VESSEL))
            .onRegister(movementBehaviour(new FluidTankMovementBehavior()))
            .item(FluidVesselItem::new)
            
            .build()
            .register();

    public static final BlockEntry<FluidVesselBlock> CREATIVE_FLUID_VESSEL =
            REGISTRATE.block("creative_fluid_vessel", FluidVesselBlock::creative)
                    .initialProperties(SharedProperties::copperMetal)
                    .properties(p -> p.noOcclusion().mapColor(MapColor.COLOR_PURPLE).lightLevel(FluidVesselBlock::getLight))
                    .transform(pickaxeOnly())
                    .transform(FeatureToggle.registerDependent(FLUID_VESSEL))
                    .tag(AllBlockTags.SAFE_NBT)
                    .item(FluidVesselItem::new)
                    .properties(p -> p.rarity(Rarity.EPIC))
                    
                    .build()
                    .register();

    public static final BlockEntry<InventoryAccessPortBlock> INVENTORY_ACCESS_PORT =
            REGISTRATE.block("inventory_access_port", InventoryAccessPortBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion())
                    .transform(axeOrPickaxe())
                    .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))

                    .item()

                    .register();

    public static final BlockEntry<InventoryBridgeBlock> INVENTORY_BRIDGE =
            REGISTRATE.block("inventory_bridge", InventoryBridgeBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.TERRACOTTA_BROWN).noOcclusion())
                    .transform(axeOrPickaxe())
                    .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))

                    .item()

                    .register();

    public static final BlockEntry<BrassChuteBlock> BRASS_CHUTE = REGISTRATE.block("brass_chute", BrassChuteBlock::new)
            .initialProperties(SharedProperties::softMetal)
            .properties(p -> p.mapColor(MapColor.TERRACOTTA_YELLOW)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .noOcclusion()
                    .isSuffocating((state, level, pos) -> false))
            .transform(pickaxeOnly())
            .transform(FeatureToggle.register(FeatureCategory.LOGISTICS))

            .item(ChuteItem::new)

            .register();

    public static final BlockEntry<DashboardBlock> DASHBOARD =
            REGISTRATE.block("dashboard", DashboardBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.PODZOL))
                    .transform(axeOrPickaxe())
                    .transform(FeatureToggle.register(FeatureCategory.KINETIC))
                    .transform(displayTarget(CCDisplayTargets.DASHBOARD))

                    .item()

                    .register();

    public static final BlockEntry<CopycatSlabBlock> COPYCAT_SLAB =
            REGISTRATE.block("copycat_slab", CopycatSlabBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .tag(BlockTags.SLABS)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_SLAB.tag)

                    .register();

    public static final BlockEntry<CopycatBlockBlock> COPYCAT_BLOCK =
            REGISTRATE.block("copycat_block", CopycatBlockBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_BLOCK.tag)

                    .register();

    public static final BlockEntry<CopycatBeamBlock> COPYCAT_BEAM =
            REGISTRATE.block("copycat_beam", CopycatBeamBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_BEAM.tag)

                    .register();

    public static final BlockEntry<CopycatVerticalStepBlock> COPYCAT_VERTICAL_STEP =
            REGISTRATE.block("copycat_vertical_step", CopycatVerticalStepBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_VERTICAL_STEP.tag)

                    .register();

    public static final BlockEntry<CopycatStairsBlock> COPYCAT_STAIRS =
            REGISTRATE.block("copycat_stairs", CopycatStairsBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .tag(BlockTags.STAIRS)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_STAIRS.tag)

                    .register();

    public static final BlockEntry<WrappedStairsBlock> WRAPPED_COPYCAT_STAIRS =
            REGISTRATE.block("wrapped_copycat_stairs", p -> new WrappedStairsBlock(Blocks.STONE.defaultBlockState(), p))
                    .initialProperties(() -> Blocks.STONE_STAIRS)
                    .onRegister(b -> CopycatStairsBlock.stairs = b)
                    .tag(BlockTags.STAIRS)

                    .register();

    public static final BlockEntry<CopycatFenceBlock> COPYCAT_FENCE =
            REGISTRATE.block("copycat_fence", CopycatFenceBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .tag(BlockTags.FENCES)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_FENCE.tag)

                    .register();

    public static final BlockEntry<WrappedFenceBlock> WRAPPED_COPYCAT_FENCE =
            REGISTRATE.block("wrapped_copycat_fence", WrappedFenceBlock::new)
                    .initialProperties(() -> Blocks.OAK_FENCE)
                    .onRegister(b -> CopycatFenceBlock.fence = b)
                    .tag(BlockTags.FENCES)

                    .register();

    public static final BlockEntry<CopycatWallBlock> COPYCAT_WALL =
            REGISTRATE.block("copycat_wall", CopycatWallBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .properties(p -> p.forceSolidOn())
                    .tag(BlockTags.WALLS)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_WALL.tag)

                    .register();

    public static final BlockEntry<WrappedWallBlock> WRAPPED_COPYCAT_WALL =
            REGISTRATE.block("wrapped_copycat_wall", WrappedWallBlock::new)
                    .initialProperties(() -> Blocks.COBBLESTONE_WALL)
                    .onRegister(b -> CopycatWallBlock.wall = b)
                    .tag(BlockTags.WALLS)

                    .register();

    public static final BlockEntry<CopycatFenceGateBlock> COPYCAT_FENCE_GATE =
            REGISTRATE.block("copycat_fence_gate", CopycatFenceGateBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .properties(p -> p.forceSolidOn())
                    .tag(BlockTags.FENCE_GATES, BlockTags.UNSTABLE_BOTTOM_CENTER, AllBlockTags.MOVABLE_EMPTY_COLLIDER)
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_FENCE_GATE.tag)

                    .register();

    public static final BlockEntry<WrappedFenceGateBlock> WRAPPED_COPYCAT_FENCE_GATE =
            REGISTRATE.block("wrapped_copycat_fence_gate", p -> new WrappedFenceGateBlock(WoodType.OAK, p))
                    .initialProperties(() -> Blocks.OAK_FENCE_GATE)
                    .onRegister(b -> CopycatFenceGateBlock.fenceGate = b)
                    .tag(BlockTags.FENCE_GATES, BlockTags.UNSTABLE_BOTTOM_CENTER, AllBlockTags.MOVABLE_EMPTY_COLLIDER)

                    .register();

    public static final BlockEntry<CopycatBoardBlock> COPYCAT_BOARD =
            REGISTRATE.block("copycat_board", CopycatBoardBlock::new)
                    .transform(CCBuilderTransformers.copycat())
                    .transform(FeatureToggle.register(FeatureCategory.COPYCATS))
                    .item()
                    .tag(CCTags.Items.COPYCAT_BOARD.tag)

                    .register();

    public static void register() {
        // Simulated integration is excluded from the build; see the excludes in build.gradle.
    }

}
