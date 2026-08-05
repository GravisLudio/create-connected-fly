package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.fluidvessel.FluidVesselMountedStorageType;
import com.hlysine.create_connected.content.itemsilo.ItemSiloMountedStorageType;
import com.zurrtum.create.api.contraption.storage.fluid.MountedFluidStorageType;
import com.zurrtum.create.api.contraption.storage.item.MountedItemStorageType;
import com.zurrtum.create.api.registry.CreateRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.function.Supplier;

public class CCMountedStorageTypes {
    public static final ItemSiloMountedStorageType SILO = simpleItem("silo", ItemSiloMountedStorageType::new);
    public static final FluidVesselMountedStorageType FLUID_VESSEL = simpleFluid("fluid_vessel", FluidVesselMountedStorageType::new);

    private static <T extends MountedItemStorageType<?>> T simpleItem(String name, Supplier<T> supplier) {
        return Registry.register(
                CreateRegistries.MOUNTED_ITEM_STORAGE_TYPE,
                Identifier.fromNamespaceAndPath(CreateConnected.MODID, name),
                supplier.get()
        );
    }

    private static <T extends MountedFluidStorageType<?>> T simpleFluid(String name, Supplier<T> supplier) {
        return Registry.register(
                CreateRegistries.MOUNTED_FLUID_STORAGE_TYPE,
                Identifier.fromNamespaceAndPath(CreateConnected.MODID, name),
                supplier.get()
        );
    }

    public static void register() {
    }
}
