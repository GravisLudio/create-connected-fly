package com.hlysine.create_connected.content.inventoryaccessport;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllUnpackingHandlers;
import com.zurrtum.create.api.packager.unpacking.UnpackingHandler;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A packager unpacking into a port in front of a Mechanical Crafter hands the order to the
 * crafter's own handler, so the items land in the recipe's layout instead of wherever the
 * default insert puts them. Upstream 5702f032 (fixes hlysine/create_connected#293).
 * <p>
 * Create Fly keeps the default handler in {@code AllUnpackingHandlers.DEFAULT}, where upstream's
 * NeoForge API had it on the interface.
 */
public enum InventoryAccessPortUnpackingHandler implements UnpackingHandler {
    INSTANCE;

    @Override
    public boolean unpack(Level level, BlockPos pos, BlockState state, Direction side, List<ItemStack> items, @Nullable PackageOrderWithCrafts orderContext, boolean simulate) {
        if (!(state.getBlock() instanceof InventoryAccessPortBlock))
            return AllUnpackingHandlers.DEFAULT.unpack(level, pos, state, side, items, orderContext, simulate);

        Direction targetDirection = InventoryAccessPortBlock.getTargetDirection(state);
        BlockPos targetPos = pos.relative(targetDirection);
        BlockState targetState = level.getBlockState(targetPos);

        if (targetState.is(AllBlocks.MECHANICAL_CRAFTER)) {
            return AllUnpackingHandlers.MECHANICAL_CRAFTER.unpack(level, targetPos, targetState, targetDirection, items, orderContext, simulate);
        } else {
            return AllUnpackingHandlers.DEFAULT.unpack(level, pos, state, side, items, orderContext, simulate);
        }
    }
}
