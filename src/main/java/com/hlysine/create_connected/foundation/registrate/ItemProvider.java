package com.hlysine.create_connected.foundation.registrate;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * The slice of Registrate's {@code ItemProviderEntry<?, ?>} that Connected uses: somewhere that
 * holds an item, has an id, and can be asked whether a stack matches. Both {@link BlockEntry} and
 * {@link ItemEntry} implement it so the creative tab can hold one list of either.
 */
public interface ItemProvider extends ItemLike {
    Identifier getId();

    Item asItem();

    ItemStack asStack();

    boolean isIn(ItemStack stack);

    /** True when this provider holds {@code item}. */
    default boolean is(Item item) {
        return asItem() == item;
    }
}
