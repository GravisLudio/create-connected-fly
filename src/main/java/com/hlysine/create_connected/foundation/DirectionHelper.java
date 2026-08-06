package com.hlysine.create_connected.foundation;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.Nullable;

/**
 * 26.2 removed {@code Direction.fromDelta}. The replacements vanilla kept
 * ({@code getNearest}, {@code getApproximateNearest}) always return a direction,
 * so they cannot express "this offset is not a single face".
 */
public class DirectionHelper {
    /**
     * The direction whose unit vector equals the given delta, or null if the delta
     * is not exactly one face offset.
     */
    @Nullable
    public static Direction fromDelta(int x, int y, int z) {
        for (Direction direction : Direction.values()) {
            Vec3i normal = direction.getUnitVec3i();
            if (normal.getX() == x && normal.getY() == y && normal.getZ() == z)
                return direction;
        }
        return null;
    }

    @Nullable
    public static Direction fromDelta(Vec3i delta) {
        return fromDelta(delta.getX(), delta.getY(), delta.getZ());
    }
}
