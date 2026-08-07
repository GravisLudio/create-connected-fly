package com.hlysine.create_connected.mixin.tint;

import com.hlysine.create_connected.client.CCBlockTints;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The stand-in for NeoForge's {@code RegisterColorHandlersEvent.Block}, which Fabric has no
 * equivalent of. Create Fly reaches its own {@code AllBlockTints} the same way.
 *
 * <p>Injecting at {@code RETURN} rather than {@code TAIL} on purpose: the return value is only
 * populated at {@code RETURN}, so this needs no MixinExtras {@code @Local} to get at the instance
 * being built.
 */
@Mixin(BlockColors.class)
public class BlockColorsMixin {
    @Inject(method = "createDefault()Lnet/minecraft/client/color/block/BlockColors;", at = @At("RETURN"))
    private static void create_connected$registerTints(CallbackInfoReturnable<BlockColors> cir) {
        CCBlockTints.register(cir.getReturnValue());
    }
}
