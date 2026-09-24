package com.hlysine.create_connected.mixin.goggleslot;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Where the panel sits on screen, which the goggle slot is drawn relative to. Both are protected. */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("leftPos")
    int create_connected$getLeftPos();

    @Accessor("topPos")
    int create_connected$getTopPos();

    /** What a shift-click lands on, to route goggles into the goggle slot instead of the helmet. */
    @Accessor("hoveredSlot")
    net.minecraft.world.inventory.Slot create_connected$getHoveredSlot();
}
