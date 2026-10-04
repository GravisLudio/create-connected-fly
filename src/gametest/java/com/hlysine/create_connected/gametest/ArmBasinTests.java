package com.hlysine.create_connected.gametest;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.logistics.chute.ChuteBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;

/**
 * The two Create Fly arm bugs that {@code mixin/createfixes/ArmInteractionPoint*} and
 * {@code ArmUpstreamPointModeMixin} fix, on the setup a player hit: an arm taking from a basin
 * into a smart chute filtered to granite, with a chest under the chute.
 */
public class ArmBasinTests {
    private static final BlockPos ARM = new BlockPos(3, 1, 1);
    private static final BlockPos BASIN = new BlockPos(1, 1, 1);
    private static final BlockPos CHUTE = new BlockPos(5, 1, 1);
    private static final BlockPos CHEST = new BlockPos(5, 0, 1);

    /** Leftover quartz sits in the input slots ahead of the granite; the arm must still move the granite. */
    @GameTest(maxTicks = 400)
    public void armTakesGraniteBehindLeftoverQuartz(GameTestHelper helper) {
        build(helper, "take", "deposit");
        BasinBlockEntity basin = helper.getBlockEntity(BASIN, BasinBlockEntity.class);
        basin.itemCapability.setItem(0, new ItemStack(Items.QUARTZ, 32));
        basin.itemCapability.setItem(9, new ItemStack(Items.GRANITE, 16));
        succeedWhenGraniteArrives(helper);
    }

    /** Create writes arm point modes uppercase; a schematic made there must keep its targets. */
    @GameTest(maxTicks = 400)
    public void armKeepsUppercaseTargetsFromCreate(GameTestHelper helper) {
        build(helper, "TAKE", "DEPOSIT");
        BasinBlockEntity basin = helper.getBlockEntity(BASIN, BasinBlockEntity.class);
        basin.itemCapability.setItem(9, new ItemStack(Items.GRANITE, 16));
        succeedWhenGraniteArrives(helper);
    }

    private static void build(GameTestHelper helper, String take, String deposit) {
        // The arm is a small cogwheel with no shaft: it turns only through a cog beside it.
        helper.setBlock(new BlockPos(3, 0, 0), AllBlocks.CREATIVE_MOTOR.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
        helper.setBlock(new BlockPos(3, 1, 0), AllBlocks.COGWHEEL.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Y));
        helper.setBlock(BASIN, AllBlocks.BASIN);
        helper.setBlock(CHEST, Blocks.CHEST);
        // The default state is powered=true; placement by a player recomputes it, setBlock does not.
        helper.setBlock(CHUTE, AllBlocks.SMART_CHUTE.defaultBlockState().setValue(BlockStateProperties.POWERED, false));
        ServerFilteringBehaviour filter = BlockEntityBehaviour.get(helper.getLevel(), helper.absolutePos(CHUTE), ServerFilteringBehaviour.TYPE);
        filter.setFilter(new ItemStack(Items.GRANITE));

        helper.setBlock(ARM, AllBlocks.MECHANICAL_ARM);
        ArmBlockEntity arm = helper.getBlockEntity(ARM, ArmBlockEntity.class);
        ListTag points = new ListTag();
        points.add(point("create:basin", BASIN, take));
        points.add(point("create:chute", CHUTE, deposit));
        CompoundTag tag = new CompoundTag();
        tag.put("InteractionPoints", points);
        // A server-side read, as when a schematic places the arm.
        arm.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), tag));
    }

    private static CompoundTag point(String type, BlockPos target, String mode) {
        BlockPos rel = target.subtract(ARM);
        CompoundTag point = new CompoundTag();
        point.putString("Type", type);
        point.put("Pos", new IntArrayTag(new int[]{rel.getX(), rel.getY(), rel.getZ()}));
        point.putString("Mode", mode);
        return point;
    }

    private static void succeedWhenGraniteArrives(GameTestHelper helper) {
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(CHEST, ChestBlockEntity.class);
            if (chest.countItem(Items.GRANITE) == 0) {
                ArmBlockEntity arm = helper.getBlockEntity(ARM, ArmBlockEntity.class);
                BasinBlockEntity basin = helper.getBlockEntity(BASIN, BasinBlockEntity.class);
                ChuteBlockEntity chute = helper.getBlockEntity(CHUTE, ChuteBlockEntity.class);
                // inputs/outputs 0 = the targets were dropped on load; slot 9 still full with the
                // arm idle in SEARCH_INPUTS = the arm never saw the granite.
                throw helper.assertionException(CHEST, "no granite in the chest yet; arm speed %s phase %s inputs %s outputs %s held %s; basin[0] %s basin[9] %s; chute %s %s".formatted(
                    arm.getSpeed(), arm.phase, arm.inputs.size(), arm.outputs.size(), arm.heldItem,
                    basin.itemCapability.getItem(0), basin.itemCapability.getItem(9), chute.getItem(), helper.getBlockState(CHUTE)));
            }
        });
    }
}
