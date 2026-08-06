package com.hlysine.create_connected.content;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.world.entity.player.Player;

/**
 * Server half of the centrifugal clutch's speed threshold.
 * <p>
 * Create Fly split {@code ScrollValueBehaviour} in two: the value and its settings live on the
 * server, the value box and its board live on the client. Upstream's single class overrode both
 * halves, so it becomes a pair -- this one and {@link RotationScrollValueBehaviour}.
 * <p>
 * The sign of the stored value carries the comparison direction: negative means "at most", positive
 * means "at least", which is why row 0 stores a negated value.
 */
public class ServerRotationScrollValueBehaviour extends ServerScrollValueBehaviour {

    public ServerRotationScrollValueBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = Math.max(1, valueSetting.value());
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(valueSetting.row() == 0 ? -value : value);
    }

    @Override
    public ValueSettings getValueSettings() {
        return new ValueSettings(getValue() < 0 ? 0 : 1, Math.abs(getValue()));
    }

    /**
     * Upstream assigned the {@code value} field directly to seed a default. It is protected on the
     * parent, and {@code setValue} would clamp against the range and fire the callback before the
     * block entity is on a level, so the default is written straight through here instead.
     */
    public ServerRotationScrollValueBehaviour startingValue(int value) {
        this.value = value;
        return this;
    }

    /** {@code ClipboardCloneable} rode along with ValueSettingsHandleBehaviour, which is the server half. */
    @Override
    public String getClipboardKey() {
        return "Speed";
    }
}