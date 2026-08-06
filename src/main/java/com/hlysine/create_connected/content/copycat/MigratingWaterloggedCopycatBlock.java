package com.hlysine.create_connected.content.copycat;

import net.minecraft.util.RandomSource;

import net.minecraft.world.level.ScheduledTickAccess;

import net.minecraft.world.level.LevelReader;

import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.hlysine.create_connected.config.CCConfigs;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
import com.zurrtum.create.content.decoration.copycat.WaterloggedCopycatBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MigratingWaterloggedCopycatBlock extends WaterloggedCopycatBlock {

    public MigratingWaterloggedCopycatBlock(Properties pProperties) {
        super(pProperties);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext pContext) {
        BlockState state = super.getStateForPlacement(pContext);
        assert state != null;
        return migrate(state);
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState pState, @NotNull LevelReader pLevel, ScheduledTickAccess tickAccess, @NotNull BlockPos pCurrentPos, @NotNull Direction pDirection, @NotNull BlockPos pNeighborPos, @NotNull BlockState pNeighborState, RandomSource random) {
        return migrateOnUpdate(pLevel.isClientSide(), super.updateShape(pState, pLevel, tickAccess, pCurrentPos, pDirection, pNeighborPos, pNeighborState, random));
    }

    protected static BlockState migrateOnUpdate(boolean isClient, BlockState state) {
        if (!isClient && CCConfigs.common().migrateCopycatsOnBlockUpdate.get())
            return migrate(state);
        return state;
    }

    // See MigratingCopycatBlock: CopycatsManager is excluded from the build, so only the
    // mod-absent fallback of each gated branch can exist.

    protected static BlockState migrate(BlockState state) {
        return state;
    }

    protected boolean isSelfState(BlockState state) {
        return state.is(this);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState pState, LootParams.@NotNull Builder pParams) {
        return super.getDrops(pState, pParams);
    }

    @Override
    public BlockEntityType<? extends CopycatBlockEntity> getBlockEntityType() {
        return CCBlockEntityTypes.COPYCAT.get();
    }
}
