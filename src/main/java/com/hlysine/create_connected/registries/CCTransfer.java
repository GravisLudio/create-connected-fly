package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.content.fluidvessel.FluidVesselBlockEntity;
import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlockEntity;
import com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlockEntity;
import com.hlysine.create_connected.content.itemsilo.ItemSiloBlockEntity;
import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedFluidInventoryBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Function;

/**
 * Publishes this mod's inventories to Fabric's transfer API, which is a separate audience from
 * Create's own.
 * <p>
 * Declaring {@code ItemInventoryProvider} / {@code FluidInventoryProvider} on the block is what
 * makes pipes and funnels work, and that is all upstream's {@code RegisterCapabilitiesEvent}
 * translated to. It is <em>not</em> what makes {@code ItemStorage.SIDED} and
 * {@code FluidStorage.SIDED} resolve, and everything outside Create reads the block through those:
 * Jade's fluid and item readouts, and any other mod written against fabric-transfer-api-v1. Create
 * Fly bridges the two halves for its own blocks in {@link AllTransfer#register()}, one hardcoded
 * line per block entity type, and there is no extension point on that list -- so an addon has to
 * repeat the pattern for its own types or stay invisible to the API.
 * <p>
 * The symptom is quiet and easy to misread: fluid still flows into a Fluid Vessel through Create's
 * pipes, so the block looks correctly wired, but Jade shows only its name -- no bar, no bucket
 * count. Nothing is logged.
 * <p>
 * The two behaviours below are Create Fly's own, and registering them is the same two-step it
 * performs: a {@code Cached*InventoryBehaviour} on the block entity holds the wrapper, and the
 * lookup resolves through that behaviour's static {@code get}. Going straight to a wrapper instead
 * would skip the cache that neighbour lookups rely on.
 */
public class CCTransfer {

    public static void register() {
        // Set when fabric-transfer-api-v1 is absent. Create Fly skips its own registration then,
        // and these calls would fail on a missing class rather than degrade.
        if (AllTransfer.DISABLE)
            return;

        // Both vessels share one declaration: the creative one is a subclass, and the capability
        // getter it inherits already resolves through the multiblock controller.
        registerFluidSide(CCBlockEntityTypes.FLUID_VESSEL.get(), FluidVesselBlockEntity::getFluidInventory);
        registerFluidSide(CCBlockEntityTypes.CREATIVE_FLUID_VESSEL.get(), FluidVesselBlockEntity::getFluidInventory);

        // The three item blocks are all side-agnostic -- their providers ignore the Direction --
        // so the plain cached behaviour is enough; the direction-keyed variant is for blocks whose
        // inventory differs per face.
        registerItemSide(CCBlockEntityTypes.ITEM_SILO.get(), ItemSiloBlockEntity::getItemInventory);
        registerItemSide(CCBlockEntityTypes.INVENTORY_BRIDGE.get(), InventoryBridgeBlockEntity::getItemInventory);
        registerItemSide(CCBlockEntityTypes.INVENTORY_ACCESS_PORT.get(), InventoryAccessPortBlockEntity::getItemInventory);
    }

    private static <T extends SmartBlockEntity> void registerItemSide(
            BlockEntityType<T> type,
            Function<T, Container> factory
    ) {
        BlockEntityBehaviour.add(type, be -> new CachedInventoryBehaviour<>(be, factory));
        ItemStorage.SIDED.registerForBlockEntity(CachedInventoryBehaviour::get, type);
    }

    private static <T extends SmartBlockEntity> void registerFluidSide(
            BlockEntityType<T> type,
            Function<T, FluidInventory> factory
    ) {
        BlockEntityBehaviour.add(type, be -> new CachedFluidInventoryBehaviour<>(be, factory));
        FluidStorage.SIDED.registerForBlockEntity(CachedFluidInventoryBehaviour::get, type);
    }
}
