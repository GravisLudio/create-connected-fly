package com.hlysine.create_connected.datagen.recipes;

import com.hlysine.create_connected.registries.CCCraftingConditions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

/**
 * See {@link FeatureEnabledCondition} for the NeoForge-to-Fabric mapping.
 * <p>
 * This one asked Copycats+ whether a feature was on, and returned false when the mod was absent.
 * CopycatsManager is excluded from the build until Copycats+ ships for 26.2 (see build.gradle), so
 * only that false branch can exist -- which is what would run without the mod anyway. The condition
 * itself stays registered because recipe JSONs reference it by id, and an unregistered condition id
 * is treated as successful by Fabric, which would silently enable recipes that should be off.
 */
public record FeatureEnabledInCopycatsCondition(Identifier feature) implements ResourceCondition {
    public static final MapCodec<FeatureEnabledInCopycatsCondition> CODEC = RecordCodecBuilder.mapCodec((builder) -> builder
            .group(Identifier.CODEC.fieldOf("tag").forGetter(FeatureEnabledInCopycatsCondition::feature))
            .apply(builder, FeatureEnabledInCopycatsCondition::new)
    );

    @Override
    public boolean test(RegistryOps.@Nullable RegistryInfoLookup registryInfo) {
        return false;
    }

    @Override
    public ResourceConditionType<?> getType() {
        return CCCraftingConditions.FEATURE_ENABLED_IN_COPYCATS;
    }

    @Override
    public String toString() {
        return "feature_enabled_in_copycats(\"" + feature + "\")";
    }
}
