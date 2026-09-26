package com.hlysine.create_connected.content.inventorybridge;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllUnpackingHandlers;
import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The bridge's version of {@code InventoryAccessPortUnpackingHandler}: when either side is a
 * Mechanical Crafter whose filter lets every item through, and the crafter would accept the
 * order, the crafter's handler lays it out. Upstream 5702f032 (fixes hlysine/create_connected#293).
 */
public enum InventoryBridgeUnpackingHandler implements UnpackingHandler {
    INSTANCE;

    @Override
    public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof InventoryBridgeBlockEntity bridgeBE))
            return AllUnpackingHandlers.DEFAULT.unpack(level, pos, state, side, items, orderContext, simulate);

        Direction negative = InventoryBridgeBlock.getNegativeTarget(state);
        if (acceptsOrder(level, pos, negative, bridgeBE.negativeFilter, items, orderContext))
            return unpackIntoCrafter(level, pos, negative, items, orderContext, simulate);
        Direction positive = InventoryBridgeBlock.getPositiveTarget(state);
        if (acceptsOrder(level, pos, positive, bridgeBE.positiveFilter, items, orderContext))
            return unpackIntoCrafter(level, pos, positive, items, orderContext, simulate);
        return AllUnpackingHandlers.DEFAULT.unpack(level, pos, state, side, items, orderContext, simulate);
    }

    private boolean unpackIntoCrafter(Level level, BlockPos pos, Direction target, List<ItemStack> items,
                                      @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
        BlockPos targetPos = pos.relative(target);
        return AllUnpackingHandlers.MECHANICAL_CRAFTER.unpack(level, targetPos, level.getBlockState(targetPos), target, items, orderContext, simulate);
    }

    /** Whether the crafter on {@code target}'s side exists, passes the filter and would take the order. */
    private boolean acceptsOrder(Level level, BlockPos pos, Direction target, ServerFilteringBehaviour filter,
                               List<ItemStack> items, @Nullable PackageOrderWithCrafts orderContext) {
        BlockPos targetPos = pos.relative(target);
        BlockState targetState = level.getBlockState(targetPos);
        if (!targetState.is(AllBlocks.MECHANICAL_CRAFTER))
            return false;
        for (ItemStack item : items) {
            if (!filter.test(item))
                return false;
        }
        return AllUnpackingHandlers.MECHANICAL_CRAFTER.unpack(level, targetPos, targetState, target, copyItems(items), orderContext, true);
    }

    private List<ItemStack> copyItems(List<ItemStack> items) {
        List<ItemStack> copy = new ArrayList<>(items.size());
        for (ItemStack item : items) {
            copy.add(item.copy());
        }
        return copy;
    }
}
