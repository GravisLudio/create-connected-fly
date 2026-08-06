package com.hlysine.create_connected.client.display;

import com.hlysine.create_connected.registries.CCDisplaySources;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.client.api.behaviour.display.DisplaySourceRender;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Supplier;

/**
 * Mirrors Create Fly's {@code AllDisplaySourceRenders}. Only the kinetic battery needs one --
 * the boiler status source reuses Create's own, which Create Fly does not give a render either.
 */
@Environment(EnvType.CLIENT)
public class CCDisplaySourceRenders {

    private static void register(DisplaySource target, Supplier<DisplaySourceRender> factory) {
        target.attachRender = factory.get();
    }

    public static void register() {
        register(CCDisplaySources.KINETIC_BATTERY, KineticBatteryDisplaySourceRender::new);
    }
}
