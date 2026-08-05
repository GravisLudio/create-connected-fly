package com.hlysine.create_connected.client;

/**
 * Block entity renderers and Flywheel visuals.
 *
 * <h2>Not implemented yet</h2>
 * Registrate chained these onto registration in {@code CCBlockEntityTypes}:
 * <pre>{@code
 * REGISTRATE.blockEntity("crank_wheel", CrankWheelBlockEntity::new)
 *     .visual(() -> CrankWheelVisual::new)
 *     .renderer(() -> HandCrankRenderer::new)
 *     .validBlocks(CCBlocks.CRANK_WHEEL)
 *     .register();
 * }</pre>
 * They cannot live there: renderers and visuals are client classes, and {@code CCBlockEntityTypes}
 * runs on both sides. Create Fly keeps the equivalent in {@code AllBlockEntityRenders}, which calls
 * {@code BlockEntityRenderers.register(type, factory)} -- the vanilla API -- from the client
 * entrypoint. Flywheel visuals register separately, via Create Fly's own visualisation registry.
 *
 * <h2>What has to be registered here</h2>
 * Renderer, then Flywheel visual where the original had one. {@code false} in the visual call was
 * Registrate's "do not render normally when a visual is active" flag.
 * <pre>
 * encased_chain_cogwheel   EncasedCogRenderer::small          EncasedCogVisual::small (false)
 * crank_wheel              HandCrankRenderer::new             CrankWheelVisual::new
 * parallel_gearbox         ParallelGearboxRenderer::new       ParallelGearboxVisual::new (false)
 * six_way_gearbox          SixWayGearboxRenderer::new         SixWayGearboxVisual::new (false)
 * brass_gearbox            BrassGearboxRenderer::new          BrassGearboxVisual::new (false)
 * overstress_clutch        SplitShaftRenderer::new            SplitShaftVisual::new (false)
 * inverted_clutch          SplitShaftRenderer::new            SplitShaftVisual::new (false)
 * inverted_gearshift       SplitShaftRenderer::new            SplitShaftVisual::new (false)
 * centrifugal_clutch       SplitShaftRenderer::new            SplitShaftVisual::new (false)
 * freewheel_clutch         SplitShaftRenderer::new            SplitShaftVisual::new (false)
 * shear_pin                BracketedKineticBlockEntityRenderer::new   ShearPinVisual::new (false)
 * kinetic_bridge           KineticBridgeRenderer::source      KineticBridgeVisual(ctx, be, pt, false) (false)
 * kinetic_bridge_destination KineticBridgeRenderer::destination KineticBridgeVisual(ctx, be, pt, true) (false)
 * fluid_vessel             FluidVesselRenderer::new           --
 * creative_fluid_vessel    FluidVesselRenderer::new           --
 * inventory_bridge         SmartBlockEntityRenderer::new      --
 * linked_transmitter       SmartBlockEntityRenderer::new      --
 * linked_analog_lever      LinkedAnalogLeverRenderer::new     AnalogLeverVisual::new
 * brass_chute              ChuteRenderer::new                 --
 * dashboard                DashboardRenderer::new             --
 * fan_ending_catalyst_dragon_head  FanCatalystRotatingHeadRenderer::dragon   --
 * fan_exploding_catalyst   FanCatalystRotatingHeadRenderer::creeper  --
 * </pre>
 * Note {@code EncasedCogRenderer} is one of the Create classes with no Create Fly equivalent, so
 * the encased chain cogwheel needs a substitute rather than a straight reference.
 *
 * <p>Consequence until this is filled in: these block entities fall back to their static block
 * model. Cogwheels and clutches will not turn, the fluid vessel shows no fluid, and the dashboard
 * shows no text. Nothing errors at any point -- it has to be seen in game.
 */
public final class CCBlockEntityRenders {
    private CCBlockEntityRenders() {
    }

    public static void register() {
        // TODO see class docs
    }
}
