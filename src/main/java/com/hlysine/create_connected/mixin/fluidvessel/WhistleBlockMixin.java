package com.hlysine.create_connected.mixin.fluidvessel;

import com.hlysine.create_connected.content.fluidvessel.FluidVesselBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WhistleBlock.class)
public class WhistleBlockMixin {
    @Inject(
            at = @At("HEAD"),
            method = "canSurvive(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;)Z",
            cancellable = true
    )
    private void canSurviveVessel(BlockState pState, LevelReader pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (FluidVesselBlock.isVessel(pLevel.getBlockState(pPos.relative(WhistleBlock.getAttachedDirection(pState))))) {
            cir.setReturnValue(true);
        }
    }

    // The INVOKE targets used to name com.simibubi.create -- the NeoForge package. Create Fly
    // repackaged under com.zurrtum.create, so with remap = false these matched nothing and both
    // injections were inert without raising anything.
    @Inject(
            at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/tank/FluidTankBlock;updateBoilerState(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", remap = false),
            method = "onPlace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Z)V"
    )
    private void onPlaceVessel(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving, CallbackInfo ci) {
        FluidVesselBlock.updateBoilerState(pState, pLevel, pPos.relative(WhistleBlock.getAttachedDirection(pState)));
    }

    /**
     * {@code onRemove} became {@code affectNeighborsAfterRemoval(state, ServerLevel, pos, isMoving)}:
     * it runs after the block is gone, so the replacement state is no longer passed. Nothing here
     * needs it -- the body only re-evaluates the boiler of the vessel the whistle was attached to,
     * and {@code pState} is still the whistle that was removed.
     */
    @Inject(
            at = @At(value = "INVOKE", target = "Lcom/zurrtum/create/content/fluids/tank/FluidTankBlock;updateBoilerState(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", remap = false),
            method = "affectNeighborsAfterRemoval(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Z)V"
    )
    private void onRemoveVessel(BlockState pState, ServerLevel pLevel, BlockPos pPos, boolean pIsMoving, CallbackInfo ci) {
        FluidVesselBlock.updateBoilerState(pState, pLevel, pPos.relative(WhistleBlock.getAttachedDirection(pState)));
    }
}
