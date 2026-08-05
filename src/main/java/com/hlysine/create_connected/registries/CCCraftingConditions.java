package com.hlysine.create_connected.registries;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.datagen.recipes.FeatureEnabledCondition;
import com.hlysine.create_connected.datagen.recipes.FeatureEnabledInCopycatsCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditionType;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;

/**
 * Fabric keeps resource conditions in a map of its own rather than a Minecraft registry, so these
 * are plain objects registered directly instead of DeferredRegister entries.
 */
public class CCCraftingConditions {
    public static final ResourceConditionType<FeatureEnabledCondition> FEATURE_ENABLED =
            ResourceConditionType.create(CreateConnected.asResource("feature_enabled"), FeatureEnabledCondition.CODEC);

    public static final ResourceConditionType<FeatureEnabledInCopycatsCondition> FEATURE_ENABLED_IN_COPYCATS =
            ResourceConditionType.create(
                    CreateConnected.asResource("feature_enabled_in_copycats"),
                    FeatureEnabledInCopycatsCondition.CODEC
            );

    public static void register() {
        ResourceConditions.register(FEATURE_ENABLED);
        ResourceConditions.register(FEATURE_ENABLED_IN_COPYCATS);
    }
}
