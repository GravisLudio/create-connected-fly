package com.hlysine.create_connected.registries;

/**
 * Stub. Not an implementation -- this records what is missing.
 * <p>
 * 26.2 removed {@code BlockColor} and {@code ItemColor}. Tinting is data-driven now: blocks declare
 * a {@code net.minecraft.client.color.block.BlockTintSource} and items a
 * {@code net.minecraft.client.color.item.ItemTintSource}, both serialised into the model JSON. There
 * is nothing left to register from code, so the two handlers that used to live here move into
 * assets:
 *
 * <table>
 *   <caption>What still needs writing</caption>
 *   <tr><th>Was</th><th>Becomes</th><th>Where</th></tr>
 *   <tr>
 *     <td>{@code waterBlockTint} -- {@code BiomeColors.getAverageWaterColor}</td>
 *     <td>a {@code minecraft:water} tint entry on the block model</td>
 *     <td>{@code assets/create_connected/models/block/fan_splashing_catalyst.json}</td>
 *   </tr>
 *   <tr>
 *     <td>{@code waterItemTint} -- NeoForge's fluid-type tint, which was just the constant
 *         water colour</td>
 *     <td>a {@code minecraft:constant} tint entry</td>
 *     <td>{@code assets/create_connected/items/fan_splashing_catalyst.json}</td>
 *   </tr>
 * </table>
 *
 * <b>Consequence until then: the fan washing catalyst renders untinted -- grey where it should be
 * water-coloured.</b> Cosmetic, and it raises no error at any point.
 */
public class CCColorHandlers {
    private CCColorHandlers() {
    }
}
