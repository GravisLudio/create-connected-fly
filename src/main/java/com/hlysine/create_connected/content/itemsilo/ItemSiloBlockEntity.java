package com.hlysine.create_connected.content.itemsilo;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.hlysine.create_connected.CreateConnected;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.api.packager.InventoryIdentifier;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import com.zurrtum.create.infrastructure.items.CombinedInvWrapper;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import net.minecraft.world.Container;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

import java.util.List;

public class ItemSiloBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Inventory, Clearable {

    /**
     * Was a Create {@code ICapabilityProvider}. Neither that nor NeoForge capabilities exist here,
     * and the indirection still has to be lazy: a non-controller resolves through its controller,
     * which may not be loaded yet. A plain supplier covers it.
     */
    protected Supplier<Container> itemCapability = null;
    protected InventoryIdentifier invId;

    protected ItemStackHandler inventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int radius;
    protected int length;
    protected Axis axis;

    public ItemSiloBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        inventory = new ItemStackHandler(AllConfigs.server().logistics.vaultCapacity.get()) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                updateComparators();
                level.blockEntityChanged(worldPosition);
            }
        };

        radius = 1;
        length = 1;
    }

    /** Exposed through {@code ItemInventoryProvider} on {@link ItemSiloBlock}. */
    public @Nullable Container getItemInventory() {
        initCapability();
        return itemCapability == null ? null : itemCapability.get();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide())
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    protected void updateComparators() {
        ItemSiloBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null)
            return;

        level.blockEntityChanged(controllerBE.worldPosition);

        BlockPos pos = controllerBE.getBlockPos();
        for (int y = 0; y < controllerBE.length; y++) {
            for (int z = 0; z < controllerBE.radius; z++) {
                for (int x = 0; x < controllerBE.radius; x++) {
                    level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity)
            updateConnectivity();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ItemSiloBlockEntity getControllerBE() {
        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof ItemSiloBlockEntity)
            return (ItemSiloBlockEntity) blockEntity;
        return null;
    }

    public void removeController(boolean keepContents) {
        if (level.isClientSide())
            return;
        updateConnectivity = true;
        controller = null;
        radius = 1;
        length = 1;

        BlockState state = getBlockState();
        if (ItemSiloBlock.isVault(state)) {
            state = state.setValue(ItemSiloBlock.LARGE, false);
            getLevel().setBlock(worldPosition, state, 22);
        }

        itemCapability = null;
        ItemHelper.invalidateInventoryCache(worldPosition);
        setChanged();
        sendData();
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide() && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
        itemCapability = null;
        ItemHelper.invalidateInventoryCache(worldPosition);
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected void read(ValueInput compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = radius;
        int prevLength = length;

        // ValueInput has no contains(): read() already returns an Optional, so an absent key and a
        // null value collapse into the same orElse(null).
        updateConnectivity = compound.getBooleanOr("Uninitialized", false);

        lastKnownPos = compound.read("LastKnownPos", BlockPos.CODEC).orElse(null);

        controller = compound.read("Controller", BlockPos.CODEC).orElse(null);

        if (isController()) {
            radius = compound.getIntOr("Size", 0);
            length = compound.getIntOr("Length", 0);
        }

        if (!clientPacket) {
            inventory.read(compound);
            return;
        }

        boolean changeOfController =
                controllerBefore == null ? controller != null : !controllerBefore.equals(controller);
        if (hasLevel() && (changeOfController || prevSize != radius || prevLength != length))
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
    }

    @Override
    protected void write(ValueOutput compound, boolean clientPacket) {
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null)
            compound.store("LastKnownPos", BlockPos.CODEC, lastKnownPos);
        if (!isController())
            compound.store("Controller", BlockPos.CODEC, controller);
        if (isController()) {
            compound.putInt("Size", radius);
            compound.putInt("Length", length);
        }

        super.write(compound, clientPacket);

        if (!clientPacket) {
            compound.putString("StorageType", "CombinedInv");
            inventory.write(compound);
        }
    }

    public ItemStackHandler getInventoryOfBlock() {
        return inventory;
    }

    public InventoryIdentifier getInvId() {
        // ensure capability is up to date first, which sets the ID
        this.initCapability();
        return this.invId;
    }

    public void applyInventoryToBlock(ItemStackHandler handler) {
        for (int i = 0; i < inventory.getContainerSize(); i++)
            inventory.setItem(i, i < handler.getContainerSize() ? handler.getItem(i) : ItemStack.EMPTY);
    }

    private void initCapability() {
        if (itemCapability != null && itemCapability.get() != null)
            return;
        if (!isController()) {
            ItemSiloBlockEntity controllerBE = getControllerBE();
            if (controllerBE == null)
                return;
            controllerBE.initCapability();
            itemCapability = () -> {
                if (controllerBE.isRemoved())
                    return null;
                if (controllerBE.itemCapability == null)
                    return null;
                return controllerBE.itemCapability.get();
            };
            invId = controllerBE.invId;
            return;
        }

        Container[] invs = new Container[length * radius * radius];
        for (int yOffset = 0; yOffset < length; yOffset++) {
            for (int xOffset = 0; xOffset < radius; xOffset++) {
                for (int zOffset = 0; zOffset < radius; zOffset++) {
                    BlockPos vaultPos = worldPosition.offset(xOffset, yOffset, zOffset);
                    ItemSiloBlockEntity vaultAt =
                            ConnectivityHandler.partAt(CCBlockEntityTypes.ITEM_SILO.get(), level, vaultPos);
                    invs[yOffset * radius * radius + xOffset * radius + zOffset] =
                            vaultAt != null ? vaultAt.inventory : new ItemStackHandler();
                }
            }
        }

        // VersionedInventoryWrapper is gone; it tracked a version so callers could invalidate
        // cached lookups. Create Fly handles that centrally via ItemHelper.invalidateInventoryCache.
        Container combined = new CombinedInvWrapper(invs);
        itemCapability = () -> combined;

        // build an identifier encompassing all component vaults
        BlockPos farCorner = worldPosition.offset(radius, length, radius);
        BoundingBox bounds = BoundingBox.fromCorners(this.worldPosition, farCorner);
        this.invId = new InventoryIdentifier.Bounds(bounds);
    }

    public static int getMaxLength(int radius) {
        return radius * 3;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (ItemSiloBlock.isVault(state)) { // safety
            level.setBlock(getBlockPos(), state.setValue(ItemSiloBlock.LARGE, radius > 2), 6);
        }
        itemCapability = null;
        ItemHelper.invalidateInventoryCache(worldPosition);
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return getMainAxisOf(this);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y) return getMaxLength(width);
        return getMaxWidth();
    }

    @Override
    public int getMaxWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return length;
    }

    @Override
    public int getWidth() {
        return radius;
    }

    @Override
    public void setHeight(int height) {
        this.length = height;
    }

    @Override
    public void setWidth(int width) {
        this.radius = width;
    }

    @Override
    public boolean hasInventory() {
        return true;
    }

    @Override
    public void clearContent() {
        // Was a mixin accessor reaching into Forge's ItemStackHandler backing list.
        // Create Fly's is a Container, which clears itself.
        inventory.clearContent();
    }
}

