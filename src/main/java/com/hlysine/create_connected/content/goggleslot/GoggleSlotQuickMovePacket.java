package com.hlysine.create_connected.content.goggleslot;

import com.hlysine.create_connected.registries.CCPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A shift-click that involves the goggle slot, in either direction.
 * <p>
 * {@code inventoryIndex} is an index into the player's {@link Inventory}, not into a menu. The
 * survival inventory and the creative Inventory tab number their menu slots differently, but both
 * wrap the same Inventory, so its index means the same thing on either screen and the server never
 * has to know which one the click came from. Only the main inventory and hotbar (0-35) are
 * accepted: goggles already in the helmet slot stay a vanilla matter.
 * <p>
 * {@link #TO_INVENTORY} goes the other way: goggle slot to inventory, dropping them if it is full.
 * <p>
 * Unlike a plain click on the slot (GoggleSlotClickPacket), nothing here touches the cursor, so the
 * server is authoritative in both game modes -- in creative the server's copy of the inventory is
 * kept in step by vanilla's own creative slot packets.
 */
public record GoggleSlotQuickMovePacket(int inventoryIndex) implements CustomPacketPayload {
    public static final int TO_INVENTORY = -1;
    private static final int MAIN_AND_HOTBAR = 36;

    public static final StreamCodec<RegistryFriendlyByteBuf, GoggleSlotQuickMovePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, GoggleSlotQuickMovePacket::inventoryIndex,
                    GoggleSlotQuickMovePacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return CCPackets.GOGGLE_SLOT_QUICK_MOVE;
    }

    public void apply(ServerPlayer player) {
        if (!GoggleSlot.isActive())
            return;
        if (!player.isCreative() && player.containerMenu != player.inventoryMenu)
            return;

        Inventory inventory = player.getInventory();
        ItemStack worn = GoggleSlot.get(player);

        if (inventoryIndex == TO_INVENTORY) {
            if (worn.isEmpty())
                return;
            GoggleSlot.set(player, ItemStack.EMPTY);
            if (!inventory.add(worn))
                player.drop(worn, false);
        } else {
            if (inventoryIndex < 0 || inventoryIndex >= MAIN_AND_HOTBAR || !worn.isEmpty())
                return;
            ItemStack source = inventory.getItem(inventoryIndex);
            if (!GoggleSlot.isGoggles(source))
                return;
            GoggleSlot.set(player, source.split(1));
            inventory.setChanged();
        }
        player.inventoryMenu.broadcastChanges();
    }
}
