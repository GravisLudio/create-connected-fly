package com.hlysine.create_connected.content.copycat;

import com.hlysine.create_connected.registries.CCBlockEntityTypes;
import com.hlysine.create_connected.compat.CopycatsManager;
import com.hlysine.create_connected.compat.Mods;
import com.hlysine.create_connected.config.CCConfigs;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.decoration.copycat.CopycatBlockEntity;
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

public abstract class MigratingCopycatBlock extends CopycatBlock {

    public MigratingCopycatBlock(Properties pProperties) {
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
    public @NotNull BlockState updateShape(@NotNull BlockState pState, @NotNull Direction pDirection, @NotNull BlockState pNeighborState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pNeighborPos) {
        return migrateOnUpdate(pLevel.isClientSide(), super.updateShape(pState, pDirection, pNeighborState, pLevel, pCurrentPos, pNeighborPos));
    }

    protected static BlockState migrateOnUpdate(boolean isClient, BlockState state) {
        if (!isClient && CCConfigs.common().migrateCopycatsOnBlockUpdate.get())
            return migrate(state);
        return state;
    }

    // Every branch below used to be gated on Mods.COPYCATS.runIfInstalled and fall back when the
    // mod was absent. CopycatsManager is excluded from the build until Copycats+ ships for 26.2
    // (see build.gradle), so only the fallback path can exist -- which is exactly what would run
    // anyway. Restore these together with the exclude.

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
