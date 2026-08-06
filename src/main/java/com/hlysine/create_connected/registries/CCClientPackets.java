package com.hlysine.create_connected.registries;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Split out of {@link CCPackets} so that class stays loadable on a dedicated server: touching
 * {@code ClientPlayNetworking} from there would resolve client-only types during verification, even
 * inside a branch that never runs.
 */
@Environment(EnvType.CLIENT)
public class CCClientPackets {
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(
                CCPackets.PLAY_CONTRAPTION_JUKEBOX,
                (payload, context) -> context.client().execute(() -> payload.handle(context.player()))
        );
    }
}
