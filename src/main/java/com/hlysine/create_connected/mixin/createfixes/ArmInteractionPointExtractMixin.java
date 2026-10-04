package com.hlysine.create_connected.mixin.createfixes;

import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

/**
 * A Create Fly bug, fixed here: a mechanical arm stalled on any inventory whose first item had
 * nowhere to go. The arm walks the slots and skips those whose item no target accepts, but
 * {@code extract} ignores its {@code slot} and returns the first non-empty stack for every one of
 * them. A basin with leftover quartz in front of finished granite was the case players hit.
 * Create takes from the requested slot; here the arm takes the kind of item sitting in it.
 * Points with their own {@code extract} (crafter, composter, jukebox) do not reach this.
 */
@Mixin(value = ArmInteractionPoint.class, remap = false)
public abstract class ArmInteractionPointExtractMixin {
    @Shadow
    protected abstract @Nullable Container getHandler(ArmBlockEntity armBlockEntity);

    @Inject(method = "extract(Lcom/zurrtum/create/content/kinetics/mechanicalArm/ArmBlockEntity;IIZ)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void create_connected$extractFromSlot(ArmBlockEntity arm, int slot, int amount, boolean simulate, CallbackInfoReturnable<ItemStack> cir) {
        Container handler = getHandler(arm);
        if (handler == null || slot < 0 || slot >= handler.getContainerSize()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack inSlot = handler.getItem(slot);
        if (inSlot.isEmpty()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack wanted = inSlot.copy();
        Predicate<ItemStack> sameItem = stack -> ItemStack.isSameItemSameComponents(stack, wanted);
        cir.setReturnValue(simulate
                ? handler.count(sameItem, amount, Direction.UP)
                : handler.extract(sameItem, amount, Direction.UP));
    }
}
