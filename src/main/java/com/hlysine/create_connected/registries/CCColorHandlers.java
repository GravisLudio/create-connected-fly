package com.hlysine.create_connected.registries;

/**
 * Stub. Not an implementation -- this records what is missing.
 * <p>
 * 26.2 removed {@code BlockColor} and {@code ItemColor} and replaced them with
 * {@code net.minecraft.client.color.block.BlockTintSource} and
 * {@code net.minecraft.client.color.item.ItemTintSource}.
 *
 * <p><b>Corrected:</b> an earlier version of this comment claimed the replacement was purely
 * data-driven and that there was nothing left to register from code. That is wrong, and it was
 * inferred rather than checked. {@code BlockColors.register(List<BlockTintSource>, Block...)} is
 * still there, {@code BlockTintSources.water()} is exactly the old
 * {@code BiomeColors.getAverageWaterColor}, and Create Fly registers its own through
 * {@code AllBlockTints.register(BlockColors)}. What actually changed is the <em>hook</em>: there is
 * no registration event on Fabric, so Create Fly calls it from
 * {@code client/mixin/BlockColorsMixin} instead. Mirroring that is the shape to follow here.
 *
 * <table>
 *   <caption>What still needs writing</caption>
 *   <tr><th>Was</th><th>Becomes</th></tr>
 *   <tr>
 *     <td>{@code waterBlockTint} -- {@code BiomeColors.getAverageWaterColor}</td>
 *     <td>{@code blockColors.register(List.of(BlockTintSources.water()), FAN_SPLASHING_CATALYST)},
 *         from a mixin into {@code BlockColors}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code waterItemTint} -- NeoForge's fluid-type tint, which was just the constant
 *         water colour</td>
 *     <td>a {@code minecraft:constant} tint entry in
 *         {@code assets/create_connected/items/fan_splashing_catalyst.json}</td>
 *   </tr>
 * </table>
 *
 * <p><b>Do this second, not first.</b> {@code fan_splashing_catalyst}'s block model still uses
 * NeoForge's {@code composite} loader, so on Fabric it currently bakes to an empty model -- there
 * are no tinted faces to colour yet. See PORTING.md, <i>Five models still carry NeoForge's composite
 * loader</i>.
 */
public class CCColorHandlers {
    private CCColorHandlers() {
    }
}
