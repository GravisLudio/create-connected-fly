package com.hlysine.create_connected.mixin.fluidvessel;

import com.hlysine.create_connected.content.fluidvessel.FluidVesselBlock;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SteamEngineBlock.class)
public class SteamEngineBlockMixin {
    @Inject(
            at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/tank/FluidTankBlock;updateBoilerState(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", remap = false),
            method = "onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
    )
    private void onPlaceVessel(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving, CallbackInfo ci) {
        FluidVesselBlock.updateBoilerState(pState, pLevel, pPos.relative(SteamEngineBlock.getFacing(pState).getOpposite()));
    }

    // 26.2 replaced Block#onRemove with Block#affectNeighborsAfterRemoval: it is server-only
    // (ServerLevel, not Level) and the replacement state is no longer passed. Neither matters here:
    // Create Fly's own body calls FluidTankBlock.updateBoilerState unconditionally at that spot --
    // there is no `newState.is(this)` guard to re-derive -- and the boiler evaluation this triggers
    // is server-authoritative, so mirroring it for the vessel is a straight port of the old hook.
    @Inject(
            at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/tank/FluidTankBlock;updateBoilerState(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", remap = false),
            method = "affectNeighborsAfterRemoval(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Z)V"
    )
    private void onRemoveVessel(BlockState pState, ServerLevel pLevel, BlockPos pPos, boolean pIsMoving, CallbackInfo ci) {
        FluidVesselBlock.updateBoilerState(pState, pLevel, pPos.relative(SteamEngineBlock.getFacing(pState).getOpposite()));
    }

    @Inject(
            at = @At("HEAD"),
            method = "canAttach",
            cancellable = true,
            remap = true
    )
    private static void canAttach(LevelReader pReader, BlockPos pPos, Direction pDirection, CallbackInfoReturnable<Boolean> cir) {
        BlockPos blockpos = pPos.relative(pDirection);
        if (pReader.getBlockState(blockpos).getBlock() instanceof FluidVesselBlock) {
            cir.setReturnValue(true);
        }
    }
}
