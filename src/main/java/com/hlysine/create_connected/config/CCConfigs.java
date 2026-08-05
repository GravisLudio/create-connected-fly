package com.hlysine.create_connected.config;

import com.hlysine.create_connected.CreateConnected;
import com.zurrtum.create.api.stress.BlockStressValues;
import com.zurrtum.create.catnip.config.Builder;

import java.util.function.Supplier;

/**
 * Mod configuration.
 * <p>
 * NeoForge needed a {@code ModConfigSpec} built through a builder pair, registered against a
 * {@code ModContainer}, with {@code ModConfigEvent.Loading}/{@code Reloading} listeners to know
 * when values became available. Create Fly's catnip ships {@code Builder.create}, which does all of
 * that in one call and owns the load/reload lifecycle itself -- so the two event handlers and the
 * spec bookkeeping are gone rather than ported.
 */
@SuppressWarnings("unused")
public class CCConfigs {

    private static CCommon common;
    private static CServer server;

    public static CCommon common() {
        return common;
    }

    public static CServer server() {
        return server;
    }

    /**
     * Reads a config value, falling back if it is touched before the config has loaded.
     * Kept from the NeoForge version: registration order still lets this happen.
     */
    public static <T> Supplier<T> safeGetter(Supplier<T> getter, T defaultValue) {
        return () -> {
            try {
                return getter.get();
            } catch (IllegalStateException | NullPointerException ex) {
                return defaultValue;
            }
        };
    }

    public static void register() {
        common = Builder.create(CCommon::new, CreateConnected.MODID, "common");
        server = Builder.create(CServer::new, CreateConnected.MODID, "server");

        CStress stress = server().stressValues;
        BlockStressValues.IMPACTS.registerProvider(stress::getImpact);
        BlockStressValues.CAPACITIES.registerProvider(stress::getCapacity);
    }
}
