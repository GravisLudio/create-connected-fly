package com.hlysine.create_connected.gametest;

import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlock;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockEntity;
import com.hlysine.create_connected.registries.CCBlocks;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.DirectionalPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * A player's report: battery -> shaft -> deployer turns; take the shaft out and put the deployer in
 * its place, touching the same face of the same battery, and it does not. KineticSourceTests places
 * the machine first and the battery second, so it never saw this; here the battery is running first
 * and the deployer arrives afterwards, placed the way a player places it.
 */
public class BatteryPlacementOrderTests {
    private static final BlockPos REDSTONE = new BlockPos(6, 1, 3);
    private static final BlockPos BATTERY = new BlockPos(5, 1, 3);
    private static final BlockPos NEAR = new BlockPos(4, 1, 3);
    private static final BlockPos FAR = new BlockPos(3, 1, 3);

    @GameTest(maxTicks = 100)
    public void deployerPlacedAgainstRunningBattery(GameTestHelper helper) {
        placeRunningBattery(helper);
        helper.runAfterDelay(10, () -> {
            placeDeployerLikeAPlayer(helper, NEAR);
            helper.runAfterDelay(10, () -> {
                assertTurning(helper, NEAR, "deployer placed against a running battery");
                helper.succeed();
            });
        });
    }

    @GameTest(maxTicks = 100)
    public void deployerMovedIntoTheShaftsPlace(GameTestHelper helper) {
        placeRunningBattery(helper);
        helper.setBlock(NEAR, AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.X));
        helper.runAfterDelay(10, () -> {
            placeDeployerLikeAPlayer(helper, FAR);
            helper.runAfterDelay(10, () -> {
                assertTurning(helper, FAR, "deployer behind a shaft (BED)");
                BlockState deployer = helper.getBlockState(FAR);
                helper.setBlock(FAR, Blocks.AIR);
                helper.setBlock(NEAR, Blocks.AIR);
                helper.runAfterDelay(10, () -> {
                    helper.setBlock(NEAR, deployer);
                    helper.runAfterDelay(10, () -> {
                        assertTurning(helper, NEAR, "same deployer state moved into the shaft's place (BD)");
                        helper.succeed();
                    });
                });
            });
        });
    }

    private static void placeRunningBattery(GameTestHelper helper) {
        helper.setBlock(REDSTONE, Blocks.REDSTONE_BLOCK);
        helper.setBlock(BATTERY, CCBlocks.KINETIC_BATTERY.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, Direction.WEST)
            .setValue(KineticBatteryBlock.POWER, 15)
            .setValue(KineticBatteryBlock.LEVEL, 5));
        helper.getBlockEntity(BATTERY, KineticBatteryBlockEntity.class).setBatteryLevel(KineticBatteryBlockEntity.getMaxBatteryLevel());
    }

    /** A player looking down, so the deployer faces up and picks its shaft axis from its neighbours. */
    private static void placeDeployerLikeAPlayer(GameTestHelper helper, BlockPos pos) {
        DirectionalPlaceContext context = new DirectionalPlaceContext(
            helper.getLevel(), helper.absolutePos(pos), Direction.DOWN, new ItemStack(AllBlocks.DEPLOYER), Direction.UP);
        helper.setBlock(pos, AllBlocks.DEPLOYER.getStateForPlacement(context));
    }

    private static void assertTurning(GameTestHelper helper, BlockPos pos, String what) {
        KineticBlockEntity be = helper.getBlockEntity(pos, KineticBlockEntity.class);
        KineticBatteryBlockEntity battery = helper.getBlockEntity(BATTERY, KineticBatteryBlockEntity.class);
        if (be.getSpeed() == 0) {
            throw helper.assertionException(pos, "%s does not turn: %s; battery %s speed %s level %s".formatted(
                what, helper.getBlockState(pos), helper.getBlockState(BATTERY), battery.getSpeed(), battery.getBatteryLevel()));
        }
    }
}
