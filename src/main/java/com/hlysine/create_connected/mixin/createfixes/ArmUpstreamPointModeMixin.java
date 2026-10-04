package com.hlysine.create_connected.mixin.createfixes;

import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

/**
 * A Create Fly bug, fixed here: arms in schematics saved with Create lost every target. Create
 * writes a point's mode with {@code NBTHelper.writeEnum}, so {@code "TAKE"}/{@code "DEPOSIT"};
 * Create Fly decodes it with {@code StringRepresentable.fromEnum}, which only knows the lowercase
 * serialized names, and drops each point that fails. Schematics carry only the points
 * ({@code writeSafe}), so the mode is all that needs translating.
 */
@Mixin(value = ArmBlockEntity.class, remap = false)
public class ArmUpstreamPointModeMixin {
    // RETURN, not TAIL: a server-side read leaves through the early `if (!clientPacket) return`.
    @Inject(method = "read", at = @At("RETURN"))
    private void create_connected$lowercaseUpstreamModes(ValueInput view, boolean clientPacket, CallbackInfo ci) {
        ArmBlockEntity arm = (ArmBlockEntity) (Object) this;
        ListTag points = arm.interactionPointTag;
        if (points == null) {
            return;
        }
        ListTag fixed = null;
        for (int i = 0; i < points.size(); i++) {
            if (!(points.get(i) instanceof CompoundTag point)) {
                continue;
            }
            String mode = point.getStringOr("Mode", "");
            String lower = mode.toLowerCase(Locale.ROOT);
            if (mode.equals(lower)) {
                continue;
            }
            if (fixed == null) {
                fixed = points.copy();
            }
            CompoundTag copy = point.copy();
            copy.putString("Mode", lower);
            fixed.set(i, copy);
        }
        if (fixed != null) {
            arm.interactionPointTag = fixed;
        }
    }
}
