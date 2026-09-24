package com.hlysine.create_connected.content.goggleslot;

import com.hlysine.create_connected.registries.CCPackets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A left click on the goggle slot. The slot is drawn by us rather than added to the menu (see
 * GoggleSlotScreen), so the click has to be carried to the server by hand.
 * <p>
 * The two game modes disagree about who owns the stack on the cursor, and that is the whole reason
 * for {@code creativeCursor}:
 * <ul>
 *   <li><b>Survival</b> -- the server owns it. The payload carries nothing; the server reads its own
 *       {@code getCarried()}, swaps, and {@code broadcastChanges} sends the cursor back.</li>
 *   <li><b>Creative</b> -- the cursor lives only on the client, and the server's copy is empty. The
 *       client reports what it holds and mirrors the result on its own cursor; the server trusts
 *       it, the same trust vanilla extends to every creative inventory click.</li>
 * </ul>
 */
public record GoggleSlotClickPacket(ItemStack creativeCursor) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, GoggleSlotClickPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ItemStack.OPTIONAL_STREAM_CODEC, GoggleSlotClickPacket::creativeCursor,
                    GoggleSlotClickPacket::new
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return CCPackets.GOGGLE_SLOT_CLICK;
    }

    public void apply(ServerPlayer player) {
        if (!GoggleSlot.isActive())
            return;
        ItemStack worn = GoggleSlot.get(player);

        if (player.isCreative()) {
            ItemStack cursor = creativeCursor;
            if (cursor.isEmpty()) {
                // The client has put the goggles on its own cursor already.
                GoggleSlot.set(player, ItemStack.EMPTY);
            } else if (GoggleSlot.isGoggles(cursor) && (worn.isEmpty() || cursor.getCount() == 1)) {
                GoggleSlot.set(player, cursor.copyWithCount(1));
            }
            return;
        }

        // Only from the player's own inventory: nothing else draws the slot, and this keeps a
        // crafted packet from reaching into some other open container's cursor.
        AbstractContainerMenu menu = player.containerMenu;
        if (menu != player.inventoryMenu)
            return;

        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            if (worn.isEmpty())
                return;
            GoggleSlot.set(player, ItemStack.EMPTY);
            menu.setCarried(worn);
        } else if (GoggleSlot.isGoggles(carried)) {
            if (worn.isEmpty()) {
                GoggleSlot.set(player, carried.split(1));
            } else if (carried.getCount() == 1) {
                GoggleSlot.set(player, carried);
                menu.setCarried(worn);
            } else {
                return;
            }
        } else {
            return;
        }
        menu.broadcastChanges();
    }
}
