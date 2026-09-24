package com.hlysine.create_connected.content.goggleslot;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * {@code /ccgoggles} and {@code /ccgoggles clear}: read and empty the goggle slot.
 * <p>
 * The slot has no screen yet, so its contents are invisible and unreachable except through the
 * sneak-and-right-click gesture, which needs open air -- right clicking a block goes to the block
 * instead and never reaches the item callback. Two debugging sessions were spent guessing at state
 * that nothing could show, hence this. It stays useful while the UI is built, and can go once the
 * slot is on screen and clickable.
 */
public final class GoggleSlotCommand {
    private GoggleSlotCommand() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("ccgoggles")
                    .executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        ItemStack worn = GoggleSlot.get(player);
                        ctx.getSource().sendSuccess(() -> Component.literal(
                                "Goggle slot: " + (worn.isEmpty() ? "empty" : worn.getHoverName().getString())
                                        + " | active: " + GoggleSlot.isActive()
                                        + " | counted as worn: " + GoggleSlot.isWearing(player)), false);
                        return 1;
                    })
                    .then(Commands.literal("clear").executes(ctx -> {
                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                        ItemStack worn = GoggleSlot.get(player);
                        GoggleSlot.set(player, ItemStack.EMPTY);
                        if (!worn.isEmpty() && !player.getInventory().add(worn))
                            player.drop(worn, false);
                        ctx.getSource().sendSuccess(() -> Component.literal("Goggle slot cleared"), false);
                        return 1;
                    }));
            dispatcher.register(root);
        });
    }
}
