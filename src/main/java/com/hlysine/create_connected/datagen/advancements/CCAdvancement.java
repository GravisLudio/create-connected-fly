package com.hlysine.create_connected.datagen.advancements;

import com.hlysine.create_connected.CreateConnected;
import com.hlysine.create_connected.foundation.registrate.ItemProvider;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Despite living under {@code datagen/}, this is runtime code: {@link AdvancementBehaviour} and the
 * brake, kinetic battery and overstress clutch award through {@link #awardTo}. The datagen half --
 * the {@code Advancement.Builder}, {@code save} and {@code provideLang} -- is gone, because the
 * advancement JSONs it produced are committed under {@code src/generated/resources} and there is no
 * Registrate left to regenerate them.
 * <p>
 * <b>What is still load-bearing is the builtin trigger.</b> Advancements that have no external
 * trigger get one registered as {@code create_connected:<id>_builtin}, and the committed JSON names
 * exactly that id as its criterion. Drop the registration and the advancement can never fire, with
 * nothing failing at build time. The builder methods that only fed the JSON are accepted and
 * ignored; {@code externalTrigger} is kept because it decides whether a builtin trigger exists at
 * all.
 */
public class CCAdvancement implements Awardable {

    private final String id;
    private SimpleCCTrigger builtinTrigger;
    private final Builder ccBuilder = new Builder();

    public CCAdvancement(String id, UnaryOperator<Builder> b) {
        this.id = id;

        b.apply(ccBuilder);

        if (!ccBuilder.externalTrigger) {
            builtinTrigger = CCTriggers.addSimple(id + "_builtin");
        }

        CCAdvancements.ENTRIES.add(this);
    }

    public boolean isAlreadyAwardedTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return true;
        // ServerPlayer.getServer() is gone; the server is reached through the level now.
        AdvancementHolder advancement = sp.level()
                .getServer()
                .getAdvancements()
                .get(CreateConnected.asResource(id));
        if (advancement == null)
            return true;
        return sp.getAdvancements()
                .getOrStartProgress(advancement)
                .isDone();
    }

    public void awardTo(Player player) {
        if (!(player instanceof ServerPlayer sp))
            return;
        if (builtinTrigger == null)
            throw new UnsupportedOperationException(
                    "Advancement " + id + " uses external Triggers, it cannot be awarded directly");
        builtinTrigger.trigger(sp);
    }

    enum TaskType {
        SILENT, NORMAL, NOISY, EXPERT, SECRET
    }

    public class Builder {

        private boolean externalTrigger;

        // --- accepted and ignored: these only shaped the generated JSON ---

        CCAdvancement.Builder special(CCAdvancement.TaskType type) {
            return this;
        }

        CCAdvancement.Builder after(CCAdvancement other) {
            return this;
        }

        CCAdvancement.Builder icon(ItemProvider item) {
            return this;
        }

        CCAdvancement.Builder icon(ItemLike item) {
            return this;
        }

        CCAdvancement.Builder icon(ItemStack stack) {
            return this;
        }

        CCAdvancement.Builder icon(Function<HolderLookup.Provider, ItemStack> func) {
            return this;
        }

        CCAdvancement.Builder title(String title) {
            return this;
        }

        CCAdvancement.Builder description(String description) {
            return this;
        }

        // --- these decide whether a builtin trigger is created, so they are real ---

        CCAdvancement.Builder whenBlockPlaced(Block block) {
            return externalTrigger();
        }

        CCAdvancement.Builder whenIconCollected() {
            return externalTrigger();
        }

        CCAdvancement.Builder whenItemCollected(ItemProvider item) {
            return externalTrigger();
        }

        CCAdvancement.Builder whenItemCollected(ItemLike itemProvider) {
            return externalTrigger();
        }

        CCAdvancement.Builder whenItemCollected(TagKey<Item> tag) {
            return externalTrigger();
        }

        CCAdvancement.Builder awardedForFree() {
            return externalTrigger();
        }

        CCAdvancement.Builder externalTrigger() {
            externalTrigger = true;
            return this;
        }
    }
}
