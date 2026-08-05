package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryDisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.redstone.displayLink.source.BoilerDisplaySource;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class CCDisplaySources {
    public static final BoilerDisplaySource BOILER_STATUS = simple("boiler_status", BoilerDisplaySource::new);
    public static final KineticBatteryDisplaySource KINETIC_BATTERY = simple("kinetic_battery", KineticBatteryDisplaySource::new);

    private static <T extends DisplaySource> T simple(String name, Supplier<T> supplier) {
        return Registry.register(
                CreateRegistries.DISPLAY_SOURCE,
                Identifier.fromNamespaceAndPath(CreateConnected.MODID, name),
                supplier.get()
        );
    }

    public static void register() {
    }
}
