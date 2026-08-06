package com.hlysine.create_connected.content.overstressclutch;

import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import com.hlysine.create_connected.registries.CCBlocks;
import com.hlysine.create_connected.ConnectedLang;
import com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock.ClutchState;
import com.hlysine.create_connected.datagen.advancements.AdvancementBehaviour;
import com.hlysine.create_connected.datagen.advancements.CCAdvancements;
import com.zurrtum.create.content.kinetics.RotationPropagator;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.zurrtum.create.content.redstone.diodes.BrassDiodeBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.catnip.lang.FontHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.ticks.TickPriority;

import java.util.List;

import static com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock.POWERED;
import static com.hlysine.create_connected.content.overstressclutch.OverstressClutchBlock.STATE;
import static net.minecraft.ChatFormatting.GOLD;

public class OverstressClutchBlockEntity extends SplitShaftBlockEntity {

    public int delay;
    public ServerTimeDelayScrollValueBehaviour maxDelay;

    public OverstressClutchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        AdvancementBehaviour.registerAwardables(this, behaviours, CCAdvancements.OVERSTRESS_CLUTCH);
        // Only the server half is a block entity behaviour now; the value box is registered
        // client-side in CCBlockEntityBehaviours.
        maxDelay = new ServerTimeDelayScrollValueBehaviour(this);
        maxDelay.between(1, 60 * 20 * 60);
        maxDelay.withCallback(this::onMaxDelayChanged);
        maxDelay.setValue(1);
        behaviours.add(maxDelay);
    }

    private void onMaxDelayChanged(int newMax) {
        delay = Mth.clamp(delay, 0, newMax);
        sendData();
    }

    private String format(int value) {
        if (value < 60)
            return value + "t";
        if (value < 20 * 60)
            return (value / 20) + "s";
        return (value / 20 / 60) + "m";
    }

    public boolean isIdle() {
        return delay == 0;
    }

    @Override
    public void initialize() {
        onKineticUpdate();
        super.initialize();
    }

    public void onKineticUpdate() {
        if (getBlockState().getValue(STATE) == ClutchState.UNCOUPLED && getBlockState().getValue(POWERED)) {
            resetClutch();
            return;
        }
        if (IRotate.StressImpact.isEnabled() && !getBlockState().getValue(POWERED)) {
            if (isOverStressed() && getBlockState().getValue(STATE) == ClutchState.COUPLED) {
                if (level != null) {
                    level.setBlock(getBlockPos(), getBlockState().setValue(STATE, ClutchState.UNCOUPLING), 2 | 16);
                    delay = maxDelay.getValue() - 1;
                    sendData();
                    return;
                }
            }
        }
        if (!isOverStressed() && getBlockState().getValue(STATE) == ClutchState.UNCOUPLING) {
            if (level != null) {
                level.setBlock(getBlockPos(), getBlockState().setValue(STATE, ClutchState.COUPLED), 2 | 16);
            }
        }
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        onKineticUpdate();
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (hasSource()) {
            if (face != getSourceFacing() && getBlockState().getValue(STATE) == ClutchState.UNCOUPLED)
                return 0;
        }
        return 1;
    }

    // addToTooltip moved to a client TooltipBehaviour -- see OverstressClutchTooltipBehaviour.

    public void resetClutch() {
        if (getBlockState().getValue(STATE) == ClutchState.UNCOUPLED && !isOverStressed()) {
            assert level != null;
            level.setBlock(getBlockPos(), getBlockState().setValue(STATE, ClutchState.COUPLED), 3);
            RotationPropagator.handleRemoved(level, getBlockPos(), this);
            RotationPropagator.handleAdded(level, getBlockPos(), this);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (getBlockState().getValue(STATE) == ClutchState.UNCOUPLING && level != null && !level.isClientSide()) {
            level.scheduleTick(getBlockPos(), CCBlocks.OVERSTRESS_CLUTCH.get(), 0, TickPriority.EXTREMELY_HIGH);
        }
    }

    @Override
    protected void read(ValueInput compound, boolean clientPacket) {
        delay = compound.getIntOr("Delay", 0);
        super.read(compound, clientPacket);
    }

    @Override
    protected void write(ValueOutput compound, boolean clientPacket) {
        compound.putInt("Delay", delay);
        super.write(compound, clientPacket);
    }

}
