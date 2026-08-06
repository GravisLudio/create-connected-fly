package com.hlysine.create_connected.content.fluidvessel;

import com.hlysine.create_connected.content.fluidvessel.FluidVesselRenderer.FluidVesselRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper;
import com.zurrtum.create.client.catnip.render.FluidRenderHelper.FluidRenderState;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelled on Create Fly's {@code FluidTankRenderer}, which is the same renderer for the vertical
 * tank. The vessel differs in being axis-aware: it can lie along X or Z, so the fluid box takes its
 * length from the axis and only the two gauges facing along it are drawn.
 * <p>
 * NeoForge's fluid renderer is gone with the rest of the platform layer;
 * {@code FluidRenderHelper.extractFluidRenderState} builds the box during extraction now, and
 * submit only replays it.
 * <p>
 * <b>Known gap:</b> upstream flipped the puddle to the top of the tank for gases via
 * {@code getFluidType().isLighterThanAir()}. Create Fly has no fluid-type API and leaves the same
 * branch stubbed to false with a TODO, so gases pool at the bottom here too.
 */
public class FluidVesselRenderer implements BlockEntityRenderer<FluidVesselBlockEntity, FluidVesselRenderState> {

    private final FluidStateModelSet fluidStateModelSet;

    public FluidVesselRenderer(BlockEntityRendererProvider.Context context) {
        this.fluidStateModelSet = context.blockModelResolver().modelManager.getFluidStateModelSet();
    }

    @Override
    public FluidVesselRenderState createRenderState() {
        return new FluidVesselRenderState();
    }

    @Override
    public boolean shouldRender(FluidVesselBlockEntity be, Vec3 cameraPosition) {
        return be.isController() && BlockEntityRenderer.super.shouldRender(be, cameraPosition);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(
            FluidVesselBlockEntity be,
            FluidVesselRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        if (!be.isController())
            return;

        if (be.hasWindow()) {
            extractFluid(be, state, tickProgress, crumblingOverlay);
        } else if (be.boiler.isActive()) {
            extractBoiler(be, state, tickProgress, crumblingOverlay);
        }
    }

    private void extractFluid(
            FluidVesselBlockEntity be,
            FluidVesselRenderState state,
            float tickProgress,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        LerpedFloat fluidLevel = be.getFluidLevel();
        if (fluidLevel == null)
            return;

        float capSize = 1 / 4f;
        float tankHullSize = 1 / 16f + 1 / 128f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = be.getWidth() - 2 * tankHullSize - minPuddleHeight;

        float level = fluidLevel.getValue(tickProgress);
        if (level < 1 / (512f * totalHeight))
            return;

        FluidStack fluidStack = be.getTankInventory().getFluid();
        if (fluidStack.isEmpty())
            return;

        Level world = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);
        state.fluidTranslateY = clampedLevel - totalHeight;

        Axis axis = be.getAxis();
        float xMin = axis == Axis.X ? capSize : tankHullSize;
        float xMax = axis == Axis.X ? xMin + be.getHeight() - 2 * capSize : xMin + be.getWidth() - 2 * tankHullSize;
        float yMin = totalHeight + tankHullSize + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;
        float zMin = axis == Axis.Z ? capSize : tankHullSize;
        float zMax = axis == Axis.Z ? zMin + be.getHeight() - 2 * capSize : zMin + be.getWidth() - 2 * tankHullSize;

        state.fluid = FluidRenderHelper.extractFluidRenderState(
                world instanceof BlockAndTintGetter getter ? getter : null,
                state.blockPos,
                fluidStateModelSet,
                fluidStack.getFluid(),
                fluidStack.getComponentChanges(),
                xMin, yMin, zMin,
                xMax, yMax, zMax,
                state.lightCoords,
                false,
                true
        );
    }

    private void extractBoiler(
            FluidVesselBlockEntity be,
            FluidVesselRenderState state,
            float tickProgress,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);

        Axis axis = be.getAxis();
        state.boilerTranslateX = axis == Axis.X ? be.getHeight() / 2f : be.getWidth() / 2f;
        state.boilerTranslateZ = axis == Axis.Z ? be.getHeight() / 2f : be.getWidth() / 2f;

        // The old chain read `.uncenter().translate(width/2 - 6/16, 0, 0)`; uncenter is a -0.5 shift
        // on each axis, which folds into the one translate the submit phase does.
        float gaugeOffsetX = be.getWidth() / 2f - 6 / 16f - 0.5f;
        Quaternionf dialRotation =
                KineticBlockEntityRenderer.getXRotateAngle(-145 * be.boiler.gauge.getValue(tickProgress) + 90);

        for (Direction d : Iterate.horizontalDirections) {
            if (be.boiler.occludedDirections[d.get2DDataValue()])
                continue;
            if (d.getAxis() != axis)
                continue;

            state.gauges.add(new GaugeRenderState(
                    KineticBlockEntityRenderer.getUpRotateAngle(-d.toYRot() - 90),
                    dialRotation,
                    gaugeOffsetX,
                    CachedBuffers.partial(AllPartialModels.BOILER_GAUGE, state.blockState)
                            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState(),
                    CachedBuffers.partial(AllPartialModels.BOILER_GAUGE_DIAL, state.blockState)
                            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState()
            ));
        }
    }

    @Override
    public void submit(
            FluidVesselRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        if (state.fluid != null) {
            matrices.translate(0, state.fluidTranslateY, 0);
            state.fluid.submit(matrices, queue);
            return;
        }

        if (state.gauges.isEmpty())
            return;

        matrices.pushPose();
        matrices.translate(state.boilerTranslateX, 0.5, state.boilerTranslateZ);
        for (GaugeRenderState gauge : state.gauges)
            gauge.submit(matrices, queue);
        matrices.popPose();
    }

    public record GaugeRenderState(
            @Nullable Quaternionf yRot,
            @Nullable Quaternionf dialRot,
            float offsetX,
            SuperByteBufferRenderState gauge,
            SuperByteBufferRenderState dial
    ) {
        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            if (yRot != null)
                matrices.mulPose(yRot);
            matrices.translate(offsetX, -0.5f, -0.5f);
            gauge.submit(matrices, queue);
            if (dialRot != null)
                matrices.rotateAround(dialRot, 0, 6f / 16, 8f / 16);
            dial.submit(matrices, queue);
            matrices.popPose();
        }
    }

    public static class FluidVesselRenderState extends BlockEntityRenderState {
        public @Nullable FluidRenderState fluid;
        public float fluidTranslateY;
        public float boilerTranslateX;
        public float boilerTranslateZ;
        public final List<GaugeRenderState> gauges = new ArrayList<>(2);
    }
}
