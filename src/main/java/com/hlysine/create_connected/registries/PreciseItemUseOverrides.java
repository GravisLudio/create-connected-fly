package com.hlysine.create_connected.registries;

import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;

/**
 * Blocks whose own interaction should win over the item held in hand.
 *
 * <h2>Currently inert</h2>
 * This used to forward each block to Create's {@code ItemUseOverrides}, which
 * {@code ItemUseOverridesMixin} then narrowed to precise placement. Create Fly has no
 * {@code ItemUseOverrides} at all -- the mechanism is gone, not renamed -- so the forward is
 * removed and the mixin is excluded from the build.
 * <p>
 * The set is still populated: it costs nothing and is exactly what a replacement would need. The
 * registration call sites are left in place for the same reason.
 * <p>
 * Consequence: right-clicking a linked transmitter block (linked button, lever, analog lever) while
 * holding a placeable item may place the item instead of operating the block. Behavioural rather
 * than a crash, and invisible at build time -- it needs checking in game.
 */
public class PreciseItemUseOverrides {

    public static final Set<Identifier> OVERRIDES = new HashSet<>();

    public static void addBlock(Block block) {
        OVERRIDES.add(RegisteredObjectsHelper.getKeyOrThrow(block));
    }
}
