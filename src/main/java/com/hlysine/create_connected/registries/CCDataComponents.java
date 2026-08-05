package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.UnaryOperator;

public class CCDataComponents {
    public static final DataComponentType<Double> KINETIC_BATTERY_CHARGE = register(
            "kinetic_battery_charge",
            builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                CreateConnected.asResource(name),
                builder.apply(DataComponentType.builder()).build()
        );
    }

    /** Kept so the initialiser still has a place to force class loading; registration is eager. */
    @ApiStatus.Internal
    public static void register() {
    }
}
