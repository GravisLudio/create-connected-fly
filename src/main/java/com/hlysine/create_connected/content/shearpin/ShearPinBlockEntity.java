package com.hlysine.create_connected.content.shearpin;

import com.hlysine.create_connected.registries.CCBlocks;
import com.hlysine.create_connected.datagen.advancements.AdvancementBehaviour;
import com.hlysine.create_connected.datagen.advancements.CCAdvancements;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.TickPriority;

import java.util.List;

public class ShearPinBlockEntity extends BracketedKineticBlockEntity {

    static final int RANDOM_DELAY = 5;

    private final BlockEntityType<?> type;

    public ShearPinBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(pos, state);
        this.type = type;
    }

    // Create Fly's BracketedKineticBlockEntity hard-codes AllBlockEntityTypes.BRACKETED_KINETIC in
    // its only constructor. BlockEntity reads its private type field from these three methods, so
    // all three redirect -- see LinkedAnalogLeverBlockEntity for the same problem.

    @Override
    public BlockEntityType<?> getType() {
        return type;
    }

    @Override
    public Holder<BlockEntityType<?>> typeHolder() {
        return type.builtInRegistryHolder();
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return type.isValid(state);
    }

    @Override
    public void initialize() {
        onKineticUpdate();
        super.initialize();
    }

    private void onKineticUpdate() {
        if (IRotate.StressImpact.isEnabled()) {
            if (isOverStressed()) {
                if (level != null) {
                    level.scheduleTick(getBlockPos(), CCBlocks.SHEAR_PIN.get(), level.getRandom().nextInt(RANDOM_DELAY), TickPriority.EXTREMELY_HIGH);
                }
            }
        }
    }

    @Override
    public void updateFromNetwork(float maxStress, float currentStress, int networkSize) {
        super.updateFromNetwork(maxStress, currentStress, networkSize);
        onKineticUpdate();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        AdvancementBehaviour.registerAwardables(this, behaviours, CCAdvancements.SHEAR_PIN);
    }
}

