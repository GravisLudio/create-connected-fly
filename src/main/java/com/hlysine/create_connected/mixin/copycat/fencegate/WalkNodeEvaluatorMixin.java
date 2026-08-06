package com.hlysine.create_connected.mixin.copycat.fencegate;

import com.hlysine.create_connected.content.copycat.ICopycatWithWrappedBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WalkNodeEvaluator.class, priority = 1100)
// A higher priority is needed so that this is attempted AFTER another mod overwrites the method
public class WalkNodeEvaluatorMixin {
    @WrapOperation(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"),
            // 26.2: getBlockPathTypeRaw -> getPathTypeFromState, and BlockPathTypes -> PathType.
            // Still the same method: it is the uncached, single-block classifier that
            // PathTypeCache.getOrCompute and PathfindingContext.getPathTypeFromState both end up calling.
            method = "getPathTypeFromState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/pathfinder/PathType;",
            require = 0 // Fail silently if target is overwritten by optimization mods
    )
    private static Block getWrappedBlock(BlockState instance, Operation<Block> original) {
        // The method reads getBlock() exactly once into a local and then instanceof-checks it
        // against DoorBlock / BaseRailBlock / LeavesBlock / FenceGateBlock. Handing it the wrapped
        // block makes the copycat fence gate hit the FenceGateBlock branch and path as PathType.FENCE.
        // The OPEN check next to it reads the *copycat's* own state, which carries FenceGateBlock.OPEN,
        // so open gates still stay passable.
        return ICopycatWithWrappedBlock.unwrap(original.call(instance));
    }
}
