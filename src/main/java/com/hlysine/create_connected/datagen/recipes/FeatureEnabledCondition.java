package com.hlysine.create_connected.datagen.recipes;

import com.hlysine.create_connected.config.FeatureToggle;
import com.hlysine.create_connected.registries.CCCraftingConditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's {@code ICondition} becomes Fabric's {@link ResourceCondition}. The shape is the same
 * -- a MapCodec plus a test -- but the type is a first-class object registered by hand rather than
 * a codec placed in a registry, and {@code test} receives a registry lookup rather than an
 * {@code IContext}.
 * <p>
 * The serialised form changed with it: the surrounding key is {@code fabric:load_conditions} and
 * each entry is discriminated by {@code condition}, not {@code type}. The recipe JSONs already
 * carry that form.
 */
public record FeatureEnabledCondition(Identifier feature) implements ResourceCondition {
    public static final MapCodec<FeatureEnabledCondition> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(Identifier.CODEC.fieldOf("tag").forGetter(FeatureEnabledCondition::feature))
            .apply(builder, FeatureEnabledCondition::new)
    );

    @Override
    public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
        return FeatureToggle.isEnabled(feature);
    }

    @Override
    public ResourceConditionType<?> getType() {
        return CCCraftingConditions.FEATURE_ENABLED;
    }

    @Override
    public String toString() {
        return "feature_enabled(\"" + feature + "\")";
    }
}
