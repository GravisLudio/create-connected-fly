package com.hlysine.create_connected.content.inventoryaccessport;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.hlysine.create_connected.CreateConnected;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.CapManipulationBehaviourBase;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
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

import static com.hlysine.create_connected.content.inventoryaccessport.InventoryAccessPortBlock.ATTACHED;

public class InventoryAccessPortBlockEntity extends SmartBlockEntity {
    protected ItemInventory itemCapability;
    private InvManipulationBehaviour observedInventory;
    private boolean powered;

    public InventoryAccessPortBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        itemCapability = null;
        powered = false;
    }

    @Override
    public void initialize() {
        super.initialize();
        updateConnectedInventory();
    }

    /** Exposed through {@code ItemInventoryProvider} on the block. */
    public ItemInventory getItemInventory() {
        if (itemCapability == null)
            refreshCapability();
        return itemCapability;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        CapManipulationBehaviourBase.InterfaceProvider towardBlockFacing =
                (w, p, s) -> new BlockFace(p, DirectedDirectionalBlock.getTargetDirection(s));
        behaviours.add(observedInventory = new InvManipulationBehaviour(this, towardBlockFacing));
    }

    public boolean isAttached() {
        return !powered && observedInventory.hasInventory() && !(observedInventory.getInventory() instanceof WrappedItemHandler);
    }

    public @Nullable BlockState getAttachedBlock() {
        if (!isAttached()) return null;
        return level.getBlockState(observedInventory.getTarget().getConnectedPos());
    }

    public void updateConnectedInventory() {
        observedInventory.findNewCapability();
        boolean previouslyPowered = powered;
        assert level != null;
        powered = level.hasNeighborSignal(worldPosition);
        if (powered != previouslyPowered) {
            notifyUpdate();
        }
        if (isAttached() != getBlockState().getValue(ATTACHED)) {
            BlockState state = getBlockState().cycle(ATTACHED);
            level.setBlockAndUpdate(worldPosition, state);
        }
    }

    @Override
    protected void read(ValueInput tag, boolean clientPacket) {
        super.read(tag, clientPacket);
        powered = tag.getBooleanOr("Powered", false);
    }

    @Override
    protected void write(ValueOutput tag, boolean clientPacket) {
        super.write(tag, clientPacket);
        tag.putBoolean("Powered", powered);
    }

    private ItemInventory getConnectedItemHandler() {
        if (powered) return null;
        // The behaviour hands back a plain Container; only an ItemInventory can be forwarded.
        if (!(observedInventory.getInventory() instanceof ItemInventory handler))
            return null;
        if (handler instanceof WrappedItemHandler)
            return null;
        return handler;
    }

    private void refreshCapability() {
        itemCapability = new InventoryAccessHandler();
        ItemHelper.invalidateInventoryCache(worldPosition);
    }

    /**
     * Forwards a neighbour's inventory through the port.
     * <p>
     * Under Forge this delegated {@code IItemHandler}: getSlots, getStackInSlot, insertItem,
     * extractItem, getSlotLimit, isItemValid. Create Fly's {@link ItemInventory} is vanilla's
     * {@code Container} underneath, which has no notion of a simulated insert or extract, so there
     * is nothing to forward for those -- {@code removeItem} and the rest come from ItemInventory's
     * own defaults, built on the four primitives below.
     * <p>
     * The recursion guard is the point of the class: two ports facing each other would otherwise
     * call into one another forever.
     */
    private class InventoryAccessHandler implements WrappedItemHandler {

        private final ThreadLocal<Boolean> recursionGuard = ThreadLocal.withInitial(() -> false);

        private <T> T preventRecursion(Supplier<T> value, T defaultValue) {
            if (recursionGuard.get()) return defaultValue;
            recursionGuard.set(true);
            T result = value.get();
            recursionGuard.set(false);
            return result;
        }

        @Override
        public int getContainerSize() {
            return preventRecursion(() -> {
                ItemInventory handler = getConnectedItemHandler();
                return handler == null ? 0 : handler.getContainerSize();
            }, 0);
        }

        @Override
        public @NotNull ItemStack getItem(int slot) {
            return preventRecursion(() -> {
                ItemInventory handler = getConnectedItemHandler();
                return handler == null ? ItemStack.EMPTY : handler.getItem(slot);
            }, ItemStack.EMPTY);
        }

        @Override
        public void setItem(int slot, @NotNull ItemStack stack) {
            preventRecursion(() -> {
                ItemInventory handler = getConnectedItemHandler();
                if (handler != null)
                    handler.setItem(slot, stack);
                return null;
            }, null);
        }

        @Override
        public int getMaxStackSize() {
            return preventRecursion(() -> {
                ItemInventory handler = getConnectedItemHandler();
                return handler == null ? 0 : handler.getMaxStackSize();
            }, 0);
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
            return preventRecursion(() -> {
                ItemInventory handler = getConnectedItemHandler();
                return handler != null && handler.canPlaceItem(slot, stack);
            }, false);
        }
    }
}