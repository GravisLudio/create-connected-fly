package com.hlysine.create_connected.content.linkedtransmitter;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 26.2 dropped the {@code HitResult} from {@code getCloneItemStack}, so a block can no longer be
 * told which part of itself the player is aiming at. Pick-block is a client action, and the crosshair
 * target is right there, so the linked transmitter blocks read it back to keep telling the base
 * apart from the module.
 */
@Environment(EnvType.CLIENT)
public class ClientHitResultAccess {
    public static @Nullable HitResult get() {
        return Minecraft.getInstance().hitResult;
    }
}
