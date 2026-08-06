package com.hlysine.create_connected.mixin.crankwheel;

import com.hlysine.create_connected.content.crankwheel.CrankWheelItem;
import com.hlysine.create_connected.content.linkedtransmitter.LinkedTransmitterItem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Client half of {@link ServerCrankWheelPlacementMixin}, so the placement does not desync. */
@Mixin(MultiPlayerGameMode.class)
public class ClientCrankWheelPlacementMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "performUseItemOn(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true
    )
    private void placeCrankWheel(
            LocalPlayer player,
            InteractionHand hand,
            BlockHitResult blockHit,
            CallbackInfoReturnable<InteractionResult> cir,
            @Local BlockPos pos,
            @Local ItemStack itemStack
    ) {
        InteractionResult result = CrankWheelItem.onItemUseFirst(minecraft.level, player, itemStack, hand, blockHit, pos);
        if (result == null)
            result = LinkedTransmitterItem.onItemUseFirst(minecraft.level, player, itemStack, hand, blockHit, pos);
        if (result != null)
            cir.setReturnValue(result);
    }
}
