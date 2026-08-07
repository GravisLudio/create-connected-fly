package com.hlysine.create_connected.client;

import com.hlysine.create_connected.content.brassgearbox.BrassGearboxRenderer;
import com.hlysine.create_connected.content.brassgearbox.BrassGearboxVisual;
import com.hlysine.create_connected.content.chaincogwheel.ChainCogwheelRenderer;
import com.hlysine.create_connected.content.crankwheel.CrankWheelRenderer;
import com.hlysine.create_connected.content.crankwheel.CrankWheelVisual;
import com.hlysine.create_connected.content.dashboard.DashboardRenderer;
import com.hlysine.create_connected.content.fancatalyst.FanCatalystRotatingHeadRenderer;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselRenderer;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryRenderer;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryVisual;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeRenderer;
import com.hlysine.create_connected.content.kineticbridge.KineticBridgeVisual;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedAnalogLeverRenderer;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxRenderer;
import com.hlysine.create_connected.content.parallelgearbox.ParallelGearboxVisual;
import com.hlysine.create_connected.content.shearpin.ShearPinVisual;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxRenderer;
import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxVisual;
import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.encased.EncasedCogVisual;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftRenderer;
import com.zurrtum.create.client.content.kinetics.transmission.SplitShaftVisual;
import com.zurrtum.create.client.content.logistics.chute.ChuteRenderer;
import com.zurrtum.create.client.content.redstone.analogLever.AnalogLeverVisual;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

/**
 * Block entity renderers and Flywheel visuals.
 *
 * <p>Registrate chained these onto registration in {@code CCBlockEntityTypes}. They cannot live
 * there: renderers and visuals are client classes, and {@code CCBlockEntityTypes} runs on both
 * sides. Create Fly keeps the equivalent in {@code AllBlockEntityRenders}, whose helpers this class
 * calls directly — {@code visual} registers renderer plus visual, {@code render} registers a
 * renderer alone.
 *
 * <h2>Reading the upstream flag</h2>
 * Upstream spelled these {@code .visual(() -> XVisual::new, false)}, where the boolean was
 * Registrate's <em>renderNormally</em>. Create Fly's {@code visual(...)} sets
 * {@code skipVanillaRender = true} and its {@code normal(...)} sets it to {@code false}, so
 * {@code renderNormally = false} is {@code visual(...)}.
 *
 * <p>Two of upstream's calls — the crank wheel and the linked analog lever — omitted the boolean.
 * Rather than infer Registrate's default, note that their Create counterparts used the identical
 * spelling ({@code HAND_CRANK}, {@code ANALOG_LEVER}) and that Create Fly registers <em>both</em>
 * with {@code visual(...)}. So every entry here is {@code visual} or {@code render}; nothing needs
 * {@code normal}.
 *
 * <h2>Two substitutions</h2>
 * <ul>
 *   <li>{@code EncasedCogRenderer::small} has no Create Fly equivalent — that class was split into
 *       {@code EncasedSmallCogRenderer} and {@code EncasedLargeCogRenderer}, and <em>neither</em>
 *       fits: both are written against {@code EncasedCogwheelBlock} and read its {@code TOP_SHAFT}
 *       and {@code BOTTOM_SHAFT}, which {@code ChainCogwheelBlock} does not have. See
 *       {@link com.hlysine.create_connected.content.chaincogwheel.ChainCogwheelRenderer}.</li>
 *   <li>{@code HandCrankRenderer} no longer asks the block entity for its model, so registering it
 *       for the crank wheel drew <em>Create's</em> hand crank. {@link
 *       com.hlysine.create_connected.content.crankwheel.CrankWheelRenderer} replaces it.</li>
 * </ul>
 *
 * <p>The list was taken from upstream's {@code CCBlockEntityTypes} at commit {@code b5e21592} —
 * {@code git show b5e21592:...} — not from notes. The hand-written table this class used to carry
 * was missing {@code brake} and {@code kinetic_battery}.
 */
public final class CCBlockEntityRenders {
    private CCBlockEntityRenders() {
    }

    public static void register() {
        AllBlockEntityRenders.visual(CCBlockEntityTypes.ENCASED_CHAIN_COGWHEEL.get(), ChainCogwheelRenderer::new, EncasedCogVisual::small);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.CRANK_WHEEL.get(), CrankWheelRenderer::new, CrankWheelVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.PARALLEL_GEARBOX.get(), ParallelGearboxRenderer::new, ParallelGearboxVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.SIX_WAY_GEARBOX.get(), SixWayGearboxRenderer::new, SixWayGearboxVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.BRASS_GEARBOX.get(), BrassGearboxRenderer::new, BrassGearboxVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.SHEAR_PIN.get(), BracketedKineticBlockEntityRenderer::new, ShearPinVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.KINETIC_BATTERY.get(), KineticBatteryRenderer::new, KineticBatteryVisual::new);

        // Six block entities share Create's split-shaft pair, exactly as upstream did.
        AllBlockEntityRenders.visual(CCBlockEntityTypes.OVERSTRESS_CLUTCH.get(), SplitShaftRenderer::new, SplitShaftVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.INVERTED_CLUTCH.get(), SplitShaftRenderer::new, SplitShaftVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.INVERTED_GEARSHIFT.get(), SplitShaftRenderer::new, SplitShaftVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.CENTRIFUGAL_CLUTCH.get(), SplitShaftRenderer::new, SplitShaftVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.FREEWHEEL_CLUTCH.get(), SplitShaftRenderer::new, SplitShaftVisual::new);
        AllBlockEntityRenders.visual(CCBlockEntityTypes.BRAKE.get(), SplitShaftRenderer::new, SplitShaftVisual::new);

        // One visual class for both ends of the bridge; the boolean picks which end it is.
        AllBlockEntityRenders.visual(
                CCBlockEntityTypes.KINETIC_BRIDGE.get(),
                KineticBridgeRenderer::source,
                (context, blockEntity, partialTick) -> new KineticBridgeVisual(context, blockEntity, partialTick, false));
        AllBlockEntityRenders.visual(
                CCBlockEntityTypes.KINETIC_BRIDGE_DESTINATION.get(),
                KineticBridgeRenderer::destination,
                (context, blockEntity, partialTick) -> new KineticBridgeVisual(context, blockEntity, partialTick, true));

        AllBlockEntityRenders.visual(CCBlockEntityTypes.LINKED_ANALOG_LEVER.get(), LinkedAnalogLeverRenderer::new, AnalogLeverVisual::new);

        // Renderer only — these had no Flywheel visual upstream either.
        AllBlockEntityRenders.render(CCBlockEntityTypes.FLUID_VESSEL.get(), FluidVesselRenderer::new);
        AllBlockEntityRenders.render(CCBlockEntityTypes.CREATIVE_FLUID_VESSEL.get(), FluidVesselRenderer::new);
        AllBlockEntityRenders.render(CCBlockEntityTypes.INVENTORY_BRIDGE.get(), SmartBlockEntityRenderer::new);
        AllBlockEntityRenders.render(CCBlockEntityTypes.LINKED_TRANSMITTER.get(), SmartBlockEntityRenderer::new);
        AllBlockEntityRenders.render(CCBlockEntityTypes.BRASS_CHUTE.get(), ChuteRenderer::new);
        AllBlockEntityRenders.render(CCBlockEntityTypes.DASHBOARD.get(), DashboardRenderer::new);
        AllBlockEntityRenders.render(CCBlockEntityTypes.FAN_ENDING_CATALYST_DRAGON_HEAD.get(), FanCatalystRotatingHeadRenderer::dragon);
        AllBlockEntityRenders.render(CCBlockEntityTypes.FAN_EXPLODING_CATALYST.get(), FanCatalystRotatingHeadRenderer::creeper);
    }
}
