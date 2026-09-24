package com.hlysine.create_connected.mixin.goggleslot;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * The creative screen shares one class across all its tabs; the goggle slot belongs on the
 * Inventory tab only. The selected tab is a private static.
 */
@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenAccessor {
    @Accessor("selectedTab")
    static CreativeModeTab create_connected$getSelectedTab() {
        throw new AssertionError();
    }
}
