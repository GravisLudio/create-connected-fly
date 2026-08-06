package com.hlysine.create_connected.content.sixwaygearbox;

import com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxRenderer.SixWayGearboxRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 26.2 splits block entity rendering in two: {@code extractRenderState} reads the block entity into
 * a state object, and {@code submit} queues draw calls without touching it. What used to be locals
 * inside {@code renderSafe} are fields on {@link SixWayGearboxRenderState} now.
 * <p>
 * The rotation is baked into the buffer during extraction via {@code rotateCentered}, the way
 * Create Fly's own {@code KineticBlockEntityRenderer} does it, so submit has nothing left to do but
 * hand each shaft to the queue.
 */
public class SixWayGearboxRenderer implements BlockEntityRenderer<SixWayGearboxBlockEntity, SixWayGearboxRenderState> {

    public SixWayGearboxRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SixWayGearboxRenderState createRenderState() {
        return new SixWayGearboxRenderState();
    }

    @Override
    public void extractRenderState(
            SixWayGearboxBlockEntity be,
            SixWayGearboxRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        state.support = VisualizationManager.supportsVisualization(level);
        if (state.support)
            return;

        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        BlockState blockState = state.blockState;
        BlockPos pos = state.blockPos;
        float time = AnimationTickHolder.getRenderTime(level);
        int color = KineticBlockEntityRenderer.getTintColor(be);

        for (Direction direction : Iterate.directions) {
            Axis axis = direction.getAxis();

            float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(be, pos, axis);
            float angle = (time * be.getSpeed() * 3f / 10) % 360;

            if (be.getSpeed() != 0 && be.hasSource()) {
                BlockPos source = be.source.subtract(pos);
                Direction sourceFacing = Direction.getApproximateNearest(source.getX(), source.getY(), source.getZ());
                angle *= SixWayGearboxBlockEntity.getRotationSpeedModifier(blockState, direction, sourceFacing);
            }

            angle += offset;
            angle = angle / 180f * (float) Math.PI;

            state.shafts[direction.get3DDataValue()] =
                    CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, direction)
                            .cardinalLighting(cardinalLighting)
                            .rotateCentered(angle, axis.getPositive())
                            .light(state.lightCoords)
                            .color(color)
                            .extractRenderState();
        }
    }

    @Override
    public void submit(
            SixWayGearboxRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        if (state.support)
            return;

        for (SuperByteBufferRenderState shaft : state.shafts) {
            if (shaft != null)
                shaft.submit(matrices, queue);
        }
    }

    public static class SixWayGearboxRenderState extends BlockEntityRenderState {
        /** Indexed by {@link Direction#get3DDataValue()}. */
        public final @Nullable SuperByteBufferRenderState[] shafts = new SuperByteBufferRenderState[6];
        public boolean support;
    }
}
