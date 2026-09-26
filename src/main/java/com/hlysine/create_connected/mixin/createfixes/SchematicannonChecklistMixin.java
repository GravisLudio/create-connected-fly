package com.hlysine.create_connected.mixin.createfixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonInventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A Create Fly bug, fixed here: printing a material checklist in the schematicannon consumed the
 * whole stack of books or clipboards in the input slot, not one. {@code tickPaperPrinter} empties
 * the slot with {@code setItem(BookInput, ItemStack.EMPTY)}, where Create extracts a single item.
 * The first {@code setItem} in the method is that one; the second fills the output slot.
 */
@Mixin(value = SchematicannonBlockEntity.class, remap = false)
public class SchematicannonChecklistMixin {
    @WrapOperation(
            method = "tickPaperPrinter",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/zurrtum/create/content/schematics/cannon/SchematicannonInventory;setItem(ILnet/minecraft/world/item/ItemStack;)V",
                    ordinal = 0
            )
    )
    private void create_connected$consumeOnePaper(SchematicannonInventory inventory, int slot, ItemStack stack, Operation<Void> original) {
        ItemStack remaining = inventory.getItem(slot).copy();
        remaining.shrink(1);
        original.call(inventory, slot, remaining);
    }
}
