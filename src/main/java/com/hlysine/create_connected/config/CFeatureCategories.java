package com.hlysine.create_connected.config;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Configures all feature categories.
 * Values in this class should NOT be accessed directly. Please access via {@link FeatureToggle} instead.
 */
public class CFeatureCategories extends SyncConfigBase {

    @Override
    public @NotNull String getName() {
        return "feature_categories";
    }

    final Map<FeatureCategory, ModConfigSpec.ConfigValue<Boolean>> toggles = new HashMap<>();

    Map<FeatureCategory, Boolean> synchronizedToggles;

    @Override
    public void registerAll(ModConfigSpec.Builder builder) {
        for (FeatureCategory r : FeatureCategory.values()) {
            builder.comment(".", r.getDescription());
            toggles.put(r, builder.define(r.getSerializedName(), true));
        }
    }

    @ApiStatus.Internal
    public boolean isEnabled(FeatureCategory category) {
        if (this.synchronizedToggles != null) {
            Boolean synced = synchronizedToggles.get(category);
            if (synced != null) return synced;
        }
        ModConfigSpec.ConfigValue<Boolean> value = toggles.get(category);
        if (value != null)
            return value.get();
        return true;
    }

    /**
     * Refreshes which items are visible after config values change.
     * <p>
     * Was an override of ConfigBase.onLoad/onReload. Create Fly's ConfigBase has no lifecycle
     * hooks at all -- it gives each config its own reload method and calls it from a resource
     * reload listener, the way AllConfigs does. This mirrors that shape.
     * <p>
     * NOTE: nothing calls this yet. It needs hooking to the same reload path as the deferred
     * config sync; until then a config change does not refresh item visibility until restart.
     */
    public void reload() {
        FeatureToggle.refreshItemVisibility();
    }

    @Override
    protected void readSyncConfig(CompoundTag nbt) {
        synchronizedToggles = new HashMap<>();
        for (String key : nbt.getAllKeys()) {
            FeatureCategory category = FeatureCategory.byName(key);
            synchronizedToggles.put(category, nbt.getBooleanOr(key, false));
        }
        FeatureToggle.refreshItemVisibility();
    }

    @Override
    protected void writeSyncConfig(CompoundTag nbt) {
        toggles.forEach((key, value) -> nbt.putBoolean(key.toString(), value.get()));
    }
}
