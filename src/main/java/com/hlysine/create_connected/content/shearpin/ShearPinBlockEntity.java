package com.hlysine.create_connected.content.shearpin;

import com.hlysine.create_connected.registries.CCBlockEntityTypes;
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

    /** Unused: the type is read from the registry entry below, not carried through the constructor.
     *  The signature stays three-argument because BlockEntityBuilder's factory demands it. */
    public ShearPinBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(pos, state);
    }

    // Create Fly's BracketedKineticBlockEntity hard-codes AllBlockEntityTypes.BRACKETED_KINETIC in
    // its only constructor. BlockEntity reads its private type field from these three methods, so
    // all three redirect -- see LinkedAnalogLeverBlockEntity for the same problem.
    //
    // Do not turn this back into a field assigned in the constructor. BlockEntity's own constructor
    // calls validateBlockState, so isValidBlockState below runs while super() is still executing --
    // before any field declared here has been assigned. A field is still null on that first call and
    // the game crashes the moment the block is placed.

    @Override
    public BlockEntityType<?> getType() {
        return CCBlockEntityTypes.SHEAR_PIN.get();
    }

    @Override
    public Holder<BlockEntityType<?>> typeHolder() {
        return getType().builtInRegistryHolder();
    }

    @Override
    public boolean isValidBlockState(BlockState state) {
        return getType().isValid(state);
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

