package com.hlysine.create_connected.content.kineticbattery;

import com.hlysine.create_connected.content.kineticbattery.KineticBatteryRenderer.KineticBatteryRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.IRotate;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** See {@link com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxRenderer} for the shape. */
public class KineticBatteryRenderer implements BlockEntityRenderer<KineticBatteryBlockEntity, KineticBatteryRenderState> {

    public KineticBatteryRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public KineticBatteryRenderState createRenderState() {
        return new KineticBatteryRenderState();
    }

    @Override
    public void extractRenderState(
            KineticBatteryBlockEntity be,
            KineticBatteryRenderState state,
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
        Axis boxAxis = ((IRotate) blockState.getBlock()).getRotationAxis(blockState);
        float time = AnimationTickHolder.getRenderTime(level);
        int color = KineticBlockEntityRenderer.getTintColor(be);

        for (Direction direction : Iterate.directions) {
            Axis axis = direction.getAxis();
            if (boxAxis != axis)
                continue;

            float offset = KineticBlockEntityRenderer.getRotationOffsetForPosition(be, pos, axis);
            float angle = (time * be.getSpeed() * 3f / 10) % 360;

            angle *= be.getRotationSpeedModifier(direction);
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
            KineticBatteryRenderState state,
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

    public static class KineticBatteryRenderState extends BlockEntityRenderState {
        /** Indexed by {@link Direction#get3DDataValue()}; only the block's own axis is filled. */
        public final @Nullable SuperByteBufferRenderState[] shafts = new SuperByteBufferRenderState[6];
        public boolean support;
    }
}
