package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryDisplaySource;
import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.redstone.displayLink.source.BoilerDisplaySource;
import com.hlysine.create_connected.foundation.registrate.CCRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

import java.util.function.Supplier;

public class CCDisplaySources {
    private static final CCRegistrate REGISTRATE = CreateConnected.getRegistrate();

    public static final RegistryEntry<DisplaySource, BoilerDisplaySource> BOILER_STATUS = simple("boiler_status", BoilerDisplaySource::new);
    public static final RegistryEntry<DisplaySource, KineticBatteryDisplaySource> KINETIC_BATTERY = simple("kinetic_battery", KineticBatteryDisplaySource::new);

    private static <T extends DisplaySource> RegistryEntry<DisplaySource, T> simple(String name, Supplier<T> supplier) {
        return REGISTRATE.displaySource(name, supplier).register();
    }

    public static void register() {
    }
}