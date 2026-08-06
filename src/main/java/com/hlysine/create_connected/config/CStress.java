package com.hlysine.create_connected.config;

import com.hlysine.create_connected.CreateConnected;
import com.zurrtum.create.Create;
import com.hlysine.create_connected.foundation.registrate.BlockBuilder;
import java.util.function.UnaryOperator;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import com.zurrtum.create.catnip.config.ConfigBase;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.DoubleRawValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

public class CStress extends ConfigBase {
    // bump this version to reset configured values.
    private static final int VERSION = 1;

    private static final Object2DoubleMap<Identifier> DEFAULT_IMPACTS = new Object2DoubleOpenHashMap<>();
    private static final Object2DoubleMap<Identifier> DEFAULT_CAPACITIES = new Object2DoubleOpenHashMap<>();

    protected final Map<Identifier, DoubleRawValue> capacities = new HashMap<>();
    protected final Map<Identifier, DoubleRawValue> impacts = new HashMap<>();

    @Override
    public void registerAll(Builder builder) {
        builder.comment(".", Comments.su, Comments.impact)
                .push("impact");
        DEFAULT_IMPACTS.forEach((id, value) -> this.impacts.put(id, builder.define(id.getPath(), value)));
        builder.pop();

        builder.comment(".", Comments.su, Comments.capacity)
                .push("capacity");
        DEFAULT_CAPACITIES.forEach((id, value) -> this.capacities.put(id, builder.define(id.getPath(), value)));
        builder.pop();
    }

    @Override
    public @NotNull String getName() {
        return "stressValues.v" + VERSION;
    }

    @Nullable
    public DoubleSupplier getImpact(Block block) {
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(block);
        DoubleRawValue value = this.impacts.get(id);
        return value == null ? null : value::get;
    }

    @Nullable
    public DoubleSupplier getCapacity(Block block) {
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(block);
        DoubleRawValue value = this.capacities.get(id);
        return value == null ? null : value::get;
    }

    public static <B extends Block> UnaryOperator<BlockBuilder<B>> setNoImpact() {
        return setImpact(0);
    }

    public static <B extends Block> UnaryOperator<BlockBuilder<B>> setImpact(double value) {
        return builder -> {
            assertFromCC(builder);
            Identifier id = CreateConnected.asResource(builder.getName());
            DEFAULT_IMPACTS.put(id, value);
            return builder;
        };
    }

    public static <B extends Block> UnaryOperator<BlockBuilder<B>> setCapacity(double value) {
        return builder -> {
            assertFromCC(builder);
            Identifier id = CreateConnected.asResource(builder.getName());
            DEFAULT_CAPACITIES.put(id, value);
            return builder;
        };
    }

    private static void assertFromCC(BlockBuilder<?> builder) {
        if (!builder.getModid().equals(CreateConnected.MODID)) {
            throw new IllegalStateException("Unrelated blocks cannot be added to the config of Create: Connected.");
        }
    }

    private static class Comments {
        static String su = "[in Stress Units]";
        static String impact =
                "Configure the individual stress impact of mechanical blocks. Note that this cost is doubled for every speed increase it receives.";
        static String capacity = "Configure how much stress a source can accommodate for.";
    }

}
