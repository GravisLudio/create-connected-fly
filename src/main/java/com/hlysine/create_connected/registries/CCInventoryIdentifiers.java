package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlockEntity;
import com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlockEntity;
import com.zurrtum.create.api.packager.InventoryIdentifier;
import com.zurrtum.create.catnip.math.BlockFace;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Makes a packager aimed at an Inventory Access Port or Bridge see the inventory behind it, so
 * the stock network does not count the same items twice. Without this, a packager on a port and
 * another on the chest itself listed the chest's contents twice, and ordering both copies
 * duplicated items. Upstream 93504609 (fixes hlysine/create_connected#305).
 */
public class CCInventoryIdentifiers {
    public static void register() {
        InventoryIdentifier.REGISTRY.register(CCBlocks.INVENTORY_ACCESS_PORT.get(), (Level level, BlockState state, BlockFace face) -> {
            BlockEntity be = level.getBlockEntity(face.getPos());
            if (be instanceof InventoryAccessPortBlockEntity inventoryAccessPort) {
                return inventoryAccessPort.getInventoryId();
            }
            return null;
        });
        InventoryIdentifier.REGISTRY.register(CCBlocks.INVENTORY_BRIDGE.get(), (Level level, BlockState state, BlockFace face) -> {
            BlockEntity be = level.getBlockEntity(face.getPos());
            if (be instanceof InventoryBridgeBlockEntity inventoryBridge) {
                return inventoryBridge.getInventoryId();
            }
            return null;
        });
    }
}
