package com.hlysine.create_connected.mixin.crankwheel;

import com.hlysine.create_connected.content.crankwheel.CrankWheelItem;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The crank wheel places diagonally off an existing cogwheel, which upstream did through NeoForge's
 * {@code Item.onItemUseFirst}. There is no vanilla hook, so this injects at the same point Create Fly
 * uses for its own cogwheel placement -- see {@code ServerPlayerGameModeMixin} there.
 */
@Mixin(ServerPlayerGameMode.class)
public class ServerCrankWheelPlacementMixin {

    @Inject(
            method = "useItemOn(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"),
            cancellable = true
    )
    private void placeCrankWheel(
            ServerPlayer player,
            Level world,
            ItemStack stack,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir,
            @Local BlockPos pos
    ) {
        InteractionResult result = CrankWheelItem.onItemUseFirst(world, player, stack, hand, hit, pos);
        if (result != null)
            cir.setReturnValue(result);
    }
}
