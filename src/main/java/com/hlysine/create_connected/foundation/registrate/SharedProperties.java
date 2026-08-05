package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * Base blocks to copy properties from, replacing Create's {@code SharedProperties}.
 * <p>
 * Create Fly has no equivalent -- it inlines {@code Properties.ofFullCopy(Blocks.ANDESITE)} at each
 * registration instead. These keep Connected's {@code .initialProperties(SharedProperties::stone)}
 * call sites working, and mirror what upstream Create used as the base for each material.
 */
public final class SharedProperties {
    private SharedProperties() {
    }

    public static Block stone() {
        return Blocks.ANDESITE;
    }

    public static Block wooden() {
        return Blocks.OAK_PLANKS;
    }

    public static Block softMetal() {
        return Blocks.GOLD_BLOCK;
    }

    public static Block copperMetal() {
        // In 26.2 Blocks.COPPER_BLOCK is a WeatheringCopperCollection, not a Block.
        // Create Fly reaches the plain variant the same way.
        return Blocks.COPPER_BLOCK.weathering().unaffected();
    }
}
