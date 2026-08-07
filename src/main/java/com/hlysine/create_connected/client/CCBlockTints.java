package com.hlysine.create_connected.client;

import com.hlysine.create_connected.registries.CCBlocks;
import com.zurrtum.create.client.AllBlockTints;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSources;

import java.util.List;

/**
 * Block tints, replacing NeoForge's {@code RegisterColorHandlersEvent.Block}.
 *
 * <p>26.2 renamed {@code BlockColor} to {@code BlockTintSource} and moved the stock implementations
 * onto {@code BlockTintSources}, but the registration itself is unchanged --
 * {@code BlockColors.register(List<BlockTintSource>, Block...)} is still there. What went away is
 * the <em>event</em>: Fabric has no hook into {@code BlockColors.createDefault()}, so this is called
 * from a mixin instead, exactly as Create Fly reaches {@code AllBlockTints}.
 *
 * <p>{@code BlockTintSources.water()} is the old {@code BiomeColors.getAverageWaterColor}. Without
 * it the fan washing catalyst's water renders white rather than taking the biome's water colour.
 * The tint needs {@code tintindex: 0} on the faces it should colour, which
 * {@code fan_splashing_catalyst/block.json} carries on all six of its content faces.
 *
 * <p>The item is a separate problem and still open: item tints are data-driven through an
 * {@code ItemTintSource} in {@code assets/create_connected/items/fan_splashing_catalyst.json}, and
 * there is no code-side registration for them at all.
 */
public final class CCBlockTints {
    private CCBlockTints() {
    }

    public static void register(BlockColors blockColors) {
        blockColors.register(List.of(BlockTintSources.water()), CCBlocks.FAN_SPLASHING_CATALYST.get());

        // A copycat draws the material's quads but not the material's tint, so anything biome-tinted
        // came out with its raw texture -- grass is grey in the file and gets its green from the
        // tint. Create Fly's WrappedBlockColor forwards the lookup to whatever block is copied;
        // three entries because a tinted vanilla model can use up to three tint indices. Create Fly
        // registers exactly this for COPYCAT_STEP and COPYCAT_PANEL, and Connected's nine were
        // simply never added to that list.
        blockColors.register(
                List.of(
                        new AllBlockTints.WrappedBlockColor(blockColors, 0),
                        new AllBlockTints.WrappedBlockColor(blockColors, 1),
                        new AllBlockTints.WrappedBlockColor(blockColors, 2)
                ),
                CCBlocks.COPYCAT_SLAB.get(),
                CCBlocks.COPYCAT_BLOCK.get(),
                CCBlocks.COPYCAT_BEAM.get(),
                CCBlocks.COPYCAT_VERTICAL_STEP.get(),
                CCBlocks.COPYCAT_STAIRS.get(),
                CCBlocks.COPYCAT_FENCE.get(),
                CCBlocks.COPYCAT_WALL.get(),
                CCBlocks.COPYCAT_FENCE_GATE.get(),
                CCBlocks.COPYCAT_BOARD.get()
        );
    }
}
