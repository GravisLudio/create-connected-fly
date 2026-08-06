package com.hlysine.create_connected.mixin.linkedtransmitter;

import com.hlysine.create_connected.registries.CCBlocks;
import com.hlysine.create_connected.registries.CCItems;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnalogLeverBlock.class)
public abstract class AnalogLeverBlockMixin extends FaceAttachedHorizontalDirectionalBlock {

    protected AnalogLeverBlockMixin(Properties properties) {
        super(properties);
    }

    /**
     * Create Fly moved the lever's right-click handling out of {@code useWithoutItem} and into
     * vanilla's {@code useItemOn(stack, state, level, pos, player, hand, hit)}, so that is the
     * target now. Returning PASS from here means vanilla goes straight on to the held item's
     * {@code useOn}, which is the same fall-through the old PASS from {@code useWithoutItem}
     * produced -- Create Fly's AnalogLeverBlock has no {@code useWithoutItem} left to skip.
     * <p>
     * The guard stays on {@code player.isHolding} rather than on the incoming {@code stack}: a
     * transmitter in the off hand has to stop the *main* hand click from toggling the lever,
     * otherwise the lever eats the interaction before the off hand ever gets a turn.
     */
    @Inject(
            cancellable = true,
            at = @At("HEAD"),
            method = "useItemOn"
    )
    private void passUseToTransmitter(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.isHolding(CCItems.LINKED_TRANSMITTER.get()) && !state.is(CCBlocks.LINKED_ANALOG_LEVER.get())) {
            cir.setReturnValue(InteractionResult.PASS);
            cir.cancel();
        }
    }

    /**
     * {@code onRemove} became {@code affectNeighborsAfterRemoval(state, ServerLevel, pos, isMoving)},
     * and it runs *after* the swap, so the replacement state is no longer a parameter -- it has to be
     * read back off the level. {@code state} is still the block that was removed.
     * <p>
     * This matters more than it did before: Create Fly's body calls {@code removeBlockEntity(pos)}
     * unconditionally, and by the time it runs the block entity at that position already belongs to
     * the *new* block. Without this cancel, attaching or wrenching off a transmitter would destroy
     * the freshly created lever block entity and reset the lever's signal to zero.
     */
    @Inject(
            cancellable = true,
            at = @At("HEAD"),
            method = "affectNeighborsAfterRemoval"
    )
    private void keepBlockEntityOnLeverSwap(BlockState state, ServerLevel worldIn, BlockPos pos, boolean isMoving, CallbackInfo ci) {
        if (state.getBlock() instanceof AnalogLeverBlock && worldIn.getBlockState(pos).getBlock() instanceof AnalogLeverBlock)
            ci.cancel();
    }
}
