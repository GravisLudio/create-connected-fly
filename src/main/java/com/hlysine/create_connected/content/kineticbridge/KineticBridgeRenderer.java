package com.hlysine.create_connected.content.kineticbridge;

import com.hlysine.create_connected.content.kineticbridge.KineticBridgeRenderer.KineticBridgeRenderState;
import com.hlysine.create_connected.registries.CCPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
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

/**
 * See {@link com.hlysine.create_connected.content.sixwaygearbox.SixWayGearboxRenderer} for the
 * extract/submit shape.
 * <p>
 * This one lights its two pieces from different positions -- the shaft from the block behind, the
 * fan from the block in front -- so it cannot use the light the base extraction stored.
 * {@code LevelRenderer.getLightColor} is reached through Create Fly's
 * {@code SmartBlockEntityRenderer.getLightCoords} now.
 */
public class KineticBridgeRenderer implements BlockEntityRenderer<KineticBlockEntity, KineticBridgeRenderState> {

    private final boolean isDestination;

    private KineticBridgeRenderer(BlockEntityRendererProvider.Context context, boolean isDestination) {
        this.isDestination = isDestination;
    }

    public static KineticBridgeRenderer source(BlockEntityRendererProvider.Context ctx) {
        return new KineticBridgeRenderer(ctx, false);
    }

    public static KineticBridgeRenderer destination(BlockEntityRendererProvider.Context ctx) {
        return new KineticBridgeRenderer(ctx, true);
    }

    @Override
    public KineticBridgeRenderState createRenderState() {
        return new KineticBridgeRenderState();
    }

    @Override
    public void extractRenderState(
            KineticBlockEntity be,
            KineticBridgeRenderState state,
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
        Direction facing = blockState.getValue(KineticBridgeBlock.FACING);
        Direction modelFacing = isDestination ? facing : facing.getOpposite();

        int lightBehind = SmartBlockEntityRenderer.getLightCoords(level, pos.relative(facing.getOpposite()));
        int lightInFront = SmartBlockEntityRenderer.getLightCoords(level, pos.relative(facing));

        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockState);
        float angle = KineticBlockEntityRenderer.getAngleForBe(be, pos, axis);
        int color = KineticBlockEntityRenderer.getTintColor(be);

        state.shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, blockState, modelFacing)
                .cardinalLighting(cardinalLighting)
                .rotateCentered(angle, axis.getPositive())
                .light(lightBehind)
                .color(color)
                .extractRenderState();

        state.fan = CachedBuffers.partialFacing(
                        isDestination ? CCPartialModels.KINETIC_BRIDGE_DESTINATION : CCPartialModels.KINETIC_BRIDGE_SOURCE,
                        blockState,
                        modelFacing)
                .cardinalLighting(cardinalLighting)
                .rotateCentered(angle, axis.getPositive())
                .light(lightInFront)
                .color(color)
                .extractRenderState();
    }

    @Override
    public void submit(
            KineticBridgeRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        if (state.support)
            return;

        // Upstream drew these into RenderType.cutoutMipped(), which was a chunk layer and does not
        // exist for block entities in 26.2 -- those became ChunkSectionLayer. The typeless submit
        // takes the buffer's own material, which is how Create Fly submits all of its own.
        if (state.shaft != null)
            state.shaft.submit(matrices, queue);
        if (state.fan != null)
            state.fan.submit(matrices, queue);
    }

    public static class KineticBridgeRenderState extends BlockEntityRenderState {
        public @Nullable SuperByteBufferRenderState shaft;
        public @Nullable SuperByteBufferRenderState fan;
        public boolean support;
    }
}
