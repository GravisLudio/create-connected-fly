package com.hlysine.create_connected.content.chaincogwheel;

import com.hlysine.create_connected.content.chaincogwheel.ChainCogwheelRenderer.ChainCogwheelRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRotateAngleWithoutBeOffset;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRotationAxisOf;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;

/**
 * The encased chain cogwheel's own block entity renderer, for when Flywheel is off.
 *
 * <p>Upstream registered Create's {@code EncasedCogRenderer::small}. Create Fly split that class in
 * two, and the replacement — {@code EncasedSmallCogRenderer} — is written against
 * {@code EncasedCogwheelBlock} and opens by reading its {@code TOP_SHAFT} and {@code BOTTOM_SHAFT}
 * properties. {@code ChainCogwheelBlock} extends {@code ChainDriveBlock} and has neither, so the
 * renderer threw {@code IllegalArgumentException} the first time the block came into view.
 *
 * <p>Only the shaftless branch of that renderer ever applied here: with no shaft properties the
 * original would always have fallen through to {@code SHAFTLESS_COGWHEEL} and drawn no shaft caps.
 * This is that branch, with the query removed. It matches what {@code EncasedCogVisual.small} draws
 * on the Flywheel path, which is why the block looked right until something turned Flywheel off.
 *
 * <p>{@code AXIS} is read through {@code getRotationAxisOf}, which goes via {@code IRotate} rather
 * than naming a property — that one is safe on any kinetic block.
 */
public class ChainCogwheelRenderer implements BlockEntityRenderer<SimpleKineticBlockEntity, ChainCogwheelRenderState> {

    public ChainCogwheelRenderer(Context context) {
    }

    @Override
    public ChainCogwheelRenderState createRenderState() {
        return new ChainCogwheelRenderState();
    }

    @Override
    public void extractRenderState(
            SimpleKineticBlockEntity be,
            ChainCogwheelRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            @Nullable CrumblingOverlay breakProgress
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        Axis axis = getRotationAxisOf(state.blockState);
        Direction direction = axis.getPositive();

        state.angle = getRotateAngleWithoutBeOffset(axis, direction, be, state, level);
        state.model = CachedBuffers.partialFacingVertical(
                        AllPartialModels.SHAFTLESS_COGWHEEL,
                        state.blockState,
                        direction)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
    }

    @Override
    public void submit(
            ChainCogwheelRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState camera
    ) {
        if (state.angle != null) {
            matrices.rotateAround(state.angle, 0.5f, 0.5f, 0.5f);
        }
        state.model.submit(matrices, queue);
    }

    public static class ChainCogwheelRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public @Nullable Quaternionf angle;
    }
}
