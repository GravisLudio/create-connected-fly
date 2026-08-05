package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.dashboard.DashboardDisplayTarget;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class CCDisplayTargets {
    public static final DashboardDisplayTarget DASHBOARD = simple("dashboard", DashboardDisplayTarget::new);

    private static <T extends DisplayTarget> T simple(String name, Supplier<T> supplier) {
        return Registry.register(
                CreateRegistries.DISPLAY_TARGET,
                Identifier.fromNamespaceAndPath(CreateConnected.MODID, name),
                supplier.get()
        );
    }

    public static void register() {
    }
}
