package com.hlysine.create_connected.content.overstressclutch;

import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollValueBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Server half of the overstress clutch's uncouple delay. See
 * {@link com.hlysine.create_connected.content.ServerRotationScrollValueBehaviour} for why these are
 * pairs now.
 * <p>
 * The stored value is always in ticks; the row only picks the unit the board shows it in.
 */
public class ServerTimeDelayScrollValueBehaviour extends ServerScrollValueBehaviour {

    public ServerTimeDelayScrollValueBehaviour(SmartBlockEntity be) {
        super(be);
    }

    @Override
    public void onShortInteract(Player player, InteractionHand hand, Direction side, BlockHitResult hitResult) {
        BlockState blockState = blockEntity.getBlockState();
        if (blockState.getBlock() instanceof BrassDiodeBlock bdb)
            bdb.toggle(getLevel(), getPos(), blockState, player, hand);
    }

    @Override
    public void setValueSettings(Player player, ValueSettings valueSetting, boolean ctrlHeld) {
        int value = valueSetting.value();
        int multiplier = switch (valueSetting.row()) {
            case 0 -> 1;
            case 1 -> 20;
            default -> 60 * 20;
        };
        if (!valueSetting.equals(getValueSettings()))
            playFeedbackSound(this);
        setValue(Math.max(1, Math.max(1, value) * multiplier));
    }

    @Override
    public ValueSettings getValueSettings() {
        int row = 0;
        int value = getValue();

        if (value > 60 * 20) {
            value = value / (60 * 20);
            row = 2;
        } else if (value > 60) {
            value = value / 20;
            row = 1;
        }

        return new ValueSettings(row, value);
    }

    /** {@code ClipboardCloneable} rode along with ValueSettingsHandleBehaviour, which is the server half. */
    @Override
    public String getClipboardKey() {
        return "Timings";
    }
}