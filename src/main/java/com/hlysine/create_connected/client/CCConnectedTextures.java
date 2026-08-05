package com.hlysine.create_connected.client;

/**
 * Connected-texture and casing-connectivity setup for Connected's encased blocks.
 *
 * <h2>Not implemented yet</h2>
 * Under Registrate this was chained onto registration in {@code CCBlocks}:
 * <pre>{@code
 * .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(AllSpriteShifts.ANDESITE_CASING)))
 * .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, AllSpriteShifts.ANDESITE_CASING,
 *         (s, f) -> f.getAxis() == s.getValue(ParallelGearboxBlock.AXIS))))
 * }</pre>
 * Those calls could not stay there: {@code EncasedCTBehaviour} and the sprite shifts are client
 * classes, and {@code CCBlocks} runs on both sides. Create Fly keeps the equivalent wiring on the
 * client, split across two places:
 * <ul>
 *   <li>{@code AllModels.register(block, CTModel.of(AllCTBehaviours.ANDESITE_CASING))} -- binds the
 *       connected-texture model, replacing {@code connectedTextures(...)}.</li>
 *   <li>{@code AllCasings.make(block, spriteShift, predicate)} -- replaces
 *       {@code casingConnectivity(...)}; the predicate is the same {@code (state, face)} test.</li>
 * </ul>
 *
 * <h2>What has to be registered here</h2>
 * <table>
 *   <tr><th>Block</th><th>Sprite shift</th><th>Predicate</th></tr>
 *   <tr><td>{@code PARALLEL_GEARBOX}</td><td>{@code ANDESITE_CASING}</td>
 *       <td>{@code (s, f) -> f.getAxis() == s.getValue(ParallelGearboxBlock.AXIS)}</td></tr>
 *   <tr><td>{@code BRASS_GEARBOX}</td><td>{@code BRASS_CASING}</td>
 *       <td>{@code (s, f) -> f.getAxis() == s.getValue(BrassGearboxBlock.AXIS)}</td></tr>
 *   <tr><td>{@code ITEM_SILO}</td><td>{@code ItemSiloCTBehaviour} (no sprite shift)</td><td>none</td></tr>
 * </table>
 * Both gearboxes also went through {@code CCBuilderTransformers.encasedCrossConnector}, which
 * carries its own sprite shift -- check that transformer when wiring these up.
 *
 * <p>Consequence until this is filled in: the encased gearboxes render with unconnected casing
 * textures. Cosmetic, and it produces no error at any point, so it needs checking in game.
 */
public final class CCConnectedTextures {
    private CCConnectedTextures() {
    }

    public static void register() {
        // TODO see class docs
    }
}
