package com.hlysine.create_connected.content.linkedtransmitter;

import com.hlysine.create_connected.content.linkedtransmitter.LinkedAnalogLeverRenderer.LinkedAnalogLeverRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.redstone.analogLever.AnalogLeverRenderer;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer.LinkRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws Create's analog lever plus the filter and link overlays the linked transmitter adds.
 * <p>
 * Under the old API those were three calls stacked inside {@code renderSafe}. With the extract and
 * submit phases split, the overlays have to be extracted into the state as well, so this subclasses
 * {@code AnalogLeverRenderState} and mirrors what Create Fly's {@code SmartBlockEntityRenderer}
 * does for the same two behaviours.
 */
public class LinkedAnalogLeverRenderer extends AnalogLeverRenderer {

    private final ItemModelResolver itemModelResolver;

    public LinkedAnalogLeverRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public LinkedAnalogLeverRenderState createRenderState() {
        return new LinkedAnalogLeverRenderState();
    }

    @Override
    public void extractRenderState(
            AnalogLeverBlockEntity be,
            AnalogLeverRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);

        if (!(state instanceof LinkedAnalogLeverRenderState linked))
            return;

        double distance = be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos));
        linked.filter = FilteringRenderer.getFilterRenderState(be, state.blockState, itemModelResolver, distance);
        linked.link = LinkRenderer.getLinkRenderState(be, itemModelResolver, distance);
    }

    @Override
    public void submit(
            AnalogLeverRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        super.submit(state, matrices, queue, cameraState);

        if (!(state instanceof LinkedAnalogLeverRenderState linked))
            return;

        if (linked.filter != null)
            linked.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        if (linked.link != null)
            linked.link.render(state.blockState, queue, matrices, state.lightCoords);
    }

    public static class LinkedAnalogLeverRenderState extends AnalogLeverRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable LinkRenderState link;
    }
}
