package com.hlysine.create_connected.mixin;

import com.hlysine.create_connected.config.CServer;
import com.zurrtum.create.content.kinetics.deployer.ManualApplicationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Returns the crafting remainder -- a bucket, say -- when an item is consumed by manual
 * application, instead of destroying it.
 * <p>
 * Retargeted for 26.2. The method used to live on {@code ManualApplicationRecipe} and took a
 * NeoForge {@code PlayerInteractEvent.RightClickBlock}, pulling the player, hand and held stack out
 * of that event. Create Fly moved it to {@link ManualApplicationHelper} -- {@code
 * ManualApplicationRecipe} is a plain record now -- and, with no event to carry them, passes those
 * values as ordinary parameters. Same values, read from the arguments instead.
 * <p>
 * The return type changed too: it returns an {@code InteractionResult} rather than void, so this
 * takes a {@link CallbackInfoReturnable}.
 */
@Mixin(value = ManualApplicationHelper.class, remap = false)
public class ManualApplicationRecipeMixin {

    @Inject(
            method = "manualApplicationRecipesApplyInWorld",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            remap = true
    )
    private static void craftingRemainingItemOnApplication(
            Level level,
            Player player,
            ItemStack heldItem,
            InteractionHand hand,
            BlockHitResult hit,
            BlockPos pos,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!CServer.ApplicationRemainingItemFix.get()) return;

        // The remainder lives on the Item as an ItemStackTemplate now, and is null when there is none.
        ItemStackTemplate remainder = heldItem.getItem().getCraftingRemainder();
        ItemStack leftover = remainder == null ? ItemStack.EMPTY : remainder.create();

        heldItem.shrink(1);

        if (heldItem.isEmpty()) {
            player.setItemInHand(hand, leftover);
        } else {
            heldItem.grow(1); // Create shrinks the stack again right after this inject
            if (!player.getInventory().add(leftover)) {
                player.drop(leftover, false);
            }
        }
    }
}
