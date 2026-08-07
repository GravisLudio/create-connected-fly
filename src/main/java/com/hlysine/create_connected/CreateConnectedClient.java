package com.hlysine.create_connected;

import com.hlysine.create_connected.client.CCConnectedTextures;
import com.hlysine.create_connected.client.CCBlockEntityRenders;
import com.hlysine.create_connected.client.CCModels;
import com.hlysine.create_connected.client.display.CCDisplaySourceRenders;
import com.hlysine.create_connected.registries.CCBlockEntityBehaviours;
import com.hlysine.create_connected.registries.CCPartialModels;
import com.hlysine.create_connected.registries.CCPonderPlugin;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import net.fabricmc.api.ClientModInitializer;

/**
 * Fabric client entrypoint (was {@code @Mod(dist = Dist.CLIENT)}).
 * <p>
 * The project uses a single source set rather than {@code splitEnvironmentSourceSets()}, so this
 * sits next to common code; Fabric only loads it on a client because it is declared under the
 * {@code client} entrypoint in {@code fabric.mod.json}.
 * <p>
 * Order matters in one place: {@code CCPartialModels.register()} has to run before
 * {@code CCBlockEntityRenders.register()}, because the Flywheel visuals resolve partial models at
 * construction.
 */
public class CreateConnectedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CCPartialModels.register();
        CCModels.register();
        CCBlockEntityRenders.register();
        CCBlockEntityBehaviours.register();
        CCDisplaySourceRenders.register();
        CCConnectedTextures.register();
        PonderIndex.addPlugin(new CCPonderPlugin());
    }
}
