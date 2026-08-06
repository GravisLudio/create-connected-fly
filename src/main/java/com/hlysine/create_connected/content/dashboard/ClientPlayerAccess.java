package com.hlysine.create_connected.content.dashboard;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class ClientPlayerAccess {
    public static Player getPlayer() {
        return Minecraft.getInstance().player;
    }

    /**
     * {@code Player.displayClientMessage(component, actionBar)} is gone in 26.2; the action bar is
     * driven straight off the HUD, which is how Create Fly does it too.
     */
    public static void sendOverlayMessage(Component component) {
        Minecraft.getInstance().gui.hud.setOverlayMessage(component, false);
    }
}
