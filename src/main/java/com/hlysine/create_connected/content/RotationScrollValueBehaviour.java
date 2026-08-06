package com.hlysine.create_connected.content;

import com.google.common.collect.ImmutableList;
import com.hlysine.create_connected.ConnectedLang;
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
 * Client half of the centrifugal clutch's speed threshold -- the value box and its board. The value
 * itself lives in {@link ServerRotationScrollValueBehaviour}; see there for why this is a pair.
 */
public class RotationScrollValueBehaviour<B extends SmartBlockEntity>
        extends ScrollValueBehaviour<B, ServerRotationScrollValueBehaviour> {

    public RotationScrollValueBehaviour(Component label, B be, ValueBoxTransform slot) {
        super(label, be, slot);
        withFormatter(v -> String.valueOf(Math.abs(v)));
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        ImmutableList<Component> rows = ImmutableList.of(
                ConnectedLang.translateDirect("centrifugal_clutch.max_speed"),
                ConnectedLang.translateDirect("centrifugal_clutch.min_speed")
        );
        ValueSettingsFormatter formatter = new ValueSettingsFormatter(this::formatSettings);
        return new ValueSettingsBoard(label, 256, 32, rows, formatter);
    }

    public MutableComponent formatSettings(ValueSettings settings) {
        return CreateLang.text(settings.row() == 0 ? "≤" : "≥")
                .add(CreateLang.number(Math.max(1, Math.abs(settings.value()))))
                .component();
    }
}
