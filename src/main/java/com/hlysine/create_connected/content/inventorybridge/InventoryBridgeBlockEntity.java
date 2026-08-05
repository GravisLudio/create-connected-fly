package com.hlysine.create_connected.content.inventorybridge;

import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.content.inventoryaccessport.WrappedItemHandler;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.SidedFilteringBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlock.ATTACHED_NEGATIVE;
import static com.hlysine.create_connected.content.inventorybridge.InventoryBridgeBlock.ATTACHED_POSITIVE;

public class InventoryBridgeBlockEntity extends SmartBlockEntity {
    protected ItemInventory itemCapability;
    private InvManipulationBehaviour negativeInventory;
    private InvManipulationBehaviour positiveInventory;

    SidedFilteringBehaviour filters;
    public FilteringBehaviour negativeFilter;
    public FilteringBehaviour positiveFilter;

    private boolean powered;

    public InventoryBridgeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        itemCapability = null;
        powered = false;
    }

    @Override
    public void initialize() {
        super.initialize();
        updateConnectedInventory();
    }

    /**
     * The combined inventory this bridge exposes.
     * <p>
     * NeoForge registered this through {@code RegisterCapabilitiesEvent} against
     * {@code Capabilities.ItemHandler.BLOCK}. Create Fly has no capability system: a block exposes
     * an inventory by implementing {@code ItemInventoryProvider}, which is vanilla's
     * {@code WorldlyContainerHolder} underneath. So the wiring lives on
     * {@link InventoryBridgeBlock} now and this just hands over the handler.
     */
    public ItemInventory getItemInventory() {
        if (itemCapability == null)
            refreshCapability();
        return itemCapability;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        CapManipulationBehaviourBase.InterfaceProvider towardBlockFacing1 =
                (w, p, s) -> new BlockFace(p, InventoryBridgeBlock.getNegativeTarget(s));
        CapManipulationBehaviourBase.InterfaceProvider towardBlockFacing2 =
                (w, p, s) -> new BlockFace(p, InventoryBridgeBlock.getPositiveTarget(s));
        behaviours.add(negativeInventory = new InvManipulationBehaviour(this, towardBlockFacing1));
        behaviours.add(positiveInventory = new InvManipulationBehaviour(this, towardBlockFacing2));
        behaviours.add(filters = new SidedFilteringBehaviour(
                this,
                new InventoryBridgeFilterSlot(),
                (facing, filter) -> {
                    if (facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                        negativeFilter = filter;
                    } else {
                        positiveFilter = filter;
                    }
                    return filter;
                },
                facing -> facing.getAxis() == getBlockState().getValue(InventoryBridgeBlock.AXIS)
        ));
    }

    public boolean isAttachedNegative() {
        return !powered && negativeInventory.hasInventory() && !(negativeInventory.getInventory() instanceof WrappedItemHandler);
    }

    public boolean isAttachedPositive() {
        return !powered && positiveInventory.hasInventory() && !(positiveInventory.getInventory() instanceof WrappedItemHandler);
    }

    public @Nullable BlockState getNegativeAttachedBlock() {
        if (!isAttachedNegative()) return null;
        return level.getBlockState(negativeInventory.getTarget().getConnectedPos());
    }

    public @Nullable BlockState getPositiveAttachedBlock() {
        if (!isAttachedPositive()) return null;
        return level.getBlockState(positiveInventory.getTarget().getConnectedPos());
    }

    public void updateConnectedInventory() {
        negativeInventory.findNewCapability();
        positiveInventory.findNewCapability();
        boolean previouslyPowered = powered;
        powered = level.hasNeighborSignal(worldPosition);
        if (powered != previouslyPowered) {
            notifyUpdate();
        }
        boolean attachedNegative = isAttachedNegative();
        boolean attachedPositive = isAttachedPositive();
        if (attachedNegative != getBlockState().getValue(ATTACHED_NEGATIVE) || attachedPositive != getBlockState().getValue(ATTACHED_POSITIVE)) {
            BlockState state = getBlockState()
                    .setValue(ATTACHED_NEGATIVE, attachedNegative)
                    .setValue(ATTACHED_POSITIVE, attachedPositive);
            level.setBlockAndUpdate(worldPosition, state);
        }
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        powered = tag.getBoolean("Powered");
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putBoolean("Powered", powered);
    }

    private ItemInventory getNegativeHandler() {
        if (powered) return null;
        ItemInventory handler = negativeInventory.getInventory();
        // Guard against bridging a bridge, which would recurse across a chain of them.
        if (handler instanceof InventoryBridgeHandler) return null;
        return handler;
    }

    private ItemInventory getPositiveHandler() {
        if (powered) return null;
        ItemInventory handler = positiveInventory.getInventory();
        if (handler instanceof InventoryBridgeHandler) return null;
        return handler;
    }

    private void refreshCapability() {
        itemCapability = new InventoryBridgeHandler();
    }

    /**
     * Exposes the two neighbouring inventories as one, applying the directional filters.
     * <p>
     * Was a Forge {@code WrappedItemHandler}. Create Fly's {@link ItemInventory} extends vanilla
     * {@link net.minecraft.world.Container} instead, which has no insert/extract pair and no
     * {@code simulate} flag -- so the old {@code insertItem}/{@code extractItem} split into a check
     * ({@link #canPlaceItem}) and a write ({@link #setItem}).
     * <p>
     * That also removes duplication: the old code repeated the same filter decision in four
     * methods because Forge's extract read the backing slots directly. Extraction now runs through
     * {@link #getItem} via {@code ItemInventory}'s default {@code removeItem}, so filtering once in
     * {@code getItem} covers reads and extraction alike.
     */
    private class InventoryBridgeHandler implements ItemInventory {

        private final ThreadLocal<Boolean> recursionGuard = ThreadLocal.withInitial(() -> false);

        private <T> T preventRecursion(Supplier<T> value, T defaultValue) {
            if (recursionGuard.get()) return defaultValue;
            recursionGuard.set(true);
            try {
                return value.get();
            } finally {
                // Was left set on exception, wedging the bridge shut for the rest of the thread.
                recursionGuard.set(false);
            }
        }

        /**
         * Whether {@code stack} may pass through {@code slot}.
         * <p>
         * {@code size1} is the negative handler's size, or -1 when only one side is attached; a
         * slot below it belongs to the negative side. An empty filter means "accept anything",
         * which is why a stack matching both filters is rejected on the side whose filter is empty
         * -- the explicitly configured side gets it.
         */
        private boolean passes(int slot, ItemStack stack, int size1, boolean hasNegative, boolean hasPositive) {
            boolean negative = negativeFilter.test(stack);
            boolean positive = positiveFilter.test(stack);
            boolean negativeEmpty = negativeFilter.getFilter().isEmpty();
            boolean positiveEmpty = positiveFilter.getFilter().isEmpty();

            if (!hasNegative && !hasPositive) return false;

            if (!hasNegative) {
                if (!positive) return false;
                return !(negative && !negativeEmpty && positiveEmpty);
            }
            if (!hasPositive) {
                if (!negative) return false;
                return !(positive && !positiveEmpty && negativeEmpty);
            }

            if (!negative && !positive) return false;
            if (negative && !positive && slot >= size1) return false;
            if (positive && !negative && slot < size1) return false;
            if (!negativeEmpty || !positiveEmpty) {
                if (slot >= size1 && negative && positiveEmpty) return false;
                if (slot < size1 && positive && negativeEmpty) return false;
            }
            return true;
        }

        @Override
        public int getContainerSize() {
            return preventRecursion(() -> {
                ItemInventory negative = getNegativeHandler();
                ItemInventory positive = getPositiveHandler();
                if (negative == null && positive == null) return 0;
                if (negative == null) return positive.getContainerSize();
                if (positive == null) return negative.getContainerSize();
                return negative.getContainerSize() + positive.getContainerSize();
            }, 0);
        }

        @Override
        public ItemStack getItem(int slot) {
            return preventRecursion(() -> {
                ItemInventory negative = getNegativeHandler();
                ItemInventory positive = getPositiveHandler();
                if (negative == null && positive == null) return ItemStack.EMPTY;

                int size1 = negative == null ? -1 : negative.getContainerSize();
                ItemStack stack = negative == null ? positive.getItem(slot)
                        : positive == null || slot < size1 ? negative.getItem(slot)
                        : positive.getItem(slot - size1);

                return passes(slot, stack, size1, negative != null, positive != null)
                        ? stack
                        : ItemStack.EMPTY;
            }, ItemStack.EMPTY);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            preventRecursion(() -> {
                ItemInventory negative = getNegativeHandler();
                ItemInventory positive = getPositiveHandler();
                if (negative == null && positive == null) return null;

                int size1 = negative == null ? -1 : negative.getContainerSize();
                if (!stack.isEmpty() && !passes(slot, stack, size1, negative != null, positive != null)) return null;

                if (negative == null) positive.setItem(slot, stack);
                else if (positive == null || slot < size1) negative.setItem(slot, stack);
                else positive.setItem(slot - size1, stack);
                return null;
            }, null);
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return preventRecursion(() -> {
                ItemInventory negative = getNegativeHandler();
                ItemInventory positive = getPositiveHandler();
                if (negative == null && positive == null) return false;

                int size1 = negative == null ? -1 : negative.getContainerSize();
                if (!passes(slot, stack, size1, negative != null, positive != null)) return false;

                if (negative == null) return positive.canPlaceItem(slot, stack);
                if (positive == null || slot < size1) return negative.canPlaceItem(slot, stack);
                return positive.canPlaceItem(slot - size1, stack);
            }, false);
        }

        @Override
        public int getMaxStackSize() {
            return preventRecursion(() -> {
                ItemInventory negative = getNegativeHandler();
                if (negative != null) return negative.getMaxStackSize();
                ItemInventory positive = getPositiveHandler();
                return positive != null ? positive.getMaxStackSize() : 0;
            }, 0);
        }

        @Override
        public void setChanged() {
            InventoryBridgeBlockEntity.this.setChanged();
        }
    }
}
