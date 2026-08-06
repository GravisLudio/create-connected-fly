package com.hlysine.create_connected.mixin.sequencedgearshift;

import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Appends TURN_AWAIT, TURN_TIME and LOOP to Create's sequencer instruction enum.
 * <p>
 * On 1.21.1 the enum carried every property the sequencer GUI needed -- parameter name,
 * background texture, whether it takes a value or a speed, its max/step/default -- so a new
 * constant was fully described by its constructor arguments. Create Fly stripped all of that:
 * {@code SequencerInstructions} is now a bare enum whose only constructor is the implicit
 * {@code (String, int)}, and every one of those properties lives as a private static switch on
 * {@code client.content.kinetics.transmission.sequencer.SequencedGearshiftScreen}. So the
 * constructor invoker here only carries name and ordinal, and the display side of these three
 * instructions has to be taught to that screen instead -- see SequencedGearshiftScreenMixin.
 * <p>
 * That move is also why this file no longer imports AllGuiTextures: it is a client class, and
 * this mixin is in the common config, so naming it here would have failed to load on a
 * dedicated server.
 */
@Mixin(value = SequencerInstructions.class, remap = false)
@Unique
public class SequencerInstructionsMixin {
    /**
     * Internal field that holds all enum values
     */
    @Shadow
    @Final
    @Mutable
    private static SequencerInstructions[] $VALUES;

    @Unique
    private static final SequencerInstructions TURN_AWAIT = create_connected$addMember("TURN_AWAIT");
    @Unique
    private static final SequencerInstructions TURN_TIME = create_connected$addMember("TURN_TIME");
    @Unique
    private static final SequencerInstructions LOOP = create_connected$addMember("LOOP");

    /**
     * Constructor. Create Fly's enum declares no fields, so the only constructor is the one javac
     * generates for every enum: {@code (Ljava/lang/String;I)V}.
     */
    @Invoker("<init>")
    public static SequencerInstructions create_connected$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    @Unique
    private static SequencerInstructions create_connected$addMember(String internalName) {
        assert $VALUES != null;
        ArrayList<SequencerInstructions> instructions = new ArrayList<>(Arrays.asList($VALUES));
        SequencerInstructions instruction = create_connected$invokeInit(internalName, instructions.get(instructions.size() - 1).ordinal() + 1);
        instructions.add(instruction);
        $VALUES = instructions.toArray(new SequencerInstructions[0]);
        return instruction;
    }

    @Inject(method = "needsPropagation()Z", at = @At("HEAD"), cancellable = true)
    private void needsPropagation(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this == TURN_AWAIT) {
            cir.setReturnValue(true);
        } else if ((Object) this == TURN_TIME) {
            cir.setReturnValue(true);
        } else if ((Object) this == LOOP) {
            cir.setReturnValue(false);
        }
    }

    // formatValue used to live here, as SequencerInstructions.formatValue(int). Create Fly moved it
    // to SequencedGearshiftScreen.formatValue(SequencerInstructions, int) along with the rest of the
    // display data, so there is nothing left on this class to inject into. TURN_TIME's "10s"/"5t"
    // rendering has to be reinstated over there; its default branch prints the raw tick count.
}
