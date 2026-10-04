package com.hlysine.create_connected.gametest;

import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlock;
import com.hlysine.create_connected.content.kineticbattery.KineticBatteryBlockEntity;
import com.hlysine.create_connected.registries.CCBlocks;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Does a kinetic battery drive every machine that takes a shaft, the way a creative motor does?
 * <p>
 * For each Create and Connected block with a block entity and a shaft face, and for each such face,
 * the machine is placed alone and driven three ways from that face: a creative motor touching it
 * (the control), a discharging battery touching it ("BD"), and a battery behind a shaft ("BED").
 * The battery always faces the machine, since it only drives through its front while discharging.
 * A machine that turns for the motor but not for a battery is a finding. Blocks that do not turn
 * for the motor either (generators, multiblocks, things that need more setup) are listed apart.
 * <p>
 * The full table goes to {@code build/gametest/kinetic-sources.md}.
 */
public class KineticSourceTests {
    private static final Logger LOGGER = LoggerFactory.getLogger("create_connected gametest");
    private static final Set<String> NAMESPACES = Set.of("create", "create_connected");
    private static final BlockPos MACHINE = new BlockPos(4, 4, 4);
    private static final int SETTLE_TICKS = 8;

    enum Source { MOTOR, BATTERY_DIRECT, BATTERY_SHAFT, BATTERY_DIRECT_BACKWARDS }

    record Case(Identifier id, BlockState machine, Direction face) {
    }

    static final class Result {
        final Case c;
        final float[] speed = new float[Source.values().length];
        String note = "";

        Result(Case c) {
            this.c = c;
        }
    }

    // padding: the redstone blocks that keep the batteries discharging must not reach other tests.
    @GameTest(maxTicks = 200_000, skyAccess = true, padding = 8)
    public void batteryDrivesEveryShaftMachine(GameTestHelper helper) {
        List<Case> cases = collectCases(helper);
        LOGGER.info("Kinetic source test: {} machine/face cases", cases.size());
        List<Result> results = new ArrayList<>();
        step(helper, cases, results, 0, 0);
    }

    private static List<Case> collectCases(GameTestHelper helper) {
        List<Case> cases = new ArrayList<>();
        BlockPos at = helper.absolutePos(MACHINE);
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (!NAMESPACES.contains(id.getNamespace())) {
                continue;
            }
            if (!(block instanceof IRotate rotate) || !(block instanceof EntityBlock)) {
                continue;
            }
            if (block == AllBlocks.CREATIVE_MOTOR || block == CCBlocks.KINETIC_BATTERY.get() || block == AllBlocks.SHAFT) {
                continue;
            }
            BlockState state = block.defaultBlockState();
            for (Direction face : Direction.values()) {
                boolean shaft;
                try {
                    shaft = rotate.hasShaftTowards(helper.getLevel(), at, state, face);
                } catch (RuntimeException e) {
                    shaft = false;
                }
                if (shaft) {
                    cases.add(new Case(id, state, face));
                }
            }
        }
        return cases;
    }

    private static void step(GameTestHelper helper, List<Case> cases, List<Result> results, int index, int source) {
        if (index >= cases.size()) {
            finish(helper, results);
            return;
        }
        Case c = cases.get(index);
        if (source == 0) {
            results.add(new Result(c));
        }
        Result result = results.getLast();
        Source s = Source.values()[source];
        try {
            clear(helper);
            helper.setBlock(MACHINE, c.machine());
            place(helper, c.face(), s);
        } catch (RuntimeException e) {
            result.note = "setup failed: " + e;
        }
        helper.runAfterDelay(SETTLE_TICKS, () -> {
            BlockEntity be = helper.getLevel().getBlockEntity(helper.absolutePos(MACHINE));
            boolean skipRest = false;
            if (!(be instanceof KineticBlockEntity kinetic)) {
                result.note = "no kinetic block entity after placement";
                skipRest = true;
            } else if (be instanceof GeneratingKineticBlockEntity) {
                result.note = "generator";
                skipRest = true;
            } else {
                result.speed[source] = kinetic.getSpeed();
                if (s == Source.MOTOR && kinetic.getSpeed() == 0) {
                    skipRest = true;
                }
            }
            if (skipRest || source == Source.values().length - 1) {
                step(helper, cases, results, index + 1, 0);
            } else {
                step(helper, cases, results, index, source + 1);
            }
        });
    }

    /** Empties every cell any case can use: the machine and three blocks out on each side. */
    private static void clear(GameTestHelper helper) {
        for (int i = 3; i >= 1; i--) {
            for (Direction face : Direction.values()) {
                helper.setBlock(MACHINE.relative(face, i), Blocks.AIR);
            }
        }
        helper.setBlock(MACHINE, Blocks.AIR);
        helper.killAllEntitiesOfClass(ItemEntity.class);
    }

    private static void place(GameTestHelper helper, Direction face, Source source) {
        Direction towardMachine = face.getOpposite();
        switch (source) {
            case MOTOR -> helper.setBlock(
                MACHINE.relative(face),
                AllBlocks.CREATIVE_MOTOR.defaultBlockState().setValue(BlockStateProperties.FACING, towardMachine)
            );
            case BATTERY_DIRECT -> placeBattery(helper, MACHINE.relative(face, 1), towardMachine, face);
            // Informational: the back of a discharging battery does not drive (getRotationSpeedModifier).
            case BATTERY_DIRECT_BACKWARDS -> placeBattery(helper, MACHINE.relative(face, 1), face, face);
            case BATTERY_SHAFT -> {
                helper.setBlock(
                    MACHINE.relative(face),
                    AllBlocks.SHAFT.defaultBlockState().setValue(BlockStateProperties.AXIS, face.getAxis())
                );
                placeBattery(helper, MACHINE.relative(face, 2), towardMachine, face);
            }
        }
    }

    /** A full battery facing the machine, kept discharging by a redstone block behind it. */
    private static void placeBattery(GameTestHelper helper, BlockPos pos, Direction facing, Direction away) {
        helper.setBlock(pos.relative(away), Blocks.REDSTONE_BLOCK);
        helper.setBlock(pos, CCBlocks.KINETIC_BATTERY.get().defaultBlockState()
            .setValue(BlockStateProperties.FACING, facing)
            .setValue(KineticBatteryBlock.POWER, 15)
            .setValue(KineticBatteryBlock.LEVEL, 5));
        if (helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof KineticBatteryBlockEntity battery) {
            battery.setBatteryLevel(KineticBatteryBlockEntity.getMaxBatteryLevel());
        }
    }

    private static void finish(GameTestHelper helper, List<Result> results) {
        List<Result> findings = new ArrayList<>();
        List<Result> working = new ArrayList<>();
        List<Result> skipped = new ArrayList<>();
        for (Result r : results) {
            boolean motor = r.speed[Source.MOTOR.ordinal()] != 0;
            if (!motor) {
                skipped.add(r);
            } else if (r.speed[Source.BATTERY_DIRECT.ordinal()] == 0 || r.speed[Source.BATTERY_SHAFT.ordinal()] == 0) {
                findings.add(r);
            } else {
                working.add(r);
            }
        }

        StringBuilder md = new StringBuilder("# Kinetic battery vs creative motor\n\n");
        md.append(String.format(Locale.ROOT, "%d cases: %d turn with every source, %d findings, %d skipped (no turn even with the motor).%n%n",
            results.size(), working.size(), findings.size(), skipped.size()));
        md.append("RPM read from the machine after ").append(SETTLE_TICKS).append(" ticks. BD = battery touching, BED = battery-shaft-machine.\n\n");
        table(md, "Findings: turn with the motor, not with a battery", findings);
        table(md, "Working", working);
        table(md, "Skipped", skipped);
        try {
            Files.writeString(Path.of("kinetic-sources.md"), md);
        } catch (IOException e) {
            LOGGER.warn("Could not write kinetic-sources.md", e);
        }
        LOGGER.info("Kinetic source test: {} working, {} findings, {} skipped", working.size(), findings.size(), skipped.size());
        for (Result r : findings) {
            LOGGER.info("  finding: {} via {} -> motor {}, BD {}, BED {}", r.c.id(), r.c.face(),
                r.speed[0], r.speed[1], r.speed[2]);
        }

        if (findings.isEmpty()) {
            helper.succeed();
        } else {
            helper.fail(findings.size() + " machine faces turn for a creative motor but not for a kinetic battery; see kinetic-sources.md");
        }
    }

    private static void table(StringBuilder md, String title, List<Result> rows) {
        md.append("## ").append(title).append(" (").append(rows.size()).append(")\n\n");
        if (rows.isEmpty()) {
            md.append("None.\n\n");
            return;
        }
        md.append("| block | shaft face | motor | BD | BED | BD backwards | note |\n|---|---|---|---|---|---|---|\n");
        for (Result r : rows) {
            md.append(String.format(Locale.ROOT, "| %s | %s | %.0f | %.0f | %.0f | %.0f | %s |%n",
                r.c.id(), r.c.face().getName(), r.speed[0], r.speed[1], r.speed[2], r.speed[3], r.note));
        }
        md.append('\n');
    }
}
