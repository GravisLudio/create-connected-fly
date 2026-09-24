package com.hlysine.create_connected.compat;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.config.FeatureToggle;
import com.hlysine.create_connected.registries.CCCreativeTabs;
import com.hlysine.create_connected.foundation.registrate.ItemProvider;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

/**
 * Hides items belonging to disabled features from JEI's list.
 * <p>
 * The creative tab already filters on {@link FeatureToggle#isEnabled}, so a feature that is off
 * before the game starts never reaches JEI either. This exists for the other case: toggling one
 * while the game is running rebuilds the creative tabs, and JEI keeps its own copy of the list,
 * which would go stale. {@link FeatureToggle#refreshItemVisibility()} calls back here.
 * <p>
 * Registered through the {@code jei_mod_plugin} entrypoint in {@code fabric.mod.json}, which is how
 * JEI finds plugins on Fabric -- its {@code FabricPluginFinder} reads that key, and the
 * {@code @JeiPlugin} annotation upstream relies on is only scanned on Forge. Nothing loads this
 * class when JEI is absent: the entrypoint is only requested by JEI itself, and the call from
 * FeatureToggle goes through {@code Mods.JEI.executeIfInstalled}, whose supplier defers the
 * reference.
 * <p>
 * Upstream also posted a {@code FeatureRefreshEvent} on NeoForge's bus around the refresh, so
 * modpacks could hook stage-based progression into it. Fabric has no such bus and the event class
 * stays out of the build; if that hook is ever wanted here, it needs a Fabric {@code Event} and a
 * documented entrypoint, not a translation of the old one.
 */
public class CreateConnectedJEI implements IModPlugin {
    private static final Identifier ID = CreateConnected.asResource("jei_plugin");

    public static IIngredientManager MANAGER;

    @Override
    @NotNull
    public Identifier getPluginUid() {
        return ID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        MANAGER = jeiRuntime.getIngredientManager();
    }

    public static void refreshItemList() {
        if (MANAGER == null || Minecraft.getInstance().level == null)
            return;

        MANAGER.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                CCCreativeTabs.ITEMS.stream()
                        .map(ItemProvider::asStack)
                        .collect(Collectors.toList())
        );
        MANAGER.addIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                CCCreativeTabs.ITEMS.stream()
                        .filter(x -> FeatureToggle.isEnabled(x.getId()))
                        .map(ItemProvider::asStack)
                        .collect(Collectors.toList())
        );
    }
}
