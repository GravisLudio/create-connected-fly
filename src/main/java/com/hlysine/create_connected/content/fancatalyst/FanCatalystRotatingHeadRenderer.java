package com.hlysine.create_connected.content.fancatalyst;

import com.hlysine.create_connected.content.fancatalyst.FanCatalystRotatingHeadRenderer.RotatingHeadRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Unlike the other renderers here this one draws an <em>entity model</em> rather than a
 * {@code SuperByteBuffer}, so it goes through {@code SkullBlockRenderer.submitSkull} instead of
 * queueing a buffer. The animation is a free-running spin rather than anything read off the block
 * entity, so it lives on the renderer and only its current value is extracted.
 */
public class FanCatalystRotatingHeadRenderer implements BlockEntityRenderer<FanCatalystRotatingHeadBlockEntity, RotatingHeadRenderState> {

    private final SkullTypes skullType;
    private float animationTick;

    public FanCatalystRotatingHeadRenderer(SkullTypes skullType) {
        this.skullType = skullType;
    }

    public static FanCatalystRotatingHeadRenderer creeper(BlockEntityRendererProvider.Context context) {
        return new FanCatalystRotatingHeadRenderer(SkullTypes.CREEPER.withModelFromContext(context));
    }

    public static FanCatalystRotatingHeadRenderer dragon(BlockEntityRendererProvider.Context context) {
        return new FanCatalystRotatingHeadRenderer(SkullTypes.DRAGON.withModelFromContext(context));
    }

    /**
     * {@code SkullBlockRenderer.SKIN_BY_TYPE} is private; {@code getSkullRenderType} is the public
     * route to the same thing and picks the render type as well as the texture.
     */
    public RenderType getRenderType() {
        return SkullBlockRenderer.getSkullRenderType(skullType.getTexture(), null);
    }

    @Override
    public RotatingHeadRenderState createRenderState() {
        return new RotatingHeadRenderState();
    }

    @Override
    public void extractRenderState(
            FanCatalystRotatingHeadBlockEntity be,
            RotatingHeadRenderState state,
            float tickProgress,
            Vec3 cameraPos,
            @Nullable CrumblingOverlay crumblingOverlay
    ) {
        SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);

        animationTick += tickProgress;
        animationTick %= 360;
        state.animationTick = animationTick;
        state.model = skullType.getModel();
        state.renderType = getRenderType();
    }

    @Override
    public void submit(
            RotatingHeadRenderState state,
            PoseStack matrices,
            SubmitNodeCollector queue,
            CameraRenderState cameraState
    ) {
        if (state.model == null)
            return;

        matrices.pushPose();
        skullType.translate(matrices);
        skullType.scale(matrices);
        // Argument order confirmed against SkullBlockRenderer.submit: the first int is the light
        // coords, the second is the outline colour, which vanilla always passes as 0.
        SkullBlockRenderer.submitSkull(
                state.animationTick,
                matrices,
                queue,
                state.lightCoords,
                state.model,
                state.renderType,
                0,
                state.breakProgress
        );
        matrices.popPose();
    }

    public static class RotatingHeadRenderState extends BlockEntityRenderState {
        public float animationTick;
        public @Nullable SkullModelBase model;
        public @Nullable RenderType renderType;
    }
}
