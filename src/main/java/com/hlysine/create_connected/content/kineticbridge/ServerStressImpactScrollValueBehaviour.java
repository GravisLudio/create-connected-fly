package com.hlysine.create_connected.content.kineticbridge;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Server half of the kinetic bridge's stress multiplier. See
 * {@link com.hlysine.create_connected.content.ServerRotationScrollValueBehaviour} for why these are
 * pairs now.
 */
public class ServerStressImpactScrollValueBehaviour extends ServerScrollValueBehaviour {

    public ServerStressImpactScrollValueBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(0, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(Mth.abs(value));
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(0, Math.abs(getValue()));
    }

    /**
     * Upstream assigned the {@code value} field directly to seed a default. It is protected on the
     * parent, and {@code setValue} would clamp against the range and fire the callback before the
     * block entity is on a level, so the default is written straight through here instead.
     */
    public ServerStressImpactScrollValueBehaviour startingValue(int value) {
        this.value = value;
        return this;
    }

    /** {@code ClipboardCloneable} rode along with ValueSettingsHandleBehaviour, which is the server half. */
    @Override
    public String getClipboardKey() {
        return "Stress Impact";
    }
}