package com.hlysine.create_connected.content.brassgearbox;

import com.hlysine.create_connected.content.brassgearbox.BrassGearboxRenderer.BrassGearboxRenderState;
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

/** See {@link com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxRenderer} for the shape. */
public class BrassGearboxRenderer implements BlockEntityRenderer<BrassGearboxBlockEntity, BrassGearboxRenderState> {

    public BrassGearboxRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BrassGearboxRenderState createRenderState() {
        return new BrassGearboxRenderState();
    }

    @Override
    public void extractRenderState(
            BrassGearboxBlockEntity be,
            BrassGearboxRenderState state,
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
        Axis boxAxis = blockState.getValue(BlockStateProperties.AXIS);
        float time = AnimationTickHolder.getRenderTime(level);
        int color = KineticBlockEntityRenderer.getTintColor(be);

        for (Direction direction : Iterate.directions) {
            Axis axis = direction.getAxis();
            if (boxAxis == axis)
                continue;

            float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(be, pos, axis);
            float angle = (time * be.getSpeed() * 3f / 10) % 360;

            if (be.getSpeed() != 0 && be.hasSource()) {
                BlockPos source = be.source.subtract(pos);
                Direction sourceFacing = Direction.getApproximateNearest(source.getX(), source.getY(), source.getZ());
                angle *= BrassGearboxBlockEntity.getRotationSpeedModifier(direction, sourceFacing, blockState);
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
            BrassGearboxRenderState state,
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

    public static class BrassGearboxRenderState extends BlockEntityRenderState {
        /** Indexed by {@link Direction#get3DDataValue()}; the box's own axis stays null. */
        public final @Nullable SuperByteBufferRenderState[] shafts = new SuperByteBufferRenderState[6];
        public boolean support;
    }
}
