package com.hlysine.create_connected.config;

import com.hlysine.create_connected.CreateConnected;
import org.jetbrains.annotations.NotNull;

public class CCommon extends SyncConfigBase {

    @Override
    public @NotNull String getName() {
        return "common";
    }

    public final ConfigBool migrateCopycatsOnBlockUpdate = b(true, "migrateCopycatsOnBlockUpdate", Comments.migrateCopycatsOnBlockUpdate);

    public final ConfigBool migrateCopycatsOnInitialize = b(true, "migrateCopycatsOnInitialize", Comments.migrateCopycatsOnInitialize);

    public final CFeatures toggle = nested(0, CFeatures::new, Comments.toggle);

    public final CFeatureCategories categories = nested(0, CFeatureCategories::new, Comments.categories);

    // TODO server -> client config sync is NOT wired up on Fabric yet.
    //
    // Under NeoForge this hooked RegisterPayloadHandlersEvent (to register the payload) and
    // RegisterConfigurationTasksEvent (to push it during the configuration phase). Fabric has
    // neither event. Create Fly solves it with:
    //   - infrastructure/config/SyncConfigTask, a vanilla ConfigurationTask, and
    //   - a mixin into ServerConfigurationPacketListenerImpl that appends the task, since there
    //     is no event to register one.
    // Mirror that here when the mod actually launches; writing the networking blind, before
    // anything can be tested, is the least useful order to do it in.
    //
    // Consequence until then: config values are local. A server does NOT override client
    // settings, so feature toggles can disagree between the two sides.

    private static class Comments {
        static String toggle = "Enable/disable features. Values on server override clients";
        static String categories = "Enable/disable categories of features. Disabling a category hides all related features. Values on server override clients";
        static String migrateCopycatsOnBlockUpdate = "Migrate copycats to Create: Copycats+ when they receive a block update";
        static String migrateCopycatsOnInitialize = "Migrate copycats to Create: Copycats+ when their block entities are initialized";
    }

    // CommonSyncConfigTask lived here and extended SyncConfigBase.SyncConfigTask, a NeoForge
    // ICustomConfigurationTask. It is removed along with the rest of the sync path; see the note
    // above. Create Fly's equivalent is a plain vanilla ConfigurationTask, so the replacement will
    // not look like this one.
}
