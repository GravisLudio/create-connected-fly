package com.hlysine.create_connected.content.crankwheel;

import com.hlysine.create_connected.content.crankwheel.CrankWheelRenderer.CrankWheelRenderState;
import com.hlysine.create_connected.registries.CCPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.crank.HandCrankRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.kinetics.simpleRelays.ICogWheel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;

/**
 * The crank wheel's own block entity renderer.
 *
 * <p>Upstream registered Create's {@code HandCrankRenderer} here, which worked because on 1.21.1 it
 * asked the block entity which model to draw. Create Fly's version hard-codes
 * {@code AllPartialModels.HAND_CRANK_BASE} and {@code HAND_CRANK_HANDLE}, so reusing it would draw
 * Create's hand crank on top of a Create: Connected block. This is the same class with the two
 * partial models swapped for the mod's own, chosen by wheel size.
 *
 * <p>This is the no-Flywheel path. {@link CrankWheelVisual} draws the same two models when Flywheel
 * is on, and the blockstate points at a particle-only model so that nothing draws the wheel twice --
 * exactly how Create Fly wires {@code hand_crank}. See PORTING.md, <i>Blocks drawn by a renderer
 * need a particle-only blockstate model</i>.
 */
public class CrankWheelRenderer implements BlockEntityRenderer<CrankWheelBlockEntity, CrankWheelRenderState> {
    public CrankWheelRenderer(Context context) {
    }

    @Override
    public CrankWheelRenderState createRenderState() {
        return new CrankWheelRenderState();
    }

    @Override
    public void extractRenderState(
            CrankWheelBlockEntity be,
            CrankWheelRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Direction facing = state.blockState.getValue(BlockStateProperties.FACING);
        Axis axis = facing.getAxis();
        Direction direction = axis.getPositive();
        int color = getTintColor(be);

        boolean large = ICogWheel.isLargeCog(state.blockState);
        PartialModel base = large ? CCPartialModels.LARGE_CRANK_WHEEL_BASE : CCPartialModels.CRANK_WHEEL_BASE;
        PartialModel handle = large ? CCPartialModels.LARGE_CRANK_WHEEL_HANDLE : CCPartialModels.CRANK_WHEEL_HANDLE;

        state.angle = KineticBlockEntityRenderer.getRotateAngleWithoutBeOffset(axis, direction, be, state, level);
        state.model = CachedBuffers.partialFacingVertical(base, state.blockState, facing)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
        state.handleAngle = getRotateAngle(HandCrankRenderer.getHandCrankIndependentAngle(be, tickProgress), direction);
        state.handle = CachedBuffers.partialFacing(handle, state.blockState, facing.getOpposite())
                .cardinalLighting(cardinalLighting).light(state.lightCoords).color(color).extractRenderState();
    }

    @Override
    public void submit(
            CrankWheelRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        if (state.angle != null) {
            matrices.pushPose();
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
            state.model.submit(matrices, queue);
            matrices.popPose();
        } else {
            state.model.submit(matrices, queue);
        }
        if (state.handleAngle != null) {
            matrices.rotateAround(state.handleAngle, 0.5f, 0.5f, 0.5f);
        }
        state.handle.submit(matrices, queue);
    }

    public static class CrankWheelRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @UnknownNullability SuperByteBufferRenderState handle;
        public @Nullable Quaternionf angle;
        public @Nullable Quaternionf handleAngle;
    }
}
