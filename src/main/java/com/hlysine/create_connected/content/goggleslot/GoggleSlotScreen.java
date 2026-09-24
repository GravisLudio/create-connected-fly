package com.hlysine.create_connected.content.goggleslot;

import com.hlysine.create_connected.mixin.goggleslot.AbstractContainerScreenAccessor;
import com.hlysine.create_connected.mixin.goggleslot.CreativeModeInventoryScreenAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Draws the goggle slot on the inventory screens and handles clicks on it.
 * <p>
 * <b>Drawn, not added to the menu.</b> The obvious way -- a real {@code Slot} in
 * {@code InventoryMenu} -- works in survival and silently does nothing in creative, because the
 * creative screen runs its own {@code ItemPickerMenu}. Fabric's screen events let us draw onto both
 * screens and take the click before vanilla sees it, with no mixin into either menu, so the slot
 * behaves the same in both modes and cannot fight another mod over slot indices. The price is that
 * nothing about slots comes for free: plain clicks and shift-clicks are handled here, each with its
 * own packet, and dragging across the slot is not handled at all.
 * <p>
 * <b>Where it sits.</b> In the survival inventory, straight above the offhand slot, inside the
 * panel: blank space in the vanilla texture, and inside the panel means it moves with the recipe
 * book instead of colliding with it. The creative Inventory tab has a different layout and no
 * recipe book, so there it hangs outside the left edge on a small panel of its own.
 */
@Environment(EnvType.CLIENT)
public final class GoggleSlotScreen {
    private static final int PANEL = 0xFFC6C6C6;
    private static final int OUTLINE = 0xFF000000;
    private static final int SHADOW = 0xFF373737;
    private static final int LIGHT = 0xFFFFFFFF;
    private static final int SLOT = 0xFF8B8B8B;
    private static final int HOVER = 0x80FFFFFF;

    private GoggleSlotScreen() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
            if (!(screen instanceof InventoryScreen) && !(screen instanceof CreativeModeInventoryScreen))
                return;
            ScreenEvents.afterExtract(screen).register(GoggleSlotScreen::extract);
            ScreenMouseEvents.allowMouseClick(screen).register((s, event) -> !click(s, event));
        });
    }

    /** Top-left of the 16x16 item area, or null when the slot should not show on this screen. */
    private static int @Nullable [] slotPosition(Screen screen) {
        if (!GoggleSlot.isActive())
            return null;
        if (screen instanceof InventoryScreen inventory) {
            AbstractContainerScreenAccessor at = (AbstractContainerScreenAccessor) inventory;
            // Vanilla's offhand slot is at (77, 62); this is the same column, one slot up.
            return new int[]{at.create_connected$getLeftPos() + 77, at.create_connected$getTopPos() + 44};
        }
        if (screen instanceof CreativeModeInventoryScreen creative) {
            CreativeModeTab tab = CreativeModeInventoryScreenAccessor.create_connected$getSelectedTab();
            if (tab == null || tab.getType() != CreativeModeTab.Type.INVENTORY)
                return null;
            AbstractContainerScreenAccessor at = (AbstractContainerScreenAccessor) creative;
            return new int[]{at.create_connected$getLeftPos() - 22, at.create_connected$getTopPos() + 20};
        }
        return null;
    }

    private static boolean isOver(int[] pos, double mouseX, double mouseY) {
        return mouseX >= pos[0] && mouseX < pos[0] + 16 && mouseY >= pos[1] && mouseY < pos[1] + 16;
    }

    private static void extract(Screen screen, GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        int[] pos = slotPosition(screen);
        LocalPlayer player = Minecraft.getInstance().player;
        if (pos == null || player == null)
            return;
        int x = pos[0], y = pos[1];

        if (screen instanceof CreativeModeInventoryScreen) {
            g.fill(x - 5, y - 5, x + 21, y + 21, OUTLINE);
            g.fill(x - 4, y - 4, x + 20, y + 20, PANEL);
        }
        // Vanilla's slot bevel: dark top-left edge, light bottom-right edge, grey well.
        g.fill(x - 1, y - 1, x + 17, y + 17, SHADOW);
        g.fill(x, y, x + 17, y + 17, LIGHT);
        g.fill(x, y, x + 16, y + 16, SLOT);

        ItemStack worn = GoggleSlot.get(player);
        if (!worn.isEmpty())
            g.item(worn, x, y);

        ItemStack carried = screen instanceof AbstractContainerScreen<?> container
                ? container.getMenu().getCarried() : ItemStack.EMPTY;

        if (isOver(pos, mouseX, mouseY)) {
            g.fill(x, y, x + 16, y + 16, HOVER);
            // Like vanilla: no tooltip while something is on the cursor.
            if (carried.isEmpty()) {
                if (!worn.isEmpty())
                    g.setTooltipForNextFrame(Minecraft.getInstance().font, worn, mouseX, mouseY);
                else
                    g.setTooltipForNextFrame(Minecraft.getInstance().font, List.of(
                            Component.translatable("gui.create_connected.goggle_slot"),
                            Component.translatable("gui.create_connected.goggle_slot.hint")
                                    .withStyle(net.minecraft.ChatFormatting.GRAY)
                    ), Optional.empty(), mouseX, mouseY);
            }
        }

        // This event runs after the screen has drawn everything, cursor stack included, so the slot
        // above was painted over whatever the player is carrying -- which a vanilla slot never
        // does. Draw the cursor stack again on top, where vanilla puts it (mouse minus 8), whenever
        // it overlaps the slot or its creative panel.
        int cx = mouseX - 8, cy = mouseY - 8;
        if (!carried.isEmpty() && cx < x + 21 && cx + 16 > x - 5 && cy < y + 21 && cy + 16 > y - 5) {
            g.item(carried, cx, cy);
            g.itemDecorations(Minecraft.getInstance().font, carried, cx, cy);
        }
    }

    /** @return true when the click was ours, so vanilla must not also handle it. */
    private static boolean click(Screen screen, MouseButtonEvent event) {
        int[] pos = slotPosition(screen);
        LocalPlayer player = Minecraft.getInstance().player;
        if (pos == null || player == null || !(screen instanceof AbstractContainerScreen<?> container))
            return false;
        boolean overSlot = isOver(pos, event.x(), event.y());

        if (event.button() == 0 && event.hasShiftDown()) {
            if (overSlot) {
                if (!GoggleSlot.get(player).isEmpty())
                    ClientPlayNetworking.send(new GoggleSlotQuickMovePacket(GoggleSlotQuickMovePacket.TO_INVENTORY));
                return true;
            }
            // Vanilla sends goggles to the helmet slot on shift-click, because they are a helmet.
            // When our slot is free it is the more useful place, and the helmet slot stays free.
            Slot hovered = ((AbstractContainerScreenAccessor) container).create_connected$getHoveredSlot();
            if (hovered != null && hovered.container == player.getInventory()
                    && hovered.getContainerSlot() < 36
                    && GoggleSlot.isGoggles(hovered.getItem())
                    && GoggleSlot.get(player).isEmpty()) {
                ClientPlayNetworking.send(new GoggleSlotQuickMovePacket(hovered.getContainerSlot()));
                return true;
            }
            return false;
        }

        if (!overSlot)
            return false;
        if (event.button() != 0)
            return true;

        AbstractContainerMenu menu = container.getMenu();
        ItemStack carried = menu.getCarried();
        ItemStack worn = GoggleSlot.get(player);
        if (carried.isEmpty() && worn.isEmpty())
            return true;
        if (!carried.isEmpty() && !GoggleSlot.isGoggles(carried))
            return true;

        boolean creative = player.isCreative();
        ClientPlayNetworking.send(new GoggleSlotClickPacket(creative ? carried.copy() : ItemStack.EMPTY));

        // In creative the cursor is ours to keep in step, mirroring what the server does with the
        // slot; in survival the server owns the cursor and sends it back itself.
        if (creative) {
            if (carried.isEmpty())
                menu.setCarried(worn.copy());
            else if (worn.isEmpty())
                carried.shrink(1);
            else if (carried.getCount() == 1)
                menu.setCarried(worn.copy());
        }
        return true;
    }
}
