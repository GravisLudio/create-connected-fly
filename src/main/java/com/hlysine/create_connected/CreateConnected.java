package com.hlysine.create_connected;

import com.hlysine.create_connected.config.CCConfigs;
import com.hlysine.create_connected.datagen.advancements.CCAdvancements;
import com.hlysine.create_connected.datagen.advancements.CCTriggers;
import com.hlysine.create_connected.foundation.registrate.CCRegistrate;
import com.hlysine.create_connected.registries.*;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

/**
 * Fabric main entrypoint (was a NeoForge {@code @Mod} class).
 * <p>
 * NeoForge deferred everything through an event bus: registries fired on {@code RegisterEvent},
 * setup on {@code FMLCommonSetupEvent}. Fabric has no such bus -- {@code onInitialize} already runs
 * at the right point in load, and {@link CCRegistrate} registers eagerly, so those phases collapse
 * into straight-line calls here. Order still matters: blocks before block entities, because
 * {@code validBlocks} resolves actual Block instances.
 * <p>
 * TODO: {@code setTooltipModifierFactory} and {@code setCreativeTab} came from Create's
 * CreateRegistrate. Both are presentation concerns -- tooltips are client-side, and creative tab
 * contents go through Fabric's {@code ItemGroupEvents} -- so they are not wired here yet.
 */
public class CreateConnected implements ModInitializer {
    public static final String MODID = "create_connected";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final CCRegistrate REGISTRATE = CCRegistrate.create(MODID);

    @Override
    public void onInitialize() {
        CCConfigs.register();

        CCSoundEvents.prepare();
        CCDataComponents.register();
        CCBlocks.register();
        CCItems.register();
        CCBlockEntityTypes.register();
        CCCreativeTabs.register();
        CCPackets.register();
        CCCraftingConditions.register();
        CCArmInteractionPointTypes.register();
        CCSoundEvents.register();

        // Previously ran from FMLCommonSetupEvent. Nothing here needs a deferred phase on Fabric:
        // every registry these touch is already populated by the calls above.
        CCInteractionBehaviours.register();
        CCMovementBehaviours.register();
        CCMountedStorageTypes.register();
        CCDisplaySources.register();
        CCDisplayTargets.register();

        // Were keyed off RegisterEvent for a specific registry; now called directly.
        CCItemAttributes.register();
        CCAdvancements.register();
        CCTriggers.register();

        // Copycats+ and Additional Placements integrations are excluded from the build until
        // those mods exist on 26.2 -- see the sourceSets excludes in build.gradle. Their source
        // is still in compat/, so re-enabling is a one-line change per mod.
    }

    public static CCRegistrate getRegistrate() {
        return REGISTRATE;
    }

    public static Identifier asResource(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
