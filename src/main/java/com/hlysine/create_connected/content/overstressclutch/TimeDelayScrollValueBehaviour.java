package com.hlysine.create_connected.content.overstressclutch;

import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Client half of the overstress clutch's uncouple delay -- the value box and its board. The value
 * lives in {@link ServerTimeDelayScrollValueBehaviour}.
 * <p>
 * Was a nested class on the block entity; it is a file of its own now because its server half has
 * to be a separate behaviour and keeping the two together read as one thing.
 */
public class TimeDelayScrollValueBehaviour<B extends SmartBlockEntity>
        extends ScrollValueBehaviour<B, ServerTimeDelayScrollValueBehaviour> {

    public TimeDelayScrollValueBehaviour(Component label, B be, ValueBoxTransform slot) {
        super(label, be, slot);
        withFormatter(TimeDelayScrollValueBehaviour::format);
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        return new ValueSettingsBoard(label, 60, 10,
                CreateLang.translatedOptions("generic.unit", "ticks", "seconds", "minutes"),
                new ValueSettingsFormatter(this::formatSettings));
    }

    /** Ticks to a human reading: raw below a second, {@code m:ss} above it. */
    public static String format(int ticks) {
        if (ticks < 20)
            return ticks + "t";
        int seconds = ticks / 20;
        if (seconds < 60)
            return "0:" + (seconds < 10 ? "0" : "") + seconds;
        return (seconds / 60) + ":" + (seconds % 60 < 10 ? "0" : "") + (seconds % 60);
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        int value = Math.max(1, settings.value());
        return Component.literal(switch (settings.row()) {
            case 0 -> value + "t";
            case 1 -> "0:" + (value < 10 ? "0" : "") + value;
            default -> value + ":00";
        });
    }
}
