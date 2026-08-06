package com.hlysine.create_connected.datagen.advancements;

import com.hlysine.create_connected.CreateConnected;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * Upstream copied Create's trigger base, which kept its own
 * {@code Map<PlayerAdvancements, Set<Listener>>} and implemented {@code addPlayerListener} /
 * {@code removePlayerListener} by hand. 26.2 moved all of that bookkeeping into
 * {@code PlayerAdvancements}: {@code CriterionTrigger} is now just a codec, and
 * {@link SimpleCriterionTrigger#trigger(ServerPlayer, java.util.function.Predicate)} is the whole
 * firing API. The listener machinery is therefore gone rather than ported -- it would be a second,
 * unused copy of what vanilla already does.
 */
public abstract class CriterionTriggerBase<T extends CriterionTriggerBase.Instance> extends SimpleCriterionTrigger<T> {

    private final Identifier id;

    public CriterionTriggerBase(String id) {
        this.id = CreateConnected.asResource(id);
    }

    public Identifier getId() {
        return id;
    }

    /**
     * Deliberately not called {@code trigger}: vanilla's own
     * {@code trigger(ServerPlayer, Predicate)} is inherited, and a null second argument would match
     * both overloads.
     */
    protected void triggerWith(ServerPlayer player, @Nullable List<Supplier<Object>> suppliers) {
        super.trigger(player, instance -> instance.test(suppliers));
    }

    public abstract static class Instance implements SimpleCriterionTrigger.SimpleInstance {
        protected abstract boolean test(@Nullable List<Supplier<Object>> suppliers);
    }
}
