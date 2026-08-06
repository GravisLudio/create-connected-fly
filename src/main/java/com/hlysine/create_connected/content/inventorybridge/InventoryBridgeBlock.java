package com.hlysine.create_connected.content.inventorybridge;

import net.minecraft.world.level.redstone.Orientation;

import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;
import net.minecraft.world.Container;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

public class InventoryBridgeBlock extends Block
        implements IBE<InventoryBridgeBlockEntity>, IWrenchable, ItemInventoryProvider<InventoryBridgeBlockEntity> {

    public static BooleanProperty ATTACHED_POSITIVE = BooleanProperty.create("attached_positive");
    public static BooleanProperty ATTACHED_NEGATIVE = BooleanProperty.create("attached_negative");
    public static EnumProperty<Axis> AXIS = BlockStateProperties.AXIS;

    public InventoryBridgeBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(ATTACHED_POSITIVE, false)
                .setValue(ATTACHED_NEGATIVE, false)
                .setValue(AXIS, Axis.X)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(ATTACHED_POSITIVE, ATTACHED_NEGATIVE, AXIS));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();

        Direction preferredFacing = null;
        for (Direction face : context.getNearestLookingDirections()) {
            BlockPos neighbour = context.getClickedPos().relative(face);
            // Was a capability lookup; Create Fly resolves inventories through ItemHelper, which
            // covers both block entities and vanilla WorldlyContainerHolder blocks.
            if (ItemHelper.getInventory(context.getLevel(), neighbour, null) != null) {
                preferredFacing = face;
                break;
            }
        }

        if (preferredFacing == null) {
            preferredFacing = context.getNearestLookingDirection();
        }

        return state.setValue(AXIS, preferredFacing.getAxis());
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving) {
        withBlockEntityDo(worldIn, pos, InventoryBridgeBlockEntity::updateConnectedInventory);
    }

    @Override
    public void neighborChanged(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Block pBlock, Orientation orientation, boolean pIsMoving) {
        withBlockEntityDo(pLevel, pPos, InventoryBridgeBlockEntity::updateConnectedInventory);
        super.neighborChanged(pState, pLevel, pPos, pBlock, orientation, pIsMoving);

        // 26.2 replaced the source position with an Orientation, and the vanilla path that reaches
        // us passes null for it -- so the notifying side can no longer be identified. Upstream
        // excluded it to keep two facing bridges from notifying each other forever; a re-entrancy
        // guard bounds the same cascade. It costs nothing here because bridges never chain:
        // getAnalogOutputSignal already ignores a target that is another bridge.
        if (propagating)
            return;
        propagating = true;
        try {
            pLevel.updateNeighborsAt(pPos, this, orientation);
        } finally {
            propagating = false;
        }
    }

    private static boolean propagating = false;

    public static Direction getNegativeTarget(BlockState state) {
        return Direction.fromAxisAndDirection(state.getValue(AXIS), Direction.AxisDirection.NEGATIVE);
    }

    public static Direction getPositiveTarget(BlockState state) {
        return Direction.fromAxisAndDirection(state.getValue(AXIS), Direction.AxisDirection.POSITIVE);
    }

    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(@NotNull BlockState blockState, @NotNull Level worldIn, @NotNull BlockPos pos, Direction side) {
        BlockPos pos1 = pos.relative(getNegativeTarget(blockState));
        BlockPos pos2 = pos.relative(getPositiveTarget(blockState));
        BlockState target1 = worldIn.getBlockState(pos1);
        BlockState target2 = worldIn.getBlockState(pos2);
        int total = 0;
        if (blockState.getValue(ATTACHED_NEGATIVE) && !target1.is(this) && target1.hasAnalogOutputSignal())
            total += target1.getAnalogOutputSignal(worldIn, pos1);
        if (blockState.getValue(ATTACHED_POSITIVE) && !target2.is(this) && target2.hasAnalogOutputSignal())
            total += target2.getAnalogOutputSignal(worldIn, pos2);
        return total / 2;
    }

    @Override
    public Class<InventoryBridgeBlockEntity> getBlockEntityClass() {
        return InventoryBridgeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends InventoryBridgeBlockEntity> getBlockEntityType() {
        return CCBlockEntityTypes.INVENTORY_BRIDGE.get();
    }

    // --- ItemInventoryProvider ---
    // Replaces the NeoForge capability registration that used to live in the block entity.

    // getBlockEntityClass() comes from Create's IBE, which already satisfies
    // ItemInventoryProvider's requirement -- no second declaration needed.

    @Override
    public @Nullable Container getInventory(
            LevelAccessor world,
            BlockPos pos,
            BlockState state,
            InventoryBridgeBlockEntity blockEntity,
            @Nullable Direction context
    ) {
        return blockEntity.getItemInventory();
    }
}

