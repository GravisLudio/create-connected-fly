package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryInteractionPoint;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public class CCArmInteractionPointTypes {
    public static final ArmInteractionPointType KINETIC_BATTERY =
            register("kinetic_battery", new KineticBatteryInteractionPoint.Type());

    private static <T extends ArmInteractionPointType> T register(String key, T type) {
        return Registry.register(
                CreateRegistries.ARM_INTERACTION_POINT_TYPE,
                Identifier.fromNamespaceAndPath(CreateConnected.MODID, key),
                type
        );
    }

    public static void register() {
    }
}
